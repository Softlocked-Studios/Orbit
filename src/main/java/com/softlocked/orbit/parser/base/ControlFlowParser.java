package com.softlocked.orbit.parser.base;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.interpreter.ast.statement.controlflow.BreakASTNode;
import com.softlocked.orbit.parser.IParser;
import com.softlocked.orbit.parser.template.ExpressionParser;

import java.util.List;

public class ControlFlowParser implements IParser {
    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public ASTNode parse(List<String> tokens, List<IParser> parsers) throws ParsingException {
        String token = tokens.get(0);

        switch (token) {
            case "continue" -> {
                return new BreakASTNode(Breakpoint.Type.CONTINUE, null);
            }
            case "break" -> {
                return new BreakASTNode(Breakpoint.Type.BREAK, null);
            }
            case "return" -> {
                if (tokens.size() == 1) {
                    return new BreakASTNode(Breakpoint.Type.RETURN, null);
                } else {
                    ExpressionParser expressionParser = new ExpressionParser();
                    return new BreakASTNode(Breakpoint.Type.RETURN, expressionParser.parse(tokens.subList(1, tokens.size()), parsers));
                }
            }
            case "throw" -> {
                ExpressionParser expressionParser = new ExpressionParser();
                return new BreakASTNode(Breakpoint.Type.THROW, expressionParser.parse(tokens.subList(1, tokens.size()), parsers));
            }
        }

        throw new RuntimeException("Invalid control flow token.");
    }

    @Override
    public boolean isValid(List<String> tokens) throws ParsingException {
        if (tokens.size() == 1) {
            String token = tokens.get(0);

            return token.equals("continue") || token.equals("break") || token.equals("return");
        } else {
            ExpressionParser expressionParser = ExpressionParser.INSTANCE;
            String token = tokens.get(0);

            return (token.equals("return") || token.equals("throw")) && expressionParser.isValid(tokens.subList(1, tokens.size()));
        }
    }
}
