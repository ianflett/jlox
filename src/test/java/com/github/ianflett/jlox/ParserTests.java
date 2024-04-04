package com.github.ianflett.jlox;

import static com.github.ianflett.jlox.TestHelper.*;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErrNormalized;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests {@link Parser} class. */
public class ParserTests {

    /** Tests {@link Parser#parse()} returns {@code null} when {@link Token} unrecognised. */
    @Test
    void parse_returnsNull_whenTokenUnrecognised() throws Exception {
        assert_parseError(tz("}", ";"), "[line 1] Error at '}': Expect expression.");
    }

    /**
     * Tests {@link Parser#parse()} generates {@link Expr}ession where left operator has precedence
     * between two binary operators of same left-associative grammar.
     *
     * @param first First operators to check.
     * @param second Second operators to check.
     */
    @ParameterizedTest
    @MethodSource
    void parse_binaryGrammarsSharePrecedence_givenGrammarsAtSameLevel(String first, String second) {
        assert_parse_leftBinaryHasHigherPrecedence(first, second);
        assert_parse_leftBinaryHasHigherPrecedence(second, first);
    }

    /**
     * Data source for {@link #parse_binaryGrammarsSharePrecedence_givenGrammarsAtSameLevel(String,
     * String)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments>
            parse_binaryGrammarsSharePrecedence_givenGrammarsAtSameLevel() {
        return Stream.of(
                arguments("==", "!="),
                arguments("<", "<="),
                arguments("<=", ">"),
                arguments(">", ">="),
                arguments("+", "-"),
                arguments("*", "/"));
    }

    /**
     * Tests {@link Parser#parse()} generates {@link Expr}ession where right operator has precedence
     * between two unary operators of same right-associative grammar.
     */
    @Test
    void parse_unaryGrammarsSharePrecedence_givenGrammarsAtSameLevel() {
        assert_parse_rightUnaryHasHigherPrecedence("-", "!");
        assert_parse_rightUnaryHasHigherPrecedence("!", "-");
    }

    /**
     * Tests {@link Parser#parse()} generates {@link Expr}ession where conditional has higher
     * precedence than sequence. However, note an implicit grouping for the middle term.
     */
    @Test
    void parse_conditionalHasHigherPrecedenceThanSequence_givenSequenceAndConditional() {
        var tokens = tz("1", ",", "2", "?", "3", ",", "1", ":", "2", ",", "3", ";");

        var expected =
                new Stmt[] {
                    new Stmt.Expression(e(e(1, t(","), e(2, e(3, t(","), 1), 2)), t(","), 3))
                };

        assert_parse(tokens, contains(expected));
    }

    /**
     * Tests {@link Parser#parse()} generates {@link Expr}ession where equality has higher
     * precedence than conditional.
     */
    @Test
    void parse_equalityHasHigherPrecedenceThanConditional_givenConditionalAndEquality() {
        var tokens = tz("1", "==", "2", "?", "3", "==", "2", ":", "1", "==", "3", ";");

        var expected =
                new Stmt[] {
                    new Stmt.Expression(e(e(1, t("=="), 2), e(3, t("=="), 2), e(1, t("=="), 3)))
                };

        assert_parse(tokens, contains(expected));
    }

    /**
     * Tests {@link Parser#parse()} generates {@link Expr}ession where {@code higher} operator has
     * precedence over {@code lower} operator.
     *
     * @param higher Expected higher precedence operator.
     * @param lower Expected lower precedence operator.
     */
    @ParameterizedTest
    @MethodSource("parse_binaryGrammarsOfDifferingPrecedence")
    void parse_binaryGrammarsDifferInPrecedence_givenGrammarsAtDifferingLevels(
            String higher, String lower) {
        assert_parse_leftBinaryHasHigherPrecedence(higher, lower);
        assert_parse_rightBinaryHasHigherPrecedence(lower, higher);
    }

    /**
     * Tests {@link Parser#parse()} generates the same {@link Expr}ession when operators of
     * differing precedence are separated by parentheses.
     *
     * @param higher Expected higher precedence operator.
     * @param lower Expected lower precedence operator.
     */
    @ParameterizedTest
    @MethodSource("parse_binaryGrammarsOfDifferingPrecedence")
    void parse_groupingChangesPrecedence_whenLowerPrecedenceGrammarIsWithinParentheses(
            String higher, String lower) {
        var tokens = tz("1", higher, "(", "2", lower, "3", ")", ";");

        // 1 higher (2 lower 3)
        var expected = new Stmt[] {new Stmt.Expression(e(1, t(higher), e(e(2, t(lower), 3))))};

        assert_parse(tokens, contains(expected));
    }

