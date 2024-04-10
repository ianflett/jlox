package com.github.ianflett.jlox;

import java.util.HashMap;
import java.util.Map;

/** Stores variable values. */
public class Environment {
    /** Stores outer scope. */
    private final Environment enclosing;

    /** Stores values bound to variables. */
    private final Map<String, Object> values;

    /** Constructs {@link Environment}. */
    public Environment() {
        this(null, new HashMap<>());
    }

    /**
     * Constructs {@link Environment}.
     *
     * @param enclosing Outer scope.
     */
    public Environment(final Environment enclosing) {
        this(enclosing, new HashMap<>());
    }

    /**
     * Constructs {@link Environment} for unit testing.
     *
     * @param values Stores bound variables.
     */
    Environment(final Map<String, Object> values) {
        this(null, values);
    }

    /**
     * Constructs {@link Environment} for unit testing.
     *
     * @param enclosing Outer scope.
     * @param values Stores bound variables.
     */
    Environment(final Environment enclosing, final Map<String, Object> values) {
        this.enclosing = enclosing;
        this.values = values;
    }

    /**
     * Retrieves value bound to name.
     *
     * @param name Definition name.
     * @return Bound value.
     * @throws RuntimeError Name undefined.
     */
    Object get(Token name) {
        if (values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }

        if (null != enclosing) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }

    /**
     * Binds value to existing name.
     *
     * @param name Definition name.
     * @param value Bound value.
     * @throws RuntimeError Name undefined.
     */
    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme())) {
            values.put(name.lexeme(), value);
            return;
        }

        if (null != enclosing) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }

    /**
     * Binds value to new name.
     *
     * @param name Definition name.
     * @param value Bound value.
     */
    void define(String name, Object value) {
        values.put(name, value);
    }
}
