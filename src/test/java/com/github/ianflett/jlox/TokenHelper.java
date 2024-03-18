package com.github.ianflett.jlox;

import static com.github.ianflett.jlox.TokenType.*;
import static java.util.Map.entry;

import java.util.*;

/** Helps unit testing with {@link Token}s. */
public class TokenHelper {

    /**
     * Gets {@link Token} with literal.
     *
     * @param literal Literal for token.
     * @return {@link Token} with literal.
     */
    static Token get(String literal) {
        return TOKENS.get(literal);
    }

    /**
     * Gets {@link Token} with literal and line number.
     *
     * @param literal Literal for token.
     * @param line New line number; default is 1.
     * @return {@link Token} with literal and line number.
     */
    static Token withLine(String literal, int line) {
        var token = TOKENS.get(literal);
        return new Token(token.type(), token.lexeme(), token.literal(), line);
    }

    /**
     * Gets {@link Token} with literal and adjusted line number.
     *
     * @param literal Literal for token.
     * @param line Line number adjustment; default is 1.
     * @return {@link Token} with literal and adjusted line number.
     */
    static Token adjustLine(String literal, int line) {
        return withLine(literal, DEFAULT_LINE + line);
    }

    /**
     * Gets {@link Token} {@link List} ending with implicit {@link TokenType#EOF}.
     *
     * @param literals Literals for tokens.
     * @return {@link Token} {@link List}.
     */
    static List<Token> asList(String... literals) {
        var tokens = new ArrayList<Token>(literals.length);
        for (var tokenKey : literals) {
            tokens.add(TOKENS.get(tokenKey));
        }
        tokens.add(TOKENS.get(EOF_LITERAL));
        return tokens;
    }

    /**
     * Gets {@link Token} array ending with implicit {@link TokenType#EOF}.
     *
     * @param literals Literals for tokens.
     * @return {@link Token} array.
     */
    static Token[] asArray(String... literals) {
        return asList(literals).toArray(new Token[0]);
    }

    /** Stores pre-generated {@link Token}s for testing. */
    private static final Map<String, Token> TOKENS =
            generateTokens(
                    entry("(", LEFT_PAREN),
                    entry(")", RIGHT_PAREN),
                    entry("{", LEFT_BRACE),
                    entry("}", RIGHT_BRACE),
                    entry(",", COMMA),
                    entry(".", DOT),
                    entry("-", MINUS),
                    entry("+", PLUS),
                    entry(";", SEMICOLON),
                    entry("*", STAR),
                    entry("!", BANG),
                    entry("!=", BANG_EQUAL),
                    entry("=", EQUAL),
                    entry("==", EQUAL_EQUAL),
                    entry("<", LESS),
                    entry("<=", LESS_EQUAL),
                    entry(">", GREATER),
                    entry(">=", GREATER_EQUAL),
                    entry("/", SLASH),
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
                    entry("while", WHILE),
                    entry("", EOF),
                    entry("1", NUMBER),
                    entry("2", NUMBER),
                    entry("3", NUMBER));

    /** Generates static {@link Map} of {@link Token}s. */
    @SafeVarargs
    private static Map<String, Token> generateTokens(Map.Entry<String, TokenType>... tokenTypes) {

        var tokens = new HashMap<String, Token>(tokenTypes.length);
        for (var tokenType : tokenTypes) {

            Object literal =
                    switch (tokenType.getValue()) {
                        case STRING -> tokenType.getKey();
                        case NUMBER -> Double.parseDouble(tokenType.getKey());
                        default -> null;
                    };

            tokens.put(
                    tokenType.getKey(),
                    new Token(tokenType.getValue(), tokenType.getKey(), literal, DEFAULT_LINE));
        }

        return Collections.unmodifiableMap(tokens);
    }

    /** Literal for {@link TokenType#EOF}. */
    static final String EOF_LITERAL = "";

    /** Default line for {@link Token} generation. */
    static final int DEFAULT_LINE = 1;
}
