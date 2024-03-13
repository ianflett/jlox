package com.github.ianflett.jlox;

import static com.github.ianflett.jlox.TokenType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

/** Unit tests {@link AstPrinter} class. */
public class AstPrinterTests {

    /**
     * Tests {@link AstPrinter#print(Expr)} produces correct output for example abstract syntax
     * tree.
     */
    @Test
    void print_producesCorrectOutput() {

        // -123 * (45.67)
        var expression =
                new Expr.Binary(
                        new Expr.Unary(new Token(MINUS, "-", null, 1), new Expr.Literal(123)),
                        new Token(STAR, "*", null, 1),
                        new Expr.Grouping(new Expr.Literal(45.67)));

        assertThat(new AstPrinter().print(expression), is(equalTo("(* (- 123) (group 45.67))")));
    }

    /** Tests {@link AstPrinter#visitBinaryExpr(Expr.Binary)} produces correct output. */
    @Test
    void visitBinaryExpr_producesCorrectOutput() {

        // 1 + 2
        var expression =
                new Expr.Binary(
                        new Expr.Literal(1), new Token(PLUS, "+", null, 1), new Expr.Literal(2));

        assertThat(new AstPrinter().visitBinaryExpr(expression), is(equalTo("(+ 1 2)")));
    }

    /** Tests {@link AstPrinter#visitGroupingExpr(Expr.Grouping)} produces correct output. */
    @Test
    void visitGroupingExpr_producesCorrectOutput() {

        // (1)
        var expression = new Expr.Grouping(new Expr.Literal(1));

        assertThat(new AstPrinter().visitGroupingExpr(expression), is(equalTo("(group 1)")));
    }

    /** Tests {@link AstPrinter#visitLiteralExpr(Expr.Literal)} produces correct output. */
    @Test
    void visitLiteralExpr_producesCorrectOutput() {

        // 1
        var expression = new Expr.Literal(1);

        assertThat(new AstPrinter().visitLiteralExpr(expression), is(equalTo("1")));
    }

    /**
     * Tests {@link AstPrinter#visitLiteralExpr(Expr.Literal)} produces {@code nil} when value is
     * {@code null}.
     */
    @Test
    void visitLiteralExpr_producesNil_whenValueIsNull() {

        // 1
        var expression = new Expr.Literal(null);

        assertThat(new AstPrinter().visitLiteralExpr(expression), is(equalTo("nil")));
    }

    /** Tests {@link AstPrinter#visitUnaryExpr(Expr.Unary)} produces correct output. */
    @Test
    void visitUnaryExpr_producesCorrectOutput() {

        // -1
        var expression = new Expr.Unary(new Token(MINUS, "-", null, 1), new Expr.Literal(1));

        assertThat(new AstPrinter().visitUnaryExpr(expression), is(equalTo("(- 1)")));
    }
}
