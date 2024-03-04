package com.softlocked.orbit.interpreter.ast.operation;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.evaluator.Evaluator;
import com.softlocked.orbit.memory.ILocalContext;

public record TernaryASTNode(ASTNode condition, ASTNode trueBranch, ASTNode falseBranch) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        Object condition = this.condition().evaluate(context);

        if (Evaluator.toBool(condition)) {
            return trueBranch().evaluate(context);
        } else {
            return falseBranch().evaluate(context);
        }
    }
}