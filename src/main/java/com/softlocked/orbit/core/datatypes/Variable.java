package com.softlocked.orbit.core.datatypes;

import com.softlocked.orbit.core.datatypes.classes.OrbitObject;
import com.softlocked.orbit.interpreter.function.coroutine.Coroutine;

import java.util.List;
import java.util.Map;

/**
 * A simple class to hold a variable and its type
 */
public class Variable {
    private Variable.Type type;
    private Object value;

    public Variable(Variable.Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Variable.Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setType(Variable.Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "reference(" + value + ")";
    }

    public enum Type {
        INT,
        FLOAT,
        DOUBLE,
        LONG,
        BYTE,
        SHORT,
        CHAR,
        BOOL,
        STRING,
        ARRAY,
        ANY,
        VOID,
        CLASS,
        LIST,
        MAP,
        REFERENCE,
        COROUTINE;

        public Class<?> getJavaClass() {
            return switch (this) {
                case INT -> int.class;
                case FLOAT -> float.class;
                case DOUBLE -> double.class;
                case LONG -> long.class;
                case BYTE -> byte.class;
                case SHORT -> short.class;
                case CHAR -> char.class;
                case BOOL -> boolean.class;
                case STRING -> String.class;
                case ARRAY -> Object[].class;
                case ANY -> Object.class;
                case VOID -> void.class;
                case CLASS -> OrbitObject.class;
                case LIST -> java.util.List.class;
                case MAP -> java.util.Map.class;
                case REFERENCE -> Variable.class;
                case COROUTINE -> Coroutine.class;
            };
        }

        public static Type fromJavaClass(Class<?> clazz) {
            if (clazz == int.class || clazz == Integer.class) return INT;
            else if (clazz == float.class || clazz == Float.class) return FLOAT;
            else if (clazz == double.class || clazz == Double.class) return DOUBLE;
            else if (clazz == long.class || clazz == Long.class) return LONG;
            else if (clazz == byte.class || clazz == Byte.class) return BYTE;
            else if (clazz == short.class || clazz == Short.class) return SHORT;
            else if (clazz == char.class || clazz == Character.class) return CHAR;
            else if (clazz == boolean.class || clazz == Boolean.class) return BOOL;
            else if (clazz == String.class) return STRING;
            else if (clazz == Coroutine.class) return COROUTINE;
            else if (clazz == Object.class) return ANY;
            else if (clazz == void.class || clazz == Void.class) return VOID;
            else if (clazz == OrbitObject.class) return CLASS;
            else if (clazz.isAssignableFrom(List.class)) return LIST;
            else if (clazz.isAssignableFrom(Map.class)) return MAP;
            else if (clazz == Variable.class) return REFERENCE;
            // arrays
            else if (clazz.isArray()) return ARRAY;
            else return null;
        }

        public static String getTypeName(Object value) {
            if (value == null) return "void";
            else if (value instanceof Integer) return "int";
            else if (value instanceof Float) return "float";
            else if (value instanceof Double) return "double";
            else if (value instanceof Long) return "long";
            else if (value instanceof Byte) return "byte";
            else if (value instanceof Short) return "short";
            else if (value instanceof Character) return "char";
            else if (value instanceof Boolean) return "bool";
            else if (value instanceof String) return "string";
            else if (value instanceof Object[]) return "array";
            else if (value instanceof List) return "list";
            else if (value instanceof Map) return "map";
            else if (value instanceof Coroutine) return "coroutine";
            else if (value instanceof Variable) return "ref";
            else if (value instanceof OrbitObject orbitObject) {
                return orbitObject.getClazz().name();
            }
            else return "var";
        }
    }

    public String getTypeName() {
        switch (type) {
            case INT -> { return "int"; }
            case FLOAT -> { return "float"; }
            case DOUBLE -> { return "double"; }
            case LONG -> { return "long"; }
            case BYTE -> { return "byte"; }
            case SHORT -> { return "short"; }
            case CHAR -> { return "char"; }
            case BOOL -> { return "bool"; }
            case STRING -> { return "string"; }
            case ARRAY -> { return "array"; }
            case CLASS, ANY -> {
                if (value instanceof OrbitObject) {
                    return ((OrbitObject) value).getClazz().name();
                }
                return "class";
            }
            case LIST -> { return "list"; }
            case MAP -> { return "map"; }
            case REFERENCE -> { return "ref"; }
            case VOID -> { return "void"; }
            case COROUTINE -> { return "coroutine"; }
            default -> { return "var"; }
        }
    }
}
