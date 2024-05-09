package com.softlocked.orbit.parser;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.interpreter.ast.generic.BodyASTNode;
import com.softlocked.orbit.utils.Pair;

import java.util.List;

public class ProgramParser implements IParser {
    @Override
    public ASTNode parse(List<String> tokens, List<IParser> parsers) throws ParsingException {
        BodyASTNode body = new BodyASTNode();

        while (!tokens.isEmpty()) {
            Pair<Integer, ASTNode> pair = checkOther(tokens, parsers);

            body.addNode(pair.second);

            tokens = tokens.subList(pair.first, tokens.size());
        }

        return body;
    }

    @Override
    public boolean isValid(List<String> tokens) {
        return true;
    }
}
