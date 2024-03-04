package com.softlocked.orbit.utils;

import com.google.gson.Gson;
import com.softlocked.orbit.core.datatypes.classes.OrbitObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class containing various utility methods and constants
 */
public class Utils {
    public static final String IDENTIFIER_REGEX = "[a-zA-Z_$.][a-zA-Z0-9_$.]*";

    public static final String NUMBER_REGEX = "-?\\d+(\\.\\d+)?";

    private static final List<String> keywords = List.of(
            "if", "else", "while", "for", "return", "break", "continue", "class", "null", "true", "false", "this", "switch", "case", "import"
    );

    public static boolean isKeyword(String word) {
        return keywords.contains(word);
    }

    public static int getPriority(Class<?> type, Class<?> other) {
        // Only for numeric types
        if(type == Double.class || other == Double.class) {
            return 1;
        } else if(type == Float.class || other == Float.class) {
            return 2;
        } else if(type == Long.class || other == Long.class) {
            return 3;
        } else if(type == Integer.class || other == Integer.class) {
            return 4;
        } else if(type == Character.class || other == Character.class) {
            return 5;
        } else if(type == Short.class || other == Short.class) {
            return 6;
        } else if(type == Byte.class || other == Byte.class) {
            return 7;
        } else {
            return 0;
        }
    }

    private static final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\\\\n");
    private static final Pattern TAB_PATTERN = Pattern.compile("\\\\t");
    private static final Pattern CARRIAGE_RETURN_PATTERN = Pattern.compile("\\\\r");
    private static final Pattern QUOTE_PATTERN = Pattern.compile("\\\\\"");

    public static String formatString(String s) {
        String formattedString = NEWLINE_PATTERN.matcher(s).replaceAll("\n");
        formattedString = TAB_PATTERN.matcher(formattedString).replaceAll("\t");
        formattedString = CARRIAGE_RETURN_PATTERN.matcher(formattedString).replaceAll("\r");
        formattedString = QUOTE_PATTERN.matcher(formattedString).replaceAll("\"");

        formattedString = unescapeUnicode(formattedString);

        return formattedString;
    }

    private static String unescapeUnicode(String input) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = UNICODE_PATTERN.matcher(input);

