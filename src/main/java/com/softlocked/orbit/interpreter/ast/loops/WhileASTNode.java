package com.softlocked.orbit.interpreter.ast.loops;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.core.evaluator.Evaluator;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;

public record WhileASTNode(ASTNode condition, ASTNode body) implements ASTNode {

    @Override
    public Object evaluate(ILocalContext context) {
        LocalContext newContext = new LocalContext(context);

        while (Evaluator.toBool(this.condition().evaluate(newContext))) {
            Object result = this.body().evaluate(newContext);

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
