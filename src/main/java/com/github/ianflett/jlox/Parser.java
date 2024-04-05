package com.github.ianflett.jlox;

import static com.github.ianflett.jlox.TokenType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Recursive decent parser consuming tokens to produce abstract syntax tree. */
public class Parser {

    /** Thrown when parsing error encountered. */
    static class ParseError extends RuntimeException {}

    /** List of tokens to process. */
    private final List<Token> tokens;

    /** Index of current token being parsed. */
    private int current = 0;

    /**
     * Constructs {@link Parser}.
     *
     * @param tokens Tokens to parse.
     */
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parses {@link #tokens} into statements.
     *
     * <pre>program -> {@link #statement()}* EOF</pre>
     *
     * @return {@link List} of statements.
     */
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    /**
     * Parses statement grammar rule.
     *
     * <pre>{@link #statement()} -> {@link #printStatement()} | {@link #expressionStatement()}</pre>
     *
     * @return {@link Stmt}.
     */
    private Stmt statement() {
        if (match(PRINT)) return printStatement();

        return expressionStatement();
    }

    /**
     * Parses {@code print} statement grammar rule.
     *
     * <pre>{@link #printStatement()} -> "print" {@link #expression()} ";"</pre>
     *
     * @return Print {@link Stmt}.
     */
    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    /**
     * Parses {@code var} statement grammar rule.
     *
     * @return Var {@link Stmt}.
     */
    private Stmt varDeclaration() {
        var name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    /**
     * Parses expression statement grammar rule.
     *
     * <pre>{@link #expressionStatement()} -> {@link #expression()} ";"</pre>
     *
     * @return Expression {@link Stmt}.
     */
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    /**
     * Parses declaration statement grammar rule.
     *
     * @return Expression {@link Stmt}.
     */
    private Stmt declaration() {
        try {
            if (match(VAR)) return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    /**
     * Parses expression grammar rule.
     *
     * <pre>{@link #expression()} -> {@link #sequence()}</pre>
     *
     * @return {@link Expr}ession or {@link #sequence()}.
     */
    private Expr expression() {
        return sequence();
    }

    /**
     * Parses a sequence point grammar rule.
     *
     * <pre>{@link #sequence()} -> {@link #conditional()} ( "," {@link #conditional()} )*</pre>
     *
     * @return {@link Expr}ession or {@link #conditional()}.
     */
    private Expr sequence() {
        return parseBinary(this::conditional, COMMA);
    }

    /**
     * Parses a conditional grammar rule.
     *
     * <pre>
     * {@link #conditional()} -> {@link #equality()} ( "?" {@link #expression()} ":" {@link #conditional()} )?
     * </pre>
     *
     * @return {@link Expr}ession or {@link #expression()}.
     */
    private Expr conditional() {
        var expr = equality();

        if (match(QUESTION)) {
            var thenBranch = expression();
            consume(COLON, "Expect ':' after then branch of conditional expression.");
            var elseBranch = conditional();
            expr = new Expr.Conditional(expr, thenBranch, elseBranch);
        }

        return expr;
    }

    /**
     * Parses equality grammar rule.
     *
     * <pre>
     * {@link #equality()} -> {@link #comparison()} ( ( "!=" | "==" ) ) {@link #comparison()} )*
     * </pre>
     *
     * @return Equality {@link Expr}ession or {@link #comparison()}.
     */
    private Expr equality() {
        return parseBinary(this::comparison, BANG_EQUAL, EQUAL_EQUAL);
    }

    /**
     * Parses comparison grammar rule.
     *
     * <pre>
     * {@link #comparison()} -> {@link #term()} ( ( ">" | ">=" | "<" | "<=" ) ) {@link #term()} )*
     * </pre>
     *
     * @return Comparison {@link Expr}ession or {@link #term()}.
     */
    private Expr comparison() {
        return parseBinary(this::term, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
    }

    /**
     * Parses term grammar rule.
     *
     * <pre>{@link #term()} -> {@link #factor()} ( ( "-" | "+" ) ) {@link #factor()} )*</pre>
     *
     * @return Term {@link Expr}ession or {@link #factor()}.
     */
    private Expr term() {
        return parseBinary(this::factor, MINUS, PLUS);
    }

    /**
     * Parses factor grammar rule.
     *
     * <pre>{@link #factor()} -> {@link #unary()} ( ( "/" | "*" ) ) {@link #unary()} )*</pre>
     *
     * @return Factor {@link Expr}ession or {@link #unary()}.
     */
    private Expr factor() {
        return parseBinary(this::unary, SLASH, STAR);
    }

    /**
     * Parses unary grammar rule.
     *
     * <pre>
     * {@link #unary()} -> ( "!" | "-" ) {@link #unary()}
     *     | {@link #primary()}
     * </pre>
     *
     * @return Unary {@link Expr}ession or {@link #primary()}.
     */
    private Expr unary() {
        if (match(BANG, MINUS)) {
            var operator = previous();
            var right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    /**
     * Parses primary grammar rule.
     *
     * <pre>
     * {@link #primary()} -> NUMBER | STRING
     *     | "true" | "false" | "nil"
     *     | "(" {@link #sequence()} ")"
     *     | IDENTIFIER | {@link #invalid()} ;
     * </pre>
     *
     * @return {@link Expr.Literal} or {@link Expr.Grouping}.
     */
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal());
        if (match(IDENTIFIER)) return new Expr.Variable(previous());

        if (match(LEFT_PAREN)) {
            var expr = sequence();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        return invalid();
    }

    /**
     * Parses invalid error productions.
     *
     * <pre>
     * {@link #invalid()} -> ( ( "!=" | "==" ) {@link #equality()} )
     *     | ( ( ">" | ">=" | "<" | "<=" ) {@link #comparison()} )
     *     | ( "+" {@link #term()} )
     *     | ( ( "/" | "*" ) {@link #factor()} )
     * </pre>
     *
     * @return {@code null}.
     * @throws ParseError Expression expected.
     */
    private Expr invalid() {

        if (missingLeftTerm("equality", this::equality, BANG_EQUAL, EQUAL_EQUAL)
                || missingLeftTerm(
                        "comparison", this::comparison, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)
                || missingLeftTerm("term", this::term, PLUS)
                || missingLeftTerm("factor", this::factor, SLASH, STAR)) return null;

        throw error(peek(), "Expect expression.");
    }

    /**
     * Checks whether binary expression is missing left term.
     *
     * @param missing What's missing.
     * @param orElse How to evaluate right term.
     * @param tokens {@link Token}s to check for.
     * @return {@code true} if found; {@code false} otherwise.
     */
    boolean missingLeftTerm(String missing, Supplier<Expr> orElse, TokenType... tokens) {
        if (match(tokens)) {
            error(previous(), String.format("Missing left hand operand for %s.", missing));
            orElse.get();
            return true;
        }
        return false;
    }

    /**
     * Parses a {@link Expr.Binary} grammar rule.
     *
     * @param higher Next higher {@link Expr} parser.
     * @param types {@link TokenType}s to check for.
     * @return {@link Expr.Binary} expression or higher.
     */
    private Expr parseBinary(Supplier<Expr> higher, TokenType... types) {
        var expr = higher.get();

        while (match(types)) {
            var operator = previous();
            var right = higher.get();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Whether current {@link Token} matches given {@link TokenType}s.
     *
     * @param types {@link TokenType}s to check for.
     * @return {@code true} if current {@link Token} matches {@link TokenType}s; {@code false}
     *     otherwise.
     */
    private boolean match(TokenType... types) {
        for (var type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    /**
     * Tries consuming {@link Token} of expected {@link TokenType}.
     *
     * @param type Expected {@link TokenType}.
     * @param message Description of error.
     * @return {@link Token}, if found.
     * @throws ParseError Unable to consume {@link Token}.
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    /**
     * Whether current {@link Token} is a {@link TokenType}.
     *
     * @param type {@link TokenType} to check for.
     * @return {@code true} if current {@link Token} is a {@link TokenType}; {@code false}
     *     otherwise.
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    /**
     * Advance to next {@link Token}.
     *
     * @return Next {@link Token}.
     */
    private Token advance() {
        if (!isAtEnd()) ++current;
        return previous();
    }

    /**
     * Whether {@link #tokens} is exhausted.
     *
     * @return {@code true} if at end of {@link #tokens}; {@code false} otherwise.
     */
    private boolean isAtEnd() {
        return peek().type() == EOF;
    }

    /**
     * Get current {@link Token}.
     *
     * @return Current {@link Token}.
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Get previous {@link Token}.
     *
     * @return Previous {@link Token}.
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * Registers parsing error.
     *
     * @param token Affected {@link Token}.
     * @param message Description of error.
     * @return {@link ParseError}
     */
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    /** Discard {@link Token}s until statement boundary. */
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (SEMICOLON == previous().type()) return;

            switch (peek().type()) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
