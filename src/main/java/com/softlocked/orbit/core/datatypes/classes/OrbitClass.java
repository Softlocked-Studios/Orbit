package com.softlocked.orbit.core.datatypes.classes;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.interpreter.function.ClassConstructor;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.utils.Pair;

import java.util.HashMap;
import java.util.List;

/**
 * Used for representing a class in the Orbit language
 *
 * @see Variable
 */
public record OrbitClass(String name, List<OrbitClass> superClasses,
                         HashMap<String, Pair<Variable.Type, ASTNode>> fields,
                         HashMap<Pair<String, Integer>, IFunction> functions,
                         HashMap<Integer, ClassConstructor> constructors) {
    public OrbitClass(String name, List<OrbitClass> superClasses, HashMap<String, Pair<Variable.Type, ASTNode>> fields, HashMap<Pair<String, Integer>, IFunction> functions, HashMap<Integer, ClassConstructor> constructors) {
        this.name = name;
        this.superClasses = superClasses;
        this.fields = fields;
        this.functions = functions;
        this.constructors = constructors;

        // Add fields from super classes if they are not overridden
        if (superClasses != null) {
            for (OrbitClass superClass : superClasses) {
                if (superClass.fields() != null) {
                    for (String s : superClass.fields().keySet()) {
                        if (!this.fields.containsKey(s)) {
                            this.fields.put(s, superClass.fields().get(s));
                        }
                    }
                }
            }
        }

        // Add constructors from super classes if they are not overridden
        if (superClasses != null) {
            for (OrbitClass superClass : superClasses) {
                if (superClass.constructors() != null) {
                    for (Integer integer : superClass.constructors().keySet()) {
                        if (!this.constructors.containsKey(integer)) {
                            this.constructors.put(integer, superClass.constructors().get(integer));
                        }
                    }
                }
            }
        }
    }

    public boolean extendsClass(OrbitClass clazz) {
        if (this.equals(clazz)) {
            return true;
        }

        if (this.superClasses != null) {
            for (OrbitClass superClass : this.superClasses) {
                if (superClass.extendsClass(clazz)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OrbitClass clazz) {
            return clazz.name().equals(this.name);
        }

        return false;
    }

    public OrbitObject createInstance(List<Object> args, GlobalContext rootContext) {
        return new OrbitObject(this, args, rootContext);
    }
}
