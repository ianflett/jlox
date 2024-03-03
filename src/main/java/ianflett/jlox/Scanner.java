package ianflett.jlox;

import static ianflett.jlox.TokenType.*;

import java.util.ArrayList;
import java.util.List;

/** Scans through text to discover {@link Token}s. */
class Scanner {

    /** Stores source text. */
    private final String source;

    /** Stores list of all discovered {@link Token}s. */
    private final List<Token> tokens = new ArrayList<>();

    /** Stores beginning position of lexeme being scanned. */
    private int start = 0;

    /** Stores position of character currently being scanned. */
    private int current = 0;

    /** Stores current line number. */
    private int line = 1;

    /**
     * Constructs {@link Scanner}.
     *
     * @param source Source text to scan.
     */
    Scanner(String source) {

        if (null == source) throw new IllegalArgumentException("Source text must be defined.");
        this.source = source;
    }

    /**
     * Scans for {@link Token}s within source text.
     *
     * @return All {@link Token}s found.
     */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // Beginning of next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * Whether scanner has reached end of source text.
     *
     * @return {@code true} if at end of source text; {@code false} otherwise.
     */
    private boolean isAtEnd() {
        return source.length() <= current;
    }

    /** Scans token in source text. */
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
            case '/':
                if (match('/')) {
                    // Comment persist to end of line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case '\n':
                ++line;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            default:
                Lox.error(line, "Unexpected character.");
                break;
        }
    }

    /**
     * Consume next character in source text.
     *
     * @return Character read.
     */
    private char advance() {
        return source.charAt(current++);
    }

    /**
     * Consumes next character in source text, if character matches {@code expected}.
     *
     * @param expected Character to attempt to consume.
     * @return {@code true} if expected character was consumed; {@code false} otherwise.
     */
    private boolean match(char expected) {
        if (peek() != expected) return false;

        ++current;
        return true;
    }

    /**
     * Looks ahead one character in source text.
     *
     * @return Character, or null character if at end of file.
     */
    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    /**
     * Converts character to {@link Token}.
     *
     * @param type {@link Token}'s {@link TokenType}.
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * Converts character to {@link Token}.
     *
     * @param type {@link Token}'s {@link TokenType}.
     * @param literal {@link Token}'s literal value.
     */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
