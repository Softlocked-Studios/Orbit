package com.softlocked.orbit.opm.ast.pkg;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.interpreter.ast.generic.ImportASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.lexer.Lexer;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.opm.packager.OrbitPackage;
import com.softlocked.orbit.parser.Parser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public record ImportModuleASTNode(String moduleName) implements ImportASTNode {
    @Override
    public Object evaluate(ILocalContext context) {
        GlobalContext globalContext = (GlobalContext) context.getRoot();

        importFile(globalContext, globalContext.getPackagePath());

        return null;
    }

    @Override
    public void importFile(GlobalContext globalContext, String parentPath) {
        try {
            String modulesFolder = globalContext.getPackagePath();

            String metadataFile = modulesFolder + File.separator + moduleName + File.separator + "metadata.yml";

            byte[] bytes = Files.readAllBytes(Paths.get(metadataFile));
            String metadata = new String(bytes);

            OrbitPackage orbitPackage = OrbitPackage.fromYaml(metadata);

            String entrypoint = orbitPackage.entrypoint();

            String entrypointPath = modulesFolder + File.separator + moduleName + File.separator + entrypoint;

            byte[] entrypointBytes = Files.readAllBytes(Paths.get(entrypointPath));
            String entrypointCode = new String(entrypointBytes);

            List<String> tokens = new Lexer(entrypointCode).tokenize();

            ASTNode ast = Parser.parse(tokens, globalContext);

            globalContext.importModule(ast, modulesFolder + File.separator + moduleName);
        } catch (Exception e) {
            throw new RuntimeException("Error while importing module " + this.moduleName(), e);
        }
    }
}
