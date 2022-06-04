package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.instructions.Instruction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ROMA implements Fingerprint {
    public static final ROMA INSTANCE = new ROMA();
    @Override
    public void load(ObjIntConsumer<Instruction> instructionSet) {
        instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().push(1), 'I');
        instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().push(5), 'V');
        instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().push(10), 'X');
        instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().push(50), 'L');
        instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().push(100), 'C');
        instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().push(500), 'D');
        instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().push(1000), 'M');
    }

    @Override
    public void unload(IntConsumer instructionSet) {
        instructionSet.accept('I');
        instructionSet.accept('V');
        instructionSet.accept('X');
        instructionSet.accept('L');
        instructionSet.accept('C');
        instructionSet.accept('D');
        instructionSet.accept('M');
    }

    @Override
    public int code() {
        return 0x524F4D41;
    }
}
