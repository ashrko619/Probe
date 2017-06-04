package com.probe;

public final class Token {

    final TokenType type;
    final String lexeme;
    final Object value;
    final int line;
    final int column;

    Token(TokenType type, String lexeme, Object value, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", lexeme='" + lexeme + '\'' +
                ", value=" + value +
                ", line=" + line +
                ", column=" + column +
                '}';
    }
}
