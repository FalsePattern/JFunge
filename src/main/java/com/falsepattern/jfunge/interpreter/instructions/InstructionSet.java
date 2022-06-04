package com.falsepattern.jfunge.interpreter.instructions;

import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

public interface InstructionSet {
    void load(ObjIntConsumer<Instruction> instructionSet);

    void unload(IntConsumer instructionSet);
}
