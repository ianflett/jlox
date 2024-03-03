package ianflett.jlox;

import static ianflett.jlox.TokenType.*;

import java.util.ArrayList;
import java.util.List;

/** Scans through text to discover {@link Token}s. */
class Scanner {

    /** Stores the source text. */
    private final String source;

    /** Stores a list of all discovered {@link Token}s. */
    private final List<Token> tokens = new ArrayList<>();

    /** Stores the beginning position of the lexeme being scanned. */
    private int start = 0;

    /** Stores the position of the character currently being scanned. */
    private int current = 0;

    /** Stores the current line number. */
    private int line = 1;

    /**
     * Constructs a {@link Scanner}.
     *
     * @param source The source text to scan.
     */
    Scanner(String source) {

        if (null == source) throw new IllegalArgumentException("Source text must be defined.");
        this.source = source;
    }

    /**
     * Scans for {@link Token}s within the source text.
     *
     * @return All {@link Token}s found.
     */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * Whether the scanner has reached the end of the source text.
     *
     * @return Whether the end of the source is reached.
     */
    private boolean isAtEnd() {
        return source.length() <= current;
    }

    /** Scans a token in the source text. */
    private void scanToken() {
        char c = advance();
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
            default:
                Lox.error(line, "Unexpected character.");
                break;
        }
    }

    /**
     * Consume next character in source text.
     *
     * @return The character read.
     */
    private char advance() {
        return source.charAt(current++);
    }

    /**
     * Consumes next character in source text, if character matches {@code expected}.
     *
     * @param expected The character to attempt to consume.
     * @return {@code true} if expected character was consumed; {@code false} otherwise.
     */
    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;

        ++current;
        return true;
    }

    /**
     * Converts a character to a {@link Token}.
     *
     * @param type The {@link Token}'s {@link TokenType}.
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * Converts a character to a {@link Token}.
     *
     * @param type The {@link Token}'s {@link TokenType}.
     * @param literal The {@link Token}'s literal value.
     */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
