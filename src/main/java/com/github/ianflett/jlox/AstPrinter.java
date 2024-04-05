package com.github.ianflett.jlox;

import java.util.Stack;

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

    /**
     * Represents variable expression.
     *
     * @param expr {@link Expr}ession to represent.
     * @return {@link String} representation of expression.
     */
    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme();
    }

    /** Represents abstract syntax tree in Lisp form. */
    static class Lisp extends AstPrinter {

        /** {@inheritDoc} */
        @Override
        public String visitBinaryExpr(Expr.Binary expr) {
            return parenthesize(expr.operator.lexeme(), expr.left, expr.right);
        }

        /** {@inheritDoc} */
        @Override
        public String visitConditionalExpr(Expr.Conditional expr) {
            return parenthesize("?:", expr.condition, expr.thenBranch, expr.elseBranch);
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
        public String visitConditionalExpr(Expr.Conditional expr) {
            return reverseNotation("?:", expr.condition, expr.thenBranch, expr.elseBranch);
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

    /** Represents abstract syntax tree in directory form. */
    static class Directory extends AstPrinter {

        /** Stores the current indents. */
        private final Stack<String> indents = new Stack<>();

        /** {@inheritDoc} */
        @Override
        public String visitBinaryExpr(Expr.Binary expr) {

            return String.format(
                    "%s%s%n%s%s",
                    processIndent(true),
                    expr.operator.lexeme(),
                    processChildNode("├", expr.left),
                    processChildNode("└", expr.right));
        }

        /** {@inheritDoc} */
        @Override
        public String visitConditionalExpr(Expr.Conditional expr) {

            return String.format(
                    "%s%s%n%s%s%s",
                    processIndent(true),
                    "?:",
                    processChildNode("├", expr.condition),
                    processChildNode("├", expr.thenBranch),
                    processChildNode("└", expr.elseBranch));
        }

        /** {@inheritDoc} */
        @Override
        public String visitGroupingExpr(Expr.Grouping expr) {

            return String.format(
                    "%s()%n%s", processIndent(true), processChildNode("└", expr.expression));
        }

        /** {@inheritDoc} */
        @Override
        public String visitLiteralExpr(Expr.Literal expr) {

            return String.format(
                    "%s%s%n", processIndent(false), (null == expr.value ? "nil" : expr.value));
        }

        /** {@inheritDoc} */
        @Override
        public String visitUnaryExpr(Expr.Unary expr) {

            return String.format(
                    "%s%s%n%s",
                    processIndent(true), expr.operator.lexeme(), processChildNode("└", expr.right));
        }

        /**
         * Process indents; can adjust initial bullet for subsequent processing.
         *
         * @param doAdjust Whether to adjust indent.
         * @return Indent to apply.
         */
        private String processIndent(boolean doAdjust) {

            if (indents.empty()) return "";

            var builder = new StringBuilder();

            for (var indent : indents) {
                builder.append(indent).append(' ');
            }

            if (doAdjust) {
                indents.push("├".equals(indents.pop()) ? "│" : " ");
            }

            return builder.toString();
        }

        /** Process child node. */
        private String processChildNode(String bullet, Expr child) {

            indents.push(bullet);
            String output = child.accept(this);
            indents.pop();
            return output;
        }
    }
}
