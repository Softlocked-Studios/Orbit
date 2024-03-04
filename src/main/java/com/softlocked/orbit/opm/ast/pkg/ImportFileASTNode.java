package com.softlocked.orbit.opm.ast.pkg;

import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.interpreter.ast.generic.ImportASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;

public record ImportFileASTNode(String fileName) implements ImportASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        GlobalContext globalContext = context.getRoot();

        try {
            globalContext.importFile(this.fileName());
        } catch (Exception e) {
            throw new RuntimeException("Error while importing file " + this.fileName(), e);
        }

        return null;
    }

    @Override
    public void importFile(GlobalContext globalContext, String parentPath) {
        try {
            String path = parentPath + "/" + fileName;
            globalContext.importFile(path);
        } catch (Exception e) {
            throw new RuntimeException("Error while importing file " + fileName + " (probably not found?)", e);
        }
    }
}
