package com.softlocked.orbit.core.evaluator;

/**
 * Used by break, continue, and return statements to control the flow of the program.
 */
public class Breakpoint {
    public enum Type {
        BREAK,
        CONTINUE,
        RETURN,
        THROW
    }

    private final Type type;

    private final Object value;

    public Breakpoint(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Breakpoint(Type type) {
        this.type = type;
        this.value = null;
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}
