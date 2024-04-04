package com.softlocked.orbit.libraries;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.memory.ILocalContext;

import java.util.List;
import java.util.Scanner;

public class Input_Library implements OrbitJavaLibrary {
    Scanner scanner = new Scanner(System.in);

    @Override
    public void load(GlobalContext context) {
        context.addFunction(new NativeFunction("input.getLine", 0, Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return scanner.nextLine();
            }
        });

        context.addFunction(new NativeFunction("input.get", 0, Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return scanner.next();
            }
        });

        context.addFunction(new NativeFunction("input.getInt", 0, Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return scanner.nextInt();
            }
        });

        context.addFunction(new NativeFunction("input.getDouble", 0, Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return scanner.nextDouble();
            }
        });

        context.addFunction(new NativeFunction("input.getBoolean", 0, Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return scanner.nextBoolean();
            }
        });

        context.addFunction(new NativeFunction("input.getByte", 0, Variable.Type.BYTE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return scanner.nextByte();
            }
        });

        context.addFunction(new NativeFunction("input.getShort", 0, Variable.Type.SHORT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return scanner.nextShort();
            }
        });

        context.addFunction(new NativeFunction("input.getLong", 0, Variable.Type.LONG) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return scanner.nextLong();
            }
        });

        context.addFunction(new NativeFunction("input.getFloat", 0, Variable.Type.FLOAT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return scanner.nextFloat();
            }
        });
    }
}
