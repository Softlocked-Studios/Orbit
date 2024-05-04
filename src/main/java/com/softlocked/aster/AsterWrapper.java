package com.softlocked.aster;

public class AsterWrapper {
    public int[] instructions;
    public int[] values;

    int currentInstruction = 0;

    public AsterWrapper() {
        this.instructions = new int[4096];
        this.values = new int[4096];
    }

    public AsterWrapper HALT() {
        instructions[currentInstruction] = 0;
        currentInstruction++;

        return this;
    }

    public AsterWrapper PUSH(int value) {
        instructions[currentInstruction] = 1;
        values[currentInstruction] = value;
        currentInstruction++;

        return this;
    }

    public AsterWrapper PUSH_VAR(int value) {
        instructions[currentInstruction] = 2;
        values[currentInstruction] = value;
        currentInstruction++;

        return this;
    }

    public AsterWrapper COPY() {
        instructions[currentInstruction] = 3;
        currentInstruction++;

        return this;
    }

    public AsterWrapper POP(int value) {
        instructions[currentInstruction] = 4;
        values[currentInstruction] = value;
        currentInstruction++;

        return this;
    }

    public AsterWrapper ADD() {
        instructions[currentInstruction] = 6;
        currentInstruction++;

        return this;
    }

    public AsterWrapper SUB() {
        instructions[currentInstruction] = 7;
        currentInstruction++;

        return this;
    }

    public AsterWrapper MUL() {
        instructions[currentInstruction] = 8;
        currentInstruction++;

        return this;
    }

    public AsterWrapper DIV() {
        instructions[currentInstruction] = 9;
        currentInstruction++;

        return this;
    }

    public AsterWrapper MOD() {
        instructions[currentInstruction] = 10;
        currentInstruction++;

        return this;
    }

    public AsterWrapper CMP() {
        instructions[currentInstruction] = 11;
        currentInstruction++;

        return this;
    }

    public AsterWrapper JUMP(int value) {
        instructions[currentInstruction] = 12;
        values[currentInstruction] = value;
        currentInstruction++;

        return this;
    }

    public AsterWrapper JUMP_IF_EQUAL(int value) {
        instructions[currentInstruction] = 13;
        values[currentInstruction] = value;
        currentInstruction++;

        return this;
    }

    public AsterWrapper JUMP_IF_NOT_EQUAL(int value) {
        instructions[currentInstruction] = 14;
        values[currentInstruction] = value;
        currentInstruction++;

        return this;
    }

    public AsterWrapper JUMP_IF_GREATER(int value) {
        instructions[currentInstruction] = 15;
        values[currentInstruction] = value;
        currentInstruction++;

        return this;
    }

    public AsterWrapper JUMP_IF_LESS(int value) {
        instructions[currentInstruction] = 16;
        values[currentInstruction] = value;
        currentInstruction++;

        return this;
    }

    public AsterWrapper JUMP_IF_GREATER_OR_EQUAL(int value) {
        instructions[currentInstruction] = 17;
        values[currentInstruction] = value;
        currentInstruction++;

        return this;
    }

    public AsterWrapper JUMP_IF_LESS_OR_EQUAL(int value) {
        instructions[currentInstruction] = 18;
        values[currentInstruction] = value;
        currentInstruction++;

        return this;
    }

    public AsterWrapper GOTO(int value) {
        instructions[currentInstruction] = 19;
        values[currentInstruction] = value;
        currentInstruction++;

        return this;
    }
}
