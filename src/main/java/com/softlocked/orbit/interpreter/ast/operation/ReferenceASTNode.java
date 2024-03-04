package com.softlocked.orbit.interpreter.ast.operation;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.interpreter.ast.variable.FunctionCallASTNode;
import com.softlocked.orbit.memory.ILocalContext;

public record ReferenceASTNode(ASTNode param, ASTNode function) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        Object left = param.evaluate(context);

        // Get the type of the left
        Variable.Type type = Variable.Type.fromJavaClass(left.getClass());

        // If the second is a function, composite the type string and the function name with a dot
        if(function instanceof FunctionCallASTNode functionCall) {
            String name = type.getTypeName(left) + "." + functionCall.name();

            // Now create a new function call, with this name, and where the first argument is the left
            FunctionCallASTNode newFunctionCall = new FunctionCallASTNode(name, functionCall.args());

            newFunctionCall.args().add(0, param);

            return newFunctionCall.evaluate(context);
        }

        throw new RuntimeException("Invalid operation");
    }
}
