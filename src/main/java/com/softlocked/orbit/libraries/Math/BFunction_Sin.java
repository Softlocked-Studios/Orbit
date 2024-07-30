package com.softlocked.orbit.libraries.Math;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.BFunction;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Utils;

import java.util.List;

public class BFunction_Sin extends BFunction {
    @Override
    public Object evaluate(ILocalContext context) {
        Object value = values.get(0).evaluate(context);

        return Math.sin((double) Utils.cast(value, Double.class));
    }
}
