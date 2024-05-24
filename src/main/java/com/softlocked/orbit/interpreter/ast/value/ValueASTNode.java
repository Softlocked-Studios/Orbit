package com.softlocked.orbit.interpreter.ast.value;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Utils;

public record ValueASTNode(Object value) implements ASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        try {
            if (this.value() instanceof String) {
                return Utils.formatString((String) this.value(), context);
            }
            return this.value();
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating value: " + e.getMessage());
        }
    }
}
