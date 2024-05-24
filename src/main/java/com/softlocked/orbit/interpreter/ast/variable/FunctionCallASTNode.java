package com.softlocked.orbit.interpreter.ast.variable;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.interpreter.function.Consumer;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;
import com.softlocked.orbit.utils.Pair;
import com.softlocked.orbit.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record FunctionCallASTNode(String name, List<ASTNode> args) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        try {
            IFunction function = context.getFunction(name, args.size());
            if (function == null) {
                throw new RuntimeException("Function " + name + " with " + args.size() + " arguments not found");
            }

            List<Object> evaluatedArgs = new ArrayList<>();

            if (function.getParameterCount() != -1) {
                List<Pair<String, Variable.Type>> parameters = function.getParameters();

                for (ASTNode arg : args) {
                    Variable.Type type = parameters.get(evaluatedArgs.size()).second;

                    if(type == Variable.Type.CONSUMER) {
                        evaluatedArgs.add(new Consumer(arg));
                    } else {
                        evaluatedArgs.add(arg.evaluate(context));
                    }
                }
            } else {
                for (ASTNode arg : args) {
                    evaluatedArgs.add(arg.evaluate(context));
                }
            }

            LocalContext localContext = new LocalContext(context.getRoot());

            if (function instanceof NativeFunction) {
                // Cast argument types
                if (function.getParameterCount() != -1)
                    for (int i = 0; i < evaluatedArgs.size(); i++) {
                        evaluatedArgs.set(i, Utils.cast(evaluatedArgs.get(i), function.getParameters().get(i).second.getJavaClass()));
                    }

                Object result = function.call(context, evaluatedArgs);

                if (result instanceof Breakpoint breakpoint) {
                    return breakpoint.getValue();
                }

                return result;
            }

            Object result = function.call(localContext, evaluatedArgs);

            if (result instanceof Breakpoint breakpoint) {
                return breakpoint.getValue();
            }

            return result;
        } catch (Error | Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
