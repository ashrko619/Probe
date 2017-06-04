package com.probe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.probe.TokenType.AND;
import static com.probe.TokenType.BANG;
import static com.probe.TokenType.BANG_EQUAL;
import static com.probe.TokenType.BREAK;
import static com.probe.TokenType.COMMA;
import static com.probe.TokenType.ELSE;
import static com.probe.TokenType.EOF;
import static com.probe.TokenType.EQUAL;
import static com.probe.TokenType.EQUAL_EQUAL;
import static com.probe.TokenType.FALSE;
import static com.probe.TokenType.FN;
import static com.probe.TokenType.FOR;
import static com.probe.TokenType.GREATER;
import static com.probe.TokenType.GREATER_EQUAL;
import static com.probe.TokenType.IDENTIFIER;
import static com.probe.TokenType.IF;
import static com.probe.TokenType.LEFT_BRACE;
import static com.probe.TokenType.LEFT_PAREN;
import static com.probe.TokenType.LEFT_SQUARE;
import static com.probe.TokenType.LESS;
import static com.probe.TokenType.LESS_EQUAL;
import static com.probe.TokenType.MINUS;
import static com.probe.TokenType.NIL;
import static com.probe.TokenType.NUMBER;
import static com.probe.TokenType.OR;
import static com.probe.TokenType.PLUS;
import static com.probe.TokenType.PRINT;
import static com.probe.TokenType.RETURN;
import static com.probe.TokenType.RIGHT_BRACE;
import static com.probe.TokenType.RIGHT_PAREN;
import static com.probe.TokenType.RIGHT_SQUARE;
import static com.probe.TokenType.SEMICOLON;
import static com.probe.TokenType.SLASH;
import static com.probe.TokenType.STAR;
import static com.probe.TokenType.STRING;
import static com.probe.TokenType.TRUE;
import static com.probe.TokenType.VAR;
import static com.probe.TokenType.WHILE;

class Parser {

    private final List<Token> tokens;
    private final ErrorReporter reporter;
    private int current;
    private boolean hasErrors = false;

    Parser(List<Token> tokens, ErrorReporter reporter) {
        this.tokens = tokens;
        this.reporter = reporter;
    }

    List<Stmt> parse() {
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) {
            stmts.add(declaration());
        }

