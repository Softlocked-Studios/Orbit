package com.softlocked.orbit.libraries.Math;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.BFunction;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.math.SimplexNoise;

import java.util.List;

public class Math_Library implements OrbitJavaLibrary {

    @Override
    public void load(GlobalContext context) {
        context.addFunction(new NativeFunction("math.sin", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.sin((double) args.get(0));
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) BFunction_Sin.class;
            }
        });

        context.addFunction(new NativeFunction("math.cos", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.cos((double) args.get(0));
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) BFunction_Cos.class;
            }
        });

        context.addFunction(new NativeFunction("math.tan", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.tan((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.asin", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.asin((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.acos", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.acos((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.atan", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.atan((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.atan2", List.of(Variable.Type.DOUBLE, Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.atan2((double) args.get(0), (double) args.get(1));
            }
        });

        context.addFunction(new NativeFunction("math.toDegrees", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.toDegrees((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.toRadians", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.toRadians((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.exp", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.exp((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.log", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.log((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.log10", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.log10((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.sqrt", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.sqrt((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.cbrt", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.cbrt((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.pow", List.of(Variable.Type.DOUBLE, Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.pow((double) args.get(0), (double) args.get(1));
            }
        });

        context.addFunction(new NativeFunction("math.abs", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.abs((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.ceil", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.ceil((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.floor", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.floor((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.round", List.of(Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.round((double) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("math.max", List.of(Variable.Type.DOUBLE, Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.max((double) args.get(0), (double) args.get(1));
            }
        });

        context.addFunction(new NativeFunction("math.min", List.of(Variable.Type.DOUBLE, Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.min((double) args.get(0), (double) args.get(1));
            }
        });

        context.addFunction(new NativeFunction("math.random", List.of(), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.random();
            }
        });

        context.addFunction(new NativeFunction("math.random", List.of(Variable.Type.INT, Variable.Type.INT), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return (int) (Math.random() * ((int) args.get(1) - (int) args.get(0) + 1)) + (int) args.get(0);
            }
        });

        context.addFunction(new NativeFunction("math.PI", List.of(), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.PI;
            }
        });

        context.addFunction(new NativeFunction("math.E", List.of(), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Math.E;
            }
        });

        // Perlin noise generation
        context.addFunction(new NativeFunction("math.noise", List.of(Variable.Type.DOUBLE, Variable.Type.DOUBLE), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return SimplexNoise.noise((double) args.get(0), (double) args.get(1));
            }
        });
    }
}
