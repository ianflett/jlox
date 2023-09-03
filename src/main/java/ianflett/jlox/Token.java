package ianflett.jlox;

/** Represents a {@link Token} (keyword, operator, punctuation, literal) in the Lox language. */
class Token {

    /** Stores the {@link Token}'s type. */
    final TokenType type;

    /** Stores the raw {@link Token} text. */
    final String lexeme;

    /** Stores the converted {@link Token} value. */
    final Object literal;

    /** Stores the line number location. */
    final int line;

    /**
     * Constructs a {@link Token}.
     *
     * @param type The {@link Token}'s type.
     * @param lexeme The raw {@link Token} text.
     * @param literal The converted {@link Token} value.
     * @param line The line number location.
     */
    Token(TokenType type, String lexeme, Object literal, int line) {

        if (null == lexeme) throw new IllegalArgumentException("Lexeme must not be null");
        if (0 > line) throw new IllegalArgumentException("Line number must not be negative");

        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    /**
     * Represents the token's type, lexeme, and literal as a string.
     *
     * @return A string representation of the object.
     */
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
