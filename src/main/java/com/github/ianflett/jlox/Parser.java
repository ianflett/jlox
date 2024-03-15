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
        return comparison();
    }

    /**
     * Parses comparison grammar rule.
     *
     * @return Comparison {@link Expr}ession or {@link #term()}.
     */
    private Expr comparison() {
        return term();
    }

    /**
     * Parses term grammar rule.
     *
     * @return Term {@link Expr}ession or {@link #factor()}.
     */
    private Expr term() {
        return factor();
    }

    /**
     * Parses factor grammar rule.
     *
     * @return Factor {@link Expr}ession or {@link #unary()}.
     */
    private Expr factor() {
        return unary();
    }

    /**
     * Parses unary grammar rule.
     *
     * @return Unary {@link Expr}ession or {@link #primary()}.
     */
    private Expr unary() {
        return primary();
    }

    /**
     * Parses primary grammar rule.
     *
     * @return {@link Expr.Literal} or {@link Expr.Grouping}.
     */
    private Expr primary() {
        throw new UnsupportedOperationException();
    }
}
