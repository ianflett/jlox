package com.github.ianflett.jlox;

import java.util.HashMap;
import java.util.Map;

/** Stores variable values. */
public class Environment {
    /** Stores values bound to variables. */
    private final Map<String, Object> values = new HashMap<>();

    /**
     * Retrieves value bound to name.
     *
     * @param name Definition name.
     * @return Bound value.
     * @throws RuntimeError Name undefined.
     */
    Object get(Token name) {
        assertDefined(name);
        return values.get(name.lexeme());
    }

    /**
     * Binds value to existing name.
     *
     * @param name Definition name.
     * @param value Bound value.
     * @throws RuntimeError Name undefined.
     */
    void assign(Token name, Object value) {
        assertDefined(name);
        define(name.lexeme(), value);
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

    /**
     * Asserts a variable is defined.
     *
     * @param name Definition name.
     */
    private void assertDefined(Token name) {
        if (!values.containsKey(name.lexeme()))
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }
}
