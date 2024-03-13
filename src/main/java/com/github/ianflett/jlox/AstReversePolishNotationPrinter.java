package com.github.ianflett.jlox;

/** Represents abstract syntax tree in reverse Polish notation form. */
class AstReversePolishNotationPrinter implements Expr.Visitor<String> {

    /**
     * Represents expression and all child components.
     *
     * @param expr Top-level {@link Expr}ession to represent.
     * @return {@link String} representation of expression.
     */
    String print(Expr expr) {
        return expr.accept(this);
    }

    /**
     * Represents binary expression.
     *
     * @param expr {@link Expr}ession to represent.
     * @return {@link String} representation of expression.
     */
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return reverseNotation(expr.operator.lexeme(), expr.left, expr.right);
    }

    /**
     * Represents grouping expression.
     *
     * @param expr {@link Expr}ession to represent.
     * @return {@link String} representation of expression.
     */
    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    /**
     * Represents literal expression; or {@code nil} if {@code null}.
     *
     * @param expr {@link Expr}ession to represent.
     * @return {@link String} representation of expression.
     */
    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (null == expr.value) return "nil";
        return expr.value.toString();
    }

    /**
     * Represents unary expression.
     *
     * @param expr {@link Expr}ession to represent.
     * @return {@link String} representation of expression.
     */
    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return reverseNotation(expr.operator.lexeme(), expr.right);
    }

    /**
     * Appends operator.
     *
     * @param name {@link Expr}ession name.
     * @param exprs Child {@link Expr}essions.
     * @return {@link String} representation of expression.
     */
    private String reverseNotation(String name, Expr... exprs) {
        var builder = new StringBuilder();

        for (Expr expr : exprs) {
            builder.append(expr.accept(this));
            builder.append(' ');
        }
        builder.append(name);

        return builder.toString();
    }
}