    /**
     * Data source for {@link
     * #parse_binaryGrammarsDifferInPrecedence_givenGrammarsAtDifferingLevels(String, String)} and
     * {@link #parse_binaryGrammarsDifferInPrecedence_givenGrammarsAtDifferingLevels(String,
     * String)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments> parse_binaryGrammarsOfDifferingPrecedence() {
        return Stream.of(
                arguments("*", "+"),
                arguments("+", "<"),
                arguments("<", "=="),
                arguments("==", ","));
    }

    /**
     * Tests {@link Parser#parse()} generates an error given binary operator missing left operand.
     *
     * @param token Binary token.
     * @param type Token type.
     * @throws Exception Unable to read from standard error.
     */
    @ParameterizedTest
    @MethodSource
    void parse_producesError_whenLeftOperandMissingFromBinaryExpression(String token, String type)
            throws Exception {
        assert_parseError(
                tz(token, "1", token, "2", ";"),
                String.format(
                        "[line 1] Error at '%s': Missing left hand operand for %s.", token, type));
    }

    /**
     * Data source for {@link
     * #parse_producesError_whenLeftOperandMissingFromBinaryExpression(String, String)} tests.
     *
     * @return Test argument data.
     */
    private static Stream<Arguments>
            parse_producesError_whenLeftOperandMissingFromBinaryExpression() {
        return Stream.of(
                arguments("==", "equality"),
                arguments("<", "comparison"),
                arguments("+", "term"),
                arguments("*", "factor"));
    }

    /**
     * Tests {@link Parser#parse()} generates {@link Expr}ession where, given a factor operator then
     * a unary operator, the unary operator has higher precedence.
     */
    @Test
    void parse_unaryHasHigherPrecedenceThanFactor_givenFactorThenUnary() {
        var tokens = tz("1", "*", "-", "2", ";");

        var expected = new Stmt[] {new Stmt.Expression(e(1, t("*"), e(t("-"), 2)))};

        assert_parse(tokens, contains(expected));
    }

    /**
     * Tests {@link Parser#parse()} generates {@link Expr}ession where, given a unary operator then
     * a factor operator, the unary operator has higher precedence.
     */
    @Test
    void parse_unaryHasHigherPrecedenceThanFactor_givenUnaryThenFactor() {
        var tokens = tz("-", "1", "*", "2", ";");

        var expected = new Stmt[] {new Stmt.Expression(e(e(t("-"), 1), t("*"), 2))};

        assert_parse(tokens, contains(expected));
    }

    /**
     * General assertion against {@link Parser#parse()}.
     *
     * @param tokens {@link Token} {@link List}.
     * @param matcher Expected matcher result.
     */
    private static void assert_parse(
            List<Token> tokens, Matcher<Iterable<? extends Stmt>> matcher) {
        assertThat(new Parser(tokens).parse(), matcher);
    }

    /**
     * Asserts left binary grammar has higher precedence.
     *
     * @param left Left operator.
     * @param right Right operator.
     */
    private static void assert_parse_leftBinaryHasHigherPrecedence(String left, String right) {
        var tokens = tz("1", left, "2", right, "3", ";");

        // (1 left 2) right 3
        var expected = new Stmt[] {new Stmt.Expression(e(e(1, t(left), 2), t(right), 3))};

        assert_parse(tokens, contains(expected));
    }

    /**
     * Asserts right binary grammar has higher precedence.
     *
     * @param left Left operator.
     * @param right Right operator.
     */
    private static void assert_parse_rightBinaryHasHigherPrecedence(String left, String right) {
        var tokens = tz("1", left, "2", right, "3", ";");

        // 1 left (2 right 3)
        var expected = new Stmt[] {new Stmt.Expression(e(1, t(left), e(2, t(right), 3)))};

        assert_parse(tokens, contains(expected));
    }

    /**
     * Asserts right unary grammar has higher precedence.
     *
     * @param left Left operator.
     * @param right Right operator.
     */
    private static void assert_parse_rightUnaryHasHigherPrecedence(String left, String right) {
        var tokens = tz(left, right, "1", ";");

        // left (right 1)
        var expected = new Stmt[] {new Stmt.Expression(e(t(left), e(t(right), 1)))};

        assert_parse(tokens, contains(expected));
    }

    /**
     * Asserts given tokens produces an error.
     *
     * @param tokens {@link Token}s.
     * @param expectedErrorMessage Expected error message.
     */
    private static void assert_parseError(List<Token> tokens, String expectedErrorMessage)
            throws Exception {
        AtomicReference<List<Stmt>> actual = new AtomicReference<>();
        AtomicReference<List<Stmt>> expected = new AtomicReference<>();

        String error =
                tapSystemErrNormalized(
                        () -> {
                            try {
                                actual.set(new Parser(tokens).parse());
                                expected.set(List.of(new Stmt.Expression(null)));
                            } catch (Parser.ParseError ignored) {
                            }
                        });

        assertThat(actual.get(), is(equalTo(expected.get())));
        assertThat(error, is(equalTo(expectedErrorMessage + "\n")));
    }
}
