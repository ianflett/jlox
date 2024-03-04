package ianflett.jlox;

import static ianflett.jlox.TokenType.*;
import static java.util.Map.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Scans through text to discover {@link Token}s. */
class Scanner {

    /** Defines all reserved keywords and associated token types. */
    private static final Map<String, TokenType> keywords =
            Map.ofEntries(
                    entry("and", AND),
                    entry("class", CLASS),
                    entry("else", ELSE),
                    entry("false", FALSE),
                    entry("for", FOR),
                    entry("fun", FUN),
                    entry("if", IF),
                    entry("nil", NIL),
                    entry("or", OR),
                    entry("print", PRINT),
                    entry("return", RETURN),
                    entry("super", SUPER),
                    entry("this", THIS),
                    entry("true", TRUE),
                    entry("var", VAR),
                    entry("while", WHILE));

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
        var c = advance();
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
                if (!comment()) {
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
            case '"':
                string();
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    /** Consumes identifier {@link Token} from source text. */
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        var text = source.substring(start, current);
        var type = keywords.get(text);
        if (null == type) type = IDENTIFIER;
        addToken(type);
    }

    /** Consumes number {@link Token} from source text. */
    private void number() {
        while (isDigit(peek())) advance();

        // Look for fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume '.'.
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    /** Consumes string {@link Token} from source text. */
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') ++line;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // Closing ".
        advance();

        // Trim surrounding quotes.
        var value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /** Consumes comment {@link Token} from source text. */
    private boolean comment() {

        if (match('/')) {
            // Comment persist to end of line.
            while (peek() != '\n' && !isAtEnd()) advance();
            return true;
        }

        if (match('*')) {
            while ((peek() != '*' || peekNext() != '/') && !isAtEnd()) {
                if (peek() == '\n') ++line;
                advance();
            }

            // Closing */.
            advance();
            advance();

            return true;
        }

        return false;
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
     * Looks ahead two characters in source text.
     *
     * @return Character, or null character if at end of file.
     */
    private char peekNext() {
        return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
    }

    /**
     * Whether character represents letter.
     *
     * @param c Character to analyse.
     * @return {@code true} if character is letter; {@code false} otherwise.
     */
    private boolean isAlpha(char c) {
        return ('a' <= c && 'z' >= c) || ('A' <= c && 'Z' >= c) || '_' == c;
    }

    /**
     * Whether character represents letter or numeral.
     *
     * @param c Character to analyse.
     * @return {@code true} if character is letter or numeral; {@code false} otherwise.
     */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * Whether character represents numeral.
     *
     * @param c Character to analyse.
     * @return {@code true} if character represents numeral; {@code false} otherwise.
     */
    private boolean isDigit(char c) {
        return '0' <= c && '9' >= c;
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
     * Adds {@link Token} to discovered list.
     *
     * @param type {@link Token}'s {@link TokenType}.
     * @param literal {@link Token}'s literal value.
     */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
