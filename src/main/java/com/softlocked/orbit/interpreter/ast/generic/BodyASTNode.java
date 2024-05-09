package com.softlocked.orbit.interpreter.ast.generic;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.memory.ILocalContext;

import java.util.Arrays;
import java.util.List;

public class BodyASTNode implements ASTNode {
    ASTNode[] statements;

    public BodyASTNode() {
        this.statements = new ASTNode[0];
    }

    public BodyASTNode(List<ASTNode> statements) {
        this.statements = statements.toArray(new ASTNode[0]);
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

    public Object evaluateFrom(ILocalContext context, ASTNode from) {
        int index = 0;
        for (ASTNode node : statements) {
            if (node == from) {
                break;
            }
            index++;
        }

        for (int i = index+1; i < statements.length; i++) {
            ASTNode node = statements[i];

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

    @Override
    public String toString() {
        return "BodyASTNode{" +
                "statements=" + Arrays.toString(statements) +
                '}';
    }
}
