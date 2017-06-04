package com.probe;

class RuntimeError extends RuntimeException {
    final Token token;
    final ErrorReporter.Mode mode;

    public RuntimeError(Token token, String message, ErrorReporter.Mode mode) {
        super(message);
        this.token = token;
        this.mode = mode;
    }
}
