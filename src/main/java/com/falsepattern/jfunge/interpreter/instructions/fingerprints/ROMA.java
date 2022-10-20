package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ROMA implements Fingerprint {
    public static final ROMA INSTANCE = new ROMA();

    @Instr('I')
    public static void I(ExecutionContext ctx) {
        ctx.stack().push(1);
    }

    @Instr('V')
    public static void V(ExecutionContext ctx) {
        ctx.stack().push(5);
    }

    @Instr('X')
    public static void X(ExecutionContext ctx) {
        ctx.stack().push(10);
    }

    @Instr('L')
    public static void L(ExecutionContext ctx) {
        ctx.stack().push(50);
    }

    @Instr('C')
    public static void C(ExecutionContext ctx) {
        ctx.stack().push(100);
    }

    @Instr('D')
    public static void D(ExecutionContext ctx) {
        ctx.stack().push(500);
    }

    @Instr('M')
    public static void M(ExecutionContext ctx) {
        ctx.stack().push(1000);
    }

    @Override
    public int code() {
        return 0x524F4D41;
    }
}
