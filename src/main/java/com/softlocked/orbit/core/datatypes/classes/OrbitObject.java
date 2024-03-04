package com.softlocked.orbit.core.datatypes.classes;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;
import com.softlocked.orbit.utils.Pair;

import java.util.HashMap;
import java.util.List;

public class OrbitObject {
    private final ILocalContext rootContext;

    private final OrbitClass clazz;

    private final HashMap<String, Variable> fields = new HashMap<>();

    public OrbitObject(OrbitClass clazz, List<Object> args, ILocalContext rootContext) {
        this.rootContext = rootContext;
        this.clazz = clazz;

        for (String field : clazz.getFields().keySet()) {
            fields.put(field, new Variable(clazz.getFields().get(field), null));
        }

        fields.put("this", new Variable(Variable.Type.CLASS, this));

        if (args != null) {
            HashMap<Integer, IFunction> constructors = clazz.getConstructors();

            if(constructors.isEmpty() && args.isEmpty()) {
                return;
            }

            if (constructors.containsKey(args.size())) {
                LocalContext context = new LocalContext(rootContext);

                // Add the fields to the context
                for (String field : fields.keySet()) {
                    context.addVariable(field, fields.get(field));
                }

                constructors.get(args.size()).call(new LocalContext(rootContext), args);
            } else {
                throw new RuntimeException("No constructor found for " + clazz.getName() + " with " + args.size() + " arguments");
            }
        }
    }

    public OrbitClass getClazz() {
        return clazz;
    }

    public Variable getField(String name) {
        return fields.get(name);
    }

    public void setField(String name, Variable value) {
        fields.put(name, value);
    }

    public HashMap<String, Variable> getFields() {
        return fields;
    }

    public Object callFunction(String name, List<Object> args) {
        HashMap<Pair<String, Integer>, IFunction> functions = clazz.getFunctions();

        if (functions.containsKey(new Pair<>(name, args.size()))) {
            LocalContext context = new LocalContext(rootContext);

            // Add the fields to the context
            for (String field : fields.keySet()) {
                context.addVariable(field, fields.get(field));
            }

            return functions.get(new Pair<>(name, args.size())).call(context, args);
        } else {
            throw new RuntimeException("No function found for " + clazz.getName() + " with name " + name + " and " + args.size() + " arguments");
        }
    }

    @Override
    public String toString() {
        return clazz.getName() + "(fields=" + fields + ")";
    }
}
