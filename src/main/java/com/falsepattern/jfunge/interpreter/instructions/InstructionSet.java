package com.falsepattern.jfunge.interpreter.instructions;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import lombok.val;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

public interface InstructionSet {
    default void load(ObjIntConsumer<Instruction> instructionSet) {
        val clazz = this.getClass();
        Arrays.stream(clazz.getDeclaredMethods()).filter((method) -> Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(Instr.class)).forEach((method) -> {
            val lookup = MethodHandles.lookup();
            val methodType = MethodType.methodType(void.class, ExecutionContext.class);
            try {
                val lambda = (Instruction) LambdaMetafactory.metafactory(lookup,
                                                                    "process",
                                                                    MethodType.methodType(Instruction.class),
                                                                    methodType,
                                                                    lookup.findStatic(clazz, method.getName(), methodType),
                                                                    methodType)
                        .getTarget()
                        .invokeExact();
                val ann = method.getAnnotation(Instr.class);
                instructionSet.accept(lambda, ann.value());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    default void unload(IntConsumer instructionSet) {
        val clazz = this.getClass();
        Arrays.stream(clazz.getDeclaredMethods()).filter((method) -> Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(Instr.class)).forEach((method) -> {
            val ann = method.getAnnotation(Instr.class);
            instructionSet.accept(ann.value());
        });
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Instr {
        int value();
    }
}
