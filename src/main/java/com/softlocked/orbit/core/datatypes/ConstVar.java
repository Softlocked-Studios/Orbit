package com.softlocked.orbit.core.datatypes;

public class ConstVar extends Variable {
    public ConstVar(Type type, Object value) {
        super(type, value);
    }

    @Override
    public void setValue(Object value) {
        throw new UnsupportedOperationException("Cannot change the value of a constant variable.");
    }

    @Override
    public void setType(Type type) {
        throw new UnsupportedOperationException("Cannot change the type of a constant variable.");
    }
}
