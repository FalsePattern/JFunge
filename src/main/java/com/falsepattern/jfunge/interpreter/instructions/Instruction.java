package com.falsepattern.jfunge.interpreter.instructions;

import com.falsepattern.jfunge.interpreter.ExecutionContext;

public interface Instruction {
    void process(ExecutionContext context);
}
