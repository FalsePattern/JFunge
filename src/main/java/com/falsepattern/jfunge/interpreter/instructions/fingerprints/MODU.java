package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.instructions.Funge98;
import com.falsepattern.jfunge.interpreter.instructions.Instruction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MODU implements Fingerprint {
    public static final MODU INSTANCE = new MODU();

    private static int safeMod(int a, int b) {
        return b == 0 ? 0 : a % b;
    }
    @Override
    public void load(ObjIntConsumer<Instruction> instructionSet) {
        instructionSet.accept((ctx) -> Funge98.binop(ctx, (a, b) -> {
            return (b < 0 ? -1 : 1) * safeMod(a, b);
        }), 'M');
        instructionSet.accept((ctx) -> Funge98.binop(ctx, (a, b) -> {
            if (b < 0) {
                a = -a;
                b = -b;
            }
            return safeMod(a, b);
        }), 'U');
        instructionSet.accept((ctx) -> Funge98.binop(ctx, (a, b) -> safeMod(a, b)), 'R');
    }

    @Override
    public void unload(IntConsumer instructionSet) {
        instructionSet.accept('M');
        instructionSet.accept('U');
        instructionSet.accept('R');
    }

    @Override
    public int code() {
        return 0x4d4f4455;
    }
}