        while (matcher.find()) {
            char ch = (char) Integer.parseInt(matcher.group(1), 16);
            matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf(ch)));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public static Object cast(Object original, Class<?> target) throws RuntimeException {
        if(original == null || target == null || target == Void.class || Object.class.equals(target)) {
            return original;
        }

        if(!target.isAssignableFrom(original.getClass())) {
            return original;
        }

        if(original instanceof OrbitObject && target.equals(OrbitObject.class)) {
            return original;
        }

        if(target.equals(Integer.class) || target.equals(int.class)) {
            if(original instanceof String) {
                return Integer.parseInt((String) original);
            } else if(original instanceof Character) {
                return (int) (char) original;
            } else if(original instanceof Boolean) {
                return (boolean) original ? 1 : 0;
            } else if(original instanceof Number) {
                return ((Number) original).intValue();
            } else if(original instanceof OrbitObject b) {
                return b.callFunction("cast", List.of("int"));
            }
        } else if(target.equals(Float.class) || target.equals(float.class)) {
            if(original instanceof String) {
                return Float.parseFloat((String) original);
            } else if(original instanceof Character) {
                return (float) (char) original;
            } else if(original instanceof Boolean) {
                return (boolean) original ? 1f : 0f;
            } else if(original instanceof Number) {
                return ((Number) original).floatValue();
            } else if(original instanceof OrbitObject b) {
                return b.callFunction("cast", List.of("float"));
            }
        } else if(target.equals(Double.class) || target.equals(double.class)) {
            if(original instanceof String) {
                return Double.parseDouble((String) original);
            } else if(original instanceof Character) {
                return (double) (char) original;
            } else if(original instanceof Boolean) {
                return (boolean) original ? 1d : 0d;
            } else if(original instanceof Number) {
                return ((Number) original).doubleValue();
            } else if(original instanceof OrbitObject b) {
                return b.callFunction("cast", List.of("double"));
            }
        } else if(target.equals(Long.class) || target.equals(long.class)) {
            if(original instanceof String) {
                return Long.parseLong((String) original);
            } else if(original instanceof Character) {
                return (long) (char) original;
            } else if(original instanceof Boolean) {
                return (boolean) original ? 1L : 0L;
            } else if(original instanceof Number) {
                return ((Number) original).longValue();
            } else if(original instanceof OrbitObject b) {
                return b.callFunction("cast", List.of("long"));
            }
        } else if(target.equals(Byte.class) || target.equals(byte.class)) {
            if(original instanceof String) {
                return Byte.parseByte((String) original);
            } else if(original instanceof Character) {
                return (byte) (char) original;
            } else if(original instanceof Boolean) {
                return (boolean) original ? (byte) 1 : (byte) 0;
            } else if(original instanceof Number) {
                return ((Number) original).byteValue();
            } else if(original instanceof OrbitObject b) {
                return b.callFunction("cast", List.of("byte"));
            }
        } else if(target.equals(Character.class) || target.equals(char.class)) {
            if(original instanceof String) {
                return ((String) original).charAt(0);
            } else if(original instanceof Number) {
                return (char) ((Number) original).intValue();
            } else if(original instanceof Boolean) {
                return (boolean) original ? 't' : 'f';
            } else if(original instanceof OrbitObject b) {
                return b.callFunction("cast", List.of("char"));
            }
        } else if(target.equals(Boolean.class) || target.equals(boolean.class)) {
            if(original instanceof String) {
                return Boolean.parseBoolean((String) original);
            } else if(original instanceof Number) {
                return ((Number) original).intValue() != 0;
            } else if(original instanceof Character) {
                return (char) original != 0;
            } else if(original instanceof OrbitObject b) {
                return b.callFunction("cast", List.of("bool"));
            }
        } else if(target.equals(String.class)) {
            if(original instanceof Map) {
                Gson gson = new Gson();
                return gson.toJson(original);
            } else if(original instanceof List) {
                Gson gson = new Gson();
                return gson.toJson(original);
            } else if(original instanceof OrbitObject b) {
                try {
                    return b.callFunction("cast", List.of("string"));
                } catch (RuntimeException e) {
                    return original.toString();
                }
            } else if(original instanceof Object[]) {
                return Arrays.toString((Object[]) original);
            } else {
                return original.toString();
            }
        } else if(target.equals(Short.class)) {
            if(original instanceof String) {
                return Short.parseShort((String) original);
            } else if(original instanceof Character) {
                return (short) (char) original;
            } else if(original instanceof Boolean) {
                return (boolean) original ? (short) 1 : (short) 0;
            } else if(original instanceof Number) {
                return ((Number) original).shortValue();
            } else if(original instanceof OrbitObject b) {
                return b.callFunction("cast", List.of("short"));
            }
        }

        else if (target.equals(Object[].class) && original instanceof List<?>) {
            List<Object> list = (List<Object>) original;
            Object[] array = new Object[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = list.get(i);
            }
            return array;
        } else if (target.equals(List.class) && original instanceof Object[]) {
            List<Object> list = new ArrayList<>();
            Collections.addAll(list, (Object[]) original);
            return list;
        }

        throw new RuntimeException("Cannot cast " + original.getClass().getSimpleName() + " to " + target.getSimpleName() + "!");
    }

    public static Object newObject(Class<?> type) {
        if(type == Integer.class || type == int.class) {
            return 0;
        } else if(type == Float.class || type == float.class) {
            return 0f;
        } else if(type == Double.class || type == double.class) {
            return 0d;
        } else if(type == Long.class || type == long.class) {
            return 0L;
        } else if(type == Byte.class || type == byte.class) {
            return (byte) 0;
        } else if(type == Character.class || type == char.class) {
            return (char) 0;
        } else if(type == Boolean.class || type == boolean.class) {
            return false;
        } else if(type == String.class) {
            return "";
        } else if(type == List.class || type == ArrayList.class) {
            return new ArrayList<>();
        } else if(type == Map.class || type == HashMap.class) {
            return new HashMap<>();
        } else if(type == Object[].class) {
            return new Object[0];
        } else {
            return null;
        }
    }
}
