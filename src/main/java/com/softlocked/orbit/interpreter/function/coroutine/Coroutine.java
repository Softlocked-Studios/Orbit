package com.softlocked.orbit.interpreter.function.coroutine;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.core.datatypes.functions.IFunction;
import com.softlocked.orbit.core.evaluator.Breakpoint;
import com.softlocked.orbit.interpreter.ast.generic.BodyASTNode;
import com.softlocked.orbit.interpreter.ast.loops.WhileASTNode;
import com.softlocked.orbit.interpreter.ast.loops.forloops.*;
import com.softlocked.orbit.interpreter.ast.statement.ConditionalASTNode;
import com.softlocked.orbit.interpreter.function.OrbitFunction;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Pair;
import com.softlocked.orbit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Coroutine {
    protected CoroutineFunction func;

    protected ASTNode body;
    protected ILocalContext context;
    protected ASTNode head; // Used to store the head of the coroutine. This is the first node that will be executed when the coroutine is started.
    protected ILocalContext headContext;

    protected List<Object> args;

    public Coroutine(ILocalContext context, CoroutineFunction function, List<Object> args) {
        this.body = function.getBody();
        this.head = body;
        this.headContext = context;

        this.context = context;
        this.func = function;

        this.args = args;
    }

    public CoroutineFunction getFunction() {
        return func;
    }

    public List<Object> getArgs() {
        return args;
    }

    public boolean isFinished() {
        return finished;
    }

    boolean finished = false;
    public Object resume() {
        if(finished) {
            return null;
        }

        BodyASTNode body = (BodyASTNode) this.body;

        if(head == body) {
            Object value = body.evaluate(context);

            if (value instanceof Breakpoint breakpoint) {
                if (breakpoint.getType() == Breakpoint.Type.YIELD) {
                    head = breakpoint.getNode();
                    headContext = breakpoint.getContext();

                    if (head == body.statements()[body.statements().length - 1]) {
                        finished = true;
                    }
                }
                if (breakpoint.getType() == Breakpoint.Type.RETURN) {
                    finished = true;
                }
                return breakpoint.getValue();
            }

            return value;
        }
        // Find the head of the coroutine
        Pair<ASTNode, ASTNode> result = findNode(body, head);

        if (result == null) {
            return null;
        }

        ASTNode parent = result.first;

        while (parent instanceof ConditionalASTNode) {
            result = findNode(body, parent);
            parent = result.first;

            headContext = headContext == context ? context : headContext.getParent();
        }

        // Resume the coroutine
        while(parent != body) {
            if (parent instanceof BodyASTNode bodyASTNode) {
                Object value = bodyASTNode.evaluateFrom(headContext, result.second);

                if (value instanceof Breakpoint breakpoint) {
                    if (breakpoint.getType() == Breakpoint.Type.YIELD) {
                        head = breakpoint.getNode();
                        headContext = breakpoint.getContext();

                        // Check if the head is the last node in the body, and if so, the coroutine is finished
                        if (head == body.statements()[body.statements().length - 1]) {
                            finished = true;
                        }
                    }
                    if (breakpoint.getType() == Breakpoint.Type.RETURN) {
                        finished = true;
                    }
                    return breakpoint.getValue();
                }
            } else {
                Object value = parent.evaluate(headContext);

                if (value instanceof Breakpoint breakpoint) {
                    if (breakpoint.getType() == Breakpoint.Type.YIELD) {
                        head = breakpoint.getNode();
                        headContext = breakpoint.getContext();

                        if (head == body.statements()[body.statements().length - 1]) {
                            finished = true;
                        }
                    }
                    if (breakpoint.getType() == Breakpoint.Type.RETURN) {
                        finished = true;
                    }
                    return breakpoint.getValue();
                }
            }

            result = findNode(body, parent);

            parent = result.first;

            if(!(parent instanceof BodyASTNode)) {
                headContext = headContext == context ? context : headContext.getParent();
            }
        }

        // Now evaluate the body
        Object resultValue = body.evaluateFrom(headContext, result.second);

        if (resultValue instanceof Breakpoint breakpoint) {
            if (breakpoint.getType() == Breakpoint.Type.YIELD) {
                head = breakpoint.getNode();
                headContext = breakpoint.getContext();

                if (head == body.statements()[body.statements().length - 1]) {
                    finished = true;
                }
            }
            if (breakpoint.getType() == Breakpoint.Type.RETURN) {
                finished = true;
            }
            return breakpoint.getValue();
        }

        return resultValue;
    }

    // Finds parent and required node to resume the coroutine
    private Pair<ASTNode, ASTNode> findNode(ASTNode where, ASTNode node) {
        if (where instanceof BodyASTNode bodyASTNode) {
            for (ASTNode child : bodyASTNode.statements()) {
                if (child == node) {
                    return new Pair<>(where, node);
                }

                Pair<ASTNode, ASTNode> result = findNode(child, node);

                if (result != null) {
                    return result;
                }
            }
            return null;
        }
        else if (where instanceof ConditionalASTNode conditionalASTNode) {
            if(conditionalASTNode.thenBranch() == node || conditionalASTNode.elseBranch() == node) {
                return new Pair<>(where, node);
            }
            Pair<ASTNode, ASTNode> result = findNode(conditionalASTNode.thenBranch(), node);

            if (result != null) {
                return result;
            }

            if (conditionalASTNode.elseBranch() != null) {
                return findNode(conditionalASTNode.elseBranch(), node);
            }
        }
        else if (where instanceof WhileASTNode whileASTNode) {
            if(whileASTNode.body() == node) {
                return new Pair<>(where, node);
            }
            return findNode(whileASTNode.body(), node);
        }
        else if (where instanceof ForInASTNode forInASTNode) {
            if(forInASTNode.body() == node) {
                return new Pair<>(where, node);
            }
            return findNode(forInASTNode.body(), node);
        }
        else if (where instanceof ForToASTNode forToASTNode) {
            if(forToASTNode.body() == node) {
                return new Pair<>(where, node);
            }
            return findNode(forToASTNode.body(), node);
        }
        else if (where instanceof ForDowntoASTNode forDowntoASTNode) {
            if(forDowntoASTNode.body() == node) {
                return new Pair<>(where, node);
            }
            return findNode(forDowntoASTNode.body(), node);
        }
        return null;
    }

    private boolean isChildOf(ASTNode where, ASTNode what) {
        return findNode(where, what) != null;
    }

    @Override
    public String toString() {
        return "coroutine(" + func.getName() + ")";
    }

    public ILocalContext getContext() {
        return context;
    }
}
