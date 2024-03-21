package com.github.ianflett.jlox;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErrNormalized;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
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
        var tokens = TokenHelper.asList("}");
        final Expr[] actual = {null};
        var error = tapSystemErrNormalized(() -> actual[0] = new Parser(tokens).parse());

        assertThat(actual[0], is(equalTo(null)));
        assertThat(error, is(equalTo("[line 1] Error  at '}': Expect expression.\n")));
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
    public static Stream<Arguments> parse_binaryGrammarsSharePrecedence_givenGrammarsAtSameLevel() {
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
     * Tests {@link Parser#parse()} generates {@link Expr}ession where conditional has higher precedence
     * than sequence. However, note an implicit grouping for the middle term.
     */
    @Test
    void parse_conditionalHasHigherPrecedenceThanSequence_givenSequenceAndConditional() {
        var tokens = TokenHelper.asList("1", ",", "2", "?", "3", ",", "1", ":", "2", ",", "3");

        var expected =
                new Expr.Binary(
                        new Expr.Binary(
                                new Expr.Literal(1d),
                                TokenHelper.get(","),
                                new Expr.Conditional(
                                        new Expr.Literal(2d),
                                        new Expr.Binary(
                                                new Expr.Literal(3d),
                                                TokenHelper.get(","),
                                                new Expr.Literal(1d)),
                                        new Expr.Literal(2d))),
                        TokenHelper.get(","),
                        new Expr.Literal(3d));

        assert_parse(tokens, is(equalTo(expected)));
    }

    /**
     * Tests {@link Parser#parse()} generates {@link Expr}ession where equality has higher
     * precedence than conditional.
     */
    @Test
    void parse_equalityHasHigherPrecedenceThanConditional_givenConditionalAndEquality() {
        var tokens = TokenHelper.asList("1", "==", "2", "?", "3", "==", "2", ":", "1", "==", "3");

        var expected =
                new Expr.Conditional(
                        new Expr.Binary(
                                new Expr.Literal(1d), TokenHelper.get("=="), new Expr.Literal(2d)),
                        new Expr.Binary(
                                new Expr.Literal(3d), TokenHelper.get("=="), new Expr.Literal(2d)),
                        new Expr.Binary(
                                new Expr.Literal(1d), TokenHelper.get("=="), new Expr.Literal(3d)));

        assert_parse(tokens, is(equalTo(expected)));
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
        var tokens = TokenHelper.asList("1", higher, "(", "2", lower, "3", ")");

        // 1 higher (2 lower 3)
        var expected =
                new Expr.Binary(
                        new Expr.Literal(1d),
                        TokenHelper.get(higher),
                        new Expr.Grouping(
                                new Expr.Binary(
                                        new Expr.Literal(2d),
                                        TokenHelper.get(lower),
                                        new Expr.Literal(3d))));

        assert_parse(tokens, is(equalTo(expected)));
    }

    /**
     * Data source for {@link
     * #parse_binaryGrammarsDifferInPrecedence_givenGrammarsAtDifferingLevels(String, String)} and
     * {@link #parse_binaryGrammarsDifferInPrecedence_givenGrammarsAtDifferingLevels(String,
     * String)} tests.
     *
     * @return Test argument data.
     */
    public static Stream<Arguments> parse_binaryGrammarsOfDifferingPrecedence() {
        return Stream.of(
                arguments("*", "+"),
                arguments("+", "<"),
                arguments("<", "=="),
                arguments("==", ","));
    }

    /**
     * Tests {@link Parser#parse()} generates {@link Expr}ession where, given a factor operator then
     * a unary operator, the unary operator has higher precedence.
     */
    @Test
    void parse_unaryHasHigherPrecedenceThanFactor_givenFactorThenUnary() {
        var tokens = TokenHelper.asList("1", "*", "-", "2");

        var expected =
                new Expr.Binary(
                        new Expr.Literal(1d),
                        TokenHelper.get("*"),
                        new Expr.Unary(TokenHelper.get("-"), new Expr.Literal(2d)));

        assert_parse(tokens, is(equalTo(expected)));
    }

    /**
     * Tests {@link Parser#parse()} generates {@link Expr}ession where, given a unary operator then
     * a factor operator, the unary operator has higher precedence.
     */
    @Test
    void parse_unaryHasHigherPrecedenceThanFactor_givenUnaryThenFactor() {
        var tokens = TokenHelper.asList("-", "1", "*", "2");

        var expected =
                new Expr.Binary(
                        new Expr.Unary(TokenHelper.get("-"), new Expr.Literal(1d)),
                        TokenHelper.get("*"),
                        new Expr.Literal(2d));

        assert_parse(tokens, is(equalTo(expected)));
    }

    /**
     * General assertion against {@link Parser#parse()}.
     *
     * @param tokens {@link Token} {@link List}.
     * @param matcher Expected matcher result.
     */
    private static void assert_parse(List<Token> tokens, Matcher<Expr> matcher) {
        assertThat(new Parser(tokens).parse(), matcher);
    }

    /**
     * Asserts left binary grammar has higher precedence.
     *
     * @param left Left operator.
     * @param right Right operator.
     */
    static void assert_parse_leftBinaryHasHigherPrecedence(String left, String right) {
        var tokens = TokenHelper.asList("1", left, "2", right, "3");

        // (1 left 2) right 3
        var expected =
                new Expr.Binary(
                        new Expr.Binary(
                                new Expr.Literal(1d), TokenHelper.get(left), new Expr.Literal(2d)),
                        TokenHelper.get(right),
                        new Expr.Literal(3d));

        assert_parse(tokens, is(equalTo(expected)));
    }

    /**
     * Asserts right binary grammar has higher precedence.
     *
     * @param left Left operator.
     * @param right Right operator.
     */
    static void assert_parse_rightBinaryHasHigherPrecedence(String left, String right) {
        var tokens = TokenHelper.asList("1", left, "2", right, "3");

        // 1 left (2 right 3)
        var expected =
                new Expr.Binary(
                        new Expr.Literal(1d),
                        TokenHelper.get(left),
                        new Expr.Binary(
                                new Expr.Literal(2d),
                                TokenHelper.get(right),
                                new Expr.Literal(3d)));

        assert_parse(tokens, is(equalTo(expected)));
    }

    /**
     * Asserts right unary grammar has higher precedence.
     *
     * @param left Left operator.
     * @param right Right operator.
     */
    static void assert_parse_rightUnaryHasHigherPrecedence(String left, String right) {
        var tokens = TokenHelper.asList(left, right, "1");

        // left (right 1)
        var expected =
                new Expr.Unary(
                        TokenHelper.get(left),
                        new Expr.Unary(TokenHelper.get(right), new Expr.Literal(1d)));

        assert_parse(tokens, is(equalTo(expected)));
    }
}
