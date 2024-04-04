package com.github.ianflett.jlox;

import static com.github.ianflett.jlox.TestHelper.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.github.ianflett.jlox.AstPrinter.Directory;
import com.github.ianflett.jlox.AstPrinter.Lisp;
import com.github.ianflett.jlox.AstPrinter.ReversePolishNotation;
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
     * Asserts {@link AstPrinter#print(Expr)} produces expected output for example abstract syntax
     * tree.
     */
    void assert_print_producesExpectedOutput(Expr expression, String expected) {
        assertThat(GetPrinter().print(expression), is(equalTo(expected)));
    }

    /** First {@link Expr} example: {@code -123 * (45.67)}. */
    static final Expr assert_print_producesExpectedOutput_expression1 =
            e(e(t("-"), 123), t("*"), e(45.67));

    /** Second {@link Expr} example: {@code (1 + 2) * (4 - 3)}. */
    static final Expr assert_print_producesExpectedOutput_expression2 =
            e(e(e(1, t("+"), 2)), t("*"), e(e(4, t("-"), 3)));

    /** Asserts {@link AstPrinter#visitBinaryExpr(Expr.Binary)} produces expected output. */
    protected void assert_visitBinaryExpr_producesExpectedOutput(String expected) {
        assertThat(
                GetPrinter().visitBinaryExpr(visitBinaryExpr_producesExpectedOutput_expression),
                is(equalTo(expected)));
    }

    /** {@code 1 + 2} */
    private static final Expr.Binary visitBinaryExpr_producesExpectedOutput_expression =
            (Expr.Binary) e(1, t("+"), 2);

    /** Asserts {@link Lisp#visitGroupingExpr(Expr.Grouping)} produces expected output. */
    protected void assert_visitGroupingExpr_producesExpectedOutput(String expected) {
        assertThat(
                GetPrinter()
                        .visitGroupingExpr(
                                assert_visitGroupingExpr_producesExpectedOutput_expression),
                is(equalTo(expected)));
    }

    /** {@code (1)} */
    private static final Expr.Grouping assert_visitGroupingExpr_producesExpectedOutput_expression =
            (Expr.Grouping) e(1);

    /** Asserts {@link AstPrinter#visitUnaryExpr(Expr.Unary)} produces expected output. */
    void assert_visitUnaryExpr_producesExpectedOutput(String expected) {
        assertThat(
                GetPrinter()
                        .visitUnaryExpr(assert_visitUnaryExpr_producesExpectedOutput_expression),
                is(equalTo(expected)));
    }

    /** {@code -1} */
    private static final Expr.Unary assert_visitUnaryExpr_producesExpectedOutput_expression =
            (Expr.Unary) e(t("-"), 1);

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
         * Tests {@link Lisp#print(Expr)} produces expected output for example abstract syntax tree.
         */
        @ParameterizedTest(name = "\"{1}\"")
        @MethodSource
        protected void print_producesExpectedOutput(Expr expression, String expected) {
            assert_print_producesExpectedOutput(expression, expected);
        }

        /**
         * Data source for {@link #print_producesExpectedOutput(Expr, String)} tests.
         *
         * @return Test argument data.
         */
        private static Stream<Arguments> print_producesExpectedOutput() {
            return Stream.of(
                    arguments(
                            assert_print_producesExpectedOutput_expression1,
                            "(* (- 123.0) (group 45.67))"),
                    arguments(
                            assert_print_producesExpectedOutput_expression2,
                            "(* (group (+ 1.0 2.0)) (group (- 4.0 3.0)))"));
        }

        /** Tests {@link Lisp#visitBinaryExpr(Expr.Binary)} produces expected output. */
        @Test
        protected void visitBinaryExpr_producesExpectedOutput() {
            assert_visitBinaryExpr_producesExpectedOutput("(+ 1.0 2.0)");
        }

        /** Tests {@link Lisp#visitGroupingExpr(Expr.Grouping)} produces expected output. */
        @Test
        protected void visitGroupingExpr_producesExpectedOutput() {
            assert_visitGroupingExpr_producesExpectedOutput("(group 1.0)");
        }

        /** Tests {@link AstPrinter#visitLiteralExpr(Expr.Literal)} produces expected output. */
        @ParameterizedTest
        @MethodSource
        void visitLiteralExpr_producesExpectedOutput(Expr.Literal expression, String expected) {
            assertThat(GetPrinter().visitLiteralExpr(expression), is(equalTo(expected)));
        }

        /**
         * Data source for {@link #visitLiteralExpr_producesExpectedOutput(Expr.Literal, String)}
         * tests.
         *
         * @return Test argument data.
         */
        private static Stream<Arguments> visitLiteralExpr_producesExpectedOutput() {
            return Stream.of(
                    arguments(new Expr.Literal(1d), "1.0"),
                    arguments(new Expr.Literal(null), "nil"));
        }

        /** Tests {@link Lisp#visitUnaryExpr(Expr.Unary)} produces expected output. */
        @Test
        void visitUnaryExpr_producesExpectedOutput() {
            assert_visitUnaryExpr_producesExpectedOutput("(- 1.0)");
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
         * Tests {@link ReversePolishNotation#print(Expr)} produces expected output for example
         * abstract syntax tree.
         */
        @ParameterizedTest(name = "\"{1}\"")
        @MethodSource
        protected void print_producesExpectedOutput(Expr expression, String expected) {
            assert_print_producesExpectedOutput(expression, expected);
        }

        /**
         * Data source for {@link #print_producesExpectedOutput(Expr, String)} tests.
         *
         * @return Test argument data.
         */
        private static Stream<Arguments> print_producesExpectedOutput() {
            return Stream.of(
                    arguments(assert_print_producesExpectedOutput_expression1, "123.0 - 45.67 *"),
                    arguments(
                            assert_print_producesExpectedOutput_expression2,
                            "1.0 2.0 + 4.0 3.0 - *"));
        }

        /**
         * Tests {@link ReversePolishNotation#visitBinaryExpr(Expr.Binary)} produces expected
         * output.
         */
        @Test
        protected void visitBinaryExpr_producesExpectedOutput() {
            assert_visitBinaryExpr_producesExpectedOutput("1.0 2.0 +");
        }

        /**
         * Tests {@link ReversePolishNotation#visitGroupingExpr(Expr.Grouping)} produces expected
         * output.
         */
        @Test
        protected void visitGroupingExpr_producesExpectedOutput() {
            assert_visitGroupingExpr_producesExpectedOutput("1.0");
        }

        /** Tests {@link AstPrinter#visitLiteralExpr(Expr.Literal)} produces expected output. */
        @ParameterizedTest
        @MethodSource
        void visitLiteralExpr_producesExpectedOutput(Expr.Literal expression, String expected) {
            assertThat(GetPrinter().visitLiteralExpr(expression), is(equalTo(expected)));
        }

        /**
         * Data source for {@link #visitLiteralExpr_producesExpectedOutput(Expr.Literal, String)}
         * tests.
         *
         * @return Test argument data.
         */
        private static Stream<Arguments> visitLiteralExpr_producesExpectedOutput() {
            return Stream.of(
                    arguments(new Expr.Literal(1d), "1.0"),
                    arguments(new Expr.Literal(null), "nil"));
        }

        /**
         * Tests {@link ReversePolishNotation#visitUnaryExpr(Expr.Unary)} produces expected output.
         */
        @Test
        void visitUnaryExpr_producesExpectedOutput() {
            assert_visitUnaryExpr_producesExpectedOutput("1.0 -");
        }
    }

    /** Unit tests {@link Directory} class. */
    static class DirectoryTests extends AstPrinterTests<Directory> {

        /**
         * Creates {@link Directory} pretty-printer for test.
         *
         * @return {@link Directory} pretty-printer.
         */
        @Override
        protected Directory GetPrinter() {
            return new Directory();
        }

        /**
         * Tests {@link Directory#print(Expr)} produces expected output for example abstract syntax
         * tree.
         */
        @ParameterizedTest(name = "\"{1}\"")
        @MethodSource
        protected void print_producesExpectedOutput(Expr expression, String expected) {
            assert_print_producesExpectedOutput(expression, expected);
        }

        /**
         * Data source for {@link #print_producesExpectedOutput(Expr, String)} tests.
         *
         * @return Test argument data.
         */
        private static Stream<Arguments> print_producesExpectedOutput() {
            return Stream.of(
                    arguments(
                            assert_print_producesExpectedOutput_expression1,
                            String.format("*%n├ -%n│ └ 123.0%n└ ()%n  └ 45.67%n")),
                    arguments(
                            assert_print_producesExpectedOutput_expression2,
                            String.format(
                                    "*%n├ ()%n│ └ +%n│   ├ 1.0%n│   └ 2.0%n└ ()%n  └ -%n    ├ 4.0%n"
                                            + "    └ 3.0%n")));
        }

        /** Tests {@link Directory#visitBinaryExpr(Expr.Binary)} produces expected output. */
        @Test
        protected void visitBinaryExpr_producesExpectedOutput() {
            assert_visitBinaryExpr_producesExpectedOutput(String.format("+%n├ 1.0%n└ 2.0%n"));
        }

        /** Tests {@link Directory#visitGroupingExpr(Expr.Grouping)} produces expected output. */
        @Test
        protected void visitGroupingExpr_producesExpectedOutput() {
            assert_visitGroupingExpr_producesExpectedOutput(String.format("()%n└ 1.0%n"));
        }

        /** Tests {@link AstPrinter#visitLiteralExpr(Expr.Literal)} produces expected output. */
        @ParameterizedTest
        @MethodSource
        void visitLiteralExpr_producesExpectedOutput(Expr.Literal expression, String expected) {
            assertThat(GetPrinter().visitLiteralExpr(expression), is(equalTo(expected)));
        }

        /**
         * Data source for {@link #visitLiteralExpr_producesExpectedOutput(Expr.Literal, String)}
         * tests.
         *
         * @return Test argument data.
         */
        private static Stream<Arguments> visitLiteralExpr_producesExpectedOutput() {
            return Stream.of(
                    arguments(new Expr.Literal(1d), String.format("1.0%n")),
                    arguments(new Expr.Literal(null), String.format("nil%n")));
        }

        /** Tests {@link Directory#visitUnaryExpr(Expr.Unary)} produces expected output. */
        @Test
        void visitUnaryExpr_producesExpectedOutput() {
            assert_visitUnaryExpr_producesExpectedOutput(String.format("-%n└ 1.0%n"));
        }
    }
}
