package com.softlocked.orbit.interpreter.ast.loops.forloops;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.interpreter.ast.variable.DecVarASTNode;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;
import com.softlocked.orbit.utils.list.ParallelList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public record ForInASTNode(ASTNode init, ASTNode iterable, ASTNode body) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        LocalContext forContext = new LocalContext(context);

        Variable variable = (Variable) this.init().evaluate(forContext);
        Object iterable = this.iterable().evaluate(forContext);

        if (iterable instanceof ParallelList list) {
            variable.setType(Variable.Type.ANY);

            int size = list.size();
            final int[] finishedIterations = {0};
            int threadCount = list.getAllocatedThreads(); // add the extra thread for the main thread, which will not be counted in the loop
            for (int i = 0; i < threadCount; i++) {
                int finalI = i;
                Thread thread = new Thread(() -> {
                    LocalContext threadContext = new LocalContext(context);

                    Variable threadVariable = new Variable(Variable.Type.ANY, null);
                    threadContext.addVariable(((DecVarASTNode) init).variableName(), threadVariable);

                    for (int j = finalI; j < size; j += threadCount) {
                        if (finishedIterations[0] >= size) {
                            return;
                        }
                        variable.setValue(list.get(j));

                        this.body().evaluate(forContext);
                        finishedIterations[0]++;
                    }
                });
                thread.start();
            }

            return null;
        } else
        if (iterable instanceof List<?> list) {
            variable.setType(Variable.Type.ANY);
            for (Object item : list) {
                variable.setValue(item);

                Object result = this.body().evaluate(forContext);
                if (result instanceof Breakpoint breakpoint) {
                    if(breakpoint.getType() == Breakpoint.Type.BREAK) {
                        return null;
                    }
                    if(breakpoint.getType() == Breakpoint.Type.RETURN || breakpoint.getType() == Breakpoint.Type.YIELD) {
                        return breakpoint;
                    }
                }
            }
        } else if (iterable instanceof String s) {
            variable.setType(Variable.Type.STRING);
            for (int i = 0; i < s.length(); i++) {
                variable.setValue(s.charAt(i));

                Object result = this.body().evaluate(forContext);
                if (result instanceof Breakpoint breakpoint) {
                    if(breakpoint.getType() == Breakpoint.Type.BREAK) {
                        return null;
                    }
                    if(breakpoint.getType() == Breakpoint.Type.RETURN || breakpoint.getType() == Breakpoint.Type.YIELD) {
                        return breakpoint;
                    }
                }
            }
        } else if (iterable instanceof Map<?, ?> m) {
            variable.setType(Variable.Type.ANY);
            for (Object key : m.keySet()) {
                variable.setValue(key);

                Object result = this.body().evaluate(forContext);
                if (result instanceof Breakpoint breakpoint) {
                    if(breakpoint.getType() == Breakpoint.Type.BREAK) {
                        return null;
                    }
                    if(breakpoint.getType() == Breakpoint.Type.RETURN || breakpoint.getType() == Breakpoint.Type.YIELD) {
                        return breakpoint;
                    }
                }
            }
        } else if (iterable instanceof Object[]) {
            variable.setType(Variable.Type.ANY);
            for (Object item : (Object[]) iterable) {
                variable.setValue(item);

                Object result = this.body().evaluate(forContext);
                if (result instanceof Breakpoint breakpoint) {
                    if(breakpoint.getType() == Breakpoint.Type.BREAK) {
                        return null;
                    }
                    if(breakpoint.getType() == Breakpoint.Type.RETURN || breakpoint.getType() == Breakpoint.Type.YIELD) {
                        return breakpoint;
                    }
                }
            }
        } else {
            throw new RuntimeException("Cannot iterate over " + iterable.getClass().getSimpleName());
        }
        return null;
    }
}
