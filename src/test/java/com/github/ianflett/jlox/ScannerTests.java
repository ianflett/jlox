package com.github.ianflett.jlox;

import static com.github.ianflett.jlox.TokenHelper.EOF_LITERAL;
import static com.github.ianflett.jlox.TokenType.*;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErrNormalized;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests {@link Scanner} class. */
public class ScannerTests {

    /**
     * Tests {@link Scanner#Scanner(String)} throws {@link IllegalArgumentException} if {@code
     * source} is {@code null}.
     */
    @Test
    void constructor_throwsIllegalArgumentException_whenSourceIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Scanner(null),
                "Source text must be defined.");
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits {@link TokenType#EOF} {@link Token} if {@code
     * source} is empty {@link String}.
     */
    @Test
    void scanTokens_emitsEof_whenSourceIsEmpty() {
        assertThat(new Scanner("").scanTokens(), contains(TokenHelper.get(EOF_LITERAL)));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits error if {@code source} contains invalid character.
     *
     * @param source Source text to analyse.
     * @throws Exception Reading from standard error threw exception.
     */
    @ParameterizedTest
    @ValueSource(strings = {"@", "#"})
    void scanTokens_emitsError_whenSourceContainsInvalidCharacter(String source) throws Exception {
        var error = tapSystemErrNormalized(() -> new Scanner(source).scanTokens());
        assertThat(error, is(equalTo("[line 1] Error : Unexpected character.\n")));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits correct {@link Token} if {@code source} contains
     * valid character sequence.
     *
     * @param source Source text to analyse.
     * @param expected Expected {@link Token} emitted.
     */
    @ParameterizedTest(name = "\"{0}\" = <{1}>")
    @MethodSource("scanTokens_emitsCorrectToken_whenSourceContainsValidCharacterSequence_data")
    void scanTokens_emitsCorrectToken_whenSourceContainsValidCharacterSequence(
            String source, Token expected) {
        assertThat(
                new Scanner(source).scanTokens(), contains(expected, TokenHelper.get(EOF_LITERAL)));
    }

    /**
     * Data source for {@link
     * #scanTokens_emitsCorrectToken_whenSourceContainsValidCharacterSequence(String, Token)} tests.
     *
     * @return Test argument data.
     */
    static Stream<Arguments>
            scanTokens_emitsCorrectToken_whenSourceContainsValidCharacterSequence_data() {
        return Set.of(
                        "(", ")", "{", "}", ",", ".", "-", "+", ";", "*", "!", "!=", "=", "==", "<",
                        "<=", ">", ">=", "/")
                .stream()
                .map(i -> arguments(i, TokenHelper.get(i)));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits no {@link Token} if {@code source} contains single
     * line comment.
     *
     * @param source Source text to analyse.
     */
    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(
            strings = {
                "//",
                "// This is a comment.",
                "// This is a / comment.",
                "// This is a // comment."
            })
    void scanTokens_emitsNothing_whenSingleComment(String source) {
        assertThat(new Scanner(source).scanTokens(), contains(TokenHelper.get(EOF_LITERAL)));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits no {@link Token} if {@code source} contains
     * multiline comment.
     *
     * @param source Source text to analyse.
     * @param line Expected line number.
     */
    @ParameterizedTest(name = "\"{0}\"")
    @MethodSource("scanTokens_emitsNothing_whenMultiComment_data")
    void scanTokens_emitsNothing_whenMultiComment(String source, int line) {
        assertThat(
                new Scanner(source).scanTokens(),
                contains(TokenHelper.withLine(EOF_LITERAL, line)));
    }

    /**
     * Data source for {@link #scanTokens_emitsNothing_whenMultiComment(String, int)}
     * scanTokens_emitsNothing_whenMultiComment()} tests.
     *
     * @return Test argument data.
     */
    static Stream<Arguments> scanTokens_emitsNothing_whenMultiComment_data() {
        return Stream.of(
                arguments("/**/", 1),
                arguments("/* This is a comment. */", 1),
                arguments("/* This\nis\na\ncomment. */", 4),
                arguments("/* This is a\n * comment. */", 2),
                arguments("/* This is a /* comment. */", 1));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits no {@link Token} if {@code source} contains
     * whitespace character.
     *
     * @param source Source text to analyse.
     */
    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {" ", "\r", "\t"})
    void scanTokens_emitsNothing_whenWhitespace(String source) {
        assertThat(new Scanner(source).scanTokens(), contains(TokenHelper.get(EOF_LITERAL)));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits no {@link Token} but advances line number if {@code
     * source} contains new line character.
     */
    @Test
    void scanTokens_emitsNothingAndAdvancesLine_whenNewLine() {
        assertThat(
                new Scanner("\n").scanTokens(), contains(TokenHelper.adjustLine(EOF_LITERAL, 1)));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits string {@link Token} if {@code source} contains text
     * within quotes.
     *
     * @param source Source text to analyse.
     * @param line Expected line number.
     */
    @ParameterizedTest(name = "\"{0}\"")
    @MethodSource("scanTokens_emitsString_whenTextWithinQuotes_data")
    void scanTokens_emitsString_whenTextWithinQuotes(String source, int line) {
        assertThat(
                new Scanner(source).scanTokens(),
                contains(
                        new Token(STRING, source, source.substring(1, source.length() - 1), line),
                        TokenHelper.withLine(EOF_LITERAL, line)));
    }

    /**
     * Data source for {@link #scanTokens_emitsString_whenTextWithinQuotes(String, int)} tests.
     *
     * @return Test argument data.
     */
    static Stream<Arguments> scanTokens_emitsString_whenTextWithinQuotes_data() {
        return Stream.of(
                arguments("\"\"", 1),
                arguments("\"This is a string.\"", 1),
                arguments("\"This is a\nmultiline string.\"", 2));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits number {@link Token} if {@code source} contains
     * contiguous numerals potentially containing one period character.
     *
     * @param source Source text to analyse.
     */
    @ParameterizedTest
    @ValueSource(strings = {" 1234", "12.34 ", "0.1234", "1234.0"})
    void scanTokens_emitsNumber_whenValidNumber(String source) {
        assertThat(
                new Scanner(source).scanTokens(),
                contains(
                        new Token(NUMBER, source.trim(), Double.parseDouble(source), 1),
                        TokenHelper.get(EOF_LITERAL)));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits dot and number {@link Token}s if {@code source}
     * contains contiguous numerals containing one period character at start.
     */
    @Test
    void scanTokens_emitsCorrectTokens_whenNumberHasLeadingDot() {
        assertThat(
                new Scanner(".1234").scanTokens(),
                contains(
                        TokenHelper.get("."),
                        new Token(NUMBER, "1234", 1234d, 1),
                        TokenHelper.get(EOF_LITERAL)));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits number and dot {@link Token}s if {@code source}
     * contains contiguous numerals containing one period character at end.
     */
    @Test
    void scanTokens_emitsCorrectTokens_whenNumberHasTrailingDot() {
        assertThat(
                new Scanner("1234.").scanTokens(),
                contains(
                        new Token(NUMBER, "1234", 1234d, 1),
                        TokenHelper.get("."),
                        TokenHelper.get(EOF_LITERAL)));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits correct number {@link Token}s if {@code source}
     * contains contiguous numerals containing multiple period characters.
     */
    @Test
    void scanTokens_emitsCorrectTokens_whenNumberHasTooManyDots() {
        assertThat(
                new Scanner("1.23.4").scanTokens(),
                contains(
                        new Token(NUMBER, "1.23", 1.23d, 1),
                        TokenHelper.get("."),
                        new Token(NUMBER, "4", 4d, 1),
                        TokenHelper.get(EOF_LITERAL)));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits correct number {@link Token}s if {@code source}
     * contains contiguous numerals containing multiple contiguous period characters.
     */
    @Test
    void scanTokens_emitsCorrectTokens_whenNumberHasContiguousDots() {
        assertThat(
                new Scanner("12..34").scanTokens(),
                contains(
                        new Token(NUMBER, "12", 12d, 1),
                        TokenHelper.get("."),
                        TokenHelper.get("."),
                        new Token(NUMBER, "34", 34d, 1),
                        TokenHelper.get(EOF_LITERAL)));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits keyword {@link Token}s if {@code source} contains
     * keyword.
     *
     * @param source Source text to analyse.
     */
    @ParameterizedTest
    @ValueSource(
            strings = {
                "and", "class", "else", "false", "for", "fun", "if", "nil", "or", "print", "return",
                "super", "this", "true", "var", "while"
            })
    void scanTokens_emitsKeyword_whenValidKeyword(String source) {
        assertThat(
                new Scanner(source).scanTokens(),
                contains(
                        new Token(
                                Enum.valueOf(TokenType.class, source.toUpperCase()),
                                source,
                                null,
                                1),
                        TokenHelper.get(EOF_LITERAL)));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits identifier {@link Token}s if {@code source} doesn't
     * contain keyword.
     *
     * @param source Source text to analyse.
     */
    @ParameterizedTest
    @ValueSource(strings = {"rand", "outclassed", "form"})
    void scanTokens_emitsIdentifier_whenInvalidKeyword(String source) {
        assertThat(
                new Scanner(source).scanTokens(),
                contains(new Token(IDENTIFIER, source, null, 1), TokenHelper.get(EOF_LITERAL)));
    }
}
