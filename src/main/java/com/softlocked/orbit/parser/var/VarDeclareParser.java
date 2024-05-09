package com.softlocked.orbit.parser.var;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.interpreter.ast.object.ConstObjASTNode;
import com.softlocked.orbit.interpreter.ast.object.DecObjASTNode;
import com.softlocked.orbit.interpreter.ast.value.ValueASTNode;
import com.softlocked.orbit.interpreter.ast.variable.ConstVarASTNode;
import com.softlocked.orbit.interpreter.ast.variable.DecVarASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.IParser;
import com.softlocked.orbit.parser.template.ExpressionParser;
import com.softlocked.orbit.utils.Utils;

import java.util.List;

public class VarDeclareParser implements IParser {
    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public ASTNode parse(List<String> tokens, List<IParser> parsers) throws ParsingException {
        boolean isConst = false;
        if(tokens.get(0).matches("const|final")) {
            isConst = true;
            tokens = tokens.subList(1, tokens.size());
        }

        String type = tokens.get(0);
        String name = tokens.get(1);

        int equals = -1;
        for (int i = 1; i < tokens.size(); i++) {
            if (tokens.get(i).equals("=")) {
                equals = i;
                break;
            }
        }

        ExpressionParser expressionParser = ExpressionParser.INSTANCE;

        Class<?> primitiveType = GlobalContext.getPrimitiveType(type);

        if(tokens.size() == 2) {
            if (primitiveType != null) {
                if(isConst) {
                    return new ConstVarASTNode(
                            name,
                            new ValueASTNode(Utils.newObject(primitiveType)),
                            Variable.Type.fromJavaClass(primitiveType)
                    );
                }
                return new DecVarASTNode(
                        name,
                        new ValueASTNode(Utils.newObject(primitiveType)),
                        Variable.Type.fromJavaClass(primitiveType)
                );
            } else {
                if(isConst) {
                    return new ConstObjASTNode(
                            name,
                            null,
                            type
                    );
                }
                return new DecObjASTNode(
                        name,
                        null,
                        type
                );
            }
        }

        if (primitiveType != null) {
            if(isConst) {
                return new ConstVarASTNode(
                        name,
                        expressionParser.parse(tokens.subList(equals + 1, tokens.size()), parsers),
                        Variable.Type.fromJavaClass(primitiveType)
                );
            }
            return new DecVarASTNode(
                    name,
                    expressionParser.parse(tokens.subList(equals + 1, tokens.size()), parsers),
                    Variable.Type.fromJavaClass(primitiveType)
            );
        } else {
            if(isConst) {
                return new ConstObjASTNode(
                        name,
                        expressionParser.parse(tokens.subList(equals + 1, tokens.size()), parsers),
                        type
                );
            }
            return new DecObjASTNode(
                    name,
                    expressionParser.parse(tokens.subList(equals + 1, tokens.size()), parsers),
                    type
            );
        }
    }

    @Override
    public boolean isValid(List<String> tokens) throws ParsingException {
        String firstToken = tokens.get(0);

        VarAssignParser varAssignParser = new VarAssignParser();

        if (tokens.size() == 1) {
            return false;
        }

        if(firstToken.matches("const|final")) {
            String name = tokens.get(1);

            if (tokens.size() == 2) {
                return false;
            }

            return name.matches(Utils.IDENTIFIER_REGEX) && (tokens.size() == 3 || varAssignParser.isValid(tokens.subList(2, tokens.size())));
        }

        return firstToken.matches(Utils.IDENTIFIER_REGEX) && (tokens.size() == 2 || varAssignParser.isValid(tokens.subList(1, tokens.size())));
    }
}
