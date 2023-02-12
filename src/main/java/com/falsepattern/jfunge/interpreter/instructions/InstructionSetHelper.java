package com.falsepattern.jfunge.interpreter.instructions;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.ObjIntConsumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class InstructionSetHelper {
    private static final Map<Class<?>, Map<Integer, Instruction>> sets = new HashMap<>();

    private static Map<Integer, Instruction> retrieve(Class<?> instructionSet) {
        if (!sets.containsKey(instructionSet)) {
            val currentSet = new HashMap<Integer, Instruction>();
            for (val method : instructionSet.getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers()) ||
                    !method.isAnnotationPresent(InstructionSet.Instr.class)) {
                    continue;
                }
                val proxy = InstructionFactory.createInstruction(method);
                val ann = method.getAnnotation(InstructionSet.Instr.class);
                val id = ann.value();
                currentSet.put(id, proxy);
            }
            sets.put(instructionSet, currentSet);
            return currentSet;
        } else {
            return sets.get(instructionSet);
        }
    }

    static void loadInstructionSet(Class<?> instructionSet, ObjIntConsumer<Instruction> loader) {
        val set = retrieve(instructionSet);
        for (val entry : set.entrySet()) {
            loader.accept(entry.getValue(), entry.getKey());
        }
    }

    static void unloadInstructionSet(Class<?> instructionSet, IntFunction<Instruction> unLoader) {
        val set = retrieve(instructionSet);
        for (val i : set.keySet()) {
            unLoader.apply(i);
        }
    }
}
