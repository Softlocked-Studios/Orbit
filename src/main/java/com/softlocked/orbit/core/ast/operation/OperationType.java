package com.softlocked.orbit.core.ast.operation;

public enum OperationType {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%"),
    EQUALS("=="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_OR_EQUALS(">="),
    LESS_THAN_OR_EQUALS("<="),

    AND("&&"),
    OR("||"),
    NOT("!"),
    BITWISE_AND("&"),
    BITWISE_OR("|"),
    BITWISE_XOR("^"),
    BITWISE_NOT("~"),
    BITWISE_LEFT_SHIFT("<<"),
    BITWISE_RIGHT_SHIFT(">>"),

    POWER("**"),
    REF(":"),

    // Unused operators. They are here for operator overloading only
    CLONE("@");

    private final String symbol;

    OperationType(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static OperationType fromSymbol(String symbol) {
        switch (symbol) {
            case "and" -> symbol = "&&";
            case "or" -> symbol = "||";
            case "not" -> symbol = "!";
            case "xor" -> symbol = "^";
            case "is" -> symbol = "==";
        }
        for (OperationType type : values()) {
            if (type.symbol.equals(symbol)) {
                return type;
            }
        }

        return null;
    }
}
