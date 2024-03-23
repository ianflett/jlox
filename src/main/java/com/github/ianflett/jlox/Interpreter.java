package com.github.ianflett.jlox;

import com.github.ianflett.jlox.Expr.Visitor;

/** Interprets abstract syntax tree. */
public class Interpreter implements Visitor<Object> {

    /**
     * Interprets {@link Expr}ession.
     *
     * @param expression {@link Expr}ession to interpret.
     */
    void interpret(Expr expression) {
        try {
            var value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    /**
     * Processes binary expression.
     *
     * @param expr {@link Expr}ession to process.
     * @return Value of expression.
     */
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        var left = evaluate(expr.left);
        var right = evaluate(expr.right);

        return switch (expr.operator.type()) {
            case BANG_EQUAL -> !isEqual(left, right);
            case EQUAL_EQUAL -> isEqual(left, right);

            case GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left > (double) right;
            }

            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left >= (double) right;
            }

            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left < (double) right;
            }

            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left <= (double) right;
            }

            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left - (double) right;
            }

            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left / (double) right;
            }

            case STAR -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left * (double) right;
            }

            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    yield (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    yield (String) left + (String) right;
                }
                throw new RuntimeError(
                        expr.operator, "Operands must be two numbers or two strings.");
            }

            default -> null; // Unreachable.
        };
    }

    /**
     * Processes conditional expression.
     *
     * @param expr {@link Expr}ession to process.
     * @return Value of expression.
     */
    @Override
    public Object visitConditionalExpr(Expr.Conditional expr) {
        var condition = evaluate(expr.condition);
        var thenBranch = evaluate(expr.thenBranch);
        var elseBranch = evaluate(expr.elseBranch);

        return condition.equals(true) ? thenBranch : elseBranch;
    }

    /**
     * Processes grouping expression.
     *
     * @param expr {@link Expr}ession to process.
     * @return Value of expression.
     */
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    /**
     * Processes literal expression.
     *
     * @param expr {@link Expr}ession to process.
     * @return Value of expression.
     */
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    /**
     * Processes unary expression.
     *
     * @param expr {@link Expr}ession to process.
     * @return Value of expression.
     */
    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        var right = evaluate(expr.right);

        return switch (expr.operator.type()) {
            case BANG -> !isTruthy(right);
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                yield -(double) right;
            }

            default -> null; // Unreachable.
        };
    }

    /**
     * Whether the operand is number.
     *
     * @param operator Operator being applied.
     * @param operand Operand to check.
     */
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    /**
     * Whether the operands are numbers.
     *
     * @param operator Operator being applied.
     * @param left Operand to check.
     * @param right Operand to check.
     */
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    /**
     * Calls expression's {@link Visitor} implementation.
     *
     * @param expr {@link Expr}ession.
     * @return {@link Visitor}'s return.
     */
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    /**
     * Whether value is {@code true} or {@code false}.
     *
     * @param object Value to evaluate.
     * @return {@code false} if value is {@code false} or {@code nil}; {@code true} otherwise.
     */
    private boolean isTruthy(Object object) {
        if (null == object) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    /**
     * Whether values are equal.
     *
     * @param a Left hand value.
     * @param b Right hand value.
     * @return {@code true} if equal; {@code false} otherwise.
     */
    private boolean isEqual(Object a, Object b) {
        return null == a && null == b || null != a && a.equals(b);
    }

    /**
     * Converts value to {@link String}.
     *
     * @param object Value to convert.
     * @return {@link String} representation of value.
     */
    private String stringify(Object object) {
        if (null == object) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }
}
