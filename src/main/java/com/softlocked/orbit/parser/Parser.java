package com.softlocked.orbit.parser;

import com.softlocked.orbit.core.ast.operation.OperationType;
import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.core.evaluator.Evaluator;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.interpreter.ast.generic.TryCatchASTNode;
import com.softlocked.orbit.interpreter.ast.object.ClassDefinitionASTNode;
import com.softlocked.orbit.interpreter.ast.object.DecObjASTNode;
import com.softlocked.orbit.interpreter.ast.operation.ReferenceASTNode;
import com.softlocked.orbit.interpreter.ast.value.ValueASTNode;
import com.softlocked.orbit.interpreter.ast.value.VariableASTNode;
import com.softlocked.orbit.interpreter.ast.variable.DeleteVarASTNode;
import com.softlocked.orbit.interpreter.function.ClassConstructor;
import com.softlocked.orbit.opm.ast.pkg.ImportFileASTNode;
import com.softlocked.orbit.opm.ast.pkg.ImportModuleASTNode;
import com.softlocked.orbit.utils.Pair;
import com.softlocked.orbit.utils.Utils;
import com.softlocked.orbit.interpreter.ast.generic.BodyASTNode;
import com.softlocked.orbit.interpreter.ast.loops.WhileASTNode;
import com.softlocked.orbit.interpreter.ast.loops.forloops.ForDowntoASTNode;
import com.softlocked.orbit.interpreter.ast.loops.forloops.ForInASTNode;
import com.softlocked.orbit.interpreter.ast.loops.forloops.ForToASTNode;
import com.softlocked.orbit.interpreter.ast.operation.OperationASTNode;
import com.softlocked.orbit.interpreter.ast.operation.TernaryASTNode;
import com.softlocked.orbit.interpreter.ast.statement.BranchASTNode;
import com.softlocked.orbit.interpreter.ast.statement.ConditionalASTNode;
import com.softlocked.orbit.interpreter.ast.statement.controlflow.BreakASTNode;
import com.softlocked.orbit.interpreter.ast.variable.AssignVarASTNode;
import com.softlocked.orbit.interpreter.ast.variable.DecVarASTNode;
import com.softlocked.orbit.interpreter.ast.variable.FunctionCallASTNode;
import com.softlocked.orbit.interpreter.function.OrbitFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.lexer.Lexer;

import java.util.*;

public class Parser {
    public static ASTNode parse(List<String> tokens, GlobalContext context) throws ParsingException {
        return parse(tokens, context, "");
    }

