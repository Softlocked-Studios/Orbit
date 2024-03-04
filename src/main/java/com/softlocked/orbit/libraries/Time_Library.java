package com.softlocked.orbit.libraries;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.memory.ILocalContext;

import java.util.List;

public class Time_Library implements OrbitJavaLibrary {
    @Override
    public void load(GlobalContext context) {
        // time
        context.addFunction(new NativeFunction("time", 0, Variable.Type.LONG) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return System.currentTimeMillis();
            }
        });

        // nanoTime
        context.addFunction(new NativeFunction("nanoTime", 0, Variable.Type.LONG) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return System.nanoTime();
            }
        });

        // sleep
        context.addFunction(new NativeFunction("sleep", List.of(Variable.Type.LONG), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                try {
                    Thread.sleep((long) args.get(0));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }
}
