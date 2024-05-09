package com.softlocked.orbit.parser;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.utils.Pair;

import java.util.List;

/**
 * The IParser interface is used to define custom parser modules.
 */
public interface IParser {
    /**
     * Returns the priority of the parser. Higher priority parsers are checked first.
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Parse the given tokens and return an ASTNode object.
     * @param tokens The list of tokens to parse.
     * @return The ASTNode object, built from the given tokens.
     */
    ASTNode parse(List<String> tokens, List<IParser> parsers) throws ParsingException;

    /**
     * Checks if the given tokens can be parsed by this parser.
     * @param tokens The list of tokens to check.
     * @return True if the tokens are valid for this parser, false otherwise.
     */
    boolean isValid(List<String> tokens) throws ParsingException;


    /**
     * Gets the longest valid token sequence from the given tokens, using a list of parsers, and returns the ASTNode object.
     * @param tokens The list of tokens to check.
     * @param parsers The list of parsers to validate with.
     * @return A pair containing the length of the longest valid token sequence and the ASTNode object.
     */
    default Pair<Integer, ASTNode> checkOther(List<String> tokens, List<IParser> parsers) throws ParsingException{
        for (int i = tokens.size(); i > 0; i--) {
            for (IParser parser : parsers) {
                if (parser.isValid(tokens.subList(0, i))) {
                    return new Pair<>(i, parser.parse(tokens.subList(0, i), parsers));
                }
            }
        }

        // If /n or ;, return nothing and give next token
        if(tokens.get(0).equals("\n") || tokens.get(0).equals(";")) {
            return new Pair<>(1, null);
        }
        throw new RuntimeException("No valid parser found for the given tokens.");
    }
}
