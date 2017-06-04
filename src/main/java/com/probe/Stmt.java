package com.probe;

import java.util.List;

public interface Stmt {
    interface Visitor<T> {
        T visitVarStatement(Var stmt);

        T visitPrintStatement(Print stmt);

        T visitIfStatement(If stmt);

        T visitWhileStatement(While stmt);

        T visitExpressionStatement(Expression stmt);

        T visitReturnStatement(Return stmt);

        T visitBlockStatement(Block stmt);

        T visitFunctionStatement(FunctionDeclaration stmt);

        T visitBreakStmt(Break stmt);
    }

    <T> T accept(Visitor<T> visitor);

    class Var implements Stmt {
        final Token name;
        final Expr initializer;

        public Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitVarStatement(this);
        }

        @Override
        public String toString() {
            return "Var{" +
                    "name=" + name +
                    ", initializer=" + initializer +
                    '}';
        }
    }

    class Print implements Stmt {
        final Expr expr;

        public Print(Expr expr) {
            this.expr = expr;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitPrintStatement(this);
        }
    }

    class If implements Stmt {
        final Expr condition;
        final Stmt then;
        final Stmt alternative;


        public If(Expr condition, Stmt then, Stmt alternative) {
            this.condition = condition;
            this.then = then;
            this.alternative = alternative;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitIfStatement(this);
        }
    }

    class While implements Stmt {
        final Expr condition;
        final Stmt then;

        public While(Expr condition, Stmt then) {
            this.condition = condition;
            this.then = then;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitWhileStatement(this);
        }
    }

    public class Expression implements Stmt {
        final Expr expr;

        public Expression(Expr expr) {
            this.expr = expr;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitExpressionStatement(this);
        }
    }

    class Return implements Stmt {
        final Expr expr;

        public Return(Expr expr) {
            this.expr = expr;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitReturnStatement(this);
        }
    }

    class Block implements Stmt {
        final List<Stmt> statements;

        public Block(List<Stmt> stmts) {
            this.statements = stmts;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitBlockStatement(this);
        }
    }

    class Break implements Stmt {
        final Token token;

        public Break(Token breakToken) {
            this.token = breakToken;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitBreakStmt(this);
        }
    }
}

