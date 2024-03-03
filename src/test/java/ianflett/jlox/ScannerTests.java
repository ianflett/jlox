package ianflett.jlox;

import static com.github.stefanbirkner.systemlambda.SystemLambda.*;
import static java.util.Map.entry;
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

/** Unit tests the {@link Scanner} class. */
public class ScannerTests {

    /**
     * Tests {@link Scanner#Scanner(String)} throws an {@link IllegalArgumentException} if {@code
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
     * Tests {@link Scanner#scanTokens()} emits a {@link TokenType#EOF} {@link Token} if {@code
     * source} is an empty {@link String}.
     */
    @Test
    void scanTokens_emitsEof_whenSourceIsEmpty() {
        assertThat(new Scanner("").scanTokens(), hasItem(EOF_TOKEN));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits an error if {@code source} contains an invalid
     * character.
     *
     * @throws Exception Reading from standard error threw an exception.
     */
    @ParameterizedTest
    @ValueSource(strings = {"@", "#"})
    void scanTokens_emitsError_whenSourceContainsInvalidCharacter(String source) throws Exception {
        var error = tapSystemErrNormalized(() -> new Scanner(source).scanTokens());
        assertThat(error, is(equalTo("[line 1] Error : Unexpected character.\n")));
    }

    /**
     * Tests {@link Scanner#scanTokens()} emits correct token if {@code source} contains a valid
     * character sequence.
     */
    @ParameterizedTest(name = "\"{0}\" = <{1}>")
    @MethodSource("scanTokens_emitsCorrectToken_whenSourceContainsValidCharacterSequence_data")
    void scanTokens_emitsCorrectToken_whenSourceContainsValidCharacterSequence(
            String source, Token token) {
        assertThat(new Scanner(source).scanTokens(), contains(token, EOF_TOKEN));
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
                        entry("(", TokenType.LEFT_PAREN),
                        entry(")", TokenType.RIGHT_PAREN),
                        entry("{", TokenType.LEFT_BRACE),
                        entry("}", TokenType.RIGHT_BRACE),
                        entry(",", TokenType.COMMA),
                        entry(".", TokenType.DOT),
                        entry("-", TokenType.MINUS),
                        entry("+", TokenType.PLUS),
                        entry(";", TokenType.SEMICOLON),
                        entry("*", TokenType.STAR),
                        entry("!", TokenType.BANG),
                        entry("!=", TokenType.BANG_EQUAL),
                        entry("=", TokenType.EQUAL),
                        entry("==", TokenType.EQUAL_EQUAL),
                        entry("<", TokenType.LESS),
                        entry("<=", TokenType.LESS_EQUAL),
                        entry(">", TokenType.GREATER),
                        entry(">=", TokenType.GREATER_EQUAL))
                .stream()
                .map(i -> arguments(i.getKey(), new Token(i.getValue(), i.getKey(), null, 1)));
    }

    /** End of file token. */
    static final Token EOF_TOKEN = new Token(TokenType.EOF, "", null, 1);
}
