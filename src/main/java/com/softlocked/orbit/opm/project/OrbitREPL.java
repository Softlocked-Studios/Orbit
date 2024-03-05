package com.softlocked.orbit.opm.project;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.lexer.Lexer;
import com.softlocked.orbit.parser.Parser;

import java.util.List;

public class OrbitREPL implements Runner {
    GlobalContext context;
    Lexer lexer = new Lexer("");

    public OrbitREPL(GlobalContext context) {
        this.context = context;
    }

    public void run(String input) {
        try {
            lexer.reset(input);
            List<String> tokens = lexer.tokenize();

            ASTNode node = Parser.parse(tokens, context);

            Object result = node.evaluate(context);

            if(result != null && !(result instanceof Variable)) {
                System.out.println(result);
            }
        } catch (Exception | Error e) {
            System.err.println("\u001B[31m[ERROR]\u001B[0m " + e.getMessage());
        }
    }
}
