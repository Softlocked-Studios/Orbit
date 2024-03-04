package com.softlocked.orbit.interpreter.ast.generic;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.memory.ILocalContext;

public interface ImportASTNode extends ASTNode {
    void importFile(GlobalContext globalContext, String parentPath);

    default Object evaluate(ILocalContext context) {
        GlobalContext globalContext = (GlobalContext) context.getRoot();

        importFile(globalContext, globalContext.getParentPath());

        return null;
    }
}
