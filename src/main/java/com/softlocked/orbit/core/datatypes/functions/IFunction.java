package com.softlocked.orbit.core.datatypes.functions;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Pair;

import java.util.List;

public interface IFunction extends ASTNode {
    String getName();
    int getParameterCount();
    List<Pair<String, Variable.Type>> getParameters();
    Variable.Type getReturnType();
    boolean isNative();

    Object call(ILocalContext context, List<Object> args);

    ASTNode getBody();

    @Override
    default Object evaluate(ILocalContext context) {
        context.addFunction(this);
        return null;
    }
}
