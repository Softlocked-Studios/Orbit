package com.softlocked.orbit.parser.var;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.ast.operation.OperationType;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.interpreter.ast.operation.OperationASTNode;
import com.softlocked.orbit.interpreter.ast.value.VariableASTNode;
import com.softlocked.orbit.interpreter.ast.variable.AssignVarASTNode;
import com.softlocked.orbit.interpreter.ast.variable.collection.CollectionAccessASTNode;
import com.softlocked.orbit.interpreter.ast.variable.collection.CollectionSetASTNode;
import com.softlocked.orbit.parser.IParser;
import com.softlocked.orbit.parser.template.ExpressionParser;
import com.softlocked.orbit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.softlocked.orbit.parser.Parser.getPair;

public class VarAssignParser implements IParser {
    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public ASTNode parse(List<String> tokens, List<IParser> parsers) throws ParsingException {
        String name = tokens.get(0);

        ExpressionParser expressionParser = ExpressionParser.INSTANCE;

        String operator = tokens.get(1);
        switch (operator) {
            case "=" -> {
                return new AssignVarASTNode(
                        name,
                        expressionParser.parse(tokens.subList(2, tokens.size()), parsers)
                );
            }
            case "+=" -> {
                return new AssignVarASTNode(
                        name,
                        new OperationASTNode(
                                new VariableASTNode(name),
                                expressionParser.parse(tokens.subList(2, tokens.size()), parsers),
                                OperationType.ADD
                        )
                );
            }
            case "-=" -> {
                return new AssignVarASTNode(
                        name,
                        new OperationASTNode(
                                new VariableASTNode(name),
                                expressionParser.parse(tokens.subList(2, tokens.size()), parsers),
                                OperationType.SUBTRACT
                        )
                );
            }
            case "*=" -> {
                return new AssignVarASTNode(
                        name,
                        new OperationASTNode(
                                new VariableASTNode(name),
                                expressionParser.parse(tokens.subList(2, tokens.size()), parsers),
                                OperationType.MULTIPLY
                        )
                );
            }
            case "/=" -> {
                return new AssignVarASTNode(
                        name,
                        new OperationASTNode(
                                new VariableASTNode(name),
                                expressionParser.parse(tokens.subList(2, tokens.size()), parsers),
                                OperationType.DIVIDE
                        )
                );
            }
            case "%=" -> {
                return new AssignVarASTNode(
                        name,
                        new OperationASTNode(
                                new VariableASTNode(name),
                                expressionParser.parse(tokens.subList(2, tokens.size()), parsers),
                                OperationType.MODULO
                        )
                );
            }
            case "[" -> {
                List<List<String>> indices = new ArrayList<>();
                String next = tokens.get(1);
                int nextIndex = 1;

                int pair = getPair(tokens, nextIndex, "[", "]");

                if(pair == -1) {
                    throw new ParsingException("Unexpected end of file.");
                }

                indices.add(tokens.subList(nextIndex + 1, pair));

                nextIndex = pair + 1;
                next = tokens.get(nextIndex);

                while(next != null && next.equals("[")) {
                    pair = getPair(tokens, nextIndex, "[", "]");
                    if(pair == -1) {
                        throw new ParsingException("Unexpected end of file.");
                    }
                    indices.add(tokens.subList(nextIndex + 1, pair));
                    nextIndex = pair + 1;
                    next = tokens.get(nextIndex);
                }

                VariableASTNode array = new VariableASTNode(name);
                List<ASTNode> indexNodes = new ArrayList<>();

                for (List<String> index : indices) {
                    indexNodes.add(expressionParser.parse(index, parsers));
                }

                ASTNode value = expressionParser.parse(tokens.subList(nextIndex + 1, tokens.size()), parsers);

                switch (tokens.get(nextIndex)) {
                    case "=" -> {
                        return new CollectionSetASTNode(
                                array,
                                indexNodes,
                                value
                        );
                    }
                    case "+=" -> {
                        return new CollectionSetASTNode(
                                array,
                                indexNodes,
                                new OperationASTNode(
                                        new CollectionAccessASTNode(array,indexNodes),
                                        value,
                                        OperationType.ADD
                                )
                        );
                    }
                    case "-=" -> {
                        return new CollectionSetASTNode(
                                array,
                                indexNodes,
                                new OperationASTNode(
                                        new CollectionAccessASTNode(array,indexNodes),
                                        value,
                                        OperationType.SUBTRACT
                                )
                        );
                    }
                    case "*=" -> {
                        return new CollectionSetASTNode(
                                array,
                                indexNodes,
                                new OperationASTNode(
                                        new CollectionAccessASTNode(array,indexNodes),
                                        value,
                                        OperationType.MULTIPLY
                                )
                        );
                    }
                    case "/=" -> {
                        return new CollectionSetASTNode(
                                array,
                                indexNodes,
                                new OperationASTNode(
                                        new CollectionAccessASTNode(array,indexNodes),
                                        value,
                                        OperationType.DIVIDE
                                )
                        );
                    }
                    case "%=" -> {
                        return new CollectionSetASTNode(
                                array,
                                indexNodes,
                                new OperationASTNode(
                                        new CollectionAccessASTNode(array,indexNodes),
                                        value,
                                        OperationType.MODULO
                                )
                        );
                    }
                    default -> throw new ParsingException("Invalid operator.");
                }
            }
        }

        throw new ParsingException("Invalid operator.");
    }

    @Override
    public boolean isValid(List<String> tokens) throws ParsingException {
        String firstToken = tokens.get(0);

        ExpressionParser expressionParser = new ExpressionParser();

        if (firstToken.matches(Utils.IDENTIFIER_REGEX) && tokens.size() > 1) {
            String secondToken = tokens.get(1);

            if (secondToken.matches("[%*-+/]?=")) {
                return expressionParser.isValid(tokens.subList(2, tokens.size()));
            }

            if (secondToken.equals("[")) {
                int i = 2;
                for (; i < tokens.size() - 1; i++) {
                    if (tokens.get(i).equals("]") && !(tokens.get(i + 1).equals("["))) {
                        break;
                    }
                }

                if(i + 1 >= tokens.size()) {
                    throw new RuntimeException("Invalid array assignment.");
                }

                return tokens.get(i + 1).matches("[%*-+/]?=") && expressionParser.isValid(tokens.subList(i + 2, tokens.size()));
            }
        }

        return false;
    }
}
