package com.softlocked.orbit.project;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.ast.operation.OperationType;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.lexer.Lexer;
import com.softlocked.orbit.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class OrbitREPL implements Runner {
    GlobalContext context;
    Lexer lexer = new Lexer("");

    int parenthesis = 0, brackets = 0, braces = 0; // () [] {}

    boolean awaitingNewLine = false;

    List<String> cachedTokens = new ArrayList<>();

    public OrbitREPL(GlobalContext context) {
        this.context = context;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Running Orbit REPL. Type 'exit' to exit.");

        while(true) {
            if (!awaitingNewLine)
                System.out.print("\u001B[32m>>> \u001B[0m");
            else
                System.out.print("\u001B[32m... \u001B[0m");
            try {
                String line = scanner.nextLine();

                if (line.equals("exit")) {
                    break;
                } else if (line.equals("clear") || line.equals("cls")) {
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                    continue;
                }

                try {
                    lexer.reset(line);
                    List<String> tokens = lexer.tokenize();

                    if (tokens.isEmpty()) {
                        continue;
                    }

                    // Count parenthesis, brackets, and braces
                    for (String token : tokens) {
                        switch (token) {
                            case "(" -> parenthesis++;
                            case ")" -> parenthesis--;
                            case "[" -> brackets++;
                            case "]" -> brackets--;
                            case "{" -> braces++;
                            case "}" -> braces--;
                        }
                    }

                    String lastToken = tokens.get(tokens.size() - 1);

                    // If the input is not complete, cache the tokens and wait for more input
                    if (parenthesis > 0 || brackets > 0 || braces > 0 || lastToken.equals("=") || lastToken.equals(":") || OperationType.fromSymbol(lastToken) != null) {
                        cachedTokens.addAll(tokens);
                        awaitingNewLine = true;
                        continue;
                    }

                    if (!awaitingNewLine) {
                        ASTNode node = Parser.parse(tokens, context);

                        Object result = node.evaluate(context);

                        if (result != null && !(result instanceof Variable)) {
                            System.out.println(result);
                        }
                    } else {
                        awaitingNewLine = false;
                        cachedTokens.addAll(tokens);

                        ASTNode node = Parser.parse(cachedTokens, context);

                        Object result = node.evaluate(context);

                        if (result != null && !(result instanceof Variable)) {
                            System.out.println(result);
                        }
                    }
                } catch (Exception | Error e) {
                    System.err.println("\u001B[31m[ERROR]\u001B[0m " + e.getMessage());
                } finally {
                    if(!awaitingNewLine) {
                        parenthesis = 0;
                        brackets = 0;
                        braces = 0;

                        cachedTokens.clear();
                    }
                }
            } catch (IllegalStateException ignored) {
            } catch (NoSuchElementException ignored) {
                break;
            }
        }
    }
}
