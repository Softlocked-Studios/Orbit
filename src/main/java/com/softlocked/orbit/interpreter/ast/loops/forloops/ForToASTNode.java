package com.softlocked.orbit.interpreter.ast.loops.forloops;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;

public record ForToASTNode(ASTNode init, ASTNode end, ASTNode body) implements ASTNode {
@Override
    public Object evaluate(ILocalContext context) {
        LocalContext forContext = new LocalContext(context);

        Variable variable = (Variable) this.init().evaluate(forContext);

        long start = ((Number) variable.getValue()).longValue();
        long end = ((Number) this.end().evaluate(forContext)).longValue();

        for (long i = start; i <= end; i++) {
            variable.setValue(i);

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
        return null;
    }
}
