package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import com.falsepattern.jfunge.interpreter.instructions.Instruction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.IntFunction;
import java.util.function.ObjIntConsumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NULL implements Fingerprint {
    public static final NULL INSTANCE = new NULL();

    @Override
    public void load(ObjIntConsumer<Instruction> loader) {
        for (int i = 'A'; i <= 'Z'; i++) {
            loader.accept((ctx) -> ctx.IP().reflect(), i);
        }
    }

    @Override
    public void unload(IntFunction<Instruction> unLoader) {
        for (int i = 'A'; i <= 'Z'; i++) {
            unLoader.apply(i);
        }
    }

    @Override
    public int code() {
        return 0x4e554c4c;
    }
}
