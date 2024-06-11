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

public class FunctionCallASTNode implements ASTNode {
    private final String name;
    private final List<ASTNode> args;

    private IFunction cachedFunction;

    public IFunction getCachedFunction(ILocalContext context) {
        if (cachedFunction == null) {
            cachedFunction = context.getFunction(name, args.size());
        }

        return cachedFunction;
    }

    public FunctionCallASTNode(String name, List<ASTNode> args) {
        this.name = name;
        this.args = args;
    }

    public FunctionCallASTNode(IFunction function, List<ASTNode> args) {
        this.name = function.getName();
        this.args = args;
        this.cachedFunction = function;
    }

    @Override
    public Object evaluate(ILocalContext context) {
        try {
            // Caching
            if (cachedFunction == null) {
                cachedFunction = context.getFunction(name, args.size());

                if (cachedFunction == null) {
                    throw new RuntimeException("Function " + name + " with " + args.size() + " arguments not found");
                }
            }

            List<Object> evaluatedArgs = new ArrayList<>();

            if (cachedFunction.getParameterCount() != -1) {
                List<Pair<String, Variable.Type>> parameters = cachedFunction.getParameters();

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

            if (cachedFunction instanceof NativeFunction) {
                // Cast argument types
                if (cachedFunction.getParameterCount() != -1)
                    for (int i = 0; i < evaluatedArgs.size(); i++) {
                        evaluatedArgs.set(i, Utils.cast(evaluatedArgs.get(i), cachedFunction.getParameters().get(i).second.getJavaClass()));
                    }

                Object result = cachedFunction.call(context, evaluatedArgs);

                if (result instanceof Breakpoint breakpoint) {
                    return breakpoint.getValue();
                }

                return result;
            }

            Object result = cachedFunction.call(localContext, evaluatedArgs);

            if (result instanceof Breakpoint breakpoint) {
                return breakpoint.getValue();
            }

            return result;
        } catch (Error | Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String name() {
        return name;
    }

    public List<ASTNode> args() {
        return args;
    }
}
