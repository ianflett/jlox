package com.github.ianflett.jlox;

import java.util.List;
import java.util.function.BiFunction;

/** Interprets abstract syntax tree. */
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    /** {@link Environment} for storing bound variables. */
    private Environment environment;

    /** Constructs new {@link Interpreter}. */
    Interpreter() {
        this(new Environment());
    }

    /**
     * Constructs new {@link Interpreter} for unit testing.
     *
     * @param environment Environment to use.
     */
    Interpreter(Environment environment) {
        this.environment = environment;
    }

    /**
     * Interprets {@link List} of statements.
     *
     * @param statements {@link Stmt}s to interpret.
     */
    void interpret(List<Stmt> statements) {
        try {
            for (var statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    /**
     * Processes binary expression.
     *
     * @param expr {@link Expr}ession to process.
     * @return Value of expression.
     * @throws RuntimeError Division by zero or invalid operand types used.
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
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                if (0d == (double) right) {
                    throw new RuntimeError(expr.operator, "Division by zero.");
                }
                yield (double) left / (double) right;
            }
            case STAR -> arithmetic(left, expr.operator, right, (l, r) -> l * r);

            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    yield (double) left + (double) right;
                }
                if (left instanceof String || right instanceof String) {
                    yield (null != left ? left.toString() : "null")
                            + (null != right ? right.toString() : "null");
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
     * Performs variable expression.
     *
     * @param expr {@link Expr}ession to process.
     * @return Value of expression.
     */
    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    /**
     * Performs standard comparison.
     *
     * @param left Left operand.
     * @param operator Operator applied.
     * @param right Right operand.
     * @param operation Comparison.
     * @return {@code true} if comparison is correct; {@code false} otherwise.
     * @throws RuntimeError Invalid operand types used.
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
     * Whether operand is number.
     *
     * @param operator Operator being applied.
     * @param operand Operand to check.
     * @throws RuntimeError Invalid operand types used.
     */
    private static void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    /**
     * Whether operands are numbers.
     *
     * @param operator Operator being applied.
     * @param left Operand to check.
     * @param right Operand to check.
     * @throws RuntimeError Invalid operand types used.
     */
    private static void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    /**
     * Calls expression's {@link Expr.Visitor} implementation.
     *
     * @param expr {@link Expr}ession.
     * @return {@link Expr.Visitor}'s return.
     */
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    /**
     * Calls statement's {@link Stmt.Visitor} implementation.
     *
     * @param stmt {@link Stmt} to execute.
     */
    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    /**
     * Calls statements' {@link Stmt.Visitor} implementations.
     *
     * @param statements {@link Stmt}s to execute.
     * @param environment New scope.
     */
    private void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (var statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    /**
     * Processes block statement.
     *
     * @param stmt {@link Stmt} to process.
     * @return {@code null}.
     */
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    /**
     * Processes expression statement.
     *
     * @param stmt {@link Stmt} to process.
     * @return {@code null}.
     */
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    /**
     * Processes {@code print} statement.
     *
     * @param stmt {@link Stmt} to process.
     * @return {@code null}.
     */
    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        var value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    /**
     * Processes {@code var} statement.
     *
     * @param stmt {@link Stmt} to process.
     * @return {@code null}.
     */
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (null != stmt.initializer) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme(), value);
        return null;
    }

    /**
     * Processes assignment statement.
     *
     * @param expr {@link Stmt} to process.
     * @return {@code null}.
     */
    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        var value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
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
