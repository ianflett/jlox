package com.github.ianflett.jlox;

/** Thrown when error encountered at runtime. */
public class RuntimeError extends RuntimeException {

    /** {@link Token} responsible for error. */
    final Token token;

    /**
     * Constructs {@link RuntimeError}.
     *
     * @param token Token responsible.
     * @param message Error message.
     */
    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
