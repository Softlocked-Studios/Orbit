package com.softlocked.orbit.interpreter.ast.operation;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.ast.operation.OperationType;
import com.softlocked.orbit.core.evaluator.Evaluator;
import com.softlocked.orbit.memory.ILocalContext;

public record OperationASTNode(ASTNode left, ASTNode right, OperationType type) implements ASTNode {

    @Override
    public Object evaluate(ILocalContext context) {
        Object left = this.left().evaluate(context);

        switch (this.type()) {
            case NOT -> {
                return Evaluator.not(left);
            }
            case BITWISE_NOT -> {
                return Evaluator.bitwiseNot(left);
            }
            case CLONE -> {
                return Evaluator.cloneObject(left);
            }
        }

        Object right = this.right().evaluate(context);

        try {
            switch (this.type()) {
                // Arithmetic operations
                case ADD -> {
                    return Evaluator.add(left, right);
                }
                case SUBTRACT -> {
                    return Evaluator.subtract(left, right);
                }
                case MULTIPLY -> {
                    return Evaluator.multiply(left, right);
                }
                case DIVIDE -> {
                    return Evaluator.divide(left, right);
                }
                case MODULO -> {
                    return Evaluator.modulo(left, right);
                }
                case POWER -> {
                    return Evaluator.power(left, right);
                }
                // Comparison operations
                case EQUALS -> {
                    return Evaluator.equal(left, right);
                }
                case NOT_EQUALS -> {
                    return Evaluator.notEquals(left, right);
                }
                case GREATER_THAN -> {
                    return Evaluator.greaterThan(left, right);
                }
                case LESS_THAN -> {
                    return Evaluator.lessThan(left, right);
                }
                case GREATER_THAN_OR_EQUALS -> {
                    return Evaluator.greaterThanOrEquals(left, right);
                }
                case LESS_THAN_OR_EQUALS -> {
                    return Evaluator.lessThanOrEquals(left, right);
                }
                // Logical operations
                case AND -> {
                    return Evaluator.and(left, right);
                }
                case OR -> {
                    return Evaluator.or(left, right);
                }
                // Bitwise operations
                case BITWISE_AND -> {
                    return Evaluator.bitwiseAnd(left, right);
                }
                case BITWISE_OR -> {
                    return Evaluator.bitwiseOr(left, right);
                }
                case BITWISE_XOR -> {
                    return Evaluator.bitwiseXor(left, right);
                }
                case BITWISE_LEFT_SHIFT -> {
                    return Evaluator.bitwiseLeftShift(left, right);
                }
                case BITWISE_RIGHT_SHIFT -> {
                    return Evaluator.bitwiseRightShift(left, right);
                }
                case EQUALS_TYPE -> {
                    return Evaluator.equalsType(left, right);
                }
                default -> {
                    return Evaluator.customOverload(left, right, this.type().getSymbol());
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error while evaluating operation", e);
        }
    }

    public static boolean toBool(Object o) {
        if (o instanceof Boolean bool) {
            return bool;
        } else if (o instanceof Number num) {
            return num.longValue() != 0;
        } else if (o instanceof String str) {
            return !str.isEmpty();
        } else if (o instanceof Character ch) {
            return ch != 0;
        } else {
            return o != null;
        }
    }
}
