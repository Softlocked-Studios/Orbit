package com.softlocked.orbit.interpreter.ast.statement.controlflow;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.memory.ILocalContext;

public record BreakASTNode(Breakpoint.Type type, ASTNode value) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        if(value != null) {
            return new Breakpoint(type, value.evaluate(context));
        } else {
            return new Breakpoint(type);
        }
    }
}
