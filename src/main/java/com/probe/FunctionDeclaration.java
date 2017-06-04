package com.probe;

import java.util.List;

class FunctionDeclaration implements Stmt, Expr {

    final Token name;
    final List<Token> parameters;
    final List<Stmt> body;

    FunctionDeclaration(Token name, List<Token> parameters, List<Stmt> body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public <T> T accept(Stmt.Visitor<T> visitor) {
        return visitor.visitFunctionStatement(this);
    }

    @Override
    public <T> T accept(Expr.Visitor<T> visitor) {
        return visitor.visitFunctionExpr(this);
    }
}
