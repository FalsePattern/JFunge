package com.falsepattern.jfunge.interpreter.instructions;

import com.falsepattern.jfunge.interpreter.LambdaHelper;
import lombok.val;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

public interface InstructionSet {
    default void load(ObjIntConsumer<Instruction> instructionSet) {
        val clazz = this.getClass();
        Arrays.stream(clazz.getDeclaredMethods())
              .filter((method) -> Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(Instr.class))
              .forEach((method) -> {
                  try {
                      val lambda = (Instruction) LambdaHelper.newLambdaMetaFactory(Instruction.class, method).invokeExact();
                      val ann = method.getAnnotation(Instr.class);
                      instructionSet.accept(lambda, ann.value());
                  } catch (Throwable e) {
                      throw new RuntimeException(e);
                  }
              });
    }

    default void unload(IntConsumer instructionSet) {
        val clazz = this.getClass();
        Arrays.stream(clazz.getDeclaredMethods())
              .filter((method) -> Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(Instr.class))
              .forEach((method) -> {
                  val ann = method.getAnnotation(Instr.class);
                  instructionSet.accept(ann.value());
              });
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Instr {
        int value();
    }
}
