package com.softlocked.orbit.interpreter.function;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Pair;
import com.softlocked.orbit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ClassConstructor implements IFunction {
    private final int argsCount;
    private final List<Pair<String, Variable.Type>> args = new ArrayList<>();

    private final ASTNode body;


    @Override
    public String getName() {
        return "ClassConstructor";
    }

    @Override
    public int getParameterCount() {
        return argsCount;
    }

    @Override
    public List<Pair<String, Variable.Type>> getParameters() {
        return args;
    }

    @Override
    public Variable.Type getReturnType() {
        return Variable.Type.CLASS;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public ASTNode getBody() {
        return body;
    }

    public ClassConstructor(int argsCount, List<Pair<String, Variable.Type>> args, ASTNode body) {
        this.argsCount = argsCount;
        this.args.addAll(args);
        this.body = body;
    }

    @Override
    public Object call(ILocalContext context, List<Object> args) {
        for (int i = 0; i < args.size(); i++) {
            Object value = Utils.cast(args.get(i), this.args.get(i).second.getJavaClass());
            context.addVariable(this.args.get(i).first, new Variable(this.args.get(i).second, value));
        }

        Object result = body.evaluate(context);

        if(result instanceof Breakpoint) {
            return ((Breakpoint) result).getValue();
        } else {
            return result;
        }
    }
}
