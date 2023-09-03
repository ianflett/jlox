package ianflett.jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Defines the Lox language interpreter. */
public final class Lox {

    /** Represents a single command line argument. */
    private static final int SINGLE_ARG = 1;

    /** Stores whether an error has been encountered during processing. */
    private static boolean hadError = false;

    /**
     * The main entry point for the interpreter.
     *
     * @param args The command line parameters. Supplying no arguments will start a REPL; supplying
     *     one argument, a file path, will process that file; any more arguments are invalid and
     *     will present usage instructions.
     * @throws IOException Thrown if input cannot be read.
     */
    public static void main(String[] args) throws IOException {

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
     * Runs Lox commands from a file input.
     *
     * @param path The path to the Lox file.
     * @throws IOException Thrown if the file cannot be read.
     */
    private static void runFile(String path) throws IOException {

        var bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate error and exit.
        if (hadError) exit(PosixExits.DATAERR);
    }

    /**
     * Runs Lox commands from user input.
     *
     * @throws IOException Thrown if the system input stream cannot be read.
     */
    private static void runPrompt() throws IOException {

        try (var input = new InputStreamReader(System.in)) {
            var reader = new BufferedReader(input);

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

        // For now, print tokens.
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    /**
     * Reports a processing error.
     *
     * @param line Affected line number.
     * @param message Description of error.
     */
    static void error(int line, String message) {
        report(line, "", message);
    }

    /**
     * Reports a message to the standard error stream.
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
     * Standardised way of exiting with a POSIX exit code.
     *
     * @param exitType The exit code to use.
     */
    private static void exit(PosixExits exitType) {
        System.exit(exitType.getCode());
    }
}
