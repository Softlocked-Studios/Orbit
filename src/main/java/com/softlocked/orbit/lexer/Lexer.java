package com.softlocked.orbit.lexer;

import com.softlocked.orbit.core.ast.operation.OperationType;
import com.softlocked.orbit.core.exception.ParsingException;

import java.util.ArrayList;
import java.util.List;

/**
 * The Lexer class is used to tokenize the input string into a list of tokens.
 */
public class Lexer {
    private String input;
    private int p = 0;
    private char c;

    private final List<String> tokens = new ArrayList<>();

    public Lexer(String input) {
        this.input = input;
        if(!input.isEmpty())
            c = input.charAt(p);
    }

    public void reset(String input) {
        this.input = input;

        p = 0;
        if(!input.isEmpty())
            c = input.charAt(p);

        tokens.clear();
    }

    public List<String> tokenize() throws ParsingException {
        try {
            if (input.isEmpty()) {
                return tokens;
            }

            reset(input);

            // 1) Break the input into tokens
            while (p < input.length()) {
                String token = nextToken();

                if (!token.isEmpty())
                    tokens.add(token);
            }

            // Check whether the last character is \0, and remove it if it is
            if (tokens.get(tokens.size() - 1).equals("\0")) {
                tokens.remove(tokens.size() - 1);
            }

            // 2) Editing the list of tokens for special interpretations (Such as comments, fields like '!=', etc.)
            for (int i = 0; i < tokens.size(); i++) {
                String token = tokens.get(i);

                switch (token) {
                    // 2.1) ++, --, +=, -=, *=, /=, ==, !=, <=, >=, ===
                    case "+" -> {
                        if (i + 1 < tokens.size()) {
                            String nextToken = tokens.get(i + 1);

                            if (nextToken.equals("=")) {
                                tokens.set(i, token + "=");
                                tokens.remove(i + 1);
                            } else if (nextToken.equals(token)) {
                                tokens.set(i, token + token);
                                tokens.remove(i + 1);
                            }
                        }
                    }
                    case "*", "%", "!", "=", ">", "<" -> {
                        if (i + 1 < tokens.size()) {
                            String nextToken = tokens.get(i + 1);

                            if (nextToken.equals("=")) {
                                tokens.set(i, token + "=");
                                tokens.remove(i + 1);

                                // if the current one is == and next one is =, then combine them
                                if (tokens.get(i).equals("==") && tokens.get(i + 1).equals("=")) {
                                    tokens.set(i, "===");
                                    tokens.remove(i + 1);
                                }
                            }

                            if (token.equals("*") || token.equals(">") || token.equals("<")) {
                                // Combine all next tokens that are the same as the current token
                                while (nextToken.equals(token.charAt(0) + "") && i + 1 < tokens.size()) {
                                    tokens.set(i, token + token);
                                    tokens.remove(i + 1);
                                    nextToken = tokens.get(i + 1);
                                }
                            }
                        }
                    }
                    // 2.2) ||, &&, :
                    case "|", "&" -> {
                        if (i + 1 < tokens.size()) {
                            String nextToken = tokens.get(i + 1);

                            if (nextToken.equals(token)) {
                                tokens.set(i, token + token);
                                tokens.remove(i + 1);
                            }
                        }
                    }
                }

                // 3) numbers (ints are supported by default, but combine numbers if they are separated by a '.' and additionally if there's "0x" at the start, it's a hex number)
                if (token.matches("[0-9]+")) {
                    if (i + 1 < tokens.size()) {
                        String nextToken = tokens.get(i + 1);

                        if (nextToken.equals(".")) {
                            if (i + 2 < tokens.size()) {
                                String nextNextToken = tokens.get(i + 2);

                                if (nextNextToken.matches("[0-9]+")) {
                                    tokens.set(i, token + "." + nextNextToken);
                                    tokens.remove(i + 1);
                                    tokens.remove(i + 1);
                                }
                            }
                        } else if (nextToken.startsWith("x")) {
                            if (nextToken.matches("x[0-9a-fA-F]+")) {
                                tokens.set(i, token + nextToken);
                                tokens.remove(i + 1);
                            }
                        } else if (nextToken.startsWith("b")) {
                            if (nextToken.matches("b[01]+")) {
                                tokens.set(i, token + nextToken);
                                tokens.remove(i + 1);
                            }
                        }
                    }
                }
            }

            // Now check for --
            for (int i = 0; i < tokens.size(); i++) {
                String token = tokens.get(i);

                // Form --
                if (token.equals("-")) {
                    if (i + 1 < tokens.size()) {
                        String nextToken = tokens.get(i + 1);

                        if (nextToken.equals("-") || nextToken.equals("=") || nextToken.equals(">")) {
                            tokens.set(i, "-" + nextToken);
                            tokens.remove(i + 1);
                        }
                    }
                }

                switch (token) {
                    case "#" -> {
                        if (i + 1 < tokens.size()) {
                            String nextToken = tokens.get(i + 1);

                            if (nextToken.equals("=")) {
                                // Search for the end of the block comment (token 1 = '=', token 2 = '#')
                                int j = i + 2;
                                while (j < tokens.size()) {
                                    if (tokens.get(j).equals("=") && tokens.get(j + 1).equals("#")) {
                                        break;
                                    }

                                    j++;
                                }

                                // Error handling
                                if (j == tokens.size()) {
                                    int line = 1;
                                    for (int k = 0; k < i; k++) {
                                        if (tokens.get(k).equals("\r") || tokens.get(k).equals("\n")) {
                                            line++;
                                        }
                                    }

                                    throw new ParsingException("Block comment not closed");
                                }

                                // Remove the tokens that are inside the block comment, including the start and end tokens
                                tokens.subList(i, j + 2).clear();
                            } else if (nextToken.equals("define")) {
                                // preprocess
                                // Get the next two tokens after the define
                                String defineName = tokens.get(i + 2);

                                int j = i + 1;
                                while (j < tokens.size()) {
                                    if (tokens.get(j).equals("\n") || tokens.get(j).equals("\r")) {
                                        break;
                                    }

                                    j++;
                                }

                                List<String> defineValueTokens = new ArrayList<>(tokens.subList(i + 3, j));

                                // Remove the tokens that are inside the define, including the start token
                                tokens.subList(i, j + 1).clear();

                                List<String> newTokens = new ArrayList<>();

                                for (String token1 : tokens) {
                                    if (token1.equals(defineName)) {
                                        newTokens.addAll(defineValueTokens);
                                    } else {
                                        newTokens.add(token1);
                                    }
                                }

                                tokens.clear();
                                tokens.addAll(newTokens);

                                i = -1;
                            } else {
                                // Look for '\n', and if found stop there
                                int j = i + 1;
                                while (j < tokens.size()) {
                                    if (tokens.get(j).equals("\n") || tokens.get(j).equals("\r")) {
                                        break;
                                    }

                                    j++;
                                }

                                // Remove the tokens that are inside the comment, including the start token
                                tokens.subList(i, j).clear();
                            }
                        }
                    }
                    case "/" -> {
                        if (i + 1 < tokens.size()) {
                            String nextToken = tokens.get(i + 1);

                            if (nextToken.equals("/")) {
                                // Line comment
                                // Look for '\n', and if found stop there
                                int j = i + 1;
                                while (j < tokens.size()) {
                                    if (tokens.get(j).equals("\n") || tokens.get(j).equals("\r")) {
                                        break;
                                    }

                                    j++;
                                }

                                // Remove the tokens that are inside the comment, including the start token
                                tokens.subList(i, j).clear();
                            } else if (nextToken.equals("*")) {
                                // Block comment
                                // Search for the end of the block comment (token 1 = '*', token 2 = '/')
                                int j = i + 2;
                                while (j < tokens.size()) {
                                    if (tokens.get(j).equals("*") && tokens.get(j + 1).equals("/")) {
                                        break;
                                    }

                                    j++;
                                }

                                // Error handling
                                if (j == tokens.size()) {
                                    throw new ParsingException("Block comment not closed");
                                }

                                // Remove the tokens that are inside the block comment, including the start and end tokens
                                tokens.subList(i, j + 2).clear();
                            }
                        }
                    }
                }
            }

            // Remove all the empty tokens, or newlines
            tokens.removeIf(token -> token.isEmpty() || token.equals("\r") || token.equals("\n"));

            return tokens;
        } catch (Exception e) {
            throw new ParsingException(e.getMessage());
        }
    }

