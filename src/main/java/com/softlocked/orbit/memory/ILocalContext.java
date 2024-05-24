package com.softlocked.orbit.memory;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;

import java.util.Map;

/**
 * Represents a container for local variables. This is used to store the values of variables in a local scope.
 */
public interface ILocalContext {
    void addVariable(String name, Variable value);

    Variable getVariable(String name);

    Map<String, Variable> getVariables();

    void removeVariable(String name);

    ILocalContext getParent();

    GlobalContext getRoot();

    IFunction getFunction(String name, int parameterCount);

    void addFunction(IFunction function);
}
