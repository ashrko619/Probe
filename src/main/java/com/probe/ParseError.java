package com.probe;

public class ParseError extends RuntimeException {
    private final Token token;
    private final ErrorReporter.Mode mode;

    public ParseError(Token token, String message, ErrorReporter.Mode mode) {
        super(message);
        this.token = token;
        this.mode = mode;
    }

    public Token getToken() {
        return token;
    }

    public ErrorReporter.Mode getMode() {
        return mode;
    }
}
