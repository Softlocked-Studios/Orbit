package com.softlocked.orbit.interpreter.ast.statement;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.evaluator.Evaluator;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;

public record ConditionalASTNode(ASTNode condition, ASTNode thenBranch, ASTNode elseBranch) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        Object condition = this.condition().evaluate(context);

        LocalContext newContext = new LocalContext(context);
        if (Evaluator.toBool(condition)) {
            return this.thenBranch().evaluate(newContext);
        } else {
            if (this.elseBranch() == null) {
                return null;
            }
            return this.elseBranch().evaluate(newContext);
        }
    }
}
