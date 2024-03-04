package com.softlocked.orbit.core.datatypes.classes;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.interpreter.function.ClassConstructor;
import com.softlocked.orbit.utils.Pair;

import java.util.HashMap;
import java.util.List;

/**
 * Used for representing a class in the Orbit language
 * @see Variable
 */
public class OrbitClass {
    private final String name;
    private final List<OrbitClass> superClasses;

    private HashMap<String, Pair<Variable.Type, ASTNode>> fields;
    private HashMap<Pair<String, Integer>, IFunction> functions;

    private HashMap<Integer, ClassConstructor> constructors;

    public OrbitClass(String name, List<OrbitClass> superClasses, HashMap<String, Pair<Variable.Type, ASTNode>> fields, HashMap<Pair<String, Integer>, IFunction> functions, HashMap<Integer, ClassConstructor> constructors) {
        this.name = name;
        this.superClasses = superClasses;
        this.fields = fields;
        this.functions = functions;
        this.constructors = constructors;
    }

    public String getName() {
        return name;
    }

    public List<OrbitClass> getSuperClasses() {
        return superClasses;
    }

    public HashMap<String, Pair<Variable.Type, ASTNode>> getFields() {
        return fields;
    }

    public HashMap<Pair<String, Integer>, IFunction> getFunctions() {
        return functions;
    }

    public HashMap<Integer, ClassConstructor> getConstructors() {
        return constructors;
    }

    public void setFields(HashMap<String, Pair<Variable.Type, ASTNode>> fields) {
        this.fields = fields;
    }

    public void setFunctions(HashMap<Pair<String, Integer>, IFunction> functions) {
        this.functions = functions;
    }

    public void setConstructors(HashMap<Integer, ClassConstructor> constructors) {
        this.constructors = constructors;
    }

    public void addField(String name, Pair<Variable.Type, ASTNode> field) {
        fields.put(name, field);
    }

    public void addFunction(IFunction function) {
        functions.put(new Pair<>(function.getName(), function.getParameterCount()), function);
    }

    public void addConstructor(ClassConstructor constructor) {
        constructors.put(constructor.getParameterCount(), constructor);
    }
}
