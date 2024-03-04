package com.softlocked.orbit.interpreter.memory;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.classes.OrbitClass;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.java.JarLoader;
import com.softlocked.orbit.lexer.Lexer;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.memory.LocalContext;
import com.softlocked.orbit.opm.ast.pkg.ImportFileASTNode;
import com.softlocked.orbit.opm.ast.pkg.ImportModuleASTNode;
import com.softlocked.orbit.parser.Parser;
import com.softlocked.orbit.utils.Pair;
import com.softlocked.orbit.interpreter.ast.generic.BodyASTNode;
import com.softlocked.orbit.interpreter.ast.generic.ImportASTNode;
import com.softlocked.orbit.interpreter.ast.object.ClassDefinitionASTNode;
import com.softlocked.orbit.interpreter.ast.variable.DecVarASTNode;
import com.softlocked.orbit.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * The global context used to store global variables, functions, and classes.
 * It is also used as the root to all local contexts.
 * @see LocalContext
 */
public class GlobalContext extends LocalContext {
    private final HashMap<Pair<String, Integer>, IFunction> functions = new HashMap<>();

    private final String parentPath;
    private final String packagePath;

    private static final HashMap<String, Class<?>> primitives = new HashMap<>();
    private final HashMap<String, OrbitClass> classes = new HashMap<>();

    private final HashSet<String> importedModules = new HashSet<>();
    private final HashSet<String> importedFiles = new HashSet<>();

    static {
        primitives.put("int", int.class);
        primitives.put("float", float.class);
        primitives.put("double", double.class);
        primitives.put("long", long.class);
        primitives.put("short", short.class);
        primitives.put("byte", byte.class);
        primitives.put("char", char.class);
        primitives.put("bool", boolean.class);
        primitives.put("string", String.class);

        primitives.put("void", void.class);

        primitives.put("var", Object.class);
        primitives.put("let", Object.class);
        primitives.put("object", Object.class);

        primitives.put("list", List.class);
        primitives.put("map", Map.class);
    }

    public static Class<?> getPrimitiveType(String name) {
        return primitives.get(name);
    }

    public OrbitClass getClassType(String name) {
        return classes.get(name);
    }

    @Override
    public IFunction getFunction(String name, int parameterCount) {
        IFunction func = functions.get(new Pair<>(name, parameterCount));

        if (func != null) {
            return func;
        }

        func = functions.get(new Pair<>(name, -1));

        return func;
    }

    @Override
    public void addFunction(IFunction function) {
        functions.put(new Pair<>(function.getName(), function.getParameterCount()), function);
    }

    public void addClass(OrbitClass orbitClass) {
        classes.put(orbitClass.getName(), orbitClass);
    }

    public GlobalContext(String parentPath, String packagePath) {
        super();

        this.parentPath = parentPath;
        this.packagePath = packagePath;

        addFunction(new NativeFunction("print", -1, Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                StringJoiner joiner = new StringJoiner(" ");

                for (Object arg : args) {
                    joiner.add(Utils.cast(arg, String.class) + "");
                }

                System.out.println(joiner);

                return null;
            }
        });

        addFunction(new NativeFunction("system.load", List.of(Variable.Type.STRING), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                String path = (String) args.get(0);

                JarLoader.loadLibrary(context.getRoot(), path);

                return null;
            }
        });
    }

    public String getPackagePath() {
        return packagePath;
    }

    public String getParentPath() {
        return parentPath;
    }


    public void importFile(String path) throws IOException, ParsingException {
        byte[] file = Files.readAllBytes(Paths.get(path));
        String code = new String(file);

        List<String> tokens = new Lexer(code).tokenize();

        ASTNode ast = Parser.parse(tokens, this);

        importModule(ast, new File(path).getParent());
    }

    public void importModule(ASTNode ast, String path) {
        if(ast instanceof IFunction || ast instanceof ClassDefinitionASTNode || ast instanceof DecVarASTNode) {
            ast.evaluate(this);
        } else if(ast instanceof ImportASTNode) {
            if(ast instanceof ImportFileASTNode importFileASTNode) {
                if(importedFiles.contains(path + File.separator + importFileASTNode.fileName())) {
                    return;
                }

                importedFiles.add(path + File.separator + importFileASTNode.fileName());

                importFileASTNode.importFile(this, path);
            }
            else if(ast instanceof ImportModuleASTNode importModuleASTNode) {
                if(importedModules.contains(importModuleASTNode.moduleName())) {
                    return;
                }

                importedModules.add(importModuleASTNode.moduleName());

                importModuleASTNode.importFile(this, path);
            }
        }
        else if(ast instanceof BodyASTNode body) {
            for(ASTNode node : body.statements()) {
                if(node instanceof IFunction || node instanceof ClassDefinitionASTNode || node instanceof DecVarASTNode) {
                    node.evaluate(this);
                }
                else if(node instanceof ImportASTNode) {
                    if(node instanceof ImportFileASTNode importFileASTNode) {
                        if(importedFiles.contains(path + File.separator + importFileASTNode.fileName())) {
                            return;
                        }

                        importedFiles.add(path + File.separator + importFileASTNode.fileName());

                        importFileASTNode.importFile(this, path);
                    }
                    else if(node instanceof ImportModuleASTNode importModuleASTNode) {
                        if(importedModules.contains(importModuleASTNode.moduleName())) {
                            return;
                        }

                        importedModules.add(importModuleASTNode.moduleName());

                        importModuleASTNode.importFile(this, path);
                    }
                }
            }
        }
    }
}
