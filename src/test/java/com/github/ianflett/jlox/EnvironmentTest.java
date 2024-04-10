package com.github.ianflett.jlox;

import static com.github.ianflett.jlox.TestHelper.t;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;

/** Unit tests {@link Environment} class. */
class EnvironmentTest {

    /** Tests {@link Environment#get(Token)} returns value when variable is defined inside scope. */
    @Test
    void get_returnsValue_whenVariableDefinedInsideScope() {

        var store = new HashMap<String, Object>();
        store.put(STORED_NAME, OLD_VALUE);

        var environment = new Environment(store);

        assertThat(environment.get(t(STORED_NAME)), is(equalTo(OLD_VALUE)));
    }

    /**
     * Tests {@link Environment#get(Token)} returns value when variable is defined outside scope.
     */
    @Test
    void get_returnsValue_whenVariableDefinedOutsideScope() {

        var store = new HashMap<String, Object>();
        store.put(STORED_NAME, OLD_VALUE);

        var environment = new Environment(new Environment(store));

        assertThat(environment.get(t(STORED_NAME)), is(equalTo(OLD_VALUE)));
    }

    /**
     * Tests {@link Environment#get(Token)} throws {@link RuntimeError} when variable is undefined.
     */
    @Test
    void get_throwsRuntimeError_whenVariableUndefined() {
        assert_throwsRuntimeError((environment, name) -> environment.get(t(name)));
    }

    /**
     * Tests {@link Environment#assign(Token, Object)} rebinds variable when defined inside scope.
     */
    @Test
    void assign_rebindsValue_whenVariableDefinedInsideScope() {
        var store = new HashMap<String, Object>();
        store.put(STORED_NAME, OLD_VALUE);

        var environment = new Environment(store);
        environment.assign(t(STORED_NAME), NEW_VALUE);

        assertThat(store.get(STORED_NAME), is(equalTo(NEW_VALUE)));
    }

    /**
     * Tests {@link Environment#assign(Token, Object)} rebinds variable when defined outside scope.
     */
    @Test
    void assign_rebindsValue_whenVariableDefinedOutsideScope() {
        var store = new HashMap<String, Object>();
        store.put(STORED_NAME, OLD_VALUE);

        var environment = new Environment(new Environment(store));
        environment.assign(t(STORED_NAME), NEW_VALUE);

        assertThat(store.get(STORED_NAME), is(equalTo(NEW_VALUE)));
    }

    /**
     * Tests {@link Environment#assign(Token, Object)} throws {@link RuntimeError} when variable is
     * undefined.
     */
    @Test
    void assign_throwsRuntimeError_whenVariableUndefined() {
        assert_throwsRuntimeError((environment, name) -> environment.assign(t(name), NEW_VALUE));
    }

    /**
     * Tests {@link Environment#define(String, Object)} binds variable when defined inside scope.
     */
    @Test
    void define_bindsValue_whenVariableDefinedInsideScope() {
        var store = new HashMap<String, Object>();
        store.put(STORED_NAME, OLD_VALUE);

        var environment = new Environment(store);
        environment.define(STORED_NAME, NEW_VALUE);

        assertThat(store.get(STORED_NAME), is(equalTo(NEW_VALUE)));
    }

    /**
     * Tests {@link Environment#define(String, Object)} binds variable when defined outside scope.
     */
    @Test
    void define_bindsValue_whenVariableDefinedOutsideScope() {
        var innerStore = new HashMap<String, Object>();
        var outerStore = new HashMap<String, Object>();
        outerStore.put(STORED_NAME, OLD_VALUE);

        var environment = new Environment(new Environment(outerStore), innerStore);
        environment.define(STORED_NAME, NEW_VALUE);

        assertThat(innerStore.get(STORED_NAME), is(equalTo(NEW_VALUE)));
        assertThat(outerStore.get(STORED_NAME), is(equalTo(OLD_VALUE)));
    }

    /**
     * Tests {@link Environment#define(String, Object)} throws {@link RuntimeError} when variable is
     * undefined.
     */
    @Test
    void define_bindsValue_whenVariableUndefined() {
        var store = new HashMap<String, Object>();

        var environment = new Environment(store);
        environment.define(STORED_NAME, NEW_VALUE);

        assertThat(store.get(STORED_NAME), is(equalTo(NEW_VALUE)));
    }

    /**
     * Asserts {@link RuntimeError} thrown when {@code action} runs.
     *
     * @param action Code to run.
     */
    private static void assert_throwsRuntimeError(BiConsumer<Environment, String> action) {
        var name = "noVariable";
        var exception =
                assertThrows(RuntimeError.class, () -> action.accept(new Environment(), name));
        assertThat(exception.getMessage(), is(equalTo("Undefined variable '" + name + "'.")));
    }

    private static final String STORED_NAME = "myVariable";

    private static final String OLD_VALUE = "oldValue";

    private static final String NEW_VALUE = "newValue";
}
