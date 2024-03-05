package com.softlocked.orbit.interpreter.ast.variable;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.memory.ILocalContext;

public record DeleteVarASTNode(String variableName) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        context.removeVariable(this.variableName());
        return null;
    }
}
