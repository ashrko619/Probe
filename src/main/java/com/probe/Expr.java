package com.probe;

import java.util.List;

interface Expr {
    interface Visitor<T> {
        T visitLiteralExpr(Literal expr);

        T visitBinaryExpr(Binary expr);

        T visitUnaryExpr(Unary expr);

        T visitVariableExpr(Variable expr);

        T visitGroupingExpr(Grouping expr);

        T visitAssignExpr(Assign expr);

        T visitLogicalExpr(Logical expr);

        T visitCallExpr(Call expr);

        T visitArrayExpr(Array expr);

        T visitIndexExpr(Index expr);

        T visitFunctionExpr(FunctionDeclaration expr);
    }

    <T> T accept(Expr.Visitor<T> visitor);

    class Literal implements Expr {
        final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        @Override
        public String toString() {
            return "Literal{" +
                    "value=" + value +
                    '}';
        }
    }

    class Binary implements Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    class Unary implements Expr {
        final Token operator;
        final Expr right;

        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    class Variable implements Expr {
        final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    class Grouping implements Expr {
        final Expr expr;

        public Grouping(Expr expr) {
            this.expr = expr;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    class Assign implements Expr {
        final Token name;
        final Expr value;

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    class Logical implements Expr {
        final Expr left;
        final Token operator;
        final Expr right;


        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    class Call implements Expr {
        final Expr callee;
        final List<Expr> arguments;
        final Token rightParen;

        public Call(Expr callee, List<Expr> arguments, Token rightParen) {
            this.callee = callee;
            this.arguments = arguments;
            this.rightParen = rightParen;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    class Array implements Expr {
        final List<Expr> elements;

        public Array(List<Expr> elements) {
            this.elements = elements;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitArrayExpr(this);
        }
    }

    class Index implements Expr {
        final Expr arr;
        final Expr pos;
        final Token leftSquare;

        public Index(Expr arr, Expr pos, Token leftSquare) {
            this.arr = arr;
            this.pos = pos;
            this.leftSquare = leftSquare;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitIndexExpr(this);
        }
    }
}
