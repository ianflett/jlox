package ianflett.jlox;

/**
 * Represents {@link Token} (keyword, operator, punctuation, literal) in Lox language.
 *
 * @param type Stores {@link Token}'s type.
 * @param lexeme Stores raw {@link Token} text.
 * @param literal Stores converted {@link Token} value.
 * @param line Stores line number location.
 */
record Token(TokenType type, String lexeme, Object literal, int line) {

    /**
     * Constructs {@link Token}.
     *
     * @param type {@link Token}'s type.
     * @param lexeme Raw {@link Token} text.
     * @param literal Converted {@link Token} value.
     * @param line Line number location.
     */
    Token {
        if (null == lexeme) throw new IllegalArgumentException("Lexeme must not be null");
        if (0 > line) throw new IllegalArgumentException("Line number must not be negative");
    }

    /**
     * Represents token's type, lexeme, and literal as {@code String}.
     *
     * @return {@link String} representation of object.
     */
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
