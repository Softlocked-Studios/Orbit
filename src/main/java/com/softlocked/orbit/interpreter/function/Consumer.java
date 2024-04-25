package com.softlocked.orbit.interpreter.function;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.interpreter.ast.value.VariableASTNode;
import com.softlocked.orbit.memory.ILocalContext;

import java.util.List;

public class Consumer {
    private final ASTNode body;

    public Consumer(ASTNode body) {
        this.body = body;
    }

    public Object accept(ILocalContext context, List<Object> args) {
        if (body instanceof IFunction) {
            return ((IFunction) body).call(context, args);
        }

        if (body instanceof VariableASTNode) {
            IFunction function = context.getFunction(((VariableASTNode) body).name(), args.size());

            if (function != null) {
                return function.call(context, args);
            }
        }

        throw new RuntimeException("Invalid function in consumer");
    }
}
