package com.softlocked.orbit.interpreter.ast.object;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.classes.OrbitClass;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Pair;

import java.util.HashMap;
import java.util.List;

public record ClassDefinitionASTNode(String name, List<String> superClasses, HashMap<String, Pair<Variable.Type, ASTNode>> fields,
                                     HashMap<Pair<String, Integer>, IFunction> functions, HashMap<Integer, IFunction> constructors) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        GlobalContext globalContext = context.getRoot();

        List<OrbitClass> superClasses = superClasses().stream().map(globalContext::getClassType).toList();

        globalContext.addClass(new OrbitClass(this.name(), superClasses, this.fields(), this.functions(), this.constructors()));

        return null;
    }
}
