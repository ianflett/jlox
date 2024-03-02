package ianflett.jlox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests the {@link Token} class. */
class TokenTests {

    /**
     * Tests {@link Token#Token(TokenType, String, Object, int)} throws an {@link
     * IllegalArgumentException} if {@code lexeme} is {@code null}.
     */
    @Test
    void constructor_throwsIllegalArgumentException_whenLexemeIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Token(TokenType.EOF, null, null, 0),
                "Lexeme must not be null");
    }

    /**
     * Tests {@link Token#Token(TokenType, String, Object, int)} throws an {@link
     * IllegalArgumentException} if {@code line} is negative.
     */
    @Test
    void constructor_throwsIllegalArgumentException_whenLineIsNegative() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Token(TokenType.EOF, "", null, -1),
                "Line number must not be negative");
    }

    /**
     * Tests {@link Token#toString()} outputs the expected field data from the {@link Token} object.
     *
     * @param tokenType The {@link Token}'s type.
     * @param lexeme The raw {@link Token} text.
     * @param literal The converted {@link Token} value.
     * @param line The line number location.
     */
    @ParameterizedTest(name = "new Token({0}, \"{1}\", {2}, {3}).toString() = \"{0} {1} {2}\"")
    @MethodSource("toString_data")
    void toString(TokenType tokenType, String lexeme, Object literal, int line) {
        assertThat(
                new Token(tokenType, lexeme, literal, line).toString(),
                is(equalTo(tokenType + " " + lexeme + " " + literal)));
    }

    /**
     * Data source for the {@link #toString(TokenType, String, Object, int)} tests.
     *
     * @return Test argument data.
     */
    static Stream<Arguments> toString_data() {
        return Stream.of(
                arguments(TokenType.LEFT_PAREN, "", null, 0),
                arguments(TokenType.RIGHT_PAREN, "", null, 0),
                arguments(TokenType.LEFT_PAREN, "X", null, 0),
                arguments(TokenType.LEFT_PAREN, "", true, 0),
                arguments(TokenType.LEFT_PAREN, "", 1, 0),
                arguments(TokenType.LEFT_PAREN, "", -1.1, 0),
                arguments(TokenType.LEFT_PAREN, "", "X", 0),
                arguments(TokenType.LEFT_PAREN, "", null, 1));
    }
}
