package com.softlocked.orbit.interpreter.ast.generic;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.memory.ILocalContext;

import java.util.List;

public class BodyASTNode implements ASTNode {
    ASTNode[] statements;

    public BodyASTNode() {
        this.statements = new ASTNode[0];
    }

    @Override
    public Object evaluate(ILocalContext context) {
        for (ASTNode node : statements) {
            Object result = node.evaluate(context);

            if (result instanceof Breakpoint) {
                return result;
            }
        }
        return null;
    }

    public ASTNode[] statements() {
        return statements;
    }

    public void addNode(ASTNode node) {
        ASTNode[] newStatements = new ASTNode[statements.length + 1];
        System.arraycopy(statements, 0, newStatements, 0, statements.length);
        newStatements[statements.length] = node;
        statements = newStatements;
    }
}
