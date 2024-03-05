package com.softlocked.orbit.libraries;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.java.JarLoader;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class Standard_Library implements OrbitJavaLibrary {
    @Override
    public void load(GlobalContext context) {
        // print
        context.addFunction(new NativeFunction("print", -1, Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                StringJoiner joiner = new StringJoiner(" ");

                for (Object arg : args) {
                    joiner.add(Utils.cast(arg, String.class) + "");
                }

                System.out.println(joiner);

                return null;
            }
        });

        // printn (print without newline)
        context.addFunction(new NativeFunction("printn", -1, Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                StringJoiner joiner = new StringJoiner(" ");

                for (Object arg : args) {
                    joiner.add(Utils.cast(arg, String.class) + "");
                }

                System.out.print(joiner);

                return null;
            }
        });

        // typeof
        context.addFunction(new NativeFunction("typeof", 1, Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                Object obj = args.get(0);

                if(obj == null) {
                    return "null";
                }

                Variable.Type type = Variable.Type.fromJavaClass(obj.getClass());

                return type != null ? type.getTypeName(obj) : "void";
            }
        });

        // isNull
        context.addFunction(new NativeFunction("isNull", 1, Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return args.get(0) == null;
            }
        });

        // toString
        context.addFunction(new NativeFunction("toString", 1, Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Utils.cast(args.get(0), String.class);
            }
        });

        // toInt
        context.addFunction(new NativeFunction("toInt", 1, Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Utils.cast(args.get(0), Integer.class);
            }
        });

        // toFloat
        context.addFunction(new NativeFunction("toFloat", 1, Variable.Type.FLOAT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Utils.cast(args.get(0), Float.class);
            }
        });

        // toDouble
        context.addFunction(new NativeFunction("toDouble", 1, Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Utils.cast(args.get(0), Double.class);
            }
        });

        // toLong
        context.addFunction(new NativeFunction("toLong", 1, Variable.Type.LONG) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Utils.cast(args.get(0), Long.class);
            }
        });

        // toByte
        context.addFunction(new NativeFunction("toByte", 1, Variable.Type.BYTE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Utils.cast(args.get(0), Byte.class);
            }
        });

        // toShort
        context.addFunction(new NativeFunction("toShort", 1, Variable.Type.SHORT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Utils.cast(args.get(0), Short.class);
            }
        });

        // toChar
        context.addFunction(new NativeFunction("toChar", 1, Variable.Type.CHAR) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Utils.cast(args.get(0), Character.class);
            }
        });

        // toBool
        context.addFunction(new NativeFunction("toBool", 1, Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return Utils.cast(args.get(0), Boolean.class);
            }
        });

        // system.load
        context.addFunction(new NativeFunction("system.load", List.of(Variable.Type.STRING), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                String path = (String) args.get(0);

                JarLoader.loadLibrary(context.getRoot(), path);

                return null;
            }
        });

        // system.modulePath
        context.addFunction(new NativeFunction("system.modulePath", 0, Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return context.getRoot().getPackagePath();
            }
        });

        // system.path
        context.addFunction(new NativeFunction("system.workingPath", 0, Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return context.getRoot().getParentPath();
            }
        });

        // system.exit
        context.addFunction(new NativeFunction("system.exit", List.of(Variable.Type.INT), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                System.exit((int) args.get(0));
                return null;
            }
        });

        // exit()
        context.addFunction(new NativeFunction("exit", 0, Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                System.exit(0);
                return null;
            }
        });

        // system.gc
        context.addFunction(new NativeFunction("system.gc", 0, Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                System.gc();
                return null;
            }
        });
    }
}
