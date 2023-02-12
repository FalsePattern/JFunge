package com.falsepattern.jfunge.interpreter.instructions;

import lombok.val;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.ObjIntConsumer;

public interface InstructionSet {
    default void load(ObjIntConsumer<Instruction> loader) {
        InstructionSetHelper.loadInstructionSet(this.getClass(), loader);
    }

    default void unload(IntFunction<Instruction> unLoader) {
        InstructionSetHelper.unloadInstructionSet(this.getClass(), unLoader);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Instr {
        int value();
    }
}
