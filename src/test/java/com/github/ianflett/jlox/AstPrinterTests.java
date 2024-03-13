package com.github.ianflett.jlox;

import static com.github.ianflett.jlox.TokenType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.github.ianflett.jlox.AstPrinter.Lisp;
import com.github.ianflett.jlox.AstPrinter.ReversePolishNotation;
import com.github.ianflett.jlox.Expr.Binary;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests {@link AstPrinter} implementation. */
public abstract class AstPrinterTests<T extends AstPrinter> {

    /** Defines {@link AstPrinter} under test. */
    protected abstract T GetPrinter();

    /**
     * Asserts {@link AstPrinter#print(Expr)} produces correct output for example abstract syntax
     * tree.
     */
    protected void assert_print_producesCorrectOutput(Expr expression, String expected) {
        assertThat(GetPrinter().print(expression), is(equalTo(expected)));
    }

    /** First {@link Expr} example: {@code -123 * (45.67)}. */
    static final Expr assert_print_producesCorrectOutput_expression1 =
            new Binary(
                    new Expr.Unary(new Token(MINUS, "-", null, 1), new Expr.Literal(123)),
                    new Token(STAR, "*", null, 1),
                    new Expr.Grouping(new Expr.Literal(45.67)));

    /** Second {@link Expr} example: {@code (1 + 2) * (4 - 3)}. */
    static final Expr assert_print_producesCorrectOutput_expression2 =
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

    /** Asserts {@link AstPrinter#visitBinaryExpr(Binary)} produces correct output. */
    protected void assert_visitBinaryExpr_producesCorrectOutput(String expected) {
        assertThat(
                GetPrinter().visitBinaryExpr(visitBinaryExpr_producesCorrectOutput_expression),
                is(equalTo(expected)));
    }

    /** {@code 1 + 2} */
    private static final Binary visitBinaryExpr_producesCorrectOutput_expression =
            new Binary(new Expr.Literal(1), new Token(PLUS, "+", null, 1), new Expr.Literal(2));

    /** Asserts {@link Lisp#visitGroupingExpr(Expr.Grouping)} produces correct output. */
    protected void assert_visitGroupingExpr_producesCorrectOutput(String expected) {
        assertThat(
                GetPrinter()
                        .visitGroupingExpr(
                                assert_visitGroupingExpr_producesCorrectOutput_expression),
                is(equalTo(expected)));
    }

    /** {@code (1)} */
    private static final Expr.Grouping assert_visitGroupingExpr_producesCorrectOutput_expression =
            new Expr.Grouping(new Expr.Literal(1));

    /** Tests {@link AstPrinter#visitLiteralExpr(Expr.Literal)} produces correct output. */
    @ParameterizedTest
    @MethodSource("visitLiteralExpr_producesCorrectOutput_data")
    void visitLiteralExpr_producesCorrectOutput(Expr.Literal expression, String expected) {
        assertThat(GetPrinter().visitLiteralExpr(expression), is(equalTo(expected)));
    }

    /**
     * Data source for {@link #visitLiteralExpr_producesCorrectOutput(Expr.Literal, String)} tests.
     *
     * @return Test argument data.
     */
    static Stream<Arguments> visitLiteralExpr_producesCorrectOutput_data() {
        return Stream.of(
                arguments(new Expr.Literal(1), "1"), arguments(new Expr.Literal(null), "nil"));
    }

    /** Asserts {@link AstPrinter#visitUnaryExpr(Expr.Unary)} produces correct output. */
    void assert_visitUnaryExpr_producesCorrectOutput(String expected) {
        assertThat(
                GetPrinter().visitUnaryExpr(assert_visitUnaryExpr_producesCorrectOutput_expression),
                is(equalTo(expected)));
    }

    /** {@code -1} */
    private static final Expr.Unary assert_visitUnaryExpr_producesCorrectOutput_expression =
            new Expr.Unary(new Token(MINUS, "-", null, 1), new Expr.Literal(1));

