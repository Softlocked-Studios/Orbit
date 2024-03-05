package com.softlocked.orbit.libraries;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.CountingList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Container_Library implements OrbitJavaLibrary {
    @Override
    public void load(GlobalContext context) {
        // Declaring a few containers: lists, maps and arrays
        context.addFunction(new NativeFunction("list", 0, Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return new java.util.ArrayList<>();
            }
        });

        context.addFunction(new NativeFunction("map", 0, Variable.Type.MAP) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return new java.util.HashMap<>();
            }
        });

        context.addFunction(new NativeFunction("array", List.of(Variable.Type.INT), Variable.Type.ARRAY) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return new Object[(int) args.get(0)];
            }
        });

        context.addFunction(new NativeFunction("list.of", -1, Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return new ArrayList<>(args);
            }
        });

        context.addFunction(new NativeFunction("map.of", -1, Variable.Type.MAP) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                Map<Object, Object> map = new java.util.HashMap<>();
                for (int i = 0; i < args.size(); i += 2) {
                    map.put(args.get(i), args.get(i + 1));
                }
                return map;
            }
        });

        context.addFunction(new NativeFunction("array.of", -1, Variable.Type.ARRAY) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                Object[] array = new Object[args.size()];
                for (int i = 0; i < args.size(); i++) {
                    array[i] = args.get(i);
                }
                return array;
            }
        });

        // Methods for lists
        context.addFunction(new NativeFunction("list.add", List.of(Variable.Type.LIST, Variable.Type.ANY), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                ((List<Object>) args.get(0)).add(args.get(1));
                return null;
            }
        });

        context.addFunction(new NativeFunction("list.get", List.of(Variable.Type.LIST, Variable.Type.INT), Variable.Type.ANY) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                int index = (int) args.get(1);
                if(index < 0) {
                    index = ((List<Object>) args.get(0)).size() + index;
                }
                return ((List<Object>) args.get(0)).get(index);
            }
        });

        context.addFunction(new NativeFunction("list.remove", List.of(Variable.Type.LIST, Variable.Type.INT), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                int index = (int) args.get(1);
                if(index < 0) {
                    index = ((List<Object>) args.get(0)).size() + index;
                }
                ((List<Object>) args.get(0)).remove(index);
                return null;
            }
        });

        context.addFunction(new NativeFunction("list.size", List.of(Variable.Type.LIST), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((List<Object>) args.get(0)).size();
            }
        });

        context.addFunction(new NativeFunction("list.clear", List.of(Variable.Type.LIST), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                ((List<Object>) args.get(0)).clear();
                return null;
            }
        });

        context.addFunction(new NativeFunction("list.contains", List.of(Variable.Type.LIST, Variable.Type.ANY), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((List<Object>) args.get(0)).contains(args.get(1));
            }
        });

        context.addFunction(new NativeFunction("list.isEmpty", List.of(Variable.Type.LIST), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((List<Object>) args.get(0)).isEmpty();
            }
        });

        context.addFunction(new NativeFunction("list.indexOf", List.of(Variable.Type.LIST, Variable.Type.ANY), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((List<Object>) args.get(0)).indexOf(args.get(1));
            }
        });

        context.addFunction(new NativeFunction("list.lastIndexOf", List.of(Variable.Type.LIST, Variable.Type.ANY), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((List<Object>) args.get(0)).lastIndexOf(args.get(1));
            }
        });

        context.addFunction(new NativeFunction("list.set", List.of(Variable.Type.LIST, Variable.Type.INT, Variable.Type.ANY), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                ((List<Object>) args.get(0)).set((int) args.get(1), args.get(2));
                return null;
            }
        });

        context.addFunction(new NativeFunction("list.subList", List.of(Variable.Type.LIST, Variable.Type.INT, Variable.Type.INT), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((List<Object>) args.get(0)).subList((int) args.get(1), (int) args.get(2));
            }
        });

        // Methods for maps
        context.addFunction(new NativeFunction("map.put", List.of(Variable.Type.MAP, Variable.Type.ANY, Variable.Type.ANY), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                ((Map<Object, Object>) args.get(0)).put(args.get(1), args.get(2));
                return null;
            }
        });

        context.addFunction(new NativeFunction("map.get", List.of(Variable.Type.MAP, Variable.Type.ANY), Variable.Type.ANY) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((Map<Object, Object>) args.get(0)).get(args.get(1));
            }
        });

        context.addFunction(new NativeFunction("map.remove", List.of(Variable.Type.MAP, Variable.Type.ANY), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                ((Map<Object, Object>) args.get(0)).remove(args.get(1));
                return null;
            }
        });

        context.addFunction(new NativeFunction("map.size", List.of(Variable.Type.MAP), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((Map<Object, Object>) args.get(0)).size();
            }
        });

        context.addFunction(new NativeFunction("map.clear", List.of(Variable.Type.MAP), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                ((Map<Object, Object>) args.get(0)).clear();
                return null;
            }
        });

        context.addFunction(new NativeFunction("map.containsKey", List.of(Variable.Type.MAP, Variable.Type.ANY), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((Map<Object, Object>) args.get(0)).containsKey(args.get(1));
            }
        });

        context.addFunction(new NativeFunction("map.containsValue", List.of(Variable.Type.MAP, Variable.Type.ANY), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((Map<Object, Object>) args.get(0)).containsValue(args.get(1));
            }
        });

        context.addFunction(new NativeFunction("map.isEmpty", List.of(Variable.Type.MAP), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((Map<Object, Object>) args.get(0)).isEmpty();
            }
        });

        context.addFunction(new NativeFunction("map.keySet", List.of(Variable.Type.MAP), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return new ArrayList<>(((Map<Object, Object>) args.get(0)).keySet());
            }
        });

        context.addFunction(new NativeFunction("map.values", List.of(Variable.Type.MAP), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return new ArrayList<>(((Map<Object, Object>) args.get(0)).values());
            }
        });

        // Methods for arrays
        context.addFunction(new NativeFunction("array.get", List.of(Variable.Type.ARRAY, Variable.Type.INT), Variable.Type.ANY) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((Object[]) args.get(0))[(int) args.get(1)];
            }
        });

        context.addFunction(new NativeFunction("array.set", List.of(Variable.Type.ARRAY, Variable.Type.INT, Variable.Type.ANY), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                ((Object[]) args.get(0))[(int) args.get(1)] = args.get(2);
                return null;
            }
        });

        context.addFunction(new NativeFunction("array.length", List.of(Variable.Type.ARRAY), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((Object[]) args.get(0)).length;
            }
        });

        context.addFunction(new NativeFunction("array.copyOf", List.of(Variable.Type.ARRAY, Variable.Type.INT), Variable.Type.ARRAY) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return java.util.Arrays.copyOf((Object[]) args.get(0), (int) args.get(1));
            }
        });

        context.addFunction(new NativeFunction("array.copyOfRange", List.of(Variable.Type.ARRAY, Variable.Type.INT, Variable.Type.INT), Variable.Type.ARRAY) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return java.util.Arrays.copyOfRange((Object[]) args.get(0), (int) args.get(1), (int) args.get(2));
            }
        });

        context.addFunction(new NativeFunction("array.asList", List.of(Variable.Type.ARRAY), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return java.util.Arrays.asList((Object[]) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("array.toString", List.of(Variable.Type.ARRAY), Variable.Type.STRING) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return java.util.Arrays.toString((Object[]) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("array.fill", List.of(Variable.Type.ARRAY, Variable.Type.ANY), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                java.util.Arrays.fill((Object[]) args.get(0), args.get(1));
                return null;
            }
        });

        context.addFunction(new NativeFunction("array.sort", List.of(Variable.Type.ARRAY), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                java.util.Arrays.sort((Object[]) args.get(0));
                return null;
            }
        });

        context.addFunction(new NativeFunction("array.binarySearch", List.of(Variable.Type.ARRAY, Variable.Type.ANY), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return java.util.Arrays.binarySearch((Object[]) args.get(0), args.get(1));
            }
        });

        context.addFunction(new NativeFunction("array.binarySearch", List.of(Variable.Type.ARRAY, Variable.Type.INT, Variable.Type.INT, Variable.Type.ANY), Variable.Type.INT) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return java.util.Arrays.binarySearch((Object[]) args.get(0), (int) args.get(1), (int) args.get(2), args.get(3));
            }
        });

        context.addFunction(new NativeFunction("array.equals", List.of(Variable.Type.ARRAY, Variable.Type.ARRAY), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return java.util.Arrays.equals((Object[]) args.get(0), (Object[]) args.get(1));
            }
        });

        context.addFunction(new NativeFunction("array.deepEquals", List.of(Variable.Type.ARRAY, Variable.Type.ARRAY), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return java.util.Arrays.deepEquals((Object[]) args.get(0), (Object[]) args.get(1));
            }
        });

        // range()
        context.addFunction(new NativeFunction("range", List.of(Variable.Type.INT), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return java.util.stream.IntStream.range(0, (int) args.get(0)).boxed().toList();
            }
        });

        context.addFunction(new NativeFunction("range", List.of(Variable.Type.INT, Variable.Type.INT), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return java.util.stream.IntStream.range((int) args.get(0), (int) args.get(1)).boxed().toList();
            }
        });

        context.addFunction(new NativeFunction("range", List.of(Variable.Type.INT, Variable.Type.INT, Variable.Type.INT), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return java.util.stream.IntStream.range((int) args.get(0), (int) args.get(1)).filter(i -> i % (int) args.get(2) == 0).boxed().toList();
            }
        });

        // countingRange()
        context.addFunction(new NativeFunction("countingRange", List.of(Variable.Type.INT), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return new CountingList<Integer>(0, (int) args.get(0));
            }
        });

        context.addFunction(new NativeFunction("countingRange", List.of(Variable.Type.INT, Variable.Type.INT), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return new CountingList<Integer>((int) args.get(0), (int) args.get(1));
            }
        });

        context.addFunction(new NativeFunction("countingRange", List.of(Variable.Type.INT, Variable.Type.INT, Variable.Type.INT), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return new CountingList<Integer>((int) args.get(0), (int) args.get(1), (int) args.get(2));
            }
        });
    }
}
