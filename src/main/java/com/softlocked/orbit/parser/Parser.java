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
import com.softlocked.orbit.interpreter.ast.variable.collection.CollectionAccessASTNode;
import com.softlocked.orbit.interpreter.ast.variable.collection.CollectionSetASTNode;
import com.softlocked.orbit.interpreter.function.BFunction;
import com.softlocked.orbit.interpreter.function.ClassConstructor;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.function.coroutine.CoroutineFunction;
import com.softlocked.orbit.memory.ILocalContext;
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

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Parser {
    public static ASTNode parse(List<String> tokens, GlobalContext context) throws ParsingException {
        return parse(tokens, context, "");
    }

    private static ASTNode parse(List<String> tokens, GlobalContext context, String clazzName) throws ParsingException {
        BodyASTNode body = new BodyASTNode();

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

            else if(token.equals("yield")) {
                String next = getNext(tokens, i + 1);
                int nextIndex = findNext(tokens, i + 1, next);

                if(next == null || next.equals(";")) {
                    body.addNode(new BreakASTNode(Breakpoint.Type.YIELD, null));

                    i = nextIndex;
                    continue;
                }

                Pair<List<String>, Integer> expression = fetchExpression(tokens, nextIndex);

                List<String> postfix = infixToPostfix(expression.first);

                body.addNode(new BreakASTNode(Breakpoint.Type.YIELD, postfixToAST(postfix, context)));

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

            else if (token.equals("class") || token.equals("record")) {
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

                HashMap<String, Pair<Variable.Type, ASTNode>> fields = new HashMap<>();
                HashMap<Pair<String, Integer>, IFunction> functions = new HashMap<>();
                HashMap<Integer, ClassConstructor> constructors = new HashMap<>();

                try {
                    switch (next) {
                        case ":", "extends" -> {
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
                        }
                        case "(" -> {
                            if (!token.equals("record")) {
                                throw new ParsingException("Invalid class declaration");
                            }
                            int pair = getPair(tokens, nextIndex, "(", ")");

                            if (pair == -1) {
                                throw new ParsingException("Unexpected end of file");
                            }

                            List<String> params = tokens.subList(nextIndex + 1, pair);

                            List<Pair<String, Variable.Type>> arguments = new ArrayList<>();

                            int nextComma = getNextSeparator(params);

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
                                nextComma = getNextSeparator(params);
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

                            String nextC = getNext(tokens, pair + 1);

                            if (nextC == null) {
                                throw new ParsingException("Unexpected end of file");
                            }

                            int bodyEnd = 0;
                            if (nextC.equals("{")) {
                                bodyEnd = getPair(tokens, pair + 1, "{", "}");

                                if (bodyEnd == -1) {
                                    throw new ParsingException("Unexpected end of file");
                                }

                                bodyNode = parse(tokens.subList(pair + 1, bodyEnd), context, className);

                                i = bodyEnd;
                            } else if (nextC.equals(":") || nextC.equals("extends")) {
                                int bodyStart = findNext(tokens, nextIndex + 1, "{");

                                if (bodyStart == -1) {
                                    throw new ParsingException("Unexpected end of file");
                                }

                                superClasses = new ArrayList<>(tokens.subList(nextIndex + 1, bodyStart));
                                superClasses.removeIf(s -> s.equals(","));

                                bodyEnd = getPair(tokens, bodyStart, "{", "}");

                                if (bodyEnd == -1) {
                                    throw new ParsingException("Unexpected end of file");
                                }

                                bodyNode = parse(tokens.subList(bodyStart + 1, bodyEnd), context, className);

                                i = bodyEnd;
                            }

                            BodyASTNode constructorBody = new BodyASTNode();

                            for (Pair<String, Variable.Type> argument : arguments) {
                                constructorBody.addNode(
                                        new AssignVarASTNode(
                                                argument.first,
                                                new VariableASTNode("_" + argument.first)
                                        )
                                );
                                fields.put(argument.first, new Pair<>(argument.second, new ValueASTNode(Utils.newObject(argument.second.getJavaClass()))));
                                argument.first = "_" + argument.first;
                            }

                            // Now create a constructor from the arguments
                            constructors.put(arguments.size(), new ClassConstructor(
                                    arguments.size(),
                                    arguments,
                                    constructorBody
                            ));

                            functions.put(
                                    new Pair<>("cast", 1),
                                    new NativeFunction("cast", List.of(Variable.Type.STRING), Variable.Type.ANY) {
                                        @Override
                                        public Object call(ILocalContext context, List<Object> args) {
                                            String type = (String) args.get(0);

                                            if (type.equals("string")) {
                                                StringBuilder sb = new StringBuilder();
                                                sb.append(className).append("(");

                                                for (int i = 0; i < arguments.size(); i++) {
                                                    Object value = new VariableASTNode(arguments.get(i).first.substring(1)).evaluate(context);

                                                    value = Utils.cast(value, String.class);

                                                    sb.append(value);

                                                    if (i != arguments.size() - 1) {
                                                        sb.append(", ");
                                                    }
                                                }

                                                sb.append(")");

                                                return sb.toString();
                                            }

                                            return null;
                                        }
                                    }
                            );
                        }
                        case "{" -> {
                            if (token.equals("record")) {
                                throw new ParsingException("Invalid record declaration");
                            }

                            int bodyEnd = getPair(tokens, nextIndex, "{", "}");

                            if (bodyEnd == -1) {
                                throw new ParsingException("Unexpected end of file");
                            }

                            bodyNode = parse(tokens.subList(nextIndex + 1, bodyEnd), context, className);

                            i = bodyEnd;
                        }
                        default -> throw new ParsingException("Invalid class declaration");
                    }

                    if (bodyNode instanceof BodyASTNode bd) {
                        for (ASTNode node : bd.statements()) {
                            if (node instanceof DecVarASTNode decVarASTNode) {
                                fields.put(decVarASTNode.variableName(), new Pair<>(decVarASTNode.type(), decVarASTNode.value()));
                            } else if (node instanceof ClassConstructor constructor) {
                                constructors.put(constructor.getParameterCount(), constructor);
                            } else if (node instanceof OrbitFunction function) {
                                functions.put(new Pair<>(function.getName(), function.getParameterCount()), function);
                            } else {
                                throw new ParsingException(token.equals("class") ? "Invalid class body" : "Invalid record body");
                            }
                        }
                    } else if (bodyNode instanceof DecVarASTNode decVarASTNode) {
                        fields.put(decVarASTNode.variableName(), new Pair<>(decVarASTNode.type(), decVarASTNode.value()));
                    } else if (bodyNode instanceof ClassConstructor constructor) {
                        constructors.put(constructor.getParameterCount(), constructor);
                    } else if (bodyNode instanceof OrbitFunction function) {
                        functions.put(new Pair<>(function.getName(), function.getParameterCount()), function);
                    } else {
                        throw new ParsingException(token.equals("class") ? "Invalid class body" : "Invalid record body");
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

            else if (token.equals("enum")) {
                String enumName = getNext(tokens, i + 1);
                int enumNameIndex = findNext(tokens, i + 1, enumName);

                if(enumName == null) {
                    throw new ParsingException("Unexpected end of file");
                }

                if(!enumName.matches(Utils.IDENTIFIER_REGEX) || Utils.isKeyword(enumName)) {
                    throw new ParsingException("Invalid enum name " + enumName);
                }

                String next = getNext(tokens, enumNameIndex + 1);
                int nextIndex = findNext(tokens, enumNameIndex + 1, next);

                if(next == null) {
                    throw new ParsingException("Unexpected end of file");
                }

                if(!next.equals("{")) {
                    throw new ParsingException("Invalid enum declaration");
                }

                int bodyEnd = getPair(tokens, nextIndex, "{", "}");

                if(bodyEnd == -1) {
                    throw new ParsingException("Unexpected end of file");
                }

                List<String> bodyTokens = tokens.subList(nextIndex + 1, bodyEnd);

                List<String> enumValues = new ArrayList<>();

                for(String bodyToken : bodyTokens) {
                    if(bodyToken.equals(",") || bodyToken.equals(";")) {
                        continue;
                    }

                    enumValues.add(bodyToken);
                }

                Map<String, Integer> enumValuesMap = new HashMap<>();

                for(int j = 0; j < enumValues.size(); j++) {
                    enumValuesMap.put(enumValues.get(j), j);
                }

                body.addNode(new DecVarASTNode(
                        enumName,
                        new ValueASTNode(
                                enumValuesMap
                        ),
                        Variable.Type.MAP
                ));

                i = bodyEnd;

                continue;
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

                            body.addNode(new DecVarASTNode(
                                    identifier,
                                    new ValueASTNode(Utils.newObject(GlobalContext.getPrimitiveType(token))),
                                    type
                            ));

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

                                int nextComma = getNextSeparator(params);

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
                                    nextComma = getNextSeparator(params);
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
                                                new BodyASTNode(),
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

                                if(!(functionBody instanceof BodyASTNode)) {
                                    functionBody = new BodyASTNode(List.of(functionBody));
                                }

                                if(token.equals("func")) {
                                    body.addNode(new OrbitFunction(
                                            identifier,
                                            arguments.size(),
                                            arguments,
                                            functionBody,
                                            Variable.Type.ANY
                                    ));
                                } else if (token.equals("coroutine")) {
                                    body.addNode(new CoroutineFunction(
                                            identifier,
                                            arguments.size(),
                                            arguments,
                                            functionBody
                                    ));
                                }
                                else {
                                    body.addNode(new OrbitFunction(
                                            identifier,
                                            arguments.size(),
                                            arguments,
                                            functionBody,
                                            Variable.Type.fromJavaClass(GlobalContext.getPrimitiveType(token))
                                    ));
                                }

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

                if(token.equals(clazzName) && next != null && next.equals("(")) {
                    // Constructor declaration
                    int end = findNext(tokens, nextIndex, ")");

                    List<String> params = tokens.subList(nextIndex + 1, end);

                    List<Pair<String, Variable.Type>> arguments = new ArrayList<>();

                    int nextComma = getNextSeparator(params);

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
                        nextComma = getNextSeparator(params);
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
                                    new BodyASTNode()
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

                    if(!(functionBody instanceof BodyASTNode)) {
                        functionBody = new BodyASTNode(List.of(functionBody));
                    }

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

                                    int nextComma = getNextSeparator(params);

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
                                        nextComma = getNextSeparator(params);
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
                                                    new BodyASTNode(),
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

                                    if(!(functionBody instanceof BodyASTNode)) {
                                        functionBody = new BodyASTNode(List.of(functionBody));
                                    }

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

                // Collection assignment
                if (next != null && next.equals("[")) {
                    List<List<String>> indices = new ArrayList<>();

                    int pair = getPair(tokens, nextIndex, "[", "]");

                    if(pair == -1) {
                        throw new ParsingException("Unexpected end of file");
                    }

                    indices.add(tokens.subList(nextIndex + 1, pair));

                    int start = pair + 1;
                    next = getNext(tokens, start);
                    nextIndex = findNext(tokens, start, next);

                    while(next != null && next.equals("[")) {
                        pair = getPair(tokens, start, "[", "]");

                        if(pair == -1) {
                            throw new ParsingException("Unexpected end of file");
                        }

                        indices.add(tokens.subList(start + 1, pair));

                        start = pair + 1;
                        next = getNext(tokens, start);
                        nextIndex = findNext(tokens, start, next);
                    }

                    if(next != null) {

                        VariableASTNode array = new VariableASTNode(token);
                        List<ASTNode> indexNodes = new ArrayList<>();

                        for (List<String> index : indices) {
                            List<String> postfix = infixToPostfix(index);

                            indexNodes.add(postfixToAST(postfix, context));
                        }

                        Pair<List<String>, Integer> expression = fetchExpression(tokens, nextIndex + 1);

                        List<String> postfix = infixToPostfix(expression.first);

                        ASTNode value = postfixToAST(postfix, context);

                        switch (next) {
                            case "=" -> {
                                body.addNode(new CollectionSetASTNode(
                                        array,
                                        indexNodes,
                                        value
                                ));

                                i = expression.second;

                                continue;
                            }
                            case "+=" -> {
                                body.addNode(new CollectionSetASTNode(
                                        array,
                                        indexNodes,
                                        new OperationASTNode(
                                                new CollectionAccessASTNode(array, indexNodes),
                                                value,
                                                OperationType.ADD
                                        )
                                ));

                                i = expression.second;

                                continue;
                            }
                            case "-=" -> {
                                body.addNode(new CollectionSetASTNode(
                                        array,
                                        indexNodes,
                                        new OperationASTNode(
                                                new CollectionAccessASTNode(array, indexNodes),
                                                value,
                                                OperationType.SUBTRACT
                                        )
                                ));

                                i = expression.second;

                                continue;
                            }
                            case "*=" -> {
                                body.addNode(new CollectionSetASTNode(
                                        array,
                                        indexNodes,
                                        new OperationASTNode(
                                                new CollectionAccessASTNode(array, indexNodes),
                                                value,
                                                OperationType.MULTIPLY
                                        )
                                ));

                                i = expression.second;

                                continue;
                            }
                            case "/=" -> {
                                body.addNode(new CollectionSetASTNode(
                                        array,
                                        indexNodes,
                                        new OperationASTNode(
                                                new CollectionAccessASTNode(array, indexNodes),
                                                value,
                                                OperationType.DIVIDE
                                        )
                                ));

                                i = expression.second;

                                continue;
                            }
                            case "%=" -> {
                                body.addNode(new CollectionSetASTNode(
                                        array,
                                        indexNodes,
                                        new OperationASTNode(
                                                new CollectionAccessASTNode(array, indexNodes),
                                                value,
                                                OperationType.MODULO
                                        )
                                ));

                                i = expression.second;

                                continue;
                            }
                        }
                    }
                }
                else if(next != null && OperationType.fromSymbol(next) == null) {
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
                            if(bd.statements().length == 0) elseNode = null;
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

            i = expression.second;
        }
//        } // catch (IndexOutOfBoundsException e) {
//
//        }

        if(body.statements().length == 1) {
            return body.statements()[0];
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

        boolean awaitingOperator = false;
        boolean awaitingStartOperator = true;
        boolean awaitingOperand = true;

        boolean end = false;

        int i;
        for (i = start; i < tokens.size(); i++) {
            String token = getNext(tokens, i);

            if (token == null || token.equals(";")) {
                end = true;
                i--;
                break;
            }

            if (token.equals("\n")) {
                continue;
            }

            boolean b = token.equals("!") || token.equals("~") || token.equals("@");

            if (awaitingStartOperator && (b || token.equals("+") || token.equals("-"))) {
                if(token.equals("+")) expression.add("@+");
                else if(token.equals("-")) expression.add("@-");
                else expression.add(token);
                continue;
            }

            if (awaitingOperator && !b && (OperationType.fromSymbol(token) != null || token.equals(",") || token.equals("=") || token.equals("?"))) {
                expression.add(token);
                awaitingOperand = true;
                awaitingOperator = false;
                awaitingStartOperator = true;
                continue;
            }

            boolean b1 = token.equals("(") || token.equals("[") || token.equals("{");
            if (awaitingOperand && !b1) {
                expression.add(token);
                awaitingOperator = true;
                awaitingOperand = false;
                awaitingStartOperator = false;
                continue;
            }

            if (b1) {
                String e = token.equals("(") ? ")" : (token.equals("[") ? "]" : "}");

                int getPair = getPair(tokens, i, token, e);

                awaitingOperator = true;
                awaitingOperand = false;
                awaitingStartOperator = false;

                if (getPair == -1) {
                    throw new ParsingException("Unmatched parenthesis");
                }

                List<String> subExpression = new ArrayList<>(tokens.subList(i + 1, getPair));
                subExpression.removeIf(s -> s.equals("\n") || s.equals("\r"));

                expression.add(token);

                // If it's { and the token before was -> or =>, just put everything as it is until the end
                if(token.equals("{")) {
                    String prev = tokens.get(i - 1);
                    if(prev != null && (prev.equals("->") || prev.equals("=>"))) {
                        expression.addAll(subExpression);
                        expression.add("}");

                        i = getPair;
                        continue;
                    }
                }

                Pair<List<String>, Integer> subExpressionResult = fetchExpression(subExpression, 0);
                expression.addAll(subExpressionResult.first);
                expression.add(e);

                i = getPair;

                continue;
            }

            break;
        }

        if(awaitingOperator && !end) i--;

        return new Pair<>(expression, i);
    }

    public static List<String> infixToPostfix(List<String> infix) throws ParsingException {
        List<String> postfixExpression = new ArrayList<>();
        Stack<String> operatorStack = new Stack<>();

        for (int i = 0; i < infix.size(); i++) {
            String token = infix.get(i);

            switch (token) {
                case "{" -> {
                    int end = getPair(infix, i, "{", "}");

                    postfixExpression.add("{");

                    List<String> subExpression = new ArrayList<>(infix.subList(i + 1, end));

                    int next = getNextSeparator(subExpression);
                    while (next != -1) {
                        int colon = subExpression.indexOf(":");
                        int equals = subExpression.indexOf("=");

                        if (equals != -1 && (colon == -1 || equals < colon)) {
                            colon = equals;
                        }

                        List<String> keyList = subExpression.subList(0, colon);
                        List<String> valueList = subExpression.subList(colon + 1, next);

                        postfixExpression.addAll(infixToPostfix(keyList));
                        postfixExpression.add("=");
                        postfixExpression.addAll(infixToPostfix(valueList));

                        subExpression = subExpression.subList(next + 1, subExpression.size());
                        next = getNextSeparator(subExpression);

                        postfixExpression.add(",");
                    }

                    if (!subExpression.isEmpty()) {
                        int colon = subExpression.indexOf(":");
                        int equals = subExpression.indexOf("=");

                        if (equals != -1 && (colon == -1 || equals < colon)) {
                            colon = equals;
                        }

                        List<String> keyList = subExpression.subList(0, colon);
                        List<String> valueList = subExpression.subList(colon + 1, subExpression.size());

                        postfixExpression.addAll(infixToPostfix(keyList));
                        postfixExpression.add("=");
                        postfixExpression.addAll(infixToPostfix(valueList));
                    }

                    postfixExpression.add("}");

                    i = end;

                    continue;
                }
                case "[" -> {
                    int end = getPair(infix, i, "[", "]");

                    postfixExpression.add("[");

                    List<String> subExpression = new ArrayList<>(infix.subList(i + 1, end));

                    int next = getNextSeparator(subExpression);
                    while (next != -1) {
                        postfixExpression.addAll(infixToPostfix(subExpression.subList(0, next)));
                        postfixExpression.add(",");

                        subExpression = subExpression.subList(next + 1, subExpression.size());
                        next = getNextSeparator(subExpression);
                    }

                    postfixExpression.addAll(infixToPostfix(subExpression));

                    postfixExpression.add("]");

                    i = end;

                    continue;
                }
                case ",", "?", "=" -> {
                    while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                        postfixExpression.add(operatorStack.pop());
                    }
                    postfixExpression.add(token);
                    continue;
                }
                case "!", "~", "@", "@-", "@+" -> {
                    operatorStack.push(token);
                    continue;
                }
                case ";" -> {
                    while (!operatorStack.isEmpty()) {
                        postfixExpression.add(operatorStack.pop());
                    }

                    return postfixExpression;
                }
            }

            if (token.matches(Utils.IDENTIFIER_REGEX)) {
                // lambdas but for single tokens
                if(i + 1 < infix.size() && (infix.get(i + 1).equals("->") || infix.get(i + 1).equals("=>"))) {
                    String next = infix.get(i + 1);

                    postfixExpression.add("(");
                    postfixExpression.add(token);
                    postfixExpression.add(")");
                    postfixExpression.add(next);

                    int nextIndex = findNext(infix, i + 1, next);

                    String nextNext = getNext(infix, nextIndex + 1);
                    int nextNextIndex = findNext(infix, nextIndex + 1, nextNext);

                    if(nextNext == null || !nextNext.equals("{")) {
                        throw new ParsingException("Expected {");
                    }

                    int end = getPair(infix, nextNextIndex, "{", "}");

                    postfixExpression.add("{");
                    postfixExpression.addAll(infix.subList(nextNextIndex + 1, end));
                    postfixExpression.add("}");

                    i = end;

                    continue;
                }

                postfixExpression.add(token);
            } else if (OperationType.fromSymbol(token) != null) {
                while (!operatorStack.isEmpty() && OperationType.fromSymbol(operatorStack.peek()) != null && precedence(token) <= precedence(operatorStack.peek())) {
                    postfixExpression.add(operatorStack.pop());
                }

                operatorStack.push(token);
            } else if (token.equals("(")) {
                // Check if at the end of the parenthesis there is -> or =>
                int pair = getPair(infix, i, "(", ")");

                String next = getNext(infix, pair + 1);

                if(next != null && (next.equals("->") || next.equals("=>"))) {
                    List<String> subExpression = infix.subList(i + 1, pair);

                    // And in this case just add everything as it is
                    postfixExpression.add("(");
                    postfixExpression.addAll(subExpression);
                    postfixExpression.add(")");
                    postfixExpression.add(next);

                    int nextIndex = findNext(infix, pair + 1, next);

                    String nextNext = getNext(infix, nextIndex + 1);
                    int nextNextIndex = findNext(infix, nextIndex + 1, nextNext);

                    if(nextNext == null || !nextNext.equals("{")) {
                        throw new ParsingException("Expected {");
                    }

                    int end = getPair(infix, nextNextIndex, "{", "}");

                    postfixExpression.add("{");
                    postfixExpression.addAll(infix.subList(nextNextIndex + 1, end));
                    postfixExpression.add("}");

                    i = end;

                    continue;
                }

                // Check whether the last token is an identifier
                if (!postfixExpression.isEmpty() && postfixExpression.get(postfixExpression.size() - 1).matches(Utils.IDENTIFIER_REGEX)) {
                    // Add all the operators to the postfix expression from this to next ")"

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

            else if (token.equals("!") || token.equals("~") || token.equals("@")) {
                ASTNode node = stack.pop();

                OperationASTNode operation = new OperationASTNode(node, null, OperationType.fromSymbol(token));

                if (node instanceof ValueASTNode) {
                    stack.push(new ValueASTNode(operation.evaluate(null)));
                } else {
                    stack.push(operation);
                }

                continue;
            }

            // If the token is - or +, but there is only 1 element on the stack, treat it as a prefix, and apply it to 0
            else if (token.equals("@-") || token.equals("@+")) {
                ASTNode node = stack.pop();

                stack.push(new OperationASTNode(new ValueASTNode(0), node, OperationType.fromSymbol(token.substring(1))));

                continue;
            }

            else if (OperationType.fromSymbol(token) != null) {
                ASTNode right = stack.pop();
                ASTNode left = stack.pop();
                OperationType operationType = OperationType.fromSymbol(token);

                if(operationType != OperationType.REF) {
                    stack.push(new OperationASTNode(left, right, operationType));

                    // Preprocess the AST. If they're both values, then we can just evaluate them
                    if (left instanceof ValueASTNode && right instanceof ValueASTNode) {
                        stack.push(new ValueASTNode(stack.pop().evaluate(null)));
                    }

                } else {
                    stack.push(new ReferenceASTNode(left, right));

                    if(right instanceof ValueASTNode) {
                        throw new ParsingException("Invalid reference");
                    }

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
                if (i - 1 >= 0) {
                    String last = postfix.get(i - 1);

                    if (last.matches(Utils.IDENTIFIER_REGEX) || last.equals("]") || last.equals(")") || last.equals("}")) {
                        ASTNode array = stack.pop();

                        List<List<String>> indexes = new ArrayList<>();

                        int pair = getPair(postfix, i, "[", "]");

                        indexes.add(postfix.subList(i + 1, pair));

                        int start = pair + 1;
                        while (pair < postfix.size() - 1 && postfix.get(start).equals("[")) {
                            pair = getPair(postfix, start, "[", "]");

                            if(pair == -1) break;

                            List<String> subList = postfix.subList(start + 1, pair);
                            indexes.add(subList);

                            start = pair + 1;
                        }

                        List<ASTNode> indexNodes = new ArrayList<>();

                        for (List<String> index : indexes) {
                            indexNodes.add(postfixToAST(index, context));
                        }

                        stack.push(new CollectionAccessASTNode(array, indexNodes));

                        i = pair;

                        continue;
                    }
                }

                ArrayList<ASTNode> list = new ArrayList<>();

                int pair = getPair(postfix, i, "[", "]");

                List<String> subExpression = postfix.subList(i + 1, pair);

                int next = getNextSeparator(subExpression);
                while (next != -1) {
                    List<String> subList = subExpression.subList(0, next);
                    list.add(postfixToAST(subList, context));

                    subExpression = subExpression.subList(next + 1, subExpression.size());
                    next = getNextSeparator(subExpression);
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

                int next = getNextSeparator(subExpression);
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

                    next = getNextSeparator(subExpression);
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
                int pair = getPair(postfix, i, "(", ")");

                // First, check if at the end of the parenthesis there is -> or =>
                String nextToken = getNext(postfix, pair + 1);
                if(nextToken != null && (nextToken.equals("->") || nextToken.equals("=>"))) {
                    // Now get the following expression. it should start with {, otherwise it's invalid and error
                    String startToken = getNext(postfix, pair + 2);
                    int startIndex = findNext(postfix, pair + 2, startToken);

                    if(startToken != null && startToken.equals("{")) {
                        int expressionEnd = getPair(postfix, startIndex, "{", "}");

                        List<String> subExpression = postfix.subList(startIndex + 1, expressionEnd);

                        ASTNode lambda = parse(subExpression, context);

                        // Now parse what is inside the parenthesis as parameters
                        List<String> params = postfix.subList(i + 1, pair);

                        // Remove commas
                        params.removeIf(s -> s.equals(","));

                        // Throw an error if any of the parameters is not an identifier
                        if(params.stream().anyMatch(s -> !s.matches(Utils.IDENTIFIER_REGEX))) {
                            throw new ParsingException("Invalid lambda expression");
                        }

                        List<Pair<String, Variable.Type>> args = new ArrayList<>();
                        for (String param : params) {
                            args.add(new Pair<>(param, Variable.Type.ANY));
                        }

                        // Now make a function from the lambda
                        stack.push(new OrbitFunction(
                                null,
                                args.size(),
                                args,
                                lambda,
                                Variable.Type.ANY
                        ));

                        i = expressionEnd;

                        continue;
                    } else {
                        throw new ParsingException("Invalid lambda expression");
                    }
                }

                ASTNode func = stack.pop();

                if(!(func instanceof VariableASTNode)) {
                    throw new ParsingException("Expected identifier before function call");
                }

                String name = ((VariableASTNode) func).name();

                List<ASTNode> params = new ArrayList<>();

                List<String> subExpression = postfix.subList(i + 1, pair);

                int next = getNextSeparator(subExpression);
                while (next != -1) {
                    List<String> subList = subExpression.subList(0, next);
                    params.add(postfixToAST(subList, context));

                    subExpression = subExpression.subList(next + 1, subExpression.size());
                    next = getNextSeparator(subExpression);
                }

                if (!subExpression.isEmpty()) {
                    params.add(postfixToAST(subExpression, context));
                }

                stack.push(new FunctionCallASTNode(name, params));

                // Check whether there is a baked function with the same name and param count in the context
                if(context.hasBakedFunction(name, params.size())) {
                    stack.pop();

                    Class<? extends BFunction> function = context.getBakedFunction(name, params.size());

                    BFunction instance = null;

                    try {
                        instance = function.getConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    if(instance == null) {
                        throw new ParsingException("Failed to create instance of baked function");
                    }

                    instance.setValues(params);

                    stack.push(instance);
                }

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
            case "~", "!", "@-", "@+" -> 11;
            case ":", "@", "->", "=>" -> 12;
            default -> -1;
        };
    }

    // next comma, that is on the same layer (so if there's depth of parenthesis or brackets, it will ignore them)
    private static int getNextSeparator(List<String> tokens) {
        int depth = 0;
        for (int i = 0; i < tokens.size(); i++) {
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
