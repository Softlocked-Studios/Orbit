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

/**
 * Implementation of the IFunction interface which represents functions written inside of Orbit.
 * @see IFunction
 */
public class OrbitFunction implements IFunction {
    private final String name;
    private final int argsCount;
    private final Variable.Type returnType;

    private final List<Pair<String, Variable.Type>> args = new ArrayList<>();

    private final ASTNode body;

    public OrbitFunction(String name, ASTNode body, Variable.Type returnType) {
        this.name = name;
        this.argsCount = 0;
        this.body = body;
        this.returnType = returnType;
    }

    public OrbitFunction(String name, int argsCount, List<Pair<String, Variable.Type>> args, ASTNode body, Variable.Type returnType) {
        this.name = name;
        this.argsCount = argsCount;
        this.args.addAll(args);
        this.body = body;
        this.returnType = returnType;
    }

    @Override
    public String getName() {
        return name;
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
        return returnType;
    }

    @Override
    public boolean isNative() {
        return false;
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

    @Override
    public String toString() {
        return "OrbitFunction{" +
                "name='" + name + '\'' +
                ", argsCount=" + argsCount +
                ", returnType=" + returnType +
                ", args=" + args +
                ", body=" + body +
                '}';
    }
}
