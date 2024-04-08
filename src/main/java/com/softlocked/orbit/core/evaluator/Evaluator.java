package com.softlocked.orbit.core.evaluator;

import com.softlocked.orbit.core.datatypes.classes.OrbitObject;
import com.softlocked.orbit.interpreter.function.coroutine.Coroutine;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;
import com.softlocked.orbit.utils.Utils;

import java.util.List;
import java.util.Map;

/**
 * The evaluator is used to evaluate expressions and statements
 */
public class Evaluator {
    public static Object cloneObject(Object a) {
        if(!(a instanceof OrbitObject)) {
            if (a instanceof String) {
                return new String((String) a);
            } else if (a instanceof Double) {
                return (double) a;
            } else if (a instanceof Float) {
                return (float) a;
            } else if (a instanceof Long) {
                return (long) a;
            } else if (a instanceof Integer) {
                return (int) a;
            } else if (a instanceof Character) {
                return (char) a;
            } else if (a instanceof Short) {
                return (short) a;
            } else if (a instanceof Byte) {
                return (byte) a;
            } else if (a instanceof Boolean) {
                return (boolean) a;
            } else if (a instanceof List<?>) {
                return List.copyOf((List<?>) a);
            } else if (a instanceof Object[]) {
                return ((Object[]) a).clone();
            } else if (a instanceof Map<?, ?>) {
                return Map.copyOf((Map<?, ?>) a);
            } else if (a instanceof Coroutine original) {
                LocalContext context = new LocalContext(original.getContext().getRoot());
                return new Coroutine(context, original.getFunction(), original.getArgs());
            }
            throw new RuntimeException("Cannot clone " + a.getClass().getSimpleName());
        } else {
            return ((OrbitObject) a).callFunction("clone", List.of());
        }
    }
    public static Object add(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            boolean stringConcat = a instanceof String || b instanceof String || a instanceof List<?> || b instanceof List<?> || a instanceof Map<?, ?> || b instanceof Map<?, ?>;

            if (stringConcat) {
                return (String) Utils.cast(a, String.class) + Utils.cast(b, String.class);
            }

            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 1 -> {
                    return (double) Utils.cast(a, Double.class) + (double) Utils.cast(b, Double.class);
                }
                case 2 -> {
                    return (float) Utils.cast(a, Float.class) + (float) Utils.cast(b, Float.class);
                }
                case 3 -> {
                    return (long) Utils.cast(a, Long.class) + (long) Utils.cast(b, Long.class);
                }
                case 4 -> {
                    return (int) Utils.cast(a, Integer.class) + (int) Utils.cast(b, Integer.class);
                }
                case 5 -> {
                    return (char) Utils.cast(a, Character.class) + (char) Utils.cast(b, Character.class);
                }
                case 6 -> {
                    return (short) Utils.cast(a, Short.class) + (short) Utils.cast(b, Short.class);
                }
                case 7 -> {
                    return (byte) Utils.cast(a, Byte.class) + (byte) Utils.cast(b, Byte.class);
                }
                case 8 -> {
                    return (int) Utils.cast(toBool(a), Integer.class) + (int) Utils.cast(toBool(b), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot add " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction("+", List.of(b));
        }
    }

    public static Object subtract(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 1 -> {
                    return (double) Utils.cast(a, Double.class) - (double) Utils.cast(b, Double.class);
                }
                case 2 -> {
                    return (float) Utils.cast(a, Float.class) - (float) Utils.cast(b, Float.class);
                }
                case 3 -> {
                    return (long) Utils.cast(a, Long.class) - (long) Utils.cast(b, Long.class);
                }
                case 4 -> {
                    return (int) Utils.cast(a, Integer.class) - (int) Utils.cast(b, Integer.class);
                }
                case 5 -> {
                    return (char) Utils.cast(a, Character.class) - (char) Utils.cast(b, Character.class);
                }
                case 6 -> {
                    return (short) Utils.cast(a, Short.class) - (short) Utils.cast(b, Short.class);
                }
                case 7 -> {
                    return (byte) Utils.cast(a, Byte.class) - (byte) Utils.cast(b, Byte.class);
                }
                case 8 -> {
                    return (int) Utils.cast(toBool(a), Integer.class) - (int) Utils.cast(toBool(b), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot subtract " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction("-", List.of(b));
        }
    }

    public static Object multiply(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 1 -> {
                    return (double) Utils.cast(a, Double.class) * (double) Utils.cast(b, Double.class);
                }
                case 2 -> {
                    return (float) Utils.cast(a, Float.class) * (float) Utils.cast(b, Float.class);
                }
                case 3 -> {
                    return (long) Utils.cast(a, Long.class) * (long) Utils.cast(b, Long.class);
                }
                case 4 -> {
                    return (int) Utils.cast(a, Integer.class) * (int) Utils.cast(b, Integer.class);
                }
                case 5 -> {
                    return (char) Utils.cast(a, Character.class) * (char) Utils.cast(b, Character.class);
                }
                case 6 -> {
                    return (short) Utils.cast(a, Short.class) * (short) Utils.cast(b, Short.class);
                }
                case 7 -> {
                    return (byte) Utils.cast(a, Byte.class) * (byte) Utils.cast(b, Byte.class);
                }
                case 8 -> {
                    return (int) Utils.cast(toBool(a), Integer.class) * (int) Utils.cast(toBool(b), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot multiply " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction("*", List.of(b));
        }
    }

    public static Object divide(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 1 -> {
                    return (double) Utils.cast(a, Double.class) / (double) Utils.cast(b, Double.class);
                }
                case 2 -> {
                    return (float) Utils.cast(a, Float.class) / (float) Utils.cast(b, Float.class);
                }
                case 3 -> {
                    return (long) Utils.cast(a, Long.class) / (long) Utils.cast(b, Long.class);
                }
                case 4 -> {
                    return (int) Utils.cast(a, Integer.class) / (int) Utils.cast(b, Integer.class);
                }
                case 5 -> {
                    return (char) Utils.cast(a, Character.class) / (char) Utils.cast(b, Character.class);
                }
                case 6 -> {
                    return (short) Utils.cast(a, Short.class) / (short) Utils.cast(b, Short.class);
                }
                case 7 -> {
                    return (byte) Utils.cast(a, Byte.class) / (byte) Utils.cast(b, Byte.class);
                }
                case 8 -> {
                    return (int) Utils.cast(toBool(a), Integer.class) / (int) Utils.cast(toBool(b), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot divide " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction("/", List.of(b));
        }
    }

    public static Object modulo(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 1 -> {
                    return (double) Utils.cast(a, Double.class) % (double) Utils.cast(b, Double.class);
                }
                case 2 -> {
                    return (float) Utils.cast(a, Float.class) % (float) Utils.cast(b, Float.class);
                }
                case 3 -> {
                    return (long) Utils.cast(a, Long.class) % (long) Utils.cast(b, Long.class);
                }
                case 4 -> {
                    return (int) Utils.cast(a, Integer.class) % (int) Utils.cast(b, Integer.class);
                }
                case 5 -> {
                    return (char) Utils.cast(a, Character.class) % (char) Utils.cast(b, Character.class);
                }
                case 6 -> {
                    return (short) Utils.cast(a, Short.class) % (short) Utils.cast(b, Short.class);
                }
                case 7 -> {
                    return (byte) Utils.cast(a, Byte.class) % (byte) Utils.cast(b, Byte.class);
                }
                case 8 -> {
                    return (int) Utils.cast(toBool(a), Integer.class) % (int) Utils.cast(toBool(b), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot modulo " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction("%", List.of(b));
        }
    }

    public static Object power(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 1 -> {
                    return Math.pow((double) Utils.cast(a, Double.class), (double) Utils.cast(b, Double.class));
                }
                case 2 -> {
                    return Math.pow((float) Utils.cast(a, Float.class), (float) Utils.cast(b, Float.class));
                }
                case 3 -> {
                    return Math.pow((long) Utils.cast(a, Long.class), (long) Utils.cast(b, Long.class));
                }
                case 4 -> {
                    return Math.pow((int) Utils.cast(a, Integer.class), (int) Utils.cast(b, Integer.class));
                }
                case 5 -> {
                    return Math.pow((char) Utils.cast(a, Character.class), (char) Utils.cast(b, Character.class));
                }
                case 6 -> {
                    return Math.pow((short) Utils.cast(a, Short.class), (short) Utils.cast(b, Short.class));
                }
                case 7 -> {
                    return Math.pow((byte) Utils.cast(a, Byte.class), (byte) Utils.cast(b, Byte.class));
                }
                case 8 -> {
                    return Math.pow((int) Utils.cast(toBool(a), Integer.class), (int) Utils.cast(toBool(b), Integer.class));
                }
                default -> {
                    throw new RuntimeException("Cannot power " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction("^", List.of(b));
        }
    }

    public static Object equal(Object a, Object b) {
        if(a == null) {
            return b == null;
        }
        else if(b == null) {
            return false;
        }
        if(!(a instanceof OrbitObject obj)) {
            // strings
            if(a instanceof String || b instanceof String) {
                return a.toString().equals(b.toString());
            }

            if(a instanceof List<?> || b instanceof List<?> || a instanceof Map<?, ?> || b instanceof Map<?, ?>) {
                return a.equals(b);
            }

            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 1 -> {
                    return Utils.cast(a, Double.class).equals(Utils.cast(b, Double.class));
                }
                case 2 -> {
                    return Utils.cast(a, Float.class).equals(Utils.cast(b, Float.class));
                }
                case 3 -> {
                    return Utils.cast(a, Long.class).equals(Utils.cast(b, Long.class));
                }
                case 4 -> {
                    return Utils.cast(a, Integer.class).equals(Utils.cast(b, Integer.class));
                }
                case 5 -> {
                    return Utils.cast(a, Character.class).equals(Utils.cast(b, Character.class));
                }
                case 6 -> {
                    return Utils.cast(a, Short.class).equals(Utils.cast(b, Short.class));
                }
                case 7 -> {
                    return Utils.cast(a, Byte.class).equals(Utils.cast(b, Byte.class));
                }
                case 8 -> {
                    return toBool(a) == toBool(b);
                }
                default -> {
                    throw new RuntimeException("Cannot compare " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            if(obj.hasFunction("==", 1)) {
                return obj.callFunction("==", List.of(b));
            }

            return obj == b;
        }
    }

    public static Object notEquals(Object a, Object b) {
        if(a == null) {
            return b != null;
        }
        else if(b == null) {
            return true;
        }
        if(!(a instanceof OrbitObject obj)) {
            // strings
            if(a instanceof String || b instanceof String) {
                return !a.toString().equals(b.toString());
            }

            if(a instanceof List<?> || b instanceof List<?> || a instanceof Map<?, ?> || b instanceof Map<?, ?>) {
                return !a.equals(b);
            }

            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 1 -> {
                    return !Utils.cast(a, Double.class).equals(Utils.cast(b, Double.class));
                }
                case 2 -> {
                    return !Utils.cast(a, Float.class).equals(Utils.cast(b, Float.class));
                }
                case 3 -> {
                    return !Utils.cast(a, Long.class).equals(Utils.cast(b, Long.class));
                }
                case 4 -> {
                    return !Utils.cast(a, Integer.class).equals(Utils.cast(b, Integer.class));
                }
                case 5 -> {
                    return !Utils.cast(a, Character.class).equals(Utils.cast(b, Character.class));
                }
                case 6 -> {
                    return !Utils.cast(a, Short.class).equals(Utils.cast(b, Short.class));
                }
                case 7 -> {
                    return !Utils.cast(a, Byte.class).equals(Utils.cast(b, Byte.class));
                }
                case 8 -> {
                    return toBool(a) != toBool(b);
                }
                default -> {
                    throw new RuntimeException("Cannot compare " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            if(obj.hasFunction("!=", 1)) {
                return obj.callFunction("!=", List.of(b));
            }

            return obj != b;
        }
    }

    public static Object equalsType(Object a, Object b) {
        return (boolean)equal(a,b) && a.getClass().equals(b.getClass());
    }

    public static Object greaterThan(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 1 -> {
                    return (double) Utils.cast(a, Double.class) > (double) Utils.cast(b, Double.class);
                }
                case 2 -> {
                    return (float) Utils.cast(a, Float.class) > (float) Utils.cast(b, Float.class);
                }
                case 3 -> {
                    return (long) Utils.cast(a, Long.class) > (long) Utils.cast(b, Long.class);
                }
                case 4 -> {
                    return (int) Utils.cast(a, Integer.class) > (int) Utils.cast(b, Integer.class);
                }
                case 5 -> {
                    return (char) Utils.cast(a, Character.class) > (char) Utils.cast(b, Character.class);
                }
                case 6 -> {
                    return (short) Utils.cast(a, Short.class) > (short) Utils.cast(b, Short.class);
                }
                case 7 -> {
                    return (byte) Utils.cast(a, Byte.class) > (byte) Utils.cast(b, Byte.class);
                }
                case 8 -> {
                    return (int) Utils.cast(toBool(a), Integer.class) > (int) Utils.cast(toBool(b), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot compare " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction(">", List.of(b));
        }
    }

    public static Object lessThan(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 1 -> {
                    return (double) Utils.cast(a, Double.class) < (double) Utils.cast(b, Double.class);
                }
                case 2 -> {
                    return (float) Utils.cast(a, Float.class) < (float) Utils.cast(b, Float.class);
                }
                case 3 -> {
                    return (long) Utils.cast(a, Long.class) < (long) Utils.cast(b, Long.class);
                }
                case 4 -> {
                    return (int) Utils.cast(a, Integer.class) < (int) Utils.cast(b, Integer.class);
                }
                case 5 -> {
                    return (char) Utils.cast(a, Character.class) < (char) Utils.cast(b, Character.class);
                }
                case 6 -> {
                    return (short) Utils.cast(a, Short.class) < (short) Utils.cast(b, Short.class);
                }
                case 7 -> {
                    return (byte) Utils.cast(a, Byte.class) < (byte) Utils.cast(b, Byte.class);
                }
                case 8 -> {
                    return (int) Utils.cast(toBool(a), Integer.class) < (int) Utils.cast(toBool(b), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot compare " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction("<", List.of(b));
        }
    }

    public static Object greaterThanOrEquals(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 1 -> {
                    return (double) Utils.cast(a, Double.class) >= (double) Utils.cast(b, Double.class);
                }
                case 2 -> {
                    return (float) Utils.cast(a, Float.class) >= (float) Utils.cast(b, Float.class);
                }
                case 3 -> {
                    return (long) Utils.cast(a, Long.class) >= (long) Utils.cast(b, Long.class);
                }
                case 4 -> {
                    return (int) Utils.cast(a, Integer.class) >= (int) Utils.cast(b, Integer.class);
                }
                case 5 -> {
                    return (char) Utils.cast(a, Character.class) >= (char) Utils.cast(b, Character.class);
                }
                case 6 -> {
                    return (short) Utils.cast(a, Short.class) >= (short) Utils.cast(b, Short.class);
                }
                case 7 -> {
                    return (byte) Utils.cast(a, Byte.class) >= (byte) Utils.cast(b, Byte.class);
                }
                case 8 -> {
                    return (int) Utils.cast(toBool(a), Integer.class) >= (int) Utils.cast(toBool(b), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot compare " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction(">=", List.of(b));
        }
    }

    public static Object lessThanOrEquals(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 1 -> {
                    return (double) Utils.cast(a, Double.class) <= (double) Utils.cast(b, Double.class);
                }
                case 2 -> {
                    return (float) Utils.cast(a, Float.class) <= (float) Utils.cast(b, Float.class);
                }
                case 3 -> {
                    return (long) Utils.cast(a, Long.class) <= (long) Utils.cast(b, Long.class);
                }
                case 4 -> {
                    return (int) Utils.cast(a, Integer.class) <= (int) Utils.cast(b, Integer.class);
                }
                case 5 -> {
                    return (char) Utils.cast(a, Character.class) <= (char) Utils.cast(b, Character.class);
                }
                case 6 -> {
                    return (short) Utils.cast(a, Short.class) <= (short) Utils.cast(b, Short.class);
                }
                case 7 -> {
                    return (byte) Utils.cast(a, Byte.class) <= (byte) Utils.cast(b, Byte.class);
                }
                case 8 -> {
                    return (int) Utils.cast(toBool(a), Integer.class) <= (int) Utils.cast(toBool(b), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot compare " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction("<=", List.of(b));
        }
    }

    public static Object and(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            return toBool(a) && toBool(b);
        } else {
            return ((OrbitObject) a).callFunction("&&", List.of(b));
        }
    }

    public static Object or(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            return toBool(a) || toBool(b);
        } else {
            return ((OrbitObject) a).callFunction("||", List.of(b));
        }
    }

    public static Object not(Object a) {
        if(!(a instanceof OrbitObject)) {
            return !(boolean) toBool(a);
        } else {
            return ((OrbitObject) a).callFunction("!", List.of());
        }
    }

    public static Object bitwiseAnd(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 3 -> {
                    return (long) Utils.cast(a, Long.class) & (long) Utils.cast(b, Long.class);
                }
                case 4 -> {
                    return (int) Utils.cast(a, Integer.class) & (int) Utils.cast(b, Integer.class);
                }
                case 5 -> {
                    return (char) Utils.cast(a, Character.class) & (char) Utils.cast(b, Character.class);
                }
                case 6 -> {
                    return (short) Utils.cast(a, Short.class) & (short) Utils.cast(b, Short.class);
                }
                case 7 -> {
                    return (byte) Utils.cast(a, Byte.class) & (byte) Utils.cast(b, Byte.class);
                }
                case 8 -> {
                    return (int) Utils.cast(toBool(a), Integer.class) & (int) Utils.cast(toBool(b), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot bitwise and " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction("&", List.of(b));
        }
    }

    public static Object bitwiseOr(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 3 -> {
                    return (long) Utils.cast(a, Long.class) | (long) Utils.cast(b, Long.class);
                }
                case 4 -> {
                    return (int) Utils.cast(a, Integer.class) | (int) Utils.cast(b, Integer.class);
                }
                case 5 -> {
                    return (char) Utils.cast(a, Character.class) | (char) Utils.cast(b, Character.class);
                }
                case 6 -> {
                    return (short) Utils.cast(a, Short.class) | (short) Utils.cast(b, Short.class);
                }
                case 7 -> {
                    return (byte) Utils.cast(a, Byte.class) | (byte) Utils.cast(b, Byte.class);
                }
                case 8 -> {
                    return (int) Utils.cast(toBool(a), Integer.class) | (int) Utils.cast(toBool(b), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot bitwise or " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction("|", List.of(b));
        }
    }

    public static Object bitwiseXor(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 3 -> {
                    return (long) Utils.cast(a, Long.class) ^ (long) Utils.cast(b, Long.class);
                }
                case 4 -> {
                    return (int) Utils.cast(a, Integer.class) ^ (int) Utils.cast(b, Integer.class);
                }
                case 5 -> {
                    return (char) Utils.cast(a, Character.class) ^ (char) Utils.cast(b, Character.class);
                }
                case 6 -> {
                    return (short) Utils.cast(a, Short.class) ^ (short) Utils.cast(b, Short.class);
                }
                case 7 -> {
                    return (byte) Utils.cast(a, Byte.class) ^ (byte) Utils.cast(b, Byte.class);
                }
                case 8 -> {
                    return (int) Utils.cast(toBool(a), Integer.class) ^ (int) Utils.cast(toBool(b), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot bitwise xor " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction("^", List.of(b));
        }
    }

    public static Object bitwiseNot(Object a) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), a.getClass());

            switch (priority) {
                case 3 -> {
                    return ~(long) Utils.cast(a, Long.class);
                }
                case 4 -> {
                    return ~(int) Utils.cast(a, Integer.class);
                }
                case 5 -> {
                    return ~(char) Utils.cast(a, Character.class);
                }
                case 6 -> {
                    return ~(short) Utils.cast(a, Short.class);
                }
                case 7 -> {
                    return ~(byte) Utils.cast(a, Byte.class);
                }
                case 8 -> {
                    return ~(int) Utils.cast(toBool(a), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot bitwise not " + a.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction("~", List.of());
        }
    }

    public static Object bitwiseLeftShift(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 3 -> {
                    return (long) Utils.cast(a, Long.class) << (long) Utils.cast(b, Long.class);
                }
                case 4 -> {
                    return (int) Utils.cast(a, Integer.class) << (int) Utils.cast(b, Integer.class);
                }
                case 5 -> {
                    return (char) Utils.cast(a, Character.class) << (char) Utils.cast(b, Character.class);
                }
                case 6 -> {
                    return (short) Utils.cast(a, Short.class) << (short) Utils.cast(b, Short.class);
                }
                case 7 -> {
                    return (byte) Utils.cast(a, Byte.class) << (byte) Utils.cast(b, Byte.class);
                }
                case 8 -> {
                    return (int) Utils.cast(toBool(a), Integer.class) << (int) Utils.cast(toBool(b), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot bitwise left shift " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction("<<", List.of(b));
        }
    }

    public static Object bitwiseRightShift(Object a, Object b) {
        if(!(a instanceof OrbitObject)) {
            int priority = Utils.getPriority(a.getClass(), b.getClass());

            switch (priority) {
                case 3 -> {
                    return (long) Utils.cast(a, Long.class) >> (long) Utils.cast(b, Long.class);
                }
                case 4 -> {
                    return (int) Utils.cast(a, Integer.class) >> (int) Utils.cast(b, Integer.class);
                }
                case 5 -> {
                    return (char) Utils.cast(a, Character.class) >> (char) Utils.cast(b, Character.class);
                }
                case 6 -> {
                    return (short) Utils.cast(a, Short.class) >> (short) Utils.cast(b, Short.class);
                }
                case 7 -> {
                    return (byte) Utils.cast(a, Byte.class) >> (byte) Utils.cast(b, Byte.class);
                }
                case 8 -> {
                    return (int) Utils.cast(toBool(a), Integer.class) >> (int) Utils.cast(toBool(b), Integer.class);
                }
                default -> {
                    throw new RuntimeException("Cannot bitwise right shift " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName());
                }
            }
        } else {
            return ((OrbitObject) a).callFunction(">>", List.of(b));
        }
    }

    public static boolean toBool(Object o) {
        if(o instanceof Boolean bool) {
            return bool;
        } else if(o instanceof Number num) {
            return num.longValue() != 0;
        } else if(o instanceof String str) {
            return !str.isEmpty();
        } else if(o instanceof Character ch) {
            return ch != 0;
        } else if(o instanceof OrbitObject) {
            return (boolean) ((OrbitObject) o).callFunction("cast", List.of("bool"));
        } else if(o instanceof List) {
            return !((List<?>) o).isEmpty();
        } else if(o instanceof Map) {
            return !((Map<?, ?>) o).isEmpty();
        } else if(o instanceof Coroutine c) {
            return !c.isFinished();
        } else {
            return o != null;
        }
    }

    public static Object customOverload(Object a, Object b, String operator) {
        return ((OrbitObject) a).callFunction(operator, List.of(b));
    }
}
