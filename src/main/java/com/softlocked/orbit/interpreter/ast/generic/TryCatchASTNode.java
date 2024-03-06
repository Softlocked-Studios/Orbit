package com.softlocked.orbit.interpreter.ast.generic;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.classes.OrbitObject;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.core.exception.InternalException;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.memory.ILocalContext;

import java.util.List;

public record TryCatchASTNode(ASTNode tryBlock, ASTNode catchBlock, String exceptionName) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        try {
            Object result = tryBlock.evaluate(context);
            if (result instanceof Breakpoint breakpoint) {
                return breakpoint.getValue();
            }

            return result;
        } catch (InternalException e) {
            OrbitObject exception = e.getObject();
            context.addVariable(exceptionName, new Variable(Variable.Type.CLASS, exception));
            return catchBlock.evaluate(context);
        } catch (RuntimeException e) {
            OrbitObject exception = new OrbitObject(context.getRoot().getClassType("exception"), List.of(e.getMessage()), context.getRoot());
            context.addVariable(exceptionName, new Variable(Variable.Type.CLASS, exception));
            return catchBlock.evaluate(context);
        }
    }
}
