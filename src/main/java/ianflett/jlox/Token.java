package ianflett.jlox;

/**
 * Represents a {@link Token} (keyword, operator, punctuation, literal) in the Lox language.
 *
 * @param type Stores the {@link Token}'s type.
 * @param lexeme Stores the raw {@link Token} text.
 * @param literal Stores the converted {@link Token} value.
 * @param line Stores the line number location.
 */
record Token(TokenType type, String lexeme, Object literal, int line) {

    /**
     * Constructs a {@link Token}.
     *
     * @param type The {@link Token}'s type.
     * @param lexeme The raw {@link Token} text.
     * @param literal The converted {@link Token} value.
     * @param line The line number location.
     */
    Token {
        if (null == lexeme) throw new IllegalArgumentException("Lexeme must not be null");
        if (0 > line) throw new IllegalArgumentException("Line number must not be negative");
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
