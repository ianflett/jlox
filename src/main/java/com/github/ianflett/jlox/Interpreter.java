package com.github.ianflett.jlox;

import com.github.ianflett.jlox.Expr.Visitor;
import java.util.function.BiFunction;

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

            case GREATER -> compare(left, expr.operator, right, (l, r) -> l > r);
            case GREATER_EQUAL -> compare(left, expr.operator, right, (l, r) -> l >= r);
            case LESS -> compare(left, expr.operator, right, (l, r) -> l < r);
            case LESS_EQUAL -> compare(left, expr.operator, right, (l, r) -> l <= r);

            case MINUS -> arithmetic(left, expr.operator, right, (l, r) -> l - r);
            case SLASH -> arithmetic(left, expr.operator, right, (l, r) -> l / r);
            case STAR -> arithmetic(left, expr.operator, right, (l, r) -> l * r);

            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    yield (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    yield (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator, OPERANDS_MUST_BE_TWO_NUMBERS_OR_STRINGS);
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
     * Performs standard comparison.
     *
     * @param left Left operand.
     * @param operator Operator applied.
     * @param right Right operand.
     * @param operation Comparison.
     * @return {@code true} if comparison is correct; {@code false} otherwise.
     */
    private static boolean compare(
            Object left,
            Token operator,
            Object right,
            BiFunction<Double, Double, Boolean> operation) {
        if (left instanceof Double && right instanceof Double) {
            return operation.apply((double) left, (double) right);
        }
        if (left instanceof String && right instanceof String) {
            return operation.apply((double) ((String) left).compareTo((String) right), 0d);
        }
        throw new RuntimeError(operator, OPERANDS_MUST_BE_TWO_NUMBERS_OR_STRINGS);
    }

    /**
     * Performs standard arithmetic.
     *
     * @param left Left operand.
     * @param operator Operator applied.
     * @param right Right operand.
     * @param operation Comparison.
     * @return Result of operation on operands.
     */
    private static double arithmetic(
            Object left,
            Token operator,
            Object right,
            BiFunction<Double, Double, Double> operation) {
        checkNumberOperands(operator, left, right);
        return operation.apply((double) left, (double) right);
    }

    /**
     * Whether the operand is number.
     *
     * @param operator Operator being applied.
     * @param operand Operand to check.
     */
    private static void checkNumberOperand(Token operator, Object operand) {
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
    private static void checkNumberOperands(Token operator, Object left, Object right) {
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
    private static boolean isTruthy(Object object) {
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
    private static boolean isEqual(Object a, Object b) {
        return null == a && null == b || null != a && a.equals(b);
    }

    /**
     * Converts value to {@link String}.
     *
     * @param object Value to convert.
     * @return {@link String} representation of value.
     */
    private static String stringify(Object object) {
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

    private static final String OPERANDS_MUST_BE_TWO_NUMBERS_OR_STRINGS =
            "Operands must be two numbers or two strings.";
}