    private static ASTNode parse(List<String> tokens, GlobalContext context, String clazzName) throws ParsingException {
        BodyASTNode body = new BodyASTNode(new ArrayList<>());

        // try {
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            if (token.equals("\n") || token.equals("\r") || token.equals(";") || token.equals("{") || token.equals("}") || token.equals("end") || token.equals("else") || token.equals("then") || token.equals("do") || token.equals("does")) {
                continue;
            }

            // 0. Importing a module
            else if (token.equals("import")) {
                String next = getNext(tokens, i + 1);
                int nextIndex = findNext(tokens, i + 1, next);

                if(next == null) {
                    throw new ParsingException("Unexpected end of file");
                }

                if(next.equals("module")) {
                    Pair<List<String>, Integer> expression = fetchExpression(tokens, nextIndex + 1);

                    if(expression.first.size() != 1) {
                        throw new ParsingException("Invalid module name");
                    }

                    String moduleName = expression.first.get(0);

                    if(moduleName.startsWith("\"") && moduleName.endsWith("\"")) {
                        moduleName = moduleName.substring(1, moduleName.length() - 1);
                    }

                    body.addNode(new ImportModuleASTNode(
                            moduleName
                    ));

                    i = expression.second;

                    continue;
                }
                else {
                    if(next.startsWith("\"") && next.endsWith("\"")) {
                        next = next.substring(1, next.length() - 1);
                    }

                    body.addNode(new ImportFileASTNode(
                            next
                    ));

                    i = nextIndex;

                    continue;
                }
            }

            else if (token.equals("delete")) {
                String next = getNext(tokens, i + 1);
                int nextIndex = findNext(tokens, i + 1, next);

                if(next == null) {
                    throw new ParsingException("Unexpected end of file");
                }

                if(next.startsWith("\"") && next.endsWith("\"")) {
                    next = next.substring(1, next.length() - 1);
                }

                body.addNode(new DeleteVarASTNode(next));

                i = nextIndex;

                continue;
            }

            else if(token.equals("return")) {
                String next = getNext(tokens, i + 1);
                int nextIndex = findNext(tokens, i + 1, next);

                if(next == null || next.equals(";")) {
                    body.addNode(new BreakASTNode(Breakpoint.Type.RETURN, null));

                    i = nextIndex;
                    continue;
                }

                Pair<List<String>, Integer> expression = fetchExpression(tokens, nextIndex);

                List<String> postfix = infixToPostfix(expression.first);

                body.addNode(new BreakASTNode(Breakpoint.Type.RETURN, postfixToAST(postfix, context)));

                i = expression.second;

                continue;
            }

            else if(token.equals("throw")) {
                String next = getNext(tokens, i + 1);
                int nextIndex = findNext(tokens, i + 1, next);

                if(next == null || next.equals(";")) {
                    throw new ParsingException("Invalid throw statement");
                }

                Pair<List<String>, Integer> expression = fetchExpression(tokens, nextIndex);

                List<String> postfix = infixToPostfix(expression.first);

                body.addNode(new BreakASTNode(Breakpoint.Type.THROW, postfixToAST(postfix, context)));

                i = expression.second;

                continue;
            }

            else if(token.equals("break")) {
                body.addNode(new BreakASTNode(Breakpoint.Type.BREAK, null));
            }

            else if(token.equals("continue")) {
                body.addNode(new BreakASTNode(Breakpoint.Type.CONTINUE, null));
            }

            // try... catch statement
            else if (token.equals("try")) {
                String next = getNext(tokens, i + 1);
                int nextIndex = findNext(tokens, i + 1, next);

                if(next == null) {
                    throw new ParsingException("Unexpected end of file");
                }

                // C-style
                if(next.equals("{")) {
                    int bodyEnd = getBodyEnd(tokens, nextIndex, "{");

                    if(bodyEnd == -1) {
                        throw new ParsingException("Unexpected end of file");
                    } else if(bodyEnd == 0) {
                        throw new ParsingException("Invalid try block");
                    }

                    List<String> bodyTokens = tokens.subList(nextIndex + 1, bodyEnd - 1);

                    ASTNode tryBody = parse(bodyTokens, context);

                    i = bodyEnd - 1;

                    next = getNext(tokens, i + 1);
                    nextIndex = findNext(tokens, i + 1, next);

                    if(next == null) {
                        throw new ParsingException("Unexpected end of file");
                    }

                    if(!next.equals("catch")) {
                        throw new ParsingException("Invalid try block");
                    }

                    next = getNext(tokens, nextIndex + 1);
                    nextIndex = findNext(tokens, nextIndex + 1, next);

                    if(next == null) {
                        throw new ParsingException("Unexpected end of file");
                    }

                    // Get the exception name
                    String exceptionName;

                    if(next.equals("(")) {
                        next = getNext(tokens, nextIndex + 1);
                        nextIndex = findNext(tokens, nextIndex + 1, next);

                        if(next == null) {
                            throw new ParsingException("Unexpected end of file");
                        }

                        if(next.equals(")")) {
                            throw new ParsingException("Invalid catch block");
                        }

                        exceptionName = next;

                        next = getNext(tokens, nextIndex + 1);
                        nextIndex = findNext(tokens, nextIndex + 1, next);
                    } else {
                        exceptionName = "e";
                    }

                    next = getNext(tokens, nextIndex + 1);
                    nextIndex = findNext(tokens, nextIndex + 1, next);

                    if(next == null) {
                        throw new ParsingException("Unexpected end of file");
                    }

                    int catchBodyEnd = getBodyEnd(tokens, nextIndex, "{");

                    if(catchBodyEnd == -1) {
                        throw new ParsingException("Unexpected end of file");
                    } else if(catchBodyEnd == 0) {
                        throw new ParsingException("Invalid catch block");
                    }

                    List<String> catchBodyTokens = tokens.subList(nextIndex + 1, catchBodyEnd - 1);

                    body.addNode(new TryCatchASTNode(
                            tryBody,
                            parse(catchBodyTokens, context),
                            exceptionName
                    ));

                    i = catchBodyEnd - 1;

                    continue;
                }

                // Lua-style
                else {
                    int pair = getPair(tokens, i, "try", "catch");

                    if(pair == -1) {
                        throw new ParsingException("Unexpected end of file");
                    } else if(pair == 0) {
                        throw new ParsingException("Invalid try block");
                    }

                    List<String> tryTokens = tokens.subList(i + 1, pair);

                    ASTNode tryBody = parse(tryTokens, context);

                    i = pair;

                    next = getNext(tokens, i + 1);
                    nextIndex = findNext(tokens, i + 1, next);

                    if(next == null) {
                        throw new ParsingException("Unexpected end of file");
                    }

                    String exceptionName;

                    if(next.equals("(")) {
                        next = getNext(tokens, i + 2);
                        nextIndex = findNext(tokens, i + 2, next);

                        if(next == null) {
                            throw new ParsingException("Unexpected end of file");
                        }

                        if(next.equals(")")) {
                            throw new ParsingException("Invalid catch block");
                        }

                        exceptionName = next;

                        next = getNext(tokens, nextIndex + 1);
                        nextIndex = findNext(tokens, nextIndex + 1, next);
                    } else {
                        exceptionName = "e";
                    }

                    next = getNext(tokens, nextIndex + 1);
                    nextIndex = findNext(tokens, nextIndex + 1, next);

                    int bodyEnd = getBodyEnd(tokens, pair, "catch", "does", "do", "then");

                    if(bodyEnd == -1) {
                        throw new ParsingException("Unexpected end of file");
                    } else if(bodyEnd == 0) {
                        throw new ParsingException("Invalid catch block");
                    }

                    List<String> catchTokens = tokens.subList(nextIndex, bodyEnd - 1);

                    body.addNode(new TryCatchASTNode(
                            tryBody,
                            parse(catchTokens, context),
                            exceptionName
                    ));

                    i = bodyEnd - 1;

                    continue;
                }
            }

            else if (token.equals("class")) {
                String className = getNext(tokens, i + 1);
                int classNameIndex = findNext(tokens, i + 1, className);

                if(className == null) {
                    throw new ParsingException("Unexpected end of file");
                }

                if(!className.matches(Utils.IDENTIFIER_REGEX) || Utils.isKeyword(className)) {
                    throw new ParsingException("Invalid class name " + className);
                }

                String next = getNext(tokens, classNameIndex + 1);
                int nextIndex = findNext(tokens, classNameIndex + 1, next);

                if(next == null) {
                    throw new ParsingException("Unexpected end of file");
                }

                // Check either for { or : / extends
                List<String> superClasses = new ArrayList<>();
                ASTNode bodyNode = null;

                try {
                    if (next.equals(":") || next.equals("extends")) {
                        int bodyStart = findNext(tokens, nextIndex + 1, "{");

                        if (bodyStart == -1) {
                            throw new ParsingException("Unexpected end of file");
                        }

                        superClasses = new ArrayList<>(tokens.subList(nextIndex + 1, bodyStart));
                        superClasses.removeIf(s -> s.equals(","));

                        int bodyEnd = getPair(tokens, bodyStart, "{", "}");

                        if (bodyEnd == -1) {
                            throw new ParsingException("Unexpected end of file");
                        }

                        bodyNode = parse(tokens.subList(bodyStart + 1, bodyEnd), context, className);

                        i = bodyEnd;
                    } else if (next.equals("{")) {
                        int bodyEnd = getPair(tokens, nextIndex, "{", "}");

                        if (bodyEnd == -1) {
                            throw new ParsingException("Unexpected end of file");
                        }

                        bodyNode = parse(tokens.subList(nextIndex + 1, bodyEnd), context, className);

                        i = bodyEnd;
                    } else {
                        throw new ParsingException("Invalid class declaration");
                    }

                    // Now go through the body. If there's function or variable declarations, add them to the class
                    // If it's anything else, throw an error
                    HashMap<String, Pair<Variable.Type, ASTNode>> fields = new HashMap<>();
                    HashMap<Pair<String, Integer>, IFunction> functions = new HashMap<>();
                    HashMap<Integer, ClassConstructor> constructors = new HashMap<>();

                    if (bodyNode instanceof BodyASTNode bd) {
                        for (ASTNode node : bd.statements()) {
                            if (node instanceof DecVarASTNode decVarASTNode) {
                                fields.put(decVarASTNode.variableName(), new Pair<>(decVarASTNode.type(), decVarASTNode.value()));
                            } else if (node instanceof ClassConstructor constructor) {
                                constructors.put(constructor.getParameterCount(), constructor);
                            } else if (node instanceof OrbitFunction function) {
                                functions.put(new Pair<>(function.getName(), function.getParameterCount()), function);
                            } else {
                                throw new ParsingException("Invalid class body");
                            }
                        }
                    } else if (bodyNode instanceof DecVarASTNode decVarASTNode) {
                        fields.put(decVarASTNode.variableName(), new Pair<>(decVarASTNode.type(), decVarASTNode.value()));
                    } else if (bodyNode instanceof ClassConstructor constructor) {
                        constructors.put(constructor.getParameterCount(), constructor);
                    } else if (bodyNode instanceof OrbitFunction function) {
                        functions.put(new Pair<>(function.getName(), function.getParameterCount()), function);
                    } else {
                        throw new ParsingException("Invalid class body");
                    }

                    ClassDefinitionASTNode classDef = new ClassDefinitionASTNode(
                            className,
                            superClasses,
                            fields,
                            functions,
                            constructors
                    );

                    body.addNode(classDef);

                    continue;
                } catch (ParsingException e) {
                    throw e;
                }
            }

            // 1. Variable Declaration
            else if (GlobalContext.getPrimitiveType(token) != null || token.equals("func")) {
                String identifier = getNext(tokens, i + 1);
                int identifierIndex = findNext(tokens, i + 1, identifier);

                if(identifier == null) {
                    throw new ParsingException("Unexpected end of file");
                }

                else if(OperationType.fromSymbol(identifier) != null || (identifier.matches(Utils.IDENTIFIER_REGEX) && !Utils.isKeyword(identifier))) {
                    if (identifierIndex < tokens.size()) {
                        String next = getNext(tokens, identifierIndex + 1);
                        int nextIndex = findNext(tokens, identifierIndex + 1, next);

                        if (next == null || next.equals(";")) {
                            if (token.equals("func"))
                                throw new ParsingException("Cannot declare function as a variable");

                            Variable.Type type = Variable.Type.fromJavaClass(GlobalContext.getPrimitiveType(token));

                            if (type == Variable.Type.LIST) {
                                body.addNode(new DecVarASTNode(
                                        identifier,
                                        new FunctionCallASTNode(
                                                "list.new",
                                                List.of()
                                        ),
                                        type
                                ));
                            } else if (type == Variable.Type.MAP) {
                                body.addNode(new DecVarASTNode(
                                        identifier,
                                        new FunctionCallASTNode(
                                                "map.new",
                                                List.of()
                                        ),
                                        type
                                ));
                            } else {
                                body.addNode(new DecVarASTNode(
                                        identifier,
                                        new ValueASTNode(Utils.newObject(GlobalContext.getPrimitiveType(token))),
                                        type
                                ));
                            }

                            i = next == null ? tokens.size() : nextIndex;

                            continue;
                        }

                        switch (next) {
                            case "=", "be" -> {
                                if (token.equals("func"))
                                    throw new ParsingException("Cannot declare function as a variable");

                                Pair<List<String>, Integer> expression = fetchExpression(tokens, nextIndex + 1);

                                List<String> postfix = infixToPostfix(expression.first);

                                body.addNode(new DecVarASTNode(
                                        identifier,
                                        postfixToAST(postfix, context),
                                        Variable.Type.fromJavaClass(GlobalContext.getPrimitiveType(token))
                                ));

                                i = expression.second;

                                continue;
                            }
                            case "(" -> {
                                // Function declaration
                                int end = findNext(tokens, nextIndex, ")");

                                List<String> params = tokens.subList(nextIndex + 1, end);

                                List<Pair<String, Variable.Type>> arguments = new ArrayList<>();

                                int nextComma = getNextSeparator(params, 0);

                                while (nextComma != -1) {
                                    List<String> subList = params.subList(0, nextComma);
                                    if (subList.size() == 1) {
                                        arguments.add(new Pair<>(subList.get(0), Variable.Type.ANY));
                                    } else if (subList.size() == 2) {
                                        Class<?> primitiveType = GlobalContext.getPrimitiveType(subList.get(0));
                                        Variable.Type type = primitiveType != null ? Variable.Type.fromJavaClass(primitiveType) : Variable.Type.CLASS;
                                        if (type == null) type = Variable.Type.CLASS;
                                        arguments.add(new Pair<>(subList.get(1), type));
                                    } else {
                                        throw new ParsingException("Invalid function declaration");
                                    }

                                    params = params.subList(nextComma + 1, params.size());
                                    nextComma = getNextSeparator(params, 0);
                                }

                                if (!params.isEmpty()) {
                                    if (params.size() == 1) {
                                        arguments.add(new Pair<>(params.get(0), Variable.Type.ANY));
                                    } else if (params.size() == 2) {
                                        Class<?> primitiveType = GlobalContext.getPrimitiveType(params.get(0));
                                        Variable.Type type = primitiveType != null ? Variable.Type.fromJavaClass(primitiveType) : Variable.Type.CLASS;
                                        if (type == null) type = Variable.Type.CLASS;
                                        arguments.add(new Pair<>(params.get(1), type));
                                    } else {
                                        throw new ParsingException("Invalid function declaration");
                                    }
                                }

                                String nextC = getNext(tokens, end + 1);
                                int nextIndexC = findNext(tokens, end + 1, nextC);

                                if (nextC == null) {
                                    throw new ParsingException("Unexpected end of file");
                                }

                                int bodyEnd = 0;

                                switch (nextC) {
                                    case ";" -> {
                                        body.addNode(new OrbitFunction(
                                                identifier,
                                                arguments.size(),
                                                arguments,
                                                new BodyASTNode(new ArrayList<>()),
                                                token.equals("func") ? Variable.Type.ANY : Variable.Type.fromJavaClass(GlobalContext.getPrimitiveType(token))
                                        ));

                                        i = nextIndexC;

                                        continue;
                                    }
                                    case "{" -> {
                                        bodyEnd = getBodyEnd(tokens, nextIndexC, "{");
                                    }
                                    case "does" -> {
                                        bodyEnd = getBodyEnd(tokens, nextIndexC, "catch", "does", "do", "then");
                                    }
                                }

                                if (bodyEnd == -1) {
                                    throw new ParsingException("Unexpected end of file");
                                } else if (bodyEnd == 0) {
                                    throw new ParsingException("Invalid function body");
                                }

                                List<String> bodyTokens = tokens.subList(nextIndexC + 1, bodyEnd - 1);

                                ASTNode functionBody = parse(bodyTokens, context);

                                body.addNode(new OrbitFunction(
                                        identifier,
                                        arguments.size(),
                                        arguments,
                                        functionBody,
                                        token.equals("func") ? Variable.Type.ANY : Variable.Type.fromJavaClass(GlobalContext.getPrimitiveType(token))
                                ));

                                i = bodyEnd - 1;

                                continue;
                            }
                        }
                    }
                }
            }

            if(token.matches(Utils.IDENTIFIER_REGEX) && !Utils.isKeyword(token) && !clazzName.isEmpty()) {
                String next = getNext(tokens, i + 1);
                int nextIndex = findNext(tokens, i + 1, next);

                String last = getLast(tokens, i - 1);

                if(token.equals(clazzName) && next != null && (last == null || last.equals("{") || last.equals("}")) && next.equals("(")) {
                    // Constructor call
                    int end = findNext(tokens, nextIndex, ")");

                    List<String> params = tokens.subList(nextIndex + 1, end);

                    List<Pair<String, Variable.Type>> arguments = new ArrayList<>();

                    int nextComma = getNextSeparator(params, 0);

                    while (nextComma != -1) {
                        List<String> subList = params.subList(0, nextComma);
                        if(subList.size() == 1) {
                            arguments.add(new Pair<>(subList.get(0), Variable.Type.ANY));
                        } else if(subList.size() == 2) {
                            Class<?> primitiveType = GlobalContext.getPrimitiveType(params.get(0));
                            Variable.Type type = primitiveType != null ? Variable.Type.fromJavaClass(primitiveType) : Variable.Type.CLASS;
                            if(type == null) type = Variable.Type.CLASS;
                            arguments.add(new Pair<>(subList.get(1), type));
                        } else {
                            throw new ParsingException("Invalid function declaration");
                        }

                        params = params.subList(nextComma + 1, params.size());
                        nextComma = getNextSeparator(params, 0);
                    }

                    if(!params.isEmpty()) {
                        if(params.size() == 1) {
                            arguments.add(new Pair<>(params.get(0), Variable.Type.ANY));
                        } else if(params.size() == 2) {
                            Class<?> primitiveType = GlobalContext.getPrimitiveType(params.get(0));
                            Variable.Type type = primitiveType != null ? Variable.Type.fromJavaClass(primitiveType) : Variable.Type.CLASS;
                            if(type == null) type = Variable.Type.CLASS;
                            arguments.add(new Pair<>(params.get(1), type));
                        } else {
                            throw new ParsingException("Invalid function declaration");
                        }
                    }

                    String nextC = getNext(tokens, end + 1);
                    int nextIndexC = findNext(tokens, end + 1, nextC);

                    if(nextC == null) {
                        throw new ParsingException("Unexpected end of file");
                    }

                    int bodyEnd = 0;

                    switch (nextC) {
                        case ";" -> {
                            body.addNode(new ClassConstructor(
                                    arguments.size(),
                                    arguments,
                                    new BodyASTNode(new ArrayList<>())
                            ));

                            i = nextIndexC;

                            continue;
                        }
                        case "{"  -> {
                            bodyEnd = getBodyEnd(tokens, nextIndexC, "{");
                        }
                        case "does" -> {
                            bodyEnd = getBodyEnd(tokens, nextIndexC, "catch", "does", "do", "then");
                        }
                    }

                    if(bodyEnd == -1) {
                        throw new ParsingException("Unexpected end of file");
                    } else if(bodyEnd == 0) {
                        throw new ParsingException("Invalid function body");
                    }

                    List<String> bodyTokens = tokens.subList(nextIndexC + 1, bodyEnd - 1);

                    ASTNode functionBody = parse(bodyTokens, context);

                    body.addNode(new ClassConstructor(
                            arguments.size(),
                            arguments,
                            functionBody
                    ));

                    i = bodyEnd - 1;

                    continue;
                }
            }

            if(token.matches(Utils.IDENTIFIER_REGEX) && !Utils.isKeyword(token)) {
                String identifier = getNext(tokens, i + 1);
                int identifierIndex = findNext(tokens, i + 1, identifier);

                if (identifier != null) {
                    if (OperationType.fromSymbol(identifier) != null || (identifier.matches(Utils.IDENTIFIER_REGEX) && !Utils.isKeyword(identifier))) {
                        if (identifierIndex < tokens.size()) {
                            String next = getNext(tokens, identifierIndex + 1);
                            int nextIndex = findNext(tokens, identifierIndex + 1, next);

                            if (next == null || next.equals(";")) {
                                body.addNode(new DecObjASTNode(
                                        identifier,
                                        new ValueASTNode(null),
                                        token
                                ));

                                i = next == null ? tokens.size() : nextIndex;

                                continue;
                            }

                            switch (next) {
                                case "=", "be" -> {
                                    Pair<List<String>, Integer> expression = fetchExpression(tokens, nextIndex + 1);

                                    List<String> postfix = infixToPostfix(expression.first);

                                    body.addNode(new DecObjASTNode(
                                            identifier,
                                            postfixToAST(postfix, context),
                                            token
                                    ));

                                    i = expression.second;

                                    continue;
                                }
                                case "(" -> {
                                    // Function declaration
                                    int end = findNext(tokens, nextIndex, ")");

                                    List<String> params = tokens.subList(nextIndex + 1, end);

                                    List<Pair<String, Variable.Type>> arguments = new ArrayList<>();

                                    int nextComma = getNextSeparator(params, 0);

                                    while (nextComma != -1) {
                                        List<String> subList = params.subList(0, nextComma);
                                        if (subList.size() == 1) {
                                            arguments.add(new Pair<>(subList.get(0), Variable.Type.ANY));
                                        } else if (subList.size() == 2) {
                                            Class<?> primitiveType = GlobalContext.getPrimitiveType(params.get(0));
                                            Variable.Type type = primitiveType != null ? Variable.Type.fromJavaClass(primitiveType) : Variable.Type.CLASS;
                                            if (type == null) type = Variable.Type.CLASS;
                                            arguments.add(new Pair<>(subList.get(1), type));
                                        } else {
                                            throw new ParsingException("Invalid function declaration");
                                        }

                                        params = params.subList(nextComma + 1, params.size());
                                        nextComma = getNextSeparator(params, 0);
                                    }

                                    if (!params.isEmpty()) {
                                        if (params.size() == 1) {
                                            arguments.add(new Pair<>(params.get(0), Variable.Type.ANY));
                                        } else if (params.size() == 2) {
                                            Class<?> primitiveType = GlobalContext.getPrimitiveType(params.get(0));
                                            Variable.Type type = primitiveType != null ? Variable.Type.fromJavaClass(primitiveType) : Variable.Type.CLASS;
                                            if (type == null) type = Variable.Type.CLASS;
                                            arguments.add(new Pair<>(params.get(1), type));
                                        } else {
                                            throw new ParsingException("Invalid function declaration");
                                        }
                                    }

                                    String nextC = getNext(tokens, end + 1);
                                    int nextIndexC = findNext(tokens, end + 1, nextC);

                                    if (nextC == null) {
                                        throw new ParsingException("Unexpected end of file");
                                    }

                                    int bodyEnd = 0;

                                    switch (nextC) {
                                        case ";" -> {
                                            body.addNode(new OrbitFunction(
                                                    identifier,
                                                    arguments.size(),
                                                    arguments,
                                                    new BodyASTNode(new ArrayList<>()),
                                                    Variable.Type.CLASS
                                            ));

                                            i = nextIndexC;

                                            continue;
                                        }
                                        case "{" -> {
                                            bodyEnd = getBodyEnd(tokens, nextIndexC, "{");
                                        }
                                        case "does" -> {
                                            bodyEnd = getBodyEnd(tokens, nextIndexC, "catch", "does", "do", "then");
                                        }
                                    }

                                    if (bodyEnd == -1) {
                                        throw new ParsingException("Unexpected end of file");
                                    } else if (bodyEnd == 0) {
                                        throw new ParsingException("Invalid function body");
                                    }

                                    List<String> bodyTokens = tokens.subList(nextIndexC + 1, bodyEnd - 1);

                                    ASTNode functionBody = parse(bodyTokens, context);

                                    body.addNode(new OrbitFunction(
                                            identifier,
                                            arguments.size(),
                                            arguments,
                                            functionBody,
                                            Variable.Type.CLASS
                                    ));

                                    i = bodyEnd - 1;

                                    continue;
                                }
                            }
                        }
                    }
                }
            }

            // 2. Variable Assignment
            if(token.matches(Utils.IDENTIFIER_REGEX) && !Utils.isKeyword(token)) {
                String next = getNext(tokens, i + 1);
                int nextIndex = findNext(tokens, i + 1, next);

                if(next != null) {
                    ASTNode value;
                    Pair<List<String>, Integer> expression = null;

                    if (!next.equals("(") && !next.equals(":")) {
                        if (!next.equals("++") && !next.equals("--")) {
                            expression = fetchExpression(tokens, nextIndex + 1);
                        }

                        switch (next) {
                            case "++" -> {
                                value = new OperationASTNode(
                                        new VariableASTNode(token),
                                        new ValueASTNode(1),
                                        OperationType.ADD
                                );
                            }
                            case "--" -> {
                                value = new OperationASTNode(
                                        new VariableASTNode(token),
                                        new ValueASTNode(1),
                                        OperationType.SUBTRACT
                                );
                            }
                            case "=" -> {
                                List<String> postfix = infixToPostfix(expression.first);

                                value = postfixToAST(postfix, context);
                            }
                            case "+=" -> {
                                List<String> postfix = infixToPostfix(expression.first);

                                value = new OperationASTNode(
                                        new VariableASTNode(token),
                                        postfixToAST(postfix, context),
                                        OperationType.ADD
                                );
                            }
                            case "-=" -> {
                                List<String> postfix = infixToPostfix(expression.first);

                                value = new OperationASTNode(
                                        new VariableASTNode(token),
                                        postfixToAST(postfix, context),
                                        OperationType.SUBTRACT
                                );
                            }
                            case "*=" -> {
                                List<String> postfix = infixToPostfix(expression.first);

                                value = new OperationASTNode(
                                        new VariableASTNode(token),
                                        postfixToAST(postfix, context),
                                        OperationType.MULTIPLY
                                );
                            }
                            case "/=" -> {
                                List<String> postfix = infixToPostfix(expression.first);

                                value = new OperationASTNode(
                                        new VariableASTNode(token),
                                        postfixToAST(postfix, context),
                                        OperationType.DIVIDE
                                );
                            }
                            case "%=" -> {
                                List<String> postfix = infixToPostfix(expression.first);

                                value = new OperationASTNode(
                                        new VariableASTNode(token),
                                        postfixToAST(postfix, context),
                                        OperationType.MODULO
                                );
                            }
                            default -> {
                                expression = fetchExpression(tokens, nextIndex);

                                List<String> functionCall = new ArrayList<>();

                                functionCall.add(token);
                                functionCall.add("(");
                                functionCall.addAll(expression.first);
                                functionCall.add(")");

                                Pair<List<String>, Integer> exp2 = fetchExpression(functionCall, 0);

                                List<String> postfix = infixToPostfix(exp2.first);

                                body.addNode(postfixToAST(postfix, context));

                                i = expression.second;

                                continue;
                            }
                        }

                        body.addNode(new AssignVarASTNode(
                                token,
                                value
                        ));

                        i = expression != null ? expression.second : nextIndex;

                        continue;
                    }
                }
            }

            // 4. While loops
            else if (token.equals("while")) {
                Pair<String, Integer> next = findNext(tokens, i + 1, "{", "do");

                if(next.first == null) {
                    throw new ParsingException("Unexpected end of file");
                }

                int bodyEnd = 0;

                switch (next.first) {
                    case "{"  -> {
                        bodyEnd = getBodyEnd(tokens, next.second, "{");
                    }
                    case "do" -> {
                        bodyEnd = getBodyEnd(tokens, next.second, "catch", "does", "do", "then");
                    }
                }

                if(bodyEnd == -1) {
                    throw new ParsingException("Unexpected end of file");
                } else if(bodyEnd == 0) {
                    throw new ParsingException("Invalid while loop body");
                }

                List<String> conditionTokens = tokens.subList(i + 1, next.second);

                List<String> bodyTokens = tokens.subList(next.second + 1, bodyEnd - 1);

                ASTNode condition = postfixToAST(infixToPostfix(conditionTokens), context);

                ASTNode bodyNode = parse(bodyTokens, context);

                body.addNode(new WhileASTNode(
                        condition,
                        bodyNode
                ));

                i = bodyEnd - 1;

                continue;
            }

            // 5. For loops
            else if (token.equals("for")) {
                Pair<String, Integer> next = findNext(tokens, i + 1, "{", "do");

                if(next.first == null) {
                    throw new ParsingException("Unexpected end of file");
                }

                int bodyEnd = 0;

                switch (next.first) {
                    case "{"  -> {
                        bodyEnd = getBodyEnd(tokens, next.second, "{");
                    }
                    case "do" -> {
                        bodyEnd = getBodyEnd(tokens, next.second, "catch", "does", "do", "then");
                    }
                }

                if(bodyEnd == -1) {
                    throw new ParsingException("Unexpected end of file");
                } else if(bodyEnd == 0) {
                    throw new ParsingException("Invalid for loop body");
                }

                List<String> bodyTokens = tokens.subList(next.second + 1, bodyEnd - 1);

                ASTNode bodyNode = parse(bodyTokens, context);

                List<String> conditionTokens = tokens.subList(i + 1, next.second);

                // If the condition starts and ends with a parenthesis, remove them
                if(conditionTokens.get(0).equals("(") && conditionTokens.get(conditionTokens.size() - 1).equals(")")) {
                    conditionTokens = conditionTokens.subList(1, conditionTokens.size() - 1);
                }

                int type = 0;

                int firstSemicolon = findNext(conditionTokens, 0, ";"); // The first semicolon
                int secondSemicolon = findNext(conditionTokens, firstSemicolon + 1, ";"); // The second semicolon

                if(firstSemicolon != -1 && secondSemicolon != -1) {
                    type = 1;
                }
                else {
                    int to = findNext(conditionTokens, 0, "to");
                    int downto = findNext(conditionTokens, 0, "downto");
                    int in = findNext(conditionTokens, 0, "in");

                    if(to != -1) {
                        type = 2;
                    } else if(downto != -1) {
                        type = 3;
                    } else if(in != -1) {
                        type = 4;
                    }
                }

                switch (type) {
                    case 1 -> {
                        List<String> initTokens = conditionTokens.subList(0, firstSemicolon);
                        List<String> condition = conditionTokens.subList(firstSemicolon + 1, secondSemicolon);
                        List<String> updateTokens = conditionTokens.subList(secondSemicolon + 1, conditionTokens.size());

                        ASTNode init = parse(initTokens, context);
                        ASTNode update = parse(updateTokens, context);
                        ASTNode conditionNode = postfixToAST(infixToPostfix(condition), context);

//                        body.addNode(new ForASTNode(
//                                init,
//                                conditionNode,
//                                update,
//                                bodyNode
//                        ));

                        // TODO: Implement ForASTNode
                    }
                    case 2 -> {
                        List<String> initTokens = conditionTokens.subList(0, findNext(conditionTokens, 0, "to"));
                        List<String> condition = conditionTokens.subList(findNext(conditionTokens, 0, "to") + 1, conditionTokens.size());

                        ASTNode initNode;
                        if(initTokens.size() == 1) {
                            initNode = new DecVarASTNode(initTokens.get(0), new ValueASTNode(0), Variable.Type.LONG);
                        } else {
                            initNode = parse(initTokens, context);

                            if(initNode instanceof AssignVarASTNode assignVarASTNode) {
                                initNode = new DecVarASTNode(assignVarASTNode.variableName(), assignVarASTNode.value(), Variable.Type.LONG);
                            }
                        }

                        ASTNode conditionNode = postfixToAST(infixToPostfix(condition), context);

                        body.addNode(new ForToASTNode(
                                initNode,
                                conditionNode,
                                bodyNode
                        ));
                    }
                    case 3 -> {
                        List<String> initTokens = conditionTokens.subList(0, findNext(conditionTokens, 0, "downto"));
                        List<String> condition = conditionTokens.subList(findNext(conditionTokens, 0, "downto") + 1, conditionTokens.size());

                        ASTNode initNode;
                        if(initTokens.size() == 1) {
                            initNode = new DecVarASTNode(initTokens.get(0), new ValueASTNode(0), Variable.Type.INT);
                        } else {
                            initNode = parse(initTokens, context);

                            if(initNode instanceof AssignVarASTNode assignVarASTNode) {
                                initNode = new DecVarASTNode(assignVarASTNode.variableName(), assignVarASTNode.value(), Variable.Type.INT);
                            }
                        }

                        ASTNode conditionNode = postfixToAST(infixToPostfix(condition), context);

                        body.addNode(new ForDowntoASTNode(
                                initNode,
                                conditionNode,
                                bodyNode
                        ));
                    }
                    case 4 -> {
                        List<String> initTokens = conditionTokens.subList(0, findNext(conditionTokens, 0, "in"));
                        List<String> condition = conditionTokens.subList(findNext(conditionTokens, 0, "in") + 1, conditionTokens.size());

                        ASTNode initNode;

                        if(initTokens.size() == 1) {
                            initNode = new DecVarASTNode(initTokens.get(0), new ValueASTNode(null), Variable.Type.ANY);
                        } else {
                            initNode = parse(initTokens, context);

                            if(initNode instanceof AssignVarASTNode assignVarASTNode) {
                                initNode = new DecVarASTNode(assignVarASTNode.variableName(), assignVarASTNode.value(), Variable.Type.ANY);
                            }
                        }

                        ASTNode conditionNode = postfixToAST(infixToPostfix(condition), context);

                        body.addNode(new ForInASTNode(
                                initNode,
                                conditionNode,
                                bodyNode
                        ));
                    }
                }

                i = bodyEnd - 1;

                continue;
            }

            // 6. If statement
            else if (token.equals("if")) {
                Pair<String, Integer> next = findNext(tokens, i + 1, "{", "then");

                if(next.first == null) {
                    throw new ParsingException("Unexpected end of file");
                }

                List<String> conditionTokens = tokens.subList(i + 1, next.second);

                List<String> ifBody = new ArrayList<>();
                List<String> elseBody = new ArrayList<>();
                List<Pair<ASTNode, ASTNode>> elseIfs = new ArrayList<>();

                int end = 0;

                // C Syntax
                if(next.first.equals("{")) {
                    int bodyEnd = getBodyEnd(tokens, next.second, "{");

                    if(bodyEnd == -1) {
                        throw new ParsingException("Unexpected end of file");
                    } else if(bodyEnd == 0) {
                        throw new ParsingException("Invalid if statement body");
                    }

                    ifBody = tokens.subList(next.second + 1, bodyEnd - 1);

                    // Check if there's an else statement
                    Pair<String, Integer> nextToken = getNext_1(tokens, bodyEnd);

                    if(nextToken.first != null && nextToken.first.equals("else")) {
                        int start = nextToken.second;

                        while (start < tokens.size()) {
                            if (tokens.get(start).equals("else")) {
                                Pair<String, Integer> nextToken2 = getNext_1(tokens, start + 1);

                                if(nextToken2.first.equals("if")) {
                                    int next2 = findNext(tokens, start + 1, "{");

                                    if(next2 == -1) {
                                        throw new ParsingException("Unexpected end of file");
                                    }

                                    List<String> conditionTokens2 = tokens.subList(nextToken2.second + 1, next2);

                                    List<String> elseIfBody;

                                    int bodyEnd2 = getBodyEnd(tokens, next2, "{");

                                    if(bodyEnd2 == -1) {
                                        throw new ParsingException("Unexpected end of file");
                                    } else if(bodyEnd2 == 0) {
                                        throw new ParsingException("Invalid if statement body");
                                    }

                                    elseIfBody = tokens.subList(next2 + 1, bodyEnd2 - 1);

                                    ASTNode condition = postfixToAST(infixToPostfix(conditionTokens2), context);
                                    ASTNode elseIfBodyNode = parse(elseIfBody, context);

                                    elseIfs.add(new Pair<>(condition, elseIfBodyNode));

                                    start = bodyEnd2;
                                } else {
                                    int bodyEnd2 = getBodyEnd(tokens, start + 1, "{");

                                    if(bodyEnd2 == -1) {
                                        throw new ParsingException("Unexpected end of file");
                                    } else if(bodyEnd2 == 0) {
                                        throw new ParsingException("Invalid if statement body");
                                    }

                                    elseBody = tokens.subList(start + 1, bodyEnd2 - 1);

                                    start = bodyEnd2;
                                }
                            } else {
                                break;
                            }
                        }

                        end = start;
                    } else {
                        end = bodyEnd;
                    }
                } else if(next.first.equals("then")) {
                    int bodyEnd = getBodyEnd(tokens, next.second, "then", "do", "does", "catch");

                    if(bodyEnd == -1) {
                        throw new ParsingException("Unexpected end of file");
                    } else if(bodyEnd == 0) {
                        throw new ParsingException("Invalid if statement body");
                    }

                    // Lua syntax is a bit more different, we should look for 'else' in-between the 'then' and 'end'
                    int start = next.second;

                    int count = 0;
                    for (int j = start + 1; j < bodyEnd; j++) {
                        if(tokens.get(j).equals("then") || tokens.get(j).equals("do")) count++;
                        if(tokens.get(j).equals("end")) count--;

                        if (tokens.get(j).equals("else") && count == 0) {
                            Pair<String, Integer> nextToken2 = getNext_1(tokens, j + 1);

                            if(nextToken2.first == null || !nextToken2.first.equals("if")) {
                                int elseEnd = getElseEnd(tokens, j);

                                ifBody = tokens.subList(start + 1, j);

                                elseBody = tokens.subList(j + 1, elseEnd);

                                end = elseEnd;
                            } else {
                                // else if statement
                            }
                        }
                    }

                    if(end == 0) {
                        ifBody = tokens.subList(start + 1, bodyEnd - 1);
                        end = bodyEnd;
                    }
                }

                // The one above would be used as follows:
                // if(condition) then ... else ... end
                // if(condition) then ... else if(condition) then ... else ... end

                ASTNode condition = postfixToAST(infixToPostfix(conditionTokens), context);
                ASTNode ifBodyNode = parse(ifBody, context);

                ASTNode elseNode = null;

                if(elseIfs.isEmpty()) {
                    if(!elseBody.isEmpty()) {
                        elseNode = parse(elseBody, context);

                        if (elseNode instanceof BodyASTNode bd) {
                            if(bd.statements().isEmpty()) elseNode = null;
                        }
                    }
                } else {
                    if(!elseBody.isEmpty()) {
                        elseIfs.add(new Pair<>(new ValueASTNode(true), parse(elseBody, context)));
                    }

                    elseNode = new BranchASTNode(elseIfs);
                }

                body.addNode(new ConditionalASTNode(
                        condition,
                        ifBodyNode,
                        elseNode
                ));

                i = end - 1;

                continue;
            }

            Pair<List<String>, Integer> expression = fetchExpression(tokens, i);

            List<String> postfix = infixToPostfix(expression.first);

            body.addNode(postfixToAST(postfix, context));

            i = expression.second + 1;
        }
//        } // catch (IndexOutOfBoundsException e) {
//
//        }

