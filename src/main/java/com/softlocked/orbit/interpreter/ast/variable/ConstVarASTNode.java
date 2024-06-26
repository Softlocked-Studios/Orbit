package com.softlocked.orbit.interpreter.ast.variable;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.ConstVar;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Utils;

public record ConstVarASTNode(String variableName, ASTNode value, Variable.Type type) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        Object value = this.value().evaluate(context);

        Object casted = Utils.cast(value, this.type().getJavaClass());

        ConstVar variable = new ConstVar(this.type(), casted);

        context.addVariable(this.variableName(), variable);

        return variable;
    }
}