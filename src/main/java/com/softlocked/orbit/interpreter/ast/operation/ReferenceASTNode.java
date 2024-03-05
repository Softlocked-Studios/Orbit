package com.softlocked.orbit.interpreter.ast.operation;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.classes.OrbitObject;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.interpreter.ast.value.VariableASTNode;
import com.softlocked.orbit.interpreter.ast.variable.FunctionCallASTNode;
import com.softlocked.orbit.memory.ILocalContext;

import java.util.ArrayList;
import java.util.List;

public record ReferenceASTNode(ASTNode param, ASTNode function) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        Object left = param.evaluate(context);

        if(left == null) {
            throw new RuntimeException("Cannot reference null");
        }

        // Get the type of the left
        Variable.Type type = Variable.Type.fromJavaClass(left.getClass());

        // If the second is a function, composite the type string and the function name with a dot
        if(function instanceof FunctionCallASTNode functionCall) {
            if(left instanceof OrbitObject orbitObject && orbitObject.hasFunction(functionCall.name(), functionCall.args().size())) {
                List<Object> args = new ArrayList<>();
                for (ASTNode arg : functionCall.args()) {
                    args.add(arg.evaluate(context));
                }
                return orbitObject.callFunction(functionCall.name(), args);
            }

            String name = type.getTypeName(left) + "." + functionCall.name();

            List<ASTNode> args = new ArrayList<>();
            args.add(param);
            args.addAll(functionCall.args());

            // Now create a new function call, with this name, and where the first argument is the left
            FunctionCallASTNode newFunctionCall = new FunctionCallASTNode(name, args);

            return newFunctionCall.evaluate(context);
        } else if(function instanceof VariableASTNode variableASTNode) {
            if(left instanceof OrbitObject orbitObject) {
                return orbitObject.getField(variableASTNode.name()).getValue();
            }

            throw new RuntimeException("Invalid operation");
        }

        throw new RuntimeException("Invalid operation");
    }
}
