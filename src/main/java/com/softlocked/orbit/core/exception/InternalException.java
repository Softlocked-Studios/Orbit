package com.softlocked.orbit.core.exception;

import com.softlocked.orbit.core.datatypes.classes.OrbitObject;

public class InternalException extends RuntimeException {
    OrbitObject obj;
    public InternalException(OrbitObject obj) {
        this.obj = obj;
    }

    public OrbitObject getObject() {
        return obj;
    }

    @Override
    public String getMessage() {
        return obj.getField("message").getValue().toString();
    }
}
