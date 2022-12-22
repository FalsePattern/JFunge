package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import com.falsepattern.jfunge.interpreter.instructions.Funge98;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MODU implements Fingerprint {
    public static final MODU INSTANCE = new MODU();

    private static int safeMod(int a, int b) {
        return b == 0 ? 0 : a % b;
    }

    @Instr('M')
    public static void signedResult(ExecutionContext ctx) {
        Funge98.binop(ctx, (a, b) -> (b < 0 ? -1 : 1) * safeMod(a, b));
    }

    @Instr('U')
    public static void samHolden(ExecutionContext ctx) {
        Funge98.binop(ctx, (a, b) -> {
            if (b < 0) {
                a = -a;
                b = -b;
            }
            return safeMod(a, b);
        });
    }

    @Instr('R')
    public static void remainder(ExecutionContext ctx) {
        Funge98.binop(ctx, MODU::safeMod);
    }

    @Override
    public int code() {
        return 0x4d4f4455;
    }
}
