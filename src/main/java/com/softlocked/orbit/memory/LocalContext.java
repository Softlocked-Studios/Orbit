package com.softlocked.orbit.memory;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.utils.BinaryMap;

import java.util.Map;
import java.util.TreeMap;

/**
 * Generic implementation of ILocalContext
 * @see ILocalContext
 */
public class LocalContext implements ILocalContext {
    protected final ILocalContext parent;
    protected final GlobalContext root;

    protected final Map<String, Variable> variables = new BinaryMap<>();

    public LocalContext(ILocalContext parent) {
        this.parent = parent;
        this.root = parent.getRoot();
    }

    public LocalContext() {
        this.parent = null;
        this.root = (GlobalContext) this;
    }

    @Override
    public void addVariable(String name, Variable value) {
        variables.put(name, value);
    }

    @Override
    public Variable getVariable(String name) {
        Variable variable = variables.get(name);

        if (variable != null) {
            return variable;
        } else {
            if(parent != null) {
                return parent.getVariable(name);
            }
            else {
                return null;
            }
        }
    }

    @Override
    public Map<String, Variable> getVariables() {
        return variables;
    }

    @Override
    public void removeVariable(String name) {
        Variable variable = variables.remove(name);

        if (variable == null && parent != null) {
            parent.removeVariable(name);
        }
    }

    @Override
    public ILocalContext getParent() {
        return parent;
    }

    @Override
    public GlobalContext getRoot() {
        return root;
    }

    @Override
    public IFunction getFunction(String name, int parameterCount) {
        return root.getFunction(name, parameterCount);
    }

    @Override
    public void addFunction(IFunction function) {
        root.addFunction(function);
    }
}
