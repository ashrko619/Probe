package com.probe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.probe.TokenType.*;

class Lexer {

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fn", FN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
        keywords.put("break", BREAK);
    }

    private final List<Token> tokens;
    private final String source;
    private final ErrorReporter reporter;

    private int start;
    private int current;
    private int line;
    private int column;
    private int startColumn;
    private boolean error;

    Lexer(final String source, ErrorReporter reporter) {
        this.source = source;
        this.reporter = reporter;
        this.tokens = new ArrayList<>();
        this.start = 0;
        this.current = 0;
        this.line = 1;
        this.column = 0;
        this.startColumn = 0;
        this.error = false;
    }

    List<Token> scan() {
        while (!isAtEnd()) {
            start = current;
            startColumn = column;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line, column));
        return tokens;
    }

    private void scanToken() {
        final char c = advance();
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case '[':
                addToken(LEFT_SQUARE);
                break;
            case ']':
                addToken(RIGHT_SQUARE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;

            case '\n':
                line++;
                column = startColumn = 0;
                break;

            case '"':
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();

                } else if (isAlpha(c)) {
                    identifier();

                } else {
                    reportError(String.format("Unexpected character '%c' ", c), line, column);
                }
        }
    }


    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, IDENTIFIER);
        addToken(type);
    }

    private boolean isAlphaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // advancing .
            while (isDigit(peek())) {
                advance();
            }
        }
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char peekNext() {
        return peek(1);
    }


    private char peek(int offset) {
        return current + offset < source.length() ? source.charAt(current + offset) : '\0';
    }

    private boolean isAlpha(char c) {
        return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || c == '_';
    }

    private boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    private void string() {
        int lastNonEmpty = column;
        while (!isAtEnd() && peek() != '"') {
            if (peek() != ' ' && peek() != '\t' && peek() != '\n' && peek() != '\r') {
                lastNonEmpty = column;
            }
            advance();
        }
        if (isAtEnd()) {
            reportError("Unterminated string", line, lastNonEmpty+1);
            return;
        }
        advance(); // last "
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void reportError(String errorMsg, int line, int col) {
        error = true;
        reporter.report(errorMsg, line, col);
    }


    private boolean match(char c) {
        if (isAtEnd()) {
            return false;
        }
        if (peek() != c) {
            return false;
        }
        current++;
        return true;
    }

    private char peek() {
        return peek(0);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object value) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, value, line, startColumn));
    }

    private char advance() {
        column++;
        return source.charAt(current++);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    public boolean isError() {
        return error;
    }
}
