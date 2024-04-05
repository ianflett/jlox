package com.github.ianflett.jlox;

import static com.github.ianflett.jlox.TestHelper.*;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOutNormalized;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.function.BiConsumer;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests {@link Interpreter} class. */
class InterpreterTest {

    // region Expr.Binary

    // region a == b

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} equals produces expected result given
     * two numbers.
     *
     * @param left Left operand.
     * @param right Right operand.
     * @param expected Expected result.
     */
    @ParameterizedTest
    @MethodSource
    void visitBinaryExpr_equalsProducesExpectedResult(Object left, Object right, boolean expected) {
        assert_visitBinaryExpr_producesExpectedResult(left, "==", right, expected);
    }

    /**
     * Data source for {@link #visitBinaryExpr_equalsProducesExpectedResult(Object, Object,
     * boolean)} and {@link #visitBinaryExpr_notEqualsProducesExpectedResult(Object, Object,
     * boolean)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments> visitBinaryExpr_equalsProducesExpectedResult() {
        var values = new Object[] {null, true, false, 0d, 1d, "", "string"};

        Builder<Arguments> stream = Stream.builder();
        for (var i = 0; i < values.length; ++i) {
            stream.accept(arguments(values[i], values[i], true));
            for (var j = i + 1; j < values.length; ++j) {
                stream.accept(arguments(values[i], values[j], false));
            }
        }

        return stream.build();
    }

    // endregion

    // region a != b

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} not equals produces expected result
     * given two numbers.
     *
     * @param left Left operand.
     * @param right Right operand.
     * @param expected Expected result.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_equalsProducesExpectedResult")
    void visitBinaryExpr_notEqualsProducesExpectedResult(
            Object left, Object right, boolean expected) {
        assert_visitBinaryExpr_producesExpectedResult(left, "!=", right, !expected);
    }

    // endregion

    // region a < b

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} less than produces error given two
     * invalid types.
     *
     * @param value Operands.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_comparisonErrors_givenInvalidTypes")
    void visitBinaryExpr_lessErrors_givenInvalidTypes(Object value) {
        assertion_visitBinaryExpr_producesError("<", OPERANDS_MUST_BE_NUMBERS_OR_STRINGS_ERROR)
                .accept(value, value);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} less than produces error given mixed
     * types.
     *
     * @param first First operand to check.
     * @param second Second operand to check.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_errors_givenMixedTypes")
    void visitBinaryExpr_lessErrors_givenMixedTypes(Object first, Object second) {
        var assertion =
                assertion_visitBinaryExpr_producesError(
                        "<", OPERANDS_MUST_BE_NUMBERS_OR_STRINGS_ERROR);
        assertion.accept(first, second);
        assertion.accept(second, first);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} less than produces expected result
     * given two operands of valid type.
     *
     * @param left Left operand.
     * @param right Right operand.
     * @param expected Expected result.
     */
    @ParameterizedTest
    @MethodSource
    void visitBinaryExpr_lessProducesExpectedResult_givenValidTypes(
            Object left, Object right, boolean expected) {
        assert_visitBinaryExpr_producesExpectedResult(left, "<", right, expected);
    }

    /**
     * Data source for {@link #visitBinaryExpr_lessProducesExpectedResult_givenValidTypes(Object,
     * Object, boolean)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments> visitBinaryExpr_lessProducesExpectedResult_givenValidTypes() {
        return Stream.of(
                arguments(2d, 2d, false),
                arguments(3d, 2d, false),
                arguments(2d, 3d, true),
                arguments("a", "a", false),
                arguments("b", "a", false),
                arguments("a", "b", true));
    }

    // endregion

    // region a <= b

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} less than or equal produces error
     * given two invalid types.
     *
     * @param value Operands.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_comparisonErrors_givenInvalidTypes")
    void visitBinaryExpr_lessOrEqualErrors_givenInvalidTypes(Object value) {
        assertion_visitBinaryExpr_producesError("<=", OPERANDS_MUST_BE_NUMBERS_OR_STRINGS_ERROR)
                .accept(value, value);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} less than or equal produces error
     * given mixed types.
     *
     * @param first First operand to check.
     * @param second Second operand to check.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_errors_givenMixedTypes")
    void visitBinaryExpr_lessOrEqualErrors_givenMixedTypes(Object first, Object second) {
        var assertion =
                assertion_visitBinaryExpr_producesError(
                        "<=", OPERANDS_MUST_BE_NUMBERS_OR_STRINGS_ERROR);
        assertion.accept(first, second);
        assertion.accept(second, first);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} less than or equal produces expected
     * result given two operands of valid type.
     *
     * @param left Left operand.
     * @param right Right operand.
     * @param expected Expected result.
     */
    @ParameterizedTest
    @MethodSource
    void visitBinaryExpr_lessOrEqualProducesExpectedResult_givenValidTypes(
            Object left, Object right, boolean expected) {
        assert_visitBinaryExpr_producesExpectedResult(left, "<=", right, expected);
    }

    /**
     * Data source for {@link
     * #visitBinaryExpr_lessOrEqualProducesExpectedResult_givenValidTypes(Object, Object, boolean)}
     * tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments>
            visitBinaryExpr_lessOrEqualProducesExpectedResult_givenValidTypes() {
        return Stream.of(
                arguments(2d, 2d, true),
                arguments(3d, 2d, false),
                arguments(2d, 3d, true),
                arguments("a", "a", true),
                arguments("b", "a", false),
                arguments("a", "b", true));
    }

    // endregion

    // region a > b

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} greater than produces error given two
     * invalid types.
     *
     * @param value Operands.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_comparisonErrors_givenInvalidTypes")
    void visitBinaryExpr_greaterErrors_givenInvalidTypes(Object value) {
        assertion_visitBinaryExpr_producesError(">", OPERANDS_MUST_BE_NUMBERS_OR_STRINGS_ERROR)
                .accept(value, value);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} greater than produces error given
     * mixed types.
     *
     * @param first First operand to check.
     * @param second Second operand to check.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_errors_givenMixedTypes")
    void visitBinaryExpr_greaterErrors_givenMixedTypes(Object first, Object second) {
        var assertion =
                assertion_visitBinaryExpr_producesError(
                        ">", OPERANDS_MUST_BE_NUMBERS_OR_STRINGS_ERROR);
        assertion.accept(first, second);
        assertion.accept(second, first);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} greater than produces expected result
     * given two operands of valid type.
     *
     * @param left Left operand.
     * @param right Right operand.
     * @param expected Expected result.
     */
    @ParameterizedTest
    @MethodSource
    void visitBinaryExpr_greaterProducesExpectedResult_givenValidTypes(
            Object left, Object right, boolean expected) {
        assert_visitBinaryExpr_producesExpectedResult(left, ">", right, expected);
    }

    /**
     * Data source for {@link #visitBinaryExpr_greaterProducesExpectedResult_givenValidTypes(Object,
     * Object, boolean)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments>
            visitBinaryExpr_greaterProducesExpectedResult_givenValidTypes() {
        return Stream.of(
                arguments(2d, 2d, false), arguments(3d, 2d, true), arguments(2d, 3d, false));
    }

    // endregion

    // region a >= b

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} greater than or equal produces error
     * given two invalid types.
     *
     * @param value Operands.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_comparisonErrors_givenInvalidTypes")
    void visitBinaryExpr_greaterOrEqualErrors_givenInvalidTypes(Object value) {
        assertion_visitBinaryExpr_producesError(">=", OPERANDS_MUST_BE_NUMBERS_OR_STRINGS_ERROR)
                .accept(value, value);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} greater than or equal produces error
     * given mixed types.
     *
     * @param first First operand to check.
     * @param second Second operand to check.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_errors_givenMixedTypes")
    void visitBinaryExpr_greaterOrEqualErrors_givenMixedTypes(Object first, Object second) {
        var assertion =
                assertion_visitBinaryExpr_producesError(
                        ">=", OPERANDS_MUST_BE_NUMBERS_OR_STRINGS_ERROR);
        assertion.accept(first, second);
        assertion.accept(second, first);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} greater than or equal produces
     * expected result given two operands of valid type.
     *
     * @param left Left operand.
     * @param right Right operand.
     * @param expected Expected result.
     */
    @ParameterizedTest
    @MethodSource
    void visitBinaryExpr_greaterOrEqualProducesExpectedResult_givenValidTypes(
            Object left, Object right, boolean expected) {
        assert_visitBinaryExpr_producesExpectedResult(left, ">=", right, expected);
    }

    /**
     * Data source for {@link
     * #visitBinaryExpr_greaterOrEqualProducesExpectedResult_givenValidTypes(Object, Object,
     * boolean)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments>
            visitBinaryExpr_greaterOrEqualProducesExpectedResult_givenValidTypes() {
        return Stream.of(
                arguments(2d, 2d, true), arguments(3d, 2d, true), arguments(2d, 3d, false));
    }

    // endregion

    // region a + b

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} addition produces error given two
     * invalid types.
     *
     * @param value Operands.
     */
    @ParameterizedTest
    @MethodSource
    void visitBinaryExpr_additionErrors_givenInvalidTypes(Object value) {
        assertion_visitBinaryExpr_producesError("+", OPERANDS_MUST_BE_NUMBERS_OR_STRINGS_ERROR)
                .accept(value, value);
    }

    /**
     * Data source for {@link #visitBinaryExpr_additionErrors_givenInvalidTypes(Object)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<?> visitBinaryExpr_additionErrors_givenInvalidTypes() {
        return Stream.of(null, true);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} addition produces error given
     * non-{@link String} mixed types.
     *
     * @param first First operand to check.
     * @param second Second operand to check.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_errors_givenMixedTypes")
    void visitBinaryExpr_additionErrors_givenNonStringMixedTypes(Object first, Object second) {
        var assertion =
                assertion_visitBinaryExpr_producesError(
                        "+", OPERANDS_MUST_BE_NUMBERS_OR_STRINGS_ERROR);
        assertion.accept(first, second);
        assertion.accept(second, first);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} addition produces expected result
     * given.
     *
     * @param other Other operand to check.
     */
    @ParameterizedTest
    @MethodSource
    void visitBinaryExpr_additionProducesExpectedResult_givenStringAndOtherType(Object other) {
        var string = "string";
        assert_visitBinaryExpr_producesExpectedResult(
                string, "+", other, String.format("%s%s", string, other));
        assert_visitBinaryExpr_producesExpectedResult(
                other, "+", string, String.format("%s%s", other, string));
    }

    /**
     * Data source for {@link
     * #visitBinaryExpr_additionProducesExpectedResult_givenStringAndOtherType(Object)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<?>
            visitBinaryExpr_additionProducesExpectedResult_givenStringAndOtherType() {
        return Stream.of(null, true, 1d);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} subtraction produces expected result
     * given two operands of valid type.
     *
     * @param left Left operand.
     * @param right Right operand.
     * @param expected Expected result.
     */
    @ParameterizedTest
    @MethodSource({
        "visitBinaryExpr_additionProducesExpectedResult_givenTwoNumbers",
        "visitBinaryExpr_additionProducesExpectedResult_givenTwoStrings"
    })
    void visitBinaryExpr_additionProducesExpectedResult_givenTwoValidTypes(
            Object left, Object right, Object expected) {
        assert_visitBinaryExpr_producesExpectedResult(left, "+", right, expected);
    }

    /**
     * Data source for {@link
     * #visitBinaryExpr_additionProducesExpectedResult_givenTwoValidTypes(Object, Object, Object)}
     * tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments>
            visitBinaryExpr_additionProducesExpectedResult_givenTwoNumbers() {
        return Stream.of(arguments(2d, 2d, 4d), arguments(3d, 2d, 5d), arguments(2d, 3d, 5d));
    }

    /**
     * Data source for {@link
     * #visitBinaryExpr_additionProducesExpectedResult_givenTwoValidTypes(Object, Object, Object)}
     * tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments>
            visitBinaryExpr_additionProducesExpectedResult_givenTwoStrings() {
        return Stream.of(
                arguments("a", "a", "aa"), arguments("b", "a", "ba"), arguments("a", "b", "ab"));
    }

    // endregion

    // region a - b

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} subtraction produces error given two
     * invalid types.
     *
     * @param value Operands.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_arithmeticErrors_givenInvalidTypes")
    void visitBinaryExpr_subtractionErrors_givenInvalidTypes(Object value) {
        assertion_visitBinaryExpr_producesError("-", OPERANDS_MUST_BE_NUMBERS_ERROR)
                .accept(value, value);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} subtraction produces error given mixed
     * types.
     *
     * @param first First operand to check.
     * @param second Second operand to check.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_errors_givenMixedTypes")
    void visitBinaryExpr_subtractionErrors_givenMixedTypes(Object first, Object second) {
        var assertion =
                assertion_visitBinaryExpr_producesError("-", OPERANDS_MUST_BE_NUMBERS_ERROR);
        assertion.accept(first, second);
        assertion.accept(second, first);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} subtraction produces expected result
     * given two operands of valid type.
     *
     * @param left Left operand.
     * @param right Right operand.
     * @param expected Expected result.
     */
    @ParameterizedTest
    @MethodSource
    void visitBinaryExpr_subtractionProducesExpectedResult_givenValidTypes(
            double left, double right, double expected) {
        assert_visitBinaryExpr_producesExpectedResult(left, "-", right, expected);
    }

    /**
     * Data source for {@link
     * #visitBinaryExpr_subtractionProducesExpectedResult_givenValidTypes(double, double, double)}
     * tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments>
            visitBinaryExpr_subtractionProducesExpectedResult_givenValidTypes() {
        return Stream.of(arguments(2d, 2d, 0d), arguments(3d, 2d, 1d), arguments(2d, 3d, -1d));
    }

    // endregion

    // region a * b

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} multiplication produces error given
     * two invalid types.
     *
     * @param value Operands.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_arithmeticErrors_givenInvalidTypes")
    void visitBinaryExpr_multiplicationErrors_givenInvalidTypes(Object value) {
        assertion_visitBinaryExpr_producesError("*", OPERANDS_MUST_BE_NUMBERS_ERROR)
                .accept(value, value);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} multiplication produces error given
     * mixed types.
     *
     * @param first First operand to check.
     * @param second Second operand to check.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_errors_givenMixedTypes")
    void visitBinaryExpr_multiplicationErrors_givenMixedTypes(Object first, Object second) {
        var assertion =
                assertion_visitBinaryExpr_producesError("*", OPERANDS_MUST_BE_NUMBERS_ERROR);
        assertion.accept(first, second);
        assertion.accept(second, first);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} multiplication produces expected
     * result given two operands of valid type.
     *
     * @param left Left operand.
     * @param right Right operand.
     * @param expected Expected result.
     */
    @ParameterizedTest
    @MethodSource
    void visitBinaryExpr_multiplicationProducesExpectedResult_givenValidTypes(
            double left, double right, double expected) {
        assert_visitBinaryExpr_producesExpectedResult(left, "*", right, expected);
    }

    /**
     * Data source for {@link
     * #visitBinaryExpr_multiplicationProducesExpectedResult_givenValidTypes(double, double,
     * double)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments>
            visitBinaryExpr_multiplicationProducesExpectedResult_givenValidTypes() {
        return Stream.of(arguments(2d, 2d, 4d), arguments(3d, 2d, 6d), arguments(2d, 3d, 6d));
    }

    // endregion

    // region a / b

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} division produces error given two
     * invalid types.
     *
     * @param value Operands.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_arithmeticErrors_givenInvalidTypes")
    void visitBinaryExpr_divisionErrors_givenInvalidTypes(Object value) {
        assertion_visitBinaryExpr_producesError("/", OPERANDS_MUST_BE_NUMBERS_ERROR)
                .accept(value, value);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} division produces error given mixed
     * types.
     *
     * @param first First operand to check.
     * @param second Second operand to check.
     */
    @ParameterizedTest
    @MethodSource("visitBinaryExpr_errors_givenMixedTypes")
    void visitBinaryExpr_divisionErrors_givenMixedTypes(Object first, Object second) {
        var assertion =
                assertion_visitBinaryExpr_producesError("/", OPERANDS_MUST_BE_NUMBERS_ERROR);
        assertion.accept(first, second);
        assertion.accept(second, first);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} division produces error given
     * denominator of zero.
     */
    @ParameterizedTest
    @ValueSource(doubles = {-0d, 0d})
    void visitBinaryExpr_divisionErrors_givenZeroDenominator(double denominator) {
        var assertion = assertion_visitBinaryExpr_producesError("/", "Division by zero.");
        assertion.accept(2d, denominator);
    }

    /**
     * Tests {@link Interpreter#visitBinaryExpr(Expr.Binary)} division produces expected result
     * given two operands of valid type.
     *
     * @param left Left operand.
     * @param right Right operand.
     * @param expected Expected result.
     */
    @ParameterizedTest
    @MethodSource
    void visitBinaryExpr_divisionProducesExpectedResult_givenValidTypes(
            double left, double right, double expected) {
        assert_visitBinaryExpr_producesExpectedResult(left, "/", right, expected);
    }

    /**
     * Data source for {@link
     * #visitBinaryExpr_divisionProducesExpectedResult_givenValidTypes(double, double, double)}
     * tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments>
            visitBinaryExpr_divisionProducesExpectedResult_givenValidTypes() {
        return Stream.of(
                arguments(2d, 2d, 1d), arguments(3d, 2d, 3d / 2d), arguments(2d, 3d, 2d / 3d));
    }

    // endregion

    /**
     * Data source for {@link #visitBinaryExpr_greaterErrors_givenInvalidTypes(Object)}, {@link
     * #visitBinaryExpr_greaterOrEqualErrors_givenInvalidTypes(Object)}, {@link
     * #visitBinaryExpr_lessErrors_givenInvalidTypes(Object)}, and {@link
     * #visitBinaryExpr_lessOrEqualErrors_givenInvalidTypes(Object)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<?> visitBinaryExpr_comparisonErrors_givenInvalidTypes() {
        return Stream.of(null, true);
    }

    /**
     * Data source for {@link #visitBinaryExpr_subtractionErrors_givenInvalidTypes(Object)}, {@link
     * #visitBinaryExpr_multiplicationErrors_givenInvalidTypes(Object)}, and {@link
     * #visitBinaryExpr_divisionErrors_givenInvalidTypes(Object)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<?> visitBinaryExpr_arithmeticErrors_givenInvalidTypes() {
        return Stream.of(null, true, "\"");
    }

    /**
     * Data source for {@link #visitBinaryExpr_additionErrors_givenNonStringMixedTypes(Object,
     * Object)}, {@link #visitBinaryExpr_subtractionErrors_givenMixedTypes(Object, Object)}, {@link
     * #visitBinaryExpr_multiplicationErrors_givenMixedTypes(Object, Object)}, and {@link
     * #visitBinaryExpr_divisionErrors_givenMixedTypes(Object, Object)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments> visitBinaryExpr_errors_givenMixedTypes() {
        var values = new Object[] {null, true, 1d};
        Builder<Arguments> stream = Stream.builder();
        for (var i = 0; i < values.length; ++i) {
            for (var j = i + 1; j < values.length; ++j) {
                stream.accept(arguments(values[i], values[j]));
            }
        }
        return stream.build();
    }

    /**
     * Assertion {@link Interpreter#visitBinaryExpr(Expr.Binary)} produces expected error.
     *
     * @param operator Operator literal.
     * @param expected Expected error message.
     * @return {@link BiConsumer} assert against both operands.
     */
    private static BiConsumer<Object, Object> assertion_visitBinaryExpr_producesError(
            String operator, String expected) {
        var token = t(operator);
        return (left, right) -> {
            try {
                new Interpreter().visitBinaryExpr((Expr.Binary) e(left, token, right));
                fail("No error thrown.");
            } catch (RuntimeError exception) {
                assertThat(exception.token, is(equalTo(token)));
                assertThat(exception.getMessage(), is(equalTo(expected)));
            }
        };
    }

    /**
     * Asserts {@link Interpreter#visitBinaryExpr(Expr.Binary)} produces expected result.
     *
     * @param left Left operand.
     * @param operator Operator literal.
     * @param right Right operand.
     * @param expected Expected result.
     */
    private static void assert_visitBinaryExpr_producesExpectedResult(
            Object left, String operator, Object right, Object expected) {
        var expression = (Expr.Binary) e(left, t(operator), right);

        assertThat(new Interpreter().visitBinaryExpr(expression), is(equalTo(expected)));
    }

    /** Error message when number operands are expected. */
    private static final String OPERANDS_MUST_BE_NUMBERS_ERROR = "Operands must be numbers.";

    /** Error message when number or string operands are expected. */
    private static final String OPERANDS_MUST_BE_NUMBERS_OR_STRINGS_ERROR =
            "Operands must be two numbers or two strings.";

    // endregion

    // region Expr.Conditional

    /**
     * Tests {@link Interpreter#visitConditionalExpr(Expr.Conditional)} produces then branch given
     * {@code true} conditional.
     */
    @Test
    void visitConditionalExpr_returnsThenBranch_whenConditionalTrue() {
        assert_visitConditionalExpr(true, 2d);
    }

    /**
     * Tests {@link Interpreter#visitConditionalExpr(Expr.Conditional)} produces else branch given
     * {@code false} conditional.
     */
    @Test
    void visitConditionalExpr_returnsElseBranch_whenConditionalFalse() {
        assert_visitConditionalExpr(false, 3d);
    }

    /**
     * Asserts {@link Interpreter#visitConditionalExpr(Expr.Conditional)} interprets {@link
     * Expr.Conditional} as expected.
     *
     * @param condition Result of condition.
     * @param expected Expected value.
     */
    private static void assert_visitConditionalExpr(boolean condition, double expected) {
        var expression = (Expr.Conditional) e(condition, 2, 3);
        assertThat(new Interpreter().visitConditionalExpr(expression), is(equalTo(expected)));
    }

    // endregion

    // region Expr.Grouping

    /**
     * Tests {@link Interpreter#visitGroupingExpr(Expr.Grouping)} interprets {@link Expr.Literal}
     * {@code value} outputs {@code value}.
     *
     * @param value Value to test.
     */
    @ParameterizedTest
    @MethodSource("literalValues")
    void visitGrouping_returnsValue_givenValue(Object value) {
        assertThat(
                new Interpreter().visitGroupingExpr((Expr.Grouping) e(value)), is(equalTo(value)));
    }

    // endregion

    // region Expr.Literal

    /**
     * Tests {@link Interpreter#visitLiteralExpr(Expr.Literal)} interprets {@link Expr.Literal}
     * {@code value} outputs {@code value}.
     *
     * @param value Value to test.
     */
    @ParameterizedTest
    @MethodSource("literalValues")
    void visitLiteral_returnsValue_givenValue(Object value) {
        assertThat(new Interpreter().visitLiteralExpr(new Expr.Literal(value)), is(equalTo(value)));
    }

    /**
     * Data source for {@link #visitGrouping_returnsValue_givenValue(Object)} and {@link
     * #visitLiteral_returnsValue_givenValue(Object)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<?> literalValues() {
        return Stream.of(null, true, false, 1d, "string");
    }

    // endregion

    // region Expr.Unary

    /**
     * Tests {@link Interpreter#visitUnaryExpr(Expr.Unary)} interprets {@link Expr.Unary} not as
     * {@code false} for {@code value}.
     *
     * @param value Input value.
     */
    @ParameterizedTest
    @MethodSource
    void visitUnary_unaryBangReturnsFalse_givenTruthy(Object value) {
        assert_visitUnary("!", value, false);
    }

    /**
     * Data source for {@link #visitUnary_unaryBangReturnsFalse_givenTruthy(Object)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<?> visitUnary_unaryBangReturnsFalse_givenTruthy() {
        return Stream.of(true, -1d, 0d, 1d, "", "string");
    }

    /**
     * Tests {@link Interpreter#visitUnaryExpr(Expr.Unary)} interprets {@link Expr.Unary} not as
     * {@code true} for {@code value}.
     *
     * @param value Input value.
     */
    @ParameterizedTest
    @MethodSource
    void visitUnary_unaryBangReturnsTrue_givenFalsy(Object value) {
        assert_visitUnary("!", value, true);
    }

    /**
     * Data source for {@link #visitUnary_unaryBangReturnsTrue_givenFalsy(Object)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<?> visitUnary_unaryBangReturnsTrue_givenFalsy() {
        return Stream.of(null, false);
    }

    /**
     * Tests {@link Interpreter#visitUnaryExpr(Expr.Unary)} interprets {@link Expr.Unary} number
     * negation as negated {@code value}.
     *
     * @param value Input value.
     */
    @ParameterizedTest
    @ValueSource(doubles = {-1, -0, 0, 1})
    void visitUnary_unaryMinusReturnsNegative_givenNumber(double value) {
        assert_visitUnary("-", value, -value);
    }

    /**
     * Tests {@link Interpreter#visitUnaryExpr(Expr.Unary)} interprets {@link Expr.Unary} non-number
     * negation by raising {@link RuntimeError}.
     *
     * @param value Input value.
     */
    @ParameterizedTest
    @MethodSource
    void visitUnary_unaryMinusErrors_givenNonNumber(Object value) {
        var token = t("-");
        var expression = (Expr.Unary) e(token, value);

        try {
            new Interpreter().visitUnaryExpr(expression);
            fail("No error thrown.");
        } catch (RuntimeError exception) {
            assertThat(exception.token, is(equalTo(token)));
            assertThat(exception.getMessage(), is(equalTo("Operand must be a number.")));
        }
    }

    /**
     * Data source for {@link #visitUnary_unaryMinusErrors_givenNonNumber(Object)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<?> visitUnary_unaryMinusErrors_givenNonNumber() {
        return Stream.of(null, true, false, "", "string");
    }

    /**
     * Asserts {@link Interpreter#visitUnaryExpr(Expr.Unary)} produces the expected value.
     *
     * @param operator Operator to test.
     * @param operand Operand to test.
     * @param expected Expected result.
     */
    private static void assert_visitUnary(String operator, Object operand, Object expected) {
        var expression = (Expr.Unary) e(t(operator), operand);
        assertThat(new Interpreter().visitUnaryExpr(expression), is(equalTo(expected)));
    }

    // endregion

    // region Expr.Variable

    /**
     * Tests {@link Interpreter#visitVariableExpr(Expr.Variable)} throws {@link RuntimeError} when
     * requesting undeclared variable.
     */
    @Test
    void visitVariableExpr_throwsRuntimeError_givenUndeclaredVariable() {
        var exception =
                assertThrows(
                        RuntimeError.class,
                        () ->
                                new Interpreter()
                                        .visitVariableExpr((Expr.Variable) e(t("noVariable"))));
        assertThat(exception.getMessage(), is(equalTo("Undefined variable 'noVariable'.")));
    }

    /**
     * Tests {@link Interpreter#visitVariableExpr(Expr.Variable)} interprets {@link Expr.Variable}
     * by returning {@code value} bound to {@code name}.
     *
     * @param name Variable name.
     * @param value Expected value.
     */
    @ParameterizedTest
    @MethodSource("variableDefinitions")
    void visitVariableExpr_returnsValue_givenValueBoundToName(String name, Object value) {
        var environment = new Environment();
        environment.define(name, value);

        assertThat(
                new Interpreter(environment).visitVariableExpr((Expr.Variable) e(t(name))),
                is(equalTo(value)));
    }

    // endregion

    // region Stmt.Print

    /**
     * Tests {@link Interpreter#visitPrintStmt(Stmt.Print)} interprets {@link Stmt.Print} by
     * producing expected output.
     *
     * @param expression {@link Expr}ession to print.
     * @param expected Expected output.
     */
    @ParameterizedTest
    @MethodSource
    void visitPrintStmt_outputsExpressionResult_givenExpression(Expr expression, String expected)
            throws Exception {
        var output =
                tapSystemOutNormalized(
                        () -> new Interpreter().visitPrintStmt(new Stmt.Print(expression)));
        assertThat(output, is(equalTo(expected + "\n")));
    }

    /**
     * Data source for {@link #visitPrintStmt_outputsExpressionResult_givenExpression(Expr, String)}
     * tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments> visitPrintStmt_outputsExpressionResult_givenExpression() {
        return Stream.of(
                arguments(e(null), "nil"),
                arguments(e(3, t("=="), 2), "false"),
                arguments(e(3, t("!="), 2), "true"),
                arguments(e(3, t("+"), 2), "5"),
                arguments(e(1, t("/"), 2), "0.5"),
                arguments(e(""), ""),
                arguments(e("foo", t("+"), "bar"), "foobar"));
    }

    // endregion

    // region Stmt.Var

    /**
     * Tests {@link Interpreter#visitVarStmt(Stmt.Var)} interprets {@link Stmt.Var} by binding
     * {@code value} to {@code name}.
     *
     * @param name Variable name.
     * @param value Expected value.
     */
    @ParameterizedTest
    @MethodSource("variableDefinitions")
    void visitVarStmt_bindsVariable_givenNameAndValue(String name, Object value) {
        var token = t(name);
        var environment = new Environment();
        new Interpreter(environment).visitVarStmt(new Stmt.Var(token, e(value)));
        assertThat(environment.get(token), is(equalTo(value)));
    }

    /**
     * Tests {@link Interpreter#visitVarStmt(Stmt.Var)} interprets {@link Stmt.Var} by rebinding
     * {@code value} to {@code name}.
     *
     * @param name Variable name.
     * @param value Expected value.
     */
    @ParameterizedTest
    @MethodSource("variableDefinitions")
    void visitVarStmt_rebindsVariable_givenNameAndValue(String name, Object value) {
        var token = t(name);
        var oldValue = -2d;
        var environment = new Environment();
        var interpreter = new Interpreter(environment);
        interpreter.visitVarStmt(new Stmt.Var(token, e(oldValue)));
        assertThat(environment.get(token), is(equalTo(oldValue)));
        interpreter.visitVarStmt(new Stmt.Var(token, e(value)));
        assertThat(environment.get(token), is(equalTo(value)));
    }

    // endregion

    /**
     * Data source for {@link #visitVariableExpr_returnsValue_givenValueBoundToName(String, Object)}
     * and {@link #visitVarStmt_bindsVariable_givenNameAndValue(String, Object)} tests.
     *
     * @return Test argument data.
     */
    static Stream<Arguments> variableDefinitions() {
        return Stream.of(
                arguments("myBoolean", false),
                arguments("myNothing", null),
                arguments("myNumber", 1d),
                arguments("myString", "Hello world!"));
    }
}
