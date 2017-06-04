package com.probe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Probe {

    public static void main(String[] args) throws IOException {

        if (args.length == 1) {
            String s = new String(Files.readAllBytes(Paths.get(args[0])));
            probeIt(s);
        } else{
            repl();
        }

//        System.out.println("Probe");
//        Scanner in = new Scanner(System.in);
//
//        System.out.println();
//        String s = new String(Files.readAllBytes(Paths.get("input.obe")));
//        probeIt(s);


    }

    private static void repl() {

    }

    private static void probeIt(final String source) {
        ErrorReporter reporter = new ErrorReporter(Arrays.asList(source.split("\n")));
        Lexer lexer = new Lexer(source, reporter);
        List<Token> tokens = lexer.scan();
        if (!lexer.isError()) {
            Parser parser = new Parser(tokens, reporter);
            List<Stmt> stmts = parser.parse();

            if (!parser.isError()) {
                Interpreter interpreter = new Interpreter(reporter);
                interpreter.interpret(stmts);
            }
        }

    }

}
