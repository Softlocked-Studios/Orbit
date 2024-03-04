package com.softlocked.orbit.interpreter.ast.generic;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.memory.ILocalContext;

import java.util.List;

public record BodyASTNode(List<ASTNode> statements) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        for (ASTNode node : statements) {
            Object result = node.evaluate(context);

            if (result instanceof Breakpoint) {
                return result;
            }
        }
        return Void.TYPE;
    }

    public void addNode(ASTNode node) {
        statements.add(node);
    }
}
