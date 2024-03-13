package com.github.ianflett.jlox;

import static com.github.ianflett.jlox.TokenType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

/** Unit tests {@link AstReversePolishNotationPrinter} class. */
public class AstReversePolishNotationPrinterTests {

    /**
     * Tests {@link AstReversePolishNotationPrinter#print(Expr)} produces correct output for example
     * abstract syntax tree.
     */
    @Test
    void print_producesCorrectOutput() {

        // (1 + 2) * (4 - 3)
        var expression =
                new Expr.Binary(
                        new Expr.Grouping(
                                new Expr.Binary(
                                        new Expr.Literal(1),
                                        new Token(PLUS, "+", null, 1),
                                        new Expr.Literal(2))),
                        new Token(STAR, "*", null, 1),
                        new Expr.Grouping(
                                new Expr.Binary(
                                        new Expr.Literal(4),
                                        new Token(MINUS, "-", null, 1),
                                        new Expr.Literal(3))));

        assertThat(
                new AstReversePolishNotationPrinter().print(expression),
                is(equalTo("1 2 + 4 3 - *")));
    }

    /**
     * Tests {@link AstReversePolishNotationPrinter#visitBinaryExpr(Expr.Binary)} produces correct
     * output.
     */
    @Test
    void visitBinaryExpr_producesCorrectOutput() {

        // 1 + 2
        var expression =
                new Expr.Binary(
                        new Expr.Literal(1), new Token(PLUS, "+", null, 1), new Expr.Literal(2));

        assertThat(
                new AstReversePolishNotationPrinter().visitBinaryExpr(expression),
                is(equalTo("1 2 +")));
    }

    /**
     * Tests {@link AstReversePolishNotationPrinter#visitGroupingExpr(Expr.Grouping)} produces
     * correct output.
     */
    @Test
    void visitGroupingExpr_producesCorrectOutput() {

        // (1)
        var expression = new Expr.Grouping(new Expr.Literal(1));

        assertThat(
                new AstReversePolishNotationPrinter().visitGroupingExpr(expression),
                is(equalTo("1")));
    }

    /**
     * Tests {@link AstReversePolishNotationPrinter#visitLiteralExpr(Expr.Literal)} produces correct
     * output.
     */
    @Test
    void visitLiteralExpr_producesCorrectOutput() {

        // 1
        var expression = new Expr.Literal(1);

        assertThat(
                new AstReversePolishNotationPrinter().visitLiteralExpr(expression),
                is(equalTo("1")));
    }

    /**
     * Tests {@link AstReversePolishNotationPrinter#visitLiteralExpr(Expr.Literal)} produces {@code
     * nil} when value is {@code null}.
     */
    @Test
    void visitLiteralExpr_producesNil_whenValueIsNull() {

        // 1
        var expression = new Expr.Literal(null);

        assertThat(
                new AstReversePolishNotationPrinter().visitLiteralExpr(expression),
                is(equalTo("nil")));
    }

    /**
     * Tests {@link AstReversePolishNotationPrinter#visitUnaryExpr(Expr.Unary)} produces correct
     * output.
     */
    @Test
    void visitUnaryExpr_producesCorrectOutput() {

        // -1
        var expression = new Expr.Unary(new Token(MINUS, "-", null, 1), new Expr.Literal(1));

        assertThat(
                new AstReversePolishNotationPrinter().visitUnaryExpr(expression),
                is(equalTo("1 -")));
    }
}
