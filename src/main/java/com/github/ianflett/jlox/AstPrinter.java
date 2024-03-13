package com.github.ianflett.jlox;

/** Prints abstract syntax tree structure. */
abstract class AstPrinter implements Expr.Visitor<String> {

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
    public abstract String visitBinaryExpr(Expr.Binary expr);

    /**
     * Represents grouping expression.
     *
     * @param expr {@link Expr}ession to represent.
     * @return {@link String} representation of expression.
     */
    public abstract String visitGroupingExpr(Expr.Grouping expr);

    /**
     * Represents literal expression; or {@code nil} if {@code null}.
     *
     * @param expr {@link Expr}ession to represent.
     * @return {@link String} representation of expression.
     */
    public abstract String visitLiteralExpr(Expr.Literal expr);

    /**
     * Represents unary expression.
     *
     * @param expr {@link Expr}ession to represent.
     * @return {@link String} representation of expression.
     */
    public abstract String visitUnaryExpr(Expr.Unary expr);

    /** Represents abstract syntax tree in Lisp form. */
    static class Lisp extends AstPrinter {

        /** {@inheritDoc} */
        @Override
        public String visitBinaryExpr(Expr.Binary expr) {
            return parenthesize(expr.operator.lexeme(), expr.left, expr.right);
        }

        /** {@inheritDoc} */
        @Override
        public String visitGroupingExpr(Expr.Grouping expr) {
            return parenthesize("group", expr.expression);
        }

        /** {@inheritDoc} */
        @Override
        public String visitLiteralExpr(Expr.Literal expr) {
            if (null == expr.value) return "nil";
            return expr.value.toString();
        }

        /** {@inheritDoc} */
        @Override
        public String visitUnaryExpr(Expr.Unary expr) {
            return parenthesize(expr.operator.lexeme(), expr.right);
        }

        /**
         * Wraps expression in parentheses.
         *
         * @param name {@link Expr}ession name.
         * @param exprs Child {@link Expr}essions.
         * @return {@link String} representation of expression.
         */
        private String parenthesize(String name, Expr... exprs) {
            var builder = new StringBuilder();

            builder.append('(').append(name);
            for (Expr expr : exprs) {
                builder.append(' ');
                builder.append(expr.accept(this));
            }
            builder.append(')');

            return builder.toString();
        }
    }

    /** Represents abstract syntax tree in reverse Polish notation form. */
    static class ReversePolishNotation extends AstPrinter {

        /** {@inheritDoc} */
        @Override
        public String visitBinaryExpr(Expr.Binary expr) {
            return reverseNotation(expr.operator.lexeme(), expr.left, expr.right);
        }

        /** {@inheritDoc} */
        @Override
        public String visitGroupingExpr(Expr.Grouping expr) {
            return expr.expression.accept(this);
        }

        /** {@inheritDoc} */
        @Override
        public String visitLiteralExpr(Expr.Literal expr) {
            if (null == expr.value) return "nil";
            return expr.value.toString();
        }

        /** {@inheritDoc} */
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
}
