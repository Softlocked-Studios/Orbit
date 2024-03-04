package com.softlocked.orbit.interpreter.ast.variable;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Utils;

public record AssignVarASTNode(String variableName, ASTNode value) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        Object value = this.value().evaluate(context);

        Variable variable = context.getVariable(this.variableName());

        if (variable == null) {
            throw new RuntimeException("Variable " + this.variableName() + " not found");
        }

        variable.setValue(Utils.cast(value, variable.getType().getJavaClass()));

        return variable;
    }
}
