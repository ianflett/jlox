package ianflett.jlox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

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
     * Tests {@link Scanner#scanTokens()} emits a {@link TokenType#EOF} {@link Token} if given an
     * empty {@link String}.
     */
    @Test
    void scanTokens_emitsEof_GivenNothing() {
        assertThat(new Scanner("").scanTokens(), hasItem(new Token(TokenType.EOF, "", null, 1)));
    }
}
