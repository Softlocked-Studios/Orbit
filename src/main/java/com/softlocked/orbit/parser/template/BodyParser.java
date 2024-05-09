package com.softlocked.orbit.parser.template;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.interpreter.ast.generic.BodyASTNode;
import com.softlocked.orbit.parser.IParser;
import com.softlocked.orbit.utils.Pair;

import java.util.List;

public class BodyParser implements IParser {
    private final String[] pairs = new String[]{"{", "}", "then", "end", "then", "else", "else", "end", "do", "end", "does", "end"};

    @Override
    public ASTNode parse(List<String> tokens, List<IParser> parsers) throws ParsingException {
        BodyASTNode body = new BodyASTNode();

        tokens = tokens.subList(1, tokens.size() - 1);

        while (!tokens.isEmpty()) {
            Pair<Integer, ASTNode> pair = checkOther(tokens, parsers);

            body.addNode(pair.second);

            tokens = tokens.subList(pair.first, tokens.size());
        }

        return body.statements().length == 1 ? body.statements()[0] : body;
    }

    @Override
    public boolean isValid(List<String> tokens) {
        if (tokens.size() < 2) {
            return false;
        }

        int count = 0, nr = 0;

        for (String token : tokens) {
            for (int i = 0; i < pairs.length; i += 2) {
                if (token.equals(pairs[i])) {
                    count++;
                }

                if (token.equals(pairs[i + 1])) {
                    count--;

                    if (count < 0) {
                        return false;
                    }

                    if (count == 0) {
                        nr++;
                    }
                }
            }
        }

        if (nr != 1) {
            return false;
        }

        // Now check if it starts and ends with a pair
        for (int i = 0; i < pairs.length; i += 2) {
            if (tokens.get(0).equals(pairs[i]) && tokens.get(tokens.size() - 1).equals(pairs[i + 1])) {
                return count == 0;
            }
        }

        return false;
    }
}
