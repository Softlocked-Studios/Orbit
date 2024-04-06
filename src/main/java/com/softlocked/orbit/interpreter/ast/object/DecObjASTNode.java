package com.softlocked.orbit.interpreter.ast.object;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.classes.OrbitClass;
import com.softlocked.orbit.core.datatypes.classes.OrbitObject;
import com.softlocked.orbit.memory.ILocalContext;

public record DecObjASTNode(String variableName, ASTNode value, String clazz) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        Object value = this.value().evaluate(context);

        OrbitClass clazz = context.getRoot().getClassType(this.clazz);

        if(clazz == null) {
            throw new RuntimeException("Class " + this.clazz + " not found");
        }

        if(!(value instanceof OrbitObject obj)) {
            throw new RuntimeException("Cannot declare a value of type " + value.getClass().getName() + " as a class");
        }

        if(!obj.getClazz().extendsClass(clazz)) {
            throw new RuntimeException(obj.getClazz().name() + " does not extend " + clazz.name());
        }

        Variable variable = new Variable(Variable.Type.CLASS, value);

        context.addVariable(this.variableName(), variable);

        return variable;
    }
}
