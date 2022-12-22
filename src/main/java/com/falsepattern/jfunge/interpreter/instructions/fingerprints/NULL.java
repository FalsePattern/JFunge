package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import com.falsepattern.jfunge.interpreter.instructions.Instruction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NULL implements Fingerprint {
    public static final NULL INSTANCE = new NULL();

    @Override
    public void load(ObjIntConsumer<Instruction> instructionSet) {
        for (int i = 'A'; i <= 'Z'; i++) {
            instructionSet.accept((ctx) -> ctx.interpret('r'), i);
        }
    }

    @Override
    public void unload(IntConsumer instructionSet) {
        for (int i = 'A'; i <= 'Z'; i++) {
            instructionSet.accept(i);
        }
    }

    @Override
    public int code() {
        return 0x4e554c4c;
    }
}
