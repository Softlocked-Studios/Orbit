package com.softlocked.orbit.interpreter.ast.statement;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.evaluator.Evaluator;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Pair;

import java.util.List;

public record BranchASTNode(List<Pair<ASTNode, ASTNode>> branches) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        for (Pair<ASTNode, ASTNode> branch : branches) {
            Object condition = branch.first.evaluate(context);
            if (Evaluator.toBool(condition)) {
                return branch.second.evaluate(context);
            }
        }
        return null;
    }
}