    /** Unit tests {@link Lisp} class. */
    static class LispTests extends AstPrinterTests<Lisp> {

        /**
         * Creates {@link Lisp} pretty-printer for test.
         *
         * @return {@link Lisp} pretty-printer.
         */
        @Override
        protected Lisp GetPrinter() {
            return new Lisp();
        }

        /**
         * Tests {@link Lisp#print(Expr)} produces correct output for example abstract syntax tree.
         */
        @ParameterizedTest(name = "\"{1}\"")
        @MethodSource("print_producesCorrectOutput_data")
        protected void print_producesCorrectOutput(Expr expression, String expected) {
            assert_print_producesCorrectOutput(expression, expected);
        }

        /**
         * Data source for {@link #print_producesCorrectOutput(Expr, String)} tests.
         *
         * @return Test argument data.
         */
        static Stream<Arguments> print_producesCorrectOutput_data() {
            return Stream.of(
                    arguments(
                            assert_print_producesCorrectOutput_expression1,
                            "(* (- 123) (group 45.67))"),
                    arguments(
                            assert_print_producesCorrectOutput_expression2,
                            "(* (group (+ 1 2)) (group (- 4 3)))"));
        }

        /** Tests {@link Lisp#visitBinaryExpr(Binary)} produces correct output. */
        @Test
        protected void visitBinaryExpr_producesCorrectOutput() {
            assert_visitBinaryExpr_producesCorrectOutput("(+ 1 2)");
        }

        /** Tests {@link Lisp#visitGroupingExpr(Expr.Grouping)} produces correct output. */
        @Test
        protected void visitGroupingExpr_producesCorrectOutput() {
            assert_visitGroupingExpr_producesCorrectOutput("(group 1)");
        }

        /** Tests {@link Lisp#visitUnaryExpr(Expr.Unary)} produces correct output. */
        @Test
        void visitUnaryExpr_producesCorrectOutput() {
            assert_visitUnaryExpr_producesCorrectOutput("(- 1)");
        }
    }

    /** Unit tests {@link ReversePolishNotation} class. */
    static class ReversePolishNotationTests extends AstPrinterTests<ReversePolishNotation> {

        /**
         * Creates {@link ReversePolishNotation} pretty-printer for test.
         *
         * @return {@link ReversePolishNotation} pretty-printer.
         */
        @Override
        protected ReversePolishNotation GetPrinter() {
            return new ReversePolishNotation();
        }

        /**
         * Tests {@link ReversePolishNotation#print(Expr)} produces correct output for example
         * abstract syntax tree.
         */
        @ParameterizedTest(name = "\"{1}\"")
        @MethodSource("print_producesCorrectOutput_data")
        protected void print_producesCorrectOutput(Expr expression, String expected) {
            assert_print_producesCorrectOutput(expression, expected);
        }

        /**
         * Data source for {@link #print_producesCorrectOutput(Expr, String)} tests.
         *
         * @return Test argument data.
         */
        static Stream<Arguments> print_producesCorrectOutput_data() {
            return Stream.of(
                    arguments(assert_print_producesCorrectOutput_expression1, "123 - 45.67 *"),
                    arguments(assert_print_producesCorrectOutput_expression2, "1 2 + 4 3 - *"));
        }

        /** Tests {@link ReversePolishNotation#visitBinaryExpr(Binary)} produces correct output. */
        @Test
        protected void visitBinaryExpr_producesCorrectOutput() {
            assert_visitBinaryExpr_producesCorrectOutput("1 2 +");
        }

        /** Tests {@link ReversePolishNotation#visitGroupingExpr(Expr.Grouping)} produces correct output. */
        @Test
        protected void visitGroupingExpr_producesCorrectOutput() {
            assert_visitGroupingExpr_producesCorrectOutput("1");
        }

        /** Tests {@link ReversePolishNotation#visitUnaryExpr(Expr.Unary)} produces correct output. */
        @Test
        void visitUnaryExpr_producesCorrectOutput() {
            assert_visitUnaryExpr_producesCorrectOutput("1 -");
        }
    }
}
