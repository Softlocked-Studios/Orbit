package com.softlocked.orbit.interpreter.ast.value;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.memory.ILocalContext;

public record VariableASTNode(String name) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        Variable variable = context.getVariable(name);

        if (variable == null) {
            return null;
        }
        return variable.getValue();
    }
}
