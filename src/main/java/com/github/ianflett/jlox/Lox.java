package com.github.ianflett.jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/** Defines Lox language interpreter. */
public final class Lox {

    /** Represents single command line argument. */
    private static final int SINGLE_ARG = 1;

    /** {@link Interpreter} to use for code. */
    private static final Interpreter interpreter = new Interpreter();

    /** Stores whether error encountered during processing. */
    private static boolean hadError = false;

    /** Stores whether error encountered during runtime. */
    private static boolean hadRuntimeError = false;

    /**
     * Main entry point for interpreter.
     *
     * @param args Command line parameters. Supplying no arguments will start REPL; supplying one
     *     argument, file path, will process file; more arguments are invalid and will present usage
     *     instructions.
     * @throws IOException Thrown if input cannot be read.
     */
    public static void main(String... args) throws IOException {

        if (SINGLE_ARG < args.length) {
            System.out.println("Usage: jlox [script]");
            exit(PosixExits.USAGE);
        }

        if (SINGLE_ARG == args.length) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /**
     * Runs Lox commands from file input.
     *
     * @param path Path to Lox file.
     * @throws IOException Thrown if file cannot be read.
     */
    private static void runFile(String path) throws IOException {

        var bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate error and exit.
        if (hadError) exit(PosixExits.DATAERR);
        if (hadRuntimeError) exit(PosixExits.SOFTWARE);
    }

    /**
     * Runs Lox commands from user input.
     *
     * @throws IOException Thrown if system input stream cannot be read.
     */
    private static void runPrompt() throws IOException {

        try (var input = new InputStreamReader(System.in);
                var reader = new BufferedReader(input)) {

            for (; ; ) {
                System.out.print("> ");
                var line = reader.readLine();
                if (null == line) break;
                run(line);
                hadError = false;
            }
        }
    }

    /**
     * Runs Lox commands.
     *
     * @param source Lox commands to process.
     */
    private static void run(String source) {

        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();
        var parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop on syntax error.
        if (hadError) return;

        interpreter.interpret(statements);
    }

    /**
     * Reports processing error.
     *
     * @param line Affected line number.
     * @param message Description of error.
     */
    static void error(int line, String message) {
        report(line, "", message);
    }

    /**
     * Reports processing error.
     *
     * @param token Affected {@link Token}.
     * @param message Description of error.
     */
    static void error(Token token, String message) {
        report(
                token.line(),
                TokenType.EOF == token.type() ? "at end" : "at '" + token.lexeme() + "'",
                message);
    }

    /**
     * Reports runtime error.
     *
     * @param error Error details.
     */
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line() + "]");
        hadRuntimeError = true;
    }

    /**
     * Reports message to standard error stream.
     *
     * @param line Affected line number.
     * @param where Location of error.
     * @param message Description of error.
     */
    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error " + where + ": " + message);
        hadError = true;
    }

    /**
     * Standardised way of exiting with POSIX exit code.
     *
     * @param exitType Exit code to use.
     */
    private static void exit(PosixExits exitType) {
        System.exit(exitType.getCode());
    }
}
