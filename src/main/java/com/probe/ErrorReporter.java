package com.probe;

import java.util.List;

//TODO ugly ugly code. clean up
public class ErrorReporter {
    private final List<String> lines;

    public ErrorReporter(List<String> lines) {
        this.lines = lines;
    }


    public void report(Token token, String msg, Mode mode) {
        System.err.println();
        System.err.println();


        System.err.println("Error : " + msg);
        int start = getStartColPos(token, mode);
        int end = getEndColPos(token, mode);
        String s = String.format("  %d | ", token.line);
        print(s + lines.get(token.line - 1));
        for (int i = 0; i < s.length(); ++i) {
            System.err.print(" ");
        }

        for (int i = 0; i < start; ++i) {
            System.err.print(" ");
        }
        for (int i = start; i < end; ++i) {
            System.err.print("^");
        }


    }

    void print(String s, Object... args) {
        System.err.println(String.format(s, args));
    }

    int getStartColPos(Token token, Mode mode) {
        switch (mode) {
            case END:
                return token.column + token.lexeme.length();
            case COVER:
                return token.column;
            case FULL_LINE:
                return 0;
            case START:
                return token.column;
        }
        return 0; // unreachable
    }

    int getEndColPos(Token token, Mode mode) {
        switch (mode) {
            case END:
                return token.column + token.lexeme.length() + 1;
            case COVER:
                return token.column + token.lexeme.length();
            case FULL_LINE:
                return 0;
            case START:
                return token.column;
        }
        return 0;  // unreachable
    }

    public void report(String error, int line, int column) {
        print(lines.get(line - 1));
        for (int i = 0; i < column - 1; ++i) {
            System.err.print(" ");
        }
        print("^ Here");
        print(error);
    }

    enum Mode {
        COVER, END, START, FULL_LINE
    }


}