    private void next() {
        p++;

        if (p >= input.length()) {
            c = '\0';
        } else {
            c = input.charAt(p);
        }
    }

    private String nextToken() {
        if (c == ' ' || c == '\t' || c == '\r') {
            EMPTY();

            return nextToken();
        }
        else if (Character.isAlphabetic(c) || c == '_' || c == '$') {
            return IDENTIFIER();
        }
        else if (c == '"') {
            return STRING();
        }
        else if (c == '\'') {
            return CHAR();
        }
        else if (Character.isDigit(c)) {
            return NUMBER();
        }
        else {
            char current = c;

            next();

            return String.valueOf(current);
        }
    }

    /* Used for empty characters */
    void EMPTY() {
        while (c == ' ' || c == '\t' || c == '\r') {
            next();
        }
    }

    /* Used for strings (e.g. "Hello world!") */
    String STRING() {
        StringBuilder sb = new StringBuilder();

        try {
            do {
                sb.append(c);
                next();
            } while (p < input.length() && (c != '"' || isEscaped()));
            sb.append(c);

            next();
        } catch (Exception ignored) {} // This is a really awful way to deal with this, but who cares?

        return sb.toString();
    }

    private boolean isEscaped() {
        int backslashCount = 0;

        while (input.charAt(p - 1 - backslashCount) == '\\') {
            backslashCount++;
        }

        return backslashCount % 2 != 0;
    }

    /* Used for characters (e.g. 'a') */
    String CHAR() {
        StringBuilder sb = new StringBuilder();

        try {
            do {
                sb.append(c);
                next();
            } while (p < input.length() && (c != '\'' || isEscaped()));
            sb.append(c);

            next();
        } catch (Exception ignored) {}

        return sb.toString();
    }

    /* Used for numbers (e.g. 123) */
    String NUMBER() {
        StringBuilder sb = new StringBuilder();

        // Read digits
        do {
            sb.append(c);
            next();
        } while (Character.isDigit(c));

        return sb.toString();
    }

    /* Used for identifiers (e.g. hello_world) */
    String IDENTIFIER() {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(c);
            next();
        } while (Character.isLetter(c)||Character.isDigit(c)||c=='_'||c=='$'||(c=='.' && (p+1>=input.length() || !Character.isDigit(input.charAt(p+1)))));

        return sb.toString();
    }

    @Override
    public String toString() {
        // Tokenize and return the tokens
        try {
            return tokenize().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isNumeric(String token) {
        return token.matches("\\d+(\\.\\d+)?") || token.matches("0x[0-9a-fA-F]+") || token.matches("0b[01]+");
    }
}
