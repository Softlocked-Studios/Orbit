package com.softlocked.orbit.interpreter.ast.variable;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;

import java.util.List;
import java.util.stream.Collectors;

public record FunctionCallASTNode(String name, List<ASTNode> args) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        IFunction function = context.getFunction(name, args.size());
        if (function == null) {
            throw new RuntimeException("Function " + name + " not found");
        }

        List<Object> evaluatedArgs = args.stream()
                .map(arg -> arg.evaluate(context))
                .collect(Collectors.toList());

        LocalContext localContext = new LocalContext(context.getRoot());

        Object result = function.call(localContext, evaluatedArgs);

        if(result instanceof Breakpoint breakpoint) {
            return breakpoint.getValue();
        }

        return result;
    }
}
