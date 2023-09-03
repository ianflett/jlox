package ianflett.jlox;

import static ianflett.jlox.TokenType.*;

import java.util.ArrayList;
import java.util.List;

/** Scans through text to discover {@link Token}s. */
class Scanner {

    /** Stores the source text. */
    private final String source;

    /** Stores a list of all discovered {@link Token}s. */
    private final List<Token> tokens = new ArrayList<>();

    /** Stores the beginning position of the lexeme being scanned. */
    private int start = 0;

    /** Stores the position of the character currently being scanned. */
    private int current = 0;

    /** Stores the current line number. */
    private int line = 1;

    /**
     * Constructs a {@link Scanner}.
     *
     * @param source The source text to scan.
     */
    Scanner(String source) {
        this.source = source;
    }

    /**
     * Scans for {@link Token}s within the source text.
     *
     * @return All {@link Token}s found.
     */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * Whether the scanner has reached the end of the source text.
     *
     * @return Whether the end of the source is reached.
     */
    private boolean isAtEnd() {
        return source.length() <= current;
    }

    /** Temporary placeholder to enable compilation. */
    void scanToken() {}
}
