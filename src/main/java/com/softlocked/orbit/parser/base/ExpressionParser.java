package com.softlocked.orbit.parser.base;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.ast.operation.OperationType;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.parser.IParser;
import com.softlocked.orbit.utils.Pair;

import java.util.List;

import static com.softlocked.orbit.parser.Parser.*;

public class ExpressionParser implements IParser {
    @Override
    public ASTNode parse(List<String> tokens, List<IParser> parsers) throws ParsingException {
        List<String> postfix = infixToPostfix(tokens);

        return postfixToAST(postfix, null);
    }

    @Override
    public boolean isValid(List<String> tokens) throws ParsingException {
        boolean awaitingOperator = false;
        boolean awaitingStartOperator = true; // If true, then we are awaiting -, +, !, ~ or @
        boolean awaitingOperand = true;

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            if (token == null || token.isEmpty()) {
                return false;
            }

            if (token.equals(";")) {
                return i == tokens.size() - 1;
            }

            if (token.equals("\n")) {
                continue;
            }

            boolean b = token.equals("!") || token.equals("~") || token.equals("@");

            if (awaitingStartOperator && (b || token.equals("-") || token.equals("+"))) {
                continue;
            }

            if (awaitingOperator && !b && (OperationType.fromSymbol(token) != null || token.equals(",") || token.equals("=") || token.equals("?"))) {
                awaitingOperand = true;
                awaitingOperator = false;
                awaitingStartOperator = true;
                continue;
            }

            boolean b1 = token.equals("(") || token.equals("[") || token.equals("{");
            if (awaitingOperand && !b1 && OperationType.fromSymbol(token) == null) {
                awaitingOperator = true;
                awaitingOperand = false;
                awaitingStartOperator = false;
                continue;
            }

            if (b1) {
                String e = token.equals("(") ? ")" : (token.equals("[") ? "]" : "}");
                int getPair = getPair(tokens, i, token, e);

                awaitingOperator = true;
                awaitingOperand = false;
                awaitingStartOperator = false;

                if (getPair == -1) {
                    throw new ParsingException("Missing closing bracket for " + token + " at index " + i);
                }

                i = getPair;

                continue;
            }

            System.out.println("Invalid token: " + token);

            return false;
        }

        return true;
    }
}
