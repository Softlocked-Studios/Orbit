package com.softlocked.aster;

public class AsterRunner {
    private int[] stack;

    private int[] variables;

    int[] instructions;
    int[] values;

    public AsterRunner(AsterWrapper wrapper) {
        this.stack = new int[4096];
        this.variables = new int[1024];

        this.instructions = wrapper.instructions;
        this.values = wrapper.values;
    }

    public int run() {
        int instructionPointer = 0;
        int stackPointer = 0;

        int cmp = 0;

        while (instructionPointer < instructions.length) {
            int instruction = instructions[instructionPointer];
            int value = values[instructionPointer];

            switch (instruction) {
                // HALT
                case 0 -> {
                    return stack[stackPointer];
                }
                // PUSH
                case 1 -> stack[stackPointer++] = value;
                // PUSH_VAR
                case 2 -> stack[stackPointer++] = variables[value];
                // COPY
                case 3 -> stack[stackPointer++] = stack[stackPointer - 2];
                // POP
                case 4 -> variables[value] = stack[--stackPointer];
                // ADD
                case 6 -> {
                    stack[stackPointer - 2] = stack[stackPointer - 2] + stack[stackPointer - 1];
                    stackPointer--;
                }
                // SUB
                case 7 -> {
                    stack[stackPointer - 2] = stack[stackPointer - 2] - stack[stackPointer - 1];
                    stackPointer--;
                }
                // MUL
                case 8 -> {
                    stack[stackPointer - 2] = stack[stackPointer - 2] * stack[stackPointer - 1];
                    stackPointer--;
                }
                // DIV
                case 9 -> {
                    stack[stackPointer - 2] = stack[stackPointer - 2] / stack[stackPointer - 1];
                    stackPointer--;
                }
                // MOD
                case 10 -> {
                    stack[stackPointer - 2] = stack[stackPointer - 2] % stack[stackPointer - 1];
                    stackPointer--;
                }
                // CMP
                case 11 -> {
                    cmp = Integer.compare(stack[stackPointer - 2], stack[stackPointer - 1]);

                    stackPointer -= 2;
                }
                // JUMP
                case 12 -> {
                    instructionPointer += value - 1;
                }
                // JUMP_IF_EQUAL
                case 13 -> {
                    if (cmp == 0) {
                        instructionPointer += value - 1;
                    }
                }
                // JUMP_IF_NOT_EQUAL
                case 14 -> {
                    if (cmp != 0) {
                        instructionPointer += value - 1;
                    }
                }
                // JUMP_IF_GREATER
                case 15 -> {
                    if (cmp == 1) {
                        instructionPointer += value - 1;
                    }
                }
                // JUMP_IF_LESS
                case 16 -> {
                    if (cmp == -1) {
                        instructionPointer += value - 1;
                    }
                }
                // JUMP_IF_GREATER_OR_EQUAL
                case 17 -> {
                    if (cmp >= 0) {
                        instructionPointer += value - 1;
                    }
                }
                // JUMP_IF_LESS_OR_EQUAL
                case 18 -> {
                    if (cmp <= 0) {
                        instructionPointer += value - 1;
                    }
                }
                // GOTO
                case 19 -> instructionPointer = value - 1;
            }

            instructionPointer++;
        }

        // print the first 5 elements of the stack
        for (int i = 0; i < 5; i++) {
            System.out.println(stack[i]);
        }

        return stack[stackPointer];
    }
}
