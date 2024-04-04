package com.softlocked.orbit.libraries;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.function.coroutine.Coroutine;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;
import com.softlocked.orbit.utils.CoroutineList;

import java.util.List;

public class Coroutine_Library implements OrbitJavaLibrary {
    @Override
    public void load(GlobalContext context) {
        context.addFunction(new NativeFunction("coroutine.resume", List.of(Variable.Type.COROUTINE), Variable.Type.ANY) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((Coroutine) args.get(0)).resume();
            }
        });

        context.addFunction(new NativeFunction("coroutine.isFinished", List.of(Variable.Type.COROUTINE), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return ((Coroutine) args.get(0)).isFinished();
            }
        });

        context.addFunction(new NativeFunction("coroutine.clone", List.of(Variable.Type.COROUTINE), Variable.Type.COROUTINE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                LocalContext newContext = new LocalContext(context.getRoot());
                Coroutine coroutine = (Coroutine) args.get(0);

                return coroutine.getFunction().call(newContext, coroutine.getArgs());
            }
        });

        context.addFunction(new NativeFunction("coroutine.iterator", List.of(Variable.Type.COROUTINE), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return new CoroutineList((Coroutine) args.get(0));
            }
        });
    }
}
