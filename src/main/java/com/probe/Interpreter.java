package com.probe;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.probe.builtin.BuiltIns;
import com.probe.builtin.ExecutionException;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    Environment env = new Environment();
    private final ErrorReporter reporter;

    public Interpreter(ErrorReporter reporter) {
        this.reporter = reporter;
        for (Map.Entry<String, Callable> entry : BuiltIns.callables.entrySet()) {
            env.define(entry.getKey(), entry.getValue());
        }
    }

    void interpret(List<Stmt> stmts) {
        try {
            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            reporter.report(error.token, error.getMessage(), error.mode);
        }
    }


    private String string(Object obj) {
        if (obj == null) return "nil";
        if (obj instanceof ProbeArray) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            ProbeArray array = (ProbeArray) obj;
            for (int i = 0; i < array.size(); ++i) {
                builder.append(string(array.get(i)));
                if (i < array.size() - 1) {
                    builder.append(", ");
                }
            }
            builder.append("]");
            return builder.toString();
        }

        String value = obj.toString();
        if (value.endsWith(".0")) {
            return value.substring(0, value.length() - 2);
        }
        return obj.toString();
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = eval(expr.left);
        Object right = eval(expr.right);
        switch (expr.operator.type) {
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator, "str or num", ErrorReporter.Mode.COVER);
            case MINUS:
                validateNumbers(expr.operator, left, right);
                return (double) left - (double) right;
            case STAR:
                validateNumbers(expr.operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                validateNumbers(expr.operator, left, right);
                return (double) left / (double) right;
            case LESS:
                validateNumbers(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                validateNumbers(expr.operator, left, right);
                return (double) left <= (double) right;
            case GREATER:
                validateNumbers(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                validateNumbers(expr.operator, left, right);
                return (double) left >= (double) right;
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
        }
        return null;
    }

    private boolean isEqual(Object a, Object b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    private void validateNumbers(Token operator, Object left, Object right) {
        if (!(left instanceof Double && right instanceof Double)) {
            throw new RuntimeError(operator, "DOUBLE NO", ErrorReporter.Mode.COVER);
        }
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object value = eval(expr.right);
        switch (expr.operator.type) {
            case MINUS:
                return -(double) value;
            case BANG:
                return !isTrue(value);
        }
        return null;
    }

    private boolean isTrue(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return true;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return env.get(expr.name);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return eval(expr.expr);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = eval(expr.value);
        env.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = eval(expr.left);
        if (isTrue(left)) {
            if (expr.operator.type == TokenType.OR) return true;
        } else if (expr.operator.type == TokenType.AND) return false;
        Object right = eval(expr.right);
        return isTrue(right);
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = eval(expr.callee);
        List<Object> arguments = expr
                .arguments
                .stream()
                .map(this::eval)
                .collect(Collectors.toList());
        Callable callable = (Callable) callee;
        if (callable.parameters() != arguments.size()) {
            throw new RuntimeError(expr.rightParen, "not enough arguments to call", ErrorReporter.Mode.COVER);
        }
        try {
            Object result = callable.call(this, arguments);
            return result;
        } catch (ExecutionException e) {
            throw new RuntimeError(expr.rightParen, e.getMessage(), ErrorReporter.Mode.COVER);
        }

    }

    @Override
    public Object visitArrayExpr(Expr.Array expr) {
        Object[] objects = new Object[expr.elements.size()];
        for (int i = 0; i < expr.elements.size(); ++i) {
            objects[i] = eval(expr.elements.get(i));
        }
        return new ProbeArray(objects);
    }

    @Override
    public Object visitIndexExpr(Expr.Index expr) {
        Object arr = eval(expr.arr);
        Object pos = eval(expr.pos);
        if (!(arr instanceof ProbeArray) && !(arr instanceof String)) {
            throw new RuntimeError(expr.leftSquare, "expected array or string here", ErrorReporter.Mode.COVER);
        }
        if (arr instanceof ProbeArray) {
            ProbeArray array = (ProbeArray) arr;
            double val = (double) pos;
            if (val != (int) val) {
                throw new RuntimeError(expr.leftSquare, "index has to been an integer", ErrorReporter.Mode.COVER);
            }
            return array.get((int) val);
        }
        double val = (double) pos;
        if (val != (int) val) {
            throw new RuntimeError(expr.leftSquare, "index has to been an integer", ErrorReporter.Mode.END);
        }
        return ((String) arr).charAt((int) val);
    }

    @Override
    public Object visitFunctionExpr(FunctionDeclaration expr) {
        return new ProbeFunction(expr, env);
    }


    @Override
    public Void visitVarStatement(Stmt.Var stmt) {
        if (stmt.initializer != null) {
            env.define(stmt.name.lexeme, eval(stmt.initializer));
        } else {
            env.define(stmt.name.lexeme, null);
        }
        return null;
    }

    @Override
    public Void visitPrintStatement(Stmt.Print stmt) {
        System.out.println(string(eval(stmt.expr)));
        return null;
    }

    @Override
    public Void visitIfStatement(Stmt.If stmt) {
        Object condition = eval(stmt.condition);
        if (isTrue(condition)) {
            execute(stmt.then);
        } else if (stmt.alternative != null) {
            execute(stmt.alternative);
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(Stmt.While stmt) {
        while (isTrue(eval(stmt.condition))) {
            try {
                execute(stmt.then);
            } catch (Break br) {
                break;
            }
        }
        return null;
    }

    @Override
    public Void visitExpressionStatement(Stmt.Expression stmt) {
        eval(stmt.expr);
        return null;
    }

    @Override
    public Void visitReturnStatement(Stmt.Return stmt) {
        if (stmt.expr != null) {
            throw new Return(eval(stmt.expr));
        }
        throw new Return();
    }

    @Override
    public Void visitBlockStatement(Stmt.Block stmt) {
        executeBody(stmt.statements, new Environment(env));

        return null;
    }

    void executeBody(List<Stmt> stmts, Environment environment) {
        final Environment outer = this.env;
        try {
            this.env = environment;
            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } finally {
            this.env = outer;
        }
    }

    @Override
    public Void visitFunctionStatement(FunctionDeclaration stmt) {
        this.env.define(stmt.name.lexeme, new ProbeFunction(stmt, env));
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new Break();
    }

    private Object eval(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }


}


