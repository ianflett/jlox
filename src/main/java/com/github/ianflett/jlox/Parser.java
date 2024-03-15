package com.github.ianflett.jlox;

import jdk.jshell.spi.ExecutionControl;

import java.util.List;

import static com.github.ianflett.jlox.TokenType.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr expression () {
        return equality();
    }

    private Expr equality() {
        return comparison();
    }

    private Expr comparison() {
        return term();
    }

    private Expr term() {
        return factor();
    }

    private Expr factor() {
        return unary();
    }

    private Expr unary() {
        return primary();
    }

    private Expr primary() {
        throw new UnsupportedOperationException();
    }
}
