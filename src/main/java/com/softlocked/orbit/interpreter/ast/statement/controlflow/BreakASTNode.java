package com.softlocked.orbit.interpreter.ast.statement.controlflow;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.classes.OrbitObject;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.core.exception.InternalException;
import com.softlocked.orbit.memory.ILocalContext;

public record BreakASTNode(Breakpoint.Type type, ASTNode value) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        if(type == Breakpoint.Type.THROW) {
            Object value = this.value.evaluate(context);

            if(!(value instanceof OrbitObject exception) || !exception.getClazz().extendsClass(context.getRoot().getClassType("exception"))) {
                throw new RuntimeException("Attempted to throw a non-exception object.");
            }

            throw new InternalException(exception);
        }
        if(value != null) {
            return new Breakpoint(type, value.evaluate(context), this, context);
        } else {
            return new Breakpoint(type, null, this, context);
        }
    }
}