        if(body.statements().size() == 1) {
            return body.statements().get(0);
        } else {
            return body;
        }
    }

    private static int getElseEnd(List<String> tokens, int j) throws ParsingException {
        int elseEnd = -1;

        int eCount = 0;
        for (int k = j; k < tokens.size(); k++) {
            if (tokens.get(k).equals("do") || tokens.get(k).equals("else")) {
                eCount++;
            }
            if (tokens.get(k).equals("end")) {
                eCount--;
            }
            if (eCount == 0) {
                elseEnd = k;
                break;
            }
        }

        if (elseEnd == -1) {
            throw new ParsingException("Unexpected end of file");
        }
        return elseEnd;
    }

    public static int findNext(List<String> tokens, int start, String token) {
        for (int i = start; i < tokens.size(); i++) {
            if (tokens.get(i).equals(token)) {
                return i;
            }
        }

        return -1;
    }

    public static Pair<String, Integer> findNext(List<String> tokens, int start, String... tokensToFind) {
        for (int i = start; i < tokens.size(); i++) {
            for (String token : tokensToFind) {
                if (tokens.get(i).equals(token)) {
                    return new Pair<>(token, i);
                }
            }
        }

        return new Pair<>(null, -1);
    }

    public static int findLast(List<String> tokens, int start, String token) {
        for (int i = start; i >= 0; i--) {
            if (tokens.get(i).equals(token)) {
                return i;
            }
        }

        return -1;
    }

    // Gets the next token in the list (while ignoring \n and \r)
    public static String getNext(List<String> tokens, int start) {
        for (int i = start; i < tokens.size(); i++) {
            if (!tokens.get(i).equals("\n") && !tokens.get(i).equals("\r")) {
                return tokens.get(i);
            }
        }

        return null;
    }

    public static Pair<String, Integer> getNext_1(List<String> tokens, int start) {
        for (int i = start; i < tokens.size(); i++) {
            if (!tokens.get(i).equals("\n") && !tokens.get(i).equals("\r")) {
                return new Pair<>(tokens.get(i), i);
            }
        }

        return new Pair<>(null, -1);
    }

    // Gets the last token in the list (while ignoring \n and \r)
    public static String getLast(List<String> tokens, int start) {
        for (int i = start; i >= 0; i--) {
            if (!tokens.get(i).equals("\n") && !tokens.get(i).equals("\r")) {
                return tokens.get(i);
            }
        }

        return null;
    }

    public static int getPair(List<String> tokens, int start, String open, String close) {
        int depth = 0;

        for (int i = start; i < tokens.size(); i++) {
            if (tokens.get(i).equals(open)) {
                depth++;
            } else if (tokens.get(i).equals(close)) {
                depth--;
            }

            if (depth == 0) {
                return i;
            }
        }

        return -1;
    }

    public static int getPair(List<String> tokens, int start, List<String> open, List<String> close) {
        int depth = 0;

        for (int i = start; i < tokens.size(); i++) {
            if (open.contains(tokens.get(i))) {
                depth++;
            } else if (close.contains(tokens.get(i))) {
                depth--;
            }

            if (depth == 0) {
                return i;
            }
        }

        return -1;
    }

    public static int getBodyEnd(List<String> tokens, int start, String... open) {
        int depth = 0;

        List<String> openList = Arrays.asList(open);

        for (int i = start; i < tokens.size(); i++) {
            if (openList.contains(tokens.get(i))) {
                depth++;
            } else if (tokens.get(i).equals("end") || tokens.get(i).equals("}")) {
                depth--;
            }

            if (depth == 0) {
                return i + 1;
            }
        }

        return -1;
    }

    public static Pair<List<String>, Integer> fetchExpression(List<String> tokens, int start) throws ParsingException {
        List<String> expression = new ArrayList<>();

        int i;
        for (i = start; i < tokens.size(); i++) {
            String token = getNext(tokens, i);

            if (token == null || token.equals(";")) {
                i--;
                break;
            }

            if (token.equals("\n")) {
                continue;
            }

            switch (token) {
                case "(":
                case "[":
                case "{":
                    String e = token.equals("(") ? ")" : (token.equals("[") ? "]" : "}");
                    int getPair = getPair(tokens, i, token, e);

                    if (getPair == -1) {
                        throw new ParsingException("Unmatched parenthesis");
                    }

                    List<String> subExpression = tokens.subList(i + 1, getPair);
                    subExpression.removeIf(s -> s.equals("\n") || s.equals("\r"));

                    expression.add(token);
                    Pair<List<String>, Integer> subExpressionResult = fetchExpression(subExpression, 0);
                    expression.addAll(subExpressionResult.first);
                    expression.add(e);

                    i = getPair;

                    break;
                default:
                    expression.add(token);
                    break;
            }

            if (i + 1 < tokens.size()) {
                String next = getNext(tokens, i + 1);

                if (next == null || next.equals(";")) {
                    break;
                }
                if (next.equals("(")) {
                    continue;
                }
                int nextIndex = findNext(tokens, i + 1, next);
                if (next.equals(",") || next.equals("?") || next.equals("=") || next.equals(":") || (OperationType.fromSymbol(next) != null && !next.equals("!") && !next.equals("~"))) {
                    expression.add(next);
                    i = nextIndex;
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        return new Pair<>(expression, i);
    }


    public static List<String> infixToPostfix(List<String> infix) {
        List<String> postfixExpression = new ArrayList<>();
        Stack<String> operatorStack = new Stack<>();

        for (int i = 0; i < infix.size(); i++) {
            String token = infix.get(i);

            if (token.equals(",") || token.equals(":") || token.equals("?") || token.equals("=") || token.equals("{") || token.equals("}") || token.equals("[") || token.equals("]")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    postfixExpression.add(operatorStack.pop());
                }
                postfixExpression.add(token);
                continue;
            }

            else if (token.equals(";")) {
                while (!operatorStack.isEmpty()) {
                    postfixExpression.add(operatorStack.pop());
                }

                return postfixExpression;
            }

            if (token.matches(Utils.IDENTIFIER_REGEX)) {
                postfixExpression.add(token);
            } else if (OperationType.fromSymbol(token) != null) {
                while (!operatorStack.isEmpty() && OperationType.fromSymbol(operatorStack.peek()) != null && precedence(token) <= precedence(operatorStack.peek())) {
                    postfixExpression.add(operatorStack.pop());
                }

                operatorStack.push(token);
            } else if (token.equals("(")) {
                // Check whether the last token is an identifier
                if (!postfixExpression.isEmpty() && postfixExpression.get(postfixExpression.size() - 1).matches(Utils.IDENTIFIER_REGEX)) {
                    // Add all the operators to the postfix expression from this to next ")"
                    int pair = getPair(infix, i, "(", ")");

                    List<String> subExpression = infix.subList(i + 1, pair);

                    postfixExpression.add("(");
                    postfixExpression.addAll(infixToPostfix(subExpression));
                    postfixExpression.add(")");

                    i = pair;

                    continue;
                }

                operatorStack.push(token);

            } else if (token.equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    postfixExpression.add(operatorStack.pop());
                }

                if(!operatorStack.isEmpty())
                    operatorStack.pop();
            } else {
                postfixExpression.add(token);
            }
        }

        while (!operatorStack.isEmpty()) {
            postfixExpression.add(operatorStack.pop());
        }


        return postfixExpression;
    }

    public static ASTNode postfixToAST(List<String> postfix, GlobalContext context) throws ParsingException {
        Stack<ASTNode> stack = new Stack<>();

        for (int i = 0; i < postfix.size(); i++) {
            String token = postfix.get(i);

            if (Lexer.isNumeric(token)) {
                if (token.startsWith("0x")) {
                    stack.push(new ValueASTNode(Integer.parseInt(token.substring(2), 16)));
                    continue;
                } else if (token.startsWith("0b")) {
                    stack.push(new ValueASTNode(Integer.parseInt(token.substring(2), 2)));
                    continue;
                } else {
                    Class<?>[] numericTypes = {Integer.class, Long.class, Float.class, Double.class};

                    for (Class<?> numericType : numericTypes) {
                        try {
                            if (numericType == Integer.class) {
                                stack.push(new ValueASTNode(Integer.parseInt(token)));
                            } else if (numericType == Long.class) {
                                stack.push(new ValueASTNode(Long.parseLong(token)));
                            } else if (numericType == Float.class) {
                                stack.push(new ValueASTNode(Float.parseFloat(token)));
                            } else if (numericType == Double.class) {
                                stack.push(new ValueASTNode(Double.parseDouble(token)));
                            }
                            break;
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            else if (token.startsWith("'")) {
                stack.push(new ValueASTNode(token.charAt(1)));

                continue;
            }

            else if (token.startsWith("\"")) {
                stack.push(new ValueASTNode(token.substring(1, token.length() - 1)));

                continue;
            }

            else if (token.equals("true") || token.equals("false")) {
                stack.push(new ValueASTNode(Boolean.parseBoolean(token)));

                continue;
            }

            else if (token.equals("null")) {
                stack.push(new ValueASTNode(null));

                continue;
            }

            else if (token.equals("!") || token.equals("~")) {
                ASTNode node = stack.pop();

                if (node instanceof ValueASTNode) {
                    stack.push(new ValueASTNode(node.evaluate(null)));
                } else {
                    stack.push(new OperationASTNode(node, new ValueASTNode(null), OperationType.fromSymbol(token)));
                }

                continue;
            }

            else if (token.equals(":")) {
                // The left token is in the stack, but the right token must be fetched. it's uh... complicated since its infix
                ASTNode left = stack.pop();

                List<String> rightTokens;

                // look for either the next :, ; or uneven parenthesis
                int end = -1;
                int depth = 0;
                for (int j = i + 1; j < postfix.size(); j++) {
                    String t = postfix.get(j);
                    if (t.equals("(")) {
                        depth++;
                    } else if (t.equals(")")) {
                        depth--;
                    }
                    if (OperationType.fromSymbol(t) != null && depth == 0) {
                        end = j;
                        break;
                    }
                    else if (t.equals(";")) {
                        end = j;
                        break;
                    }
                    else if (depth < 0) {
                        end = j;
                        break;
                    }
                }

                if(end == -1) {
                    end = postfix.size();
                }

                rightTokens = postfix.subList(i + 1, end);

                ASTNode right = postfixToAST(rightTokens, context);

                stack.push(new ReferenceASTNode(left, right));

                i = end - 1;

                continue;
            }

            else if (OperationType.fromSymbol(token) != null) {
                ASTNode right = stack.pop();
                ASTNode left = stack.pop();
                OperationType operationType = OperationType.fromSymbol(token);

                stack.push(new OperationASTNode(left, right, operationType));

                // Preprocess the AST. If they're both values, then we can just evaluate them
                if (left instanceof ValueASTNode && right instanceof ValueASTNode) {
                    stack.push(new ValueASTNode(stack.pop().evaluate(null)));
                }

                continue;
            }

            else if (token.equals("?")) {
                ASTNode condition = stack.pop();

                int colon = findNext(postfix, i, ":");

                List<String> trueExpression = postfix.subList(i + 1, colon);

                List<String> falseExpression = postfix.subList(colon + 1, postfix.size());

                stack.push(new TernaryASTNode(
                        condition,
                        postfixToAST(trueExpression, context),
                        postfixToAST(falseExpression, context)
                ));

                // Check whether the condition is a value. If it is, then we can just evaluate it
                if (condition instanceof ValueASTNode) {
                    TernaryASTNode ternary = (TernaryASTNode) stack.pop();

                    if(Evaluator.toBool(ternary.condition().evaluate(null))) {
                        stack.push(ternary.trueBranch());
                    } else {
                        stack.push(ternary.falseBranch());
                    }
                }

                i = postfix.size();

                continue;
            }

            else if (token.equals("[")) {
                ArrayList<ASTNode> list = new ArrayList<>();

                int pair = getPair(postfix, i, "[", "]");

                List<String> subExpression = postfix.subList(i + 1, pair);

                int next = getNextSeparator(subExpression, 0);
                while (next != -1) {
                    List<String> subList = subExpression.subList(0, next);
                    list.add(postfixToAST(subList, context));

                    subExpression = subExpression.subList(next + 1, subExpression.size());
                    next = getNextSeparator(subExpression, 0);
                }

                if (!subExpression.isEmpty()) {
                    list.add(postfixToAST(subExpression, context));
                }

                stack.push(new FunctionCallASTNode(
                        "list.of",
                        list
                ));

                // Check whether all the params are values. If they are, then we can just evaluate them
                if(list.stream().allMatch(astNode -> astNode instanceof ValueASTNode)) {
                    stack.pop();

                    List<Object> values = new ArrayList<>();
                    for (ASTNode node : list) {
                        values.add(node.evaluate(context));
                    }

                    stack.push(new ValueASTNode(values));
                }

                i = pair;

                continue;
            }

            else if (token.equals("{")) { // maps in the format {key: value, key: value, ...}
                ArrayList<ASTNode> map = new ArrayList<>();

                int pair = getPair(postfix, i, "{", "}");

                List<String> subExpression = postfix.subList(i + 1, pair);

                int next = getNextSeparator(subExpression, 0);
                while (next != -1) {
                    int colon = subExpression.indexOf(":");
                    int equals = subExpression.indexOf("=");

                    if (equals != -1 && (colon == -1 || equals < colon)) {
                        colon = equals;
                    }

                    List<String> keyList = subExpression.subList(0, colon);
                    List<String> valueList = subExpression.subList(colon + 1, next);

                    if(keyList.size() != 1) {
                        throw new ParsingException("Invalid map format");
                    }

                    if(!keyList.get(0).startsWith("\"")) {
                        if(keyList.get(0).startsWith("'")) {
                            keyList.set(0, "\"" + keyList.get(0).substring(1, keyList.get(0).length() - 1) + "\"");
                        } else {
                            keyList.set(0, "\"" + keyList.get(0) + "\"");
                        }
                    }

                    ASTNode key = postfixToAST(keyList, context);
                    ASTNode value = postfixToAST(valueList, context);

                    map.add(key);
                    map.add(value);

                    subExpression = subExpression.subList(next + 1, subExpression.size());

                    next = getNextSeparator(subExpression, 0);
                }

                if (!subExpression.isEmpty()) {
                    // Split by the first colon or equals sign
                    int colon = subExpression.indexOf(":");
                    int equals = subExpression.indexOf("=");

                    if(equals != -1 && (colon == -1 || equals < colon)) {
                        colon = equals;
                    }
                    List<String> keyList = subExpression.subList(0, colon);
                    List<String> valueList = subExpression.subList(colon + 1, subExpression.size());

                    if(keyList.size() != 1) {
                        throw new ParsingException("Invalid map format");
                    }

                    // Check whether the key starts with quotes. if it doesn't add them
                    if(!keyList.get(0).startsWith("\"")) {
                        if(keyList.get(0).startsWith("'")) {
                            keyList.set(0, "\"" + keyList.get(0).substring(1, keyList.get(0).length() - 1) + "\"");
                        } else {
                            keyList.set(0, "\"" + keyList.get(0) + "\"");
                        }
                    }

                    ASTNode key = postfixToAST(keyList, context);
                    ASTNode value = postfixToAST(valueList, context);

                    map.add(key);
                    map.add(value);
                }

                stack.push(new FunctionCallASTNode(
                        "map.of",
                        map
                ));

                // Check whether all the params are values. If they are, then we can just evaluate them
                if(map.stream().allMatch(astNode -> astNode instanceof ValueASTNode)) {
                    stack.pop();

                    Map<Object, Object> values = new HashMap<>();
                    for (int j = 0; j < map.size(); j += 2) {
                        values.put(map.get(j).evaluate(null), map.get(j + 1).evaluate(null));
                    }

                    stack.push(new ValueASTNode(values));
                }

                i = pair;

                continue;
            }

            else if (token.equals("(")) {
                ASTNode func = stack.pop();

                if(!(func instanceof VariableASTNode)) {
                    throw new ParsingException("Expected identifier before function call");
                }

                String name = ((VariableASTNode) func).name();

                List<ASTNode> params = new ArrayList<>();

                int pair = getPair(postfix, i, "(", ")");

                List<String> subExpression = postfix.subList(i + 1, pair);

                int next = getNextSeparator(subExpression, 0);
                while (next != -1) {
                    List<String> subList = subExpression.subList(0, next);
                    params.add(postfixToAST(subList, context));

                    subExpression = subExpression.subList(next + 1, subExpression.size());
                    next = getNextSeparator(subExpression, 0);
                }

                if (!subExpression.isEmpty()) {
                    params.add(postfixToAST(subExpression, context));
                }

                stack.push(new FunctionCallASTNode(name, params));

                i = pair;

                continue;
            }

            else {
                stack.push(new VariableASTNode(token));
            }
        }

        if(stack.peek() != null && stack.size() == 1) {
            return stack.pop();
        } else {
            throw new ParsingException("Invalid expression");
        }
    }

    private static int precedence(String operator) {
        return switch (operator) {
            case "||" -> 0;
            case "&&" -> 1;
            case "|" -> 2;
            case "^" -> 3;
            case "&" -> 4;
            case "==", "!=" -> 5;
            case "<", "<=", ">", ">=" -> 6;
            case "<<", ">>" -> 7;
            case "+", "-" -> 8;
            case "*", "/", "%" -> 9;
            case "**" -> 10;
            case "~", "!" -> 11;
            case ":" -> 12;
            default -> -1;
        };
    }

    // next comma, that is on the same layer (so if there's depth of parenthesis or brackets, it will ignore them)
    private static int getNextSeparator(List<String> tokens, int start) {
        int depth = 0;
        for (int i = start; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.equals("(") || token.equals("[") || token.equals("{")) {
                depth++;
            } else if (token.equals(")") || token.equals("]") || token.equals("}")) {
                depth--;
            } else if (token.equals(",") && depth == 0) {
                return i;
            }
        }

        return -1;
    }
}
