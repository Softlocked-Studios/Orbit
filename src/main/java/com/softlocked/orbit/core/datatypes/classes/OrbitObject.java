package com.softlocked.orbit.core.datatypes.classes;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.interpreter.function.ClassConstructor;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.memory.LocalContext;
import com.softlocked.orbit.utils.Pair;

import java.util.HashMap;
import java.util.List;

public class OrbitObject {
    private final GlobalContext rootContext;

    private final OrbitClass clazz;

    private final HashMap<String, Variable> fields = new HashMap<>();

    public OrbitObject(OrbitClass clazz, List<Object> args, GlobalContext rootContext) {
        this.rootContext = rootContext;
        this.clazz = clazz;

        LocalContext context = new LocalContext(rootContext);
        for (String field : clazz.fields().keySet()) {
            Pair<Variable.Type, ASTNode> fieldData = clazz.fields().get(field);
            if (fieldData.second != null) {
                fields.put(field, new Variable(fieldData.first, fieldData.second.evaluate(context)));
            } else {
                fields.put(field, new Variable(fieldData.first, null));
            }
        }

        fields.put("this", new Variable(Variable.Type.CLASS, this));

        if (args != null) {
            HashMap<Integer, ClassConstructor> constructors = clazz.constructors();

            if(constructors.isEmpty() && args.isEmpty()) {
                return;
            }

            if (constructors.containsKey(args.size())) {
                // Add the fields to the context
                for (String field : fields.keySet()) {
                    context.addVariable(field, fields.get(field));
                }

                constructors.get(args.size()).call(new LocalContext(context), args);
            } else {
                throw new RuntimeException("No constructor found for " + clazz.name() + " with " + args.size() + " arguments");
            }
        }
    }

    public OrbitClass getClazz() {
        return clazz;
    }

    public Variable getField(String name) {
        return fields.get(name);
    }

    public Object callFunction(String name, List<Object> args) {
        return callFunction(name, args, false);
    }

    public Object callFunction(String name, List<Object> args, boolean superCall) {
        HashMap<Pair<String, Integer>, IFunction> functions = clazz.functions();

        IFunction func = null;

        if(!superCall) {
            func = functions.get(new Pair<>(name, args.size()));
        }

        if (func != null) {
            LocalContext context = new LocalContext(rootContext);

            // Add the fields to the context
            for (String field : fields.keySet()) {
                context.addVariable(field, fields.get(field));
            }

            return func.call(context, args);
        }

        if (clazz.superClasses() != null) {
            for (OrbitClass superClass : clazz.superClasses()) {
                func = superClass.functions().get(new Pair<>(name, args.size()));

                if (func != null) {
                    LocalContext context = new LocalContext(rootContext);

                    // Add the fields to the context
                    for (String field : fields.keySet()) {
                        context.addVariable(field, fields.get(field));
                    }

                    return func.call(context, args);
                }
            }
        }

        throw new RuntimeException("No function " + clazz.name() + ":" + name + " with " + args.size() + " arguments found");
    }

    public boolean hasFunction(String name, int argsCount) {
        HashMap<Pair<String, Integer>, IFunction> functions = clazz.functions();

        if (functions.containsKey(new Pair<>(name, argsCount))) {
            return true;
        }

        if (clazz.superClasses() != null) {
            for (OrbitClass superClass : clazz.superClasses()) {
                if (superClass.functions().containsKey(new Pair<>(name, argsCount))) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasFunction(IFunction function) {
        if (clazz.functions().containsValue(function)) {
            return true;
        }

        if (clazz.superClasses() != null) {
            for (OrbitClass superClass : clazz.superClasses()) {
                if (superClass.functions().containsValue(function)) {
                    return true;
                }
            }
        }

        return false;
    }

    public IFunction getFunction(String name, int argsCount) {
        HashMap<Pair<String, Integer>, IFunction> functions = clazz.functions();

        if (functions.containsKey(new Pair<>(name, argsCount))) {
            return functions.get(new Pair<>(name, argsCount));
        }

        if (clazz.superClasses() != null) {
            for (OrbitClass superClass : clazz.superClasses()) {
                if (superClass.functions().containsKey(new Pair<>(name, argsCount))) {
                    return superClass.functions().get(new Pair<>(name, argsCount));
                }
            }
        }

        return null;
    }

    public Object callFunction(IFunction function, List<Object> args) {
        return callFunction(function, args, false);
    }

    public Object callFunction(IFunction function, List<Object> args, boolean superCall) {
        LocalContext context = new LocalContext(rootContext);

        // Add the fields to the context
        for (String field : fields.keySet()) {
            context.addVariable(field, fields.get(field));
        }

        return function.call(context, args);
    }

    @Override
    public String toString() {
        String res = super.toString();

        // Split by @ and add with the class name
        String[] split = res.split("@");
        return clazz.name() + "@" + split[1];
    }

    public Object clone() {
        OrbitObject obj = new OrbitObject(clazz, null, rootContext);

        for (String field : fields.keySet()) {
            obj.fields.put(field, fields.get(field));
        }

        return obj;
    }
}
