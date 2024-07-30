package com.softlocked.orbit.interpreter.function;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.memory.ILocalContext;

import java.util.List;

public class BFunction implements ASTNode {
    protected List<ASTNode> values;

    public BFunction() {}

    public void setValues(List<ASTNode> values) {
        this.values = values;
    }

    @Override
    public Object evaluate(ILocalContext context) {
        throw new RuntimeException("Not implemented");
    }
}

