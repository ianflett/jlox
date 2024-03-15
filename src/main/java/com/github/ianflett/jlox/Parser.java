package com.github.ianflett.jlox;

import static com.github.ianflett.jlox.TokenType.*;

import java.util.List;

/** Recursive decent parser that consumes tokens to produce abstract syntax tree. */
public class Parser {

    /** List of tokens to process. */
    private final List<Token> tokens;

    /** Index of current token being parsed. */
    private int current = 0;

    /**
     * Constructs parser.
     *
     * @param tokens Tokens to parse.
     */
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parses expression grammar rule.
     *
     * @return {@link Expr}ession or {@link #equality()}.
     */
    private Expr expression() {
        return equality();
    }

    /**
     * Parses equality grammar rule.
     *
     * @return Equality {@link Expr}ession or {@link #comparison()}.
     */
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses comparison grammar rule.
     *
     * @return Comparison {@link Expr}ession or {@link #term()}.
     */
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses term grammar rule.
     *
     * @return Term {@link Expr}ession or {@link #factor()}.
     */
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses factor grammar rule.
     *
     * @return Factor {@link Expr}ession or {@link #unary()}.
     */
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses unary grammar rule.
     *
     * @return Unary {@link Expr}ession or {@link #primary()}.
     */
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    /**
     * Parses primary grammar rule.
     *
     * @return {@link Expr.Literal} or {@link Expr.Grouping}.
     */
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal());

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw new UnsupportedOperationException();
    }

    private Token consume(TokenType type, String message) {
        throw new UnsupportedOperationException();
    }

    /**
     * Whether current {@link Token} matches given {@link TokenType}s.
     *
     * @param types {@link TokenType}s to check for.
     * @return {@code true} if current {@link Token} matches {@link TokenType}s; {@code false}
     *     otherwise.
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    /**
     * Whether current {@link Token} is a {@link TokenType}.
     *
     * @param type {@link TokenType} to check for.
     * @return {@code true} if current {@link Token} is a {@link TokenType}; {@code false}
     *     otherwise.
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    /**
     * Advance to next {@link Token}.
     *
     * @return Next {@link Token}.
     */
    private Token advance() {
        if (!isAtEnd()) ++current;
        return previous();
    }

    /**
     * Whether {@link #tokens} is exhausted.
     *
     * @return {@code true} if at end of {@link #tokens}; {@code false} otherwise.
     */
    private boolean isAtEnd() {
        return peek().type() == EOF;
    }

    /**
     * Get current {@link Token}.
     *
     * @return Current {@link Token}.
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Get previous {@link Token}.
     *
     * @return Previous {@link Token}.
     */
    private Token previous() {
        return tokens.get(current - 1);
    }
}
