package com.github.ianflett.jlox;

import static com.github.ianflett.jlox.TokenType.*;
import static java.util.Map.entry;

import java.util.*;

/** Helper for unit testing. */
public class TestHelper {

    // region Token creation

    /** Lexeme for {@link TokenType#EOF}. */
    static final String EOF_LEXEME = "";

    /** Default line for {@link Token} generation. */
    static final int DEFAULT_LINE = 1;

    /** Caches requested {@link Token}s. */
    private static final Map<String, Token> tokenCache = new HashMap<>();

    /** Stores {@link TokenType} lookup. */
    private static final Map<String, TokenType> TOKEN_TYPES =
            Map.ofEntries(
                    entry("(", LEFT_PAREN),
                    entry(")", RIGHT_PAREN),
                    entry("{", LEFT_BRACE),
                    entry("}", RIGHT_BRACE),
                    entry(":", COLON),
                    entry(",", COMMA),
                    entry(".", DOT),
                    entry("-", MINUS),
                    entry("+", PLUS),
                    entry("?", QUESTION),
                    entry(";", SEMICOLON),
                    entry("*", STAR),
                    entry("!", BANG),
                    entry("!=", BANG_EQUAL),
                    entry("=", EQUAL),
                    entry("==", EQUAL_EQUAL),
                    entry("<", LESS),
                    entry("<=", LESS_EQUAL),
                    entry(">", GREATER),
                    entry(">=", GREATER_EQUAL),
                    entry("/", SLASH));

    /**
     * Retrieves {@link Token}.
     *
     * @param lexeme Raw {@link Token} text.
     * @return Requested {@link Token}.
     */
    static Token t(String lexeme) {
        var token = tokenCache.get(lexeme);
        if (null == token) {
            token = createToken(lexeme);
            tokenCache.put(lexeme, token);
        }
        return token;
    }

    /**
     * Retrieves {@link Token}.
     *
     * @param line Line number to use.
     * @param lexeme Raw {@link Token} text.
     * @return Requested {@link Token}.
     */
    static Token t(int line, String lexeme) {
        var token = t(lexeme);
        return new Token(token.type(), token.lexeme(), token.literal(), line);
    }

    /**
     * Retrieves array of {@link Token}s.
     *
     * @param lexemes Raw {@link Token} texts.
     * @return Requested {@link Token}s.
     */
    static Token[] ts(String... lexemes) {
        Token[] tokens = new Token[1 + lexemes.length];
        for (var i = 0; i < lexemes.length; ++i) {
            tokens[i] = t(lexemes[i]);
        }
        tokens[lexemes.length] = t(EOF_LEXEME);
        return tokens;
    }

    /**
     * Retrieves {@link List} of {@link Token}s.
     *
     * @param lexemes Raw {@link Token} texts.
     * @return Requested {@link Token}s.
     */
    static List<Token> tz(String... lexemes) {
        return Arrays.stream(ts(lexemes)).toList();
    }

    /**
     * Creates {@link Token} from {@code lexeme}.
     *
     * <p>Unless {@code lexeme} is empty, in which case create {@link TokenType#EOF}, tries getting
     * type from {@link #TOKEN_TYPES}; otherwise tries parsing {@link TokenType#NUMBER}; otherwise
     * converting {@code lexeme} to {@link TokenType}; otherwise create {@link
     * TokenType#IDENTIFIER}.
     *
     * @param lexeme Raw {@link Token} text.
     * @return {@link Token} representing {@code lexeme}.
     */
    private static Token createToken(String lexeme) {
        var type = lexeme.isEmpty() ? EOF : TOKEN_TYPES.get(lexeme);
        if (null != type) return new Token(type, lexeme, null, DEFAULT_LINE);

        try {
            return new Token(NUMBER, lexeme, Double.parseDouble(lexeme), DEFAULT_LINE);
        } catch (NumberFormatException ignored) {
        }

        try {
            type = Enum.valueOf(TokenType.class, lexeme.toUpperCase());
            return new Token(type, lexeme, null, DEFAULT_LINE);
        } catch (IllegalArgumentException ignored) {
        }

        return '"' == lexeme.charAt(0)
                ? new Token(STRING, lexeme, lexeme.substring(1, lexeme.length() - 1), DEFAULT_LINE)
                : new Token(IDENTIFIER, lexeme, null, DEFAULT_LINE);
    }

    // endregion

    // region Expr creation

    /** Caches requested {@link Expr.Literal}s. */
    private static final Map<Object, Expr.Literal> literalCache = new HashMap<>();

    /**
     * {@link Expr.Binary} or {@link Expr.Conditional} construction.
     *
     * @param first Left operand or conditional.
     * @param second Operator or then branch.
     * @param third Right operand or else branch.
     * @return {@link Expr.Binary} or {@link Expr.Conditional}.
     */
    static Expr e(Object first, Object second, Object third) {
        return second instanceof Token
                ? new Expr.Binary(eValue(first), (Token) second, eValue(third))
                : new Expr.Conditional(eValue(first), eValue(second), eValue(third));
    }

    /**
     * {@link Expr.Assign} or {@link Expr.Unary} construction.
     *
     * @param left Operator or left operand.
     * @param right Right operand.
     * @return {@link Expr.Unary}.
     */
    static Expr e(Token left, Object right) {
        return IDENTIFIER == left.type()
                ? new Expr.Assign(left, eValue(right))
                : new Expr.Unary(left, eValue(right));
    }

    /**
     * {@link Expr.Grouping} or {@link Expr.Variable} construction.
     *
     * @param value Operand.
     * @return {@link Expr.Grouping} or {@link Expr.Variable}.
     */
    static Expr e(Object value) {
        return value instanceof Token
                ? new Expr.Variable((Token) value)
                : new Expr.Grouping(eValue(value));
    }

    /**
     * Processes value as either {@link Expr} or new {@link Expr.Literal}.
     *
     * @param value Operand.
     * @return {@link Expr} or {@link Expr.Literal}.
     */
    private static Expr eValue(Object value) {
        if (value instanceof Expr) return (Expr) value;

        var literal = literalCache.get(value);
        if (null == literal) {
            literal =
                    new Expr.Literal(
                            value instanceof Integer ? Double.valueOf((int) value) : value);
            literalCache.put(value, literal);
        }
        return literal;
    }

    // endregion
}
