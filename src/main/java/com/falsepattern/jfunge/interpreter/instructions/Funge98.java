package com.falsepattern.jfunge.interpreter.instructions;

import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

public class Funge98 implements InstructionSet {
    @Override
    public void load(ObjIntConsumer<Instruction> instructionSet) {
        for (int i = 0; i <= 9; i++) {
            int finalI = i;
            instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().push(finalI), '0' + i);
        }
        for (int i = 0; i <= 5; i++) {
            int finalI = i;
            instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().push(10 + finalI), 'a' + i);
        }
        instructionSet.accept((ctx) -> ctx.IP().delta.set(1, 0, 0), '>');
        instructionSet.accept((ctx) -> ctx.IP().delta.set(0, 1, 0), 'v');
        instructionSet.accept((ctx) -> ctx.IP().delta.set(-1, 0, 0), '<');
        instructionSet.accept((ctx) -> ctx.IP().delta.set(0, -1, 0), '^');
        instructionSet.accept((ctx) -> ctx.IP().position.add(ctx.IP().delta), '#');
        instructionSet.accept((ctx) -> System.out.print(ctx.IP().stackStack.TOSS().pop()), '.');
        instructionSet.accept((ctx) -> System.out.print((char)ctx.IP().stackStack.TOSS().pop()), ',');
        instructionSet.accept((ctx) -> ctx.IP().delta.mul(-1), 'r');
        instructionSet.accept((ctx) -> ctx.IP().die(), '@');
    }

    @Override
    public void unload(IntConsumer instructionSet) {

    }
}
