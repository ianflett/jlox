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
        if (values.containsKey(name.lexeme())) return values.get(name.lexeme());

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }

    /**
     * Binds new name to value.
     *
     * @param name Definition name.
     * @param value Bound value.
     */
    void define(String name, Object value) {
        values.put(name, value);
    }
}
