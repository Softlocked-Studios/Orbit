package com.softlocked.orbit.libraries;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.memory.ILocalContext;
import java.util.List;

public class Types_Library implements OrbitJavaLibrary {
    @Override
    public void load(GlobalContext context) {
        // string type
        context.addFunction(new NativeFunction("string.length", List.of(Variable.Type.STRING), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((String) args.get(0)).length();
            }
        });

        context.addFunction(new NativeFunction("string.concat", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return args.get(0) + ((String) args.get(1));
            }
        });

        context.addFunction(new NativeFunction("string.equals", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return args.get(0).equals(args.get(1));
            }
        });

        context.addFunction(new NativeFunction("string.contains", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((String) args.get(0)).contains((String) args.get(1));
            }
        });

        context.addFunction(new NativeFunction("string.startsWith", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((String) args.get(0)).startsWith((String) args.get(1));
            }
        });

        context.addFunction(new NativeFunction("string.endsWith", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((String) args.get(0)).endsWith((String) args.get(1));
            }
        });

        context.addFunction(new NativeFunction("string.substring", List.of(Variable.Type.STRING, Variable.Type.INT, Variable.Type.INT), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((String) args.get(0)).substring((int) args.get(1), (int) args.get(2));
            }
        });

        context.addFunction(new NativeFunction("string.replace", List.of(Variable.Type.STRING, Variable.Type.STRING, Variable.Type.STRING), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((String) args.get(0)).replace((String) args.get(1), (String) args.get(2));
            }
        });

        context.addFunction(new NativeFunction("string.split", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return List.of(((String) args.get(0)).split((String) args.get(1)));
            }
        });

        context.addFunction(new NativeFunction("string.trim", List.of(Variable.Type.STRING), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((String) args.get(0)).trim();
            }
        });

        context.addFunction(new NativeFunction("string.toUpper", List.of(Variable.Type.STRING), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((String) args.get(0)).toUpperCase();
            }
        });

        context.addFunction(new NativeFunction("string.toLower", List.of(Variable.Type.STRING), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((String) args.get(0)).toLowerCase();
            }
        });

        context.addFunction(new NativeFunction("string.charAt", List.of(Variable.Type.STRING, Variable.Type.INT), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return String.valueOf(((String) args.get(0)).charAt((int) args.get(1)));
            }
        });

        context.addFunction(new NativeFunction("string.indexOf", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((String) args.get(0)).indexOf((String) args.get(1));
            }
        });

        context.addFunction(new NativeFunction("string.lastIndexOf", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((String) args.get(0)).lastIndexOf((String) args.get(1));
            }
        });

        context.addFunction(new NativeFunction("string.isEmpty", List.of(Variable.Type.STRING), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((String) args.get(0)).isEmpty();
            }
        });

        context.addFunction(new NativeFunction("string.isBlank", List.of(Variable.Type.STRING), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((String) args.get(0)).isBlank();
            }
        });

        context.addFunction(new NativeFunction("string.matches", List.of(Variable.Type.STRING, Variable.Type.STRING), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((String) args.get(0)).matches((String) args.get(1));
            }
        });

        // ref type
        context.addFunction(new NativeFunction("ref.get", List.of(Variable.Type.REFERENCE), Variable.Type.ANY) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((Variable) args.get(0)).getValue();
            }
        });

        context.addFunction(new NativeFunction("ref.set", List.of(Variable.Type.REFERENCE, Variable.Type.ANY), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                ((Variable) args.get(0)).setValue(args.get(1));
                return null;
            }
        });

        context.addFunction(new NativeFunction("ref.type", List.of(Variable.Type.REFERENCE), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                Variable ref = (Variable) args.get(0);

                return ref.getType().getTypeName(ref.getValue());
            }
        });

        context.addFunction(new NativeFunction("ref.pointer", List.of(Variable.Type.REFERENCE), Variable.Type.REFERENCE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                Variable ref = (Variable) args.get(0);
                Object value = ref.getValue();

                while(value instanceof Variable) {
                    value = ((Variable) value).getValue();
                }

                return value;
            }
        });

        context.addFunction(new NativeFunction("ref.ref", List.of(Variable.Type.REFERENCE, Variable.Type.REFERENCE), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                ((Variable) args.get(0)).setValue(args.get(1));
                return null;
            }
        });

        context.addFunction(new NativeFunction("ref.deref", List.of(Variable.Type.REFERENCE), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                ((Variable) args.get(0)).setValue(null);
                return null;
            }
        });

        context.addFunction(new NativeFunction("ref.address", List.of(Variable.Type.REFERENCE), Variable.Type.LONG) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                Variable ref = (Variable) args.get(0);
                return System.identityHashCode(ref);
            }
        });

        context.addFunction(new NativeFunction("ref.hexAddress", List.of(Variable.Type.REFERENCE), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                Variable ref = (Variable) args.get(0);
                return "0x" + Long.toHexString(System.identityHashCode(ref));
            }
        });
    }
}
