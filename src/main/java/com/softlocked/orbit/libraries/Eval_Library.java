package com.softlocked.orbit.libraries;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.lexer.Lexer;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.parser.Parser;

import java.util.List;

public class Eval_Library implements OrbitJavaLibrary {
    @Override
    public void load(GlobalContext context) {
        // Function for deleting functions
        context.addFunction(new NativeFunction("unload", List.of(Variable.Type.STRING, Variable.Type.INT), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                context.getRoot().removeFunction((String) args.get(0), (int) args.get(1));

                return null;
            }
        });

        // Function for deleting classes
        context.addFunction(new NativeFunction("unload", List.of(Variable.Type.STRING), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                context.getRoot().removeClass((String) args.get(0));

                return null;
            }
        });

        // eval function
        context.addFunction(new NativeFunction("eval", List.of(Variable.Type.STRING), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                String code = (String) args.get(0);

                try {
                    List<String> tokens = new Lexer(code).tokenize();

                    ASTNode program = Parser.parse(tokens, context.getRoot());

                    Object result = program.evaluate(context.getParent());

                    if(result instanceof Variable) {
                        return null;
                    } else {
                        return result;
                    }
                } catch (Throwable e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        });
    }
}
