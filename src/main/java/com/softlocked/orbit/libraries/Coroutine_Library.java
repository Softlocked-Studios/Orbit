package com.softlocked.orbit.libraries;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.Consumer;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.function.coroutine.Coroutine;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;
import com.softlocked.orbit.utils.list.CoroutineList;

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

        context.addFunction(new NativeFunction("coroutine.hasNext", List.of(Variable.Type.COROUTINE), Variable.Type.BOOL) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                return !((Coroutine) args.get(0)).isFinished();
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
                Coroutine coroutine = (Coroutine) args.get(0);

                return new CoroutineList(coroutine);
            }
        });

        context.addFunction(new NativeFunction("coroutine.then", List.of(Variable.Type.COROUTINE, Variable.Type.CONSUMER), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                Coroutine coroutine = (Coroutine) args.get(0);

                Consumer consumer = (Consumer) args.get(1);

                coroutine.addConsumer(consumer);

                return null;
            }
        });

        // Async
        context.addFunction(new NativeFunction("coroutine.async", List.of(Variable.Type.COROUTINE), Variable.Type.ANY) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                Coroutine coroutine = (Coroutine) args.get(0);

                coroutine.setAsync(true);

                Thread thread = new Thread(() -> {
                    while (!coroutine.isFinished()) {
                        if (coroutine.getReturnValue() == null) {
                            coroutine.resume();
                        }
                    }
                });

                thread.start();

                return null;
            }
        });

        // Await
        context.addFunction(new NativeFunction("coroutine.await", List.of(Variable.Type.COROUTINE), Variable.Type.ANY) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                Coroutine coroutine = (Coroutine) args.get(0);

                while (coroutine.getReturnValue() == null) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Object value = coroutine.getReturnValue().get();
                coroutine.resetReturnValue();

                return value;
            }
        });
    }
}