        return stmts;
    }

    private Stmt declaration() {
        try {
            if (match(VAR)) return varDeclaration();
            if (match(FN)) return functionDeclaration(false);
            return statement();
        } catch (ParseError error) {

            // Probe.report(error.getToken(), error.getMessage());
            reporter.report(error.getToken(), error.getMessage(), error.getMode());
            sync();
            hasErrors = true;
            return null;
        }
    }

    //TODO add EOF checking
    private FunctionDeclaration functionDeclaration(boolean isAnonymous) {
        Token name = null;
        if (!isAnonymous) {
            name = consume(IDENTIFIER, "expect function name", ErrorReporter.Mode.END);
            consume(LEFT_PAREN, "expect ( after function name", ErrorReporter.Mode.END);
        } else {
            if (check(IDENTIFIER)) {
                throw new ParseError(peek(), "Anonymous function expected here", ErrorReporter.Mode.COVER);
            }
            consume(LEFT_PAREN, "expect ( after fn", ErrorReporter.Mode.END);
        }

        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                parameters.add(consume(IDENTIFIER, "Expect parameter name.", ErrorReporter.Mode.END));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "expect ) after parameters", ErrorReporter.Mode.END);
        List<Stmt> body = block();
        return new FunctionDeclaration(name, parameters, body);
    }

    private Stmt statement() {
        if (match(PRINT)) return printStmt();
        if (match(IF)) return ifStmt();
        if (match(WHILE)) return whileStmt();
        if (match(FOR)) return forStmt();
        if (match(RETURN)) return returnStmt();
        if (match(BREAK)) return breakStmt();
        if (check(LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt forStmt() {

        consume(LEFT_PAREN, "expect ( after for", ErrorReporter.Mode.END);

        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "expect ; after condition", ErrorReporter.Mode.END);

        Stmt increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = new Stmt.Expression(expression());
        }
        consume(RIGHT_PAREN, "expect ) after for", ErrorReporter.Mode.END);

        Stmt body = statement();

        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(body, increment));
        }

        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt breakStmt() {
        Token breakToken = previous();
        consume(SEMICOLON, "expect ; after break", ErrorReporter.Mode.END);
        return new Stmt.Break(breakToken);
    }

    private List<Stmt> block() {
        consume(LEFT_BRACE, "expect { before block", ErrorReporter.Mode.END);
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd() && !check(RIGHT_BRACE)) {
            stmts.add(declaration()); //
        }
        consume(RIGHT_BRACE, "expect } after block", ErrorReporter.Mode.END);
        return stmts;
    }

    private Stmt returnStmt() {
        Expr expr = expression();
        consume(SEMICOLON, "expect ; after expression", ErrorReporter.Mode.END);
        return new Stmt.Return(expr);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        //match(SEMICOLON);
        consume(SEMICOLON, "expect ; after exmpression", ErrorReporter.Mode.END);
        return new Stmt.Expression(expr);
    }

    private Stmt whileStmt() {
        consume(LEFT_PAREN, "expect ( after while", ErrorReporter.Mode.END);
        Expr expr = expression();
        consume(RIGHT_PAREN, "expect ) after while condition", ErrorReporter.Mode.END);
        Stmt then = statement();
        return new Stmt.While(expr, then);
    }

    private Stmt ifStmt() {
        consume(LEFT_PAREN, "expect ( after if", ErrorReporter.Mode.END);
        Expr expr = expression();
        consume(RIGHT_PAREN, "expect ) after if condition", ErrorReporter.Mode.END);
        Stmt then = statement();
        Stmt alternative = null;
        if (match(ELSE)) {
            alternative = statement();
        }
        return new Stmt.If(expr, then, alternative);
    }

    private Stmt printStmt() {
        Expr expr = expression();
        consume(SEMICOLON, "expect ;", ErrorReporter.Mode.END);
        return new Stmt.Print(expr);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect identifier after var", ErrorReporter.Mode.END);
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ; after var declaration", ErrorReporter.Mode.END);
        return new Stmt.Var(name, initializer);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                return new Expr.Assign(((Expr.Variable) expr).name, value);
            } else {
                throw error(equals, "Invalid assignment", ErrorReporter.Mode.END);
            }
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        return binary(this::comparison, BANG_EQUAL, EQUAL_EQUAL);
    }

    private Expr comparison() {
        return binary(this::term, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
    }

    private Expr term() {
        return binary(this::factor, PLUS, MINUS);
    }

    private Expr factor() {
        return binary(this::unary, SLASH, STAR);
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return array();
    }

    private Expr array() {
        if (match(LEFT_SQUARE)) {
            List<Expr> values = new ArrayList<>();
            if (!check(RIGHT_PAREN)) {
                do {
                    values.add(expression());
                } while (match(COMMA));
            }
            consume(RIGHT_SQUARE, "Expect ] after values", ErrorReporter.Mode.END);
            return new Expr.Array(values);
        }
        return index();
    }

    private Expr index() {
        Expr expr = call();
        if (match(LEFT_SQUARE)) {
            Token leftSquare = previous();
            Expr pos = expression();
            consume(RIGHT_SQUARE, "expect ] after index", ErrorReporter.Mode.END);
            return new Expr.Index(expr, pos, leftSquare);
        }
        return expr;
    }

    private Expr call() {
        Expr expr = primary();
        if (match(LEFT_PAREN)) {
            List<Expr> arguments = new ArrayList<>();
            if (!check(RIGHT_PAREN)) {
                do {
                    arguments.add(expression());
                } while (match(COMMA));
            }
            Token rightParen = consume(RIGHT_PAREN, "Expect ) after arguments", ErrorReporter.Mode.END);
            return new Expr.Call(expr, arguments, rightParen);
        }
        return expr;
    }

    private Expr primary() {
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(STRING, NUMBER)) return new Expr.Literal(previous().value);
        if (match(IDENTIFIER)) return new Expr.Variable(previous());
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ) ", ErrorReporter.Mode.END);
            return new Expr.Grouping(expr);
        }
        if (match(FN)) {
            return functionDeclaration(true);
        }
        throw new ParseError(peek(), "expect expression", ErrorReporter.Mode.END);
    }


    private Expr binary(Supplier<Expr> operand, TokenType... types) {
        Expr expr = operand.get();
        while (match(types)) {
            Token operator = previous();
            Expr right = operand.get();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean match(TokenType... types) {
        if (isAtEnd()) return false;
        boolean matches = Arrays.stream(types).anyMatch(this::check);
        if (matches) {
            advance();
            return true;
        }
        return false;
    }

    private Token consume(TokenType type, String error, ErrorReporter.Mode mode) {
        if (check(type)) return advance();
        throw error(previous(), error, mode);
    }

    private ParseError error(Token token, String message, ErrorReporter.Mode mode) {
        return new ParseError(token, message, mode);
    }

    private void sync() {

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;
            switch (peek().type) {
                case FN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }

    private boolean check(TokenType type) {
        return !isAtEnd() && tokens.get(current).type == type;
    }

    private Token advance() {
        return tokens.get(current++);
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return tokens.get(current).type == EOF;
    }

    public boolean isError() {
        return hasErrors;
    }
}
