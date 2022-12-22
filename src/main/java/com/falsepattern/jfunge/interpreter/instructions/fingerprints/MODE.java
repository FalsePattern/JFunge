package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import com.falsepattern.jfunge.interpreter.instructions.Funge98;
import com.falsepattern.jfunge.interpreter.instructions.InstructionSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MODE implements Fingerprint {
    public static final MODE INSTANCE = new MODE();
    private static final int HM_BIT = 1;
    private static final int SM_BIT = 2;
    private static final HoverMode HOVER_MODE = new HoverMode();
    private static final SwitchMode SWITCH_MODE = new SwitchMode();

    @Instr('H')
    public static void hoverMode(ExecutionContext ctx) {
        toggleMode(ctx, HOVER_MODE, HM_BIT);
    }

    @Instr('S')
    public static void switchMode(ExecutionContext ctx) {
        toggleMode(ctx, SWITCH_MODE, SM_BIT);
    }

    @Instr('I')
    public static void invertMode(ExecutionContext ctx) {
        ctx.IP().stackStack().invertMode(!ctx.IP().stackStack().invertMode());
    }

    @Instr('Q')
    public static void queueMode(ExecutionContext ctx) {
        ctx.IP().stackStack().queueMode(!ctx.IP().stackStack().queueMode());
    }

    private static void toggleMode(ExecutionContext ctx, InstructionSet set, int bit) {
        val ip = ctx.IP();
        val state = ip.customStorage().get("mode");
        var mode = (state & bit) == bit;
        mode = !mode;
        if (mode) {
            ip.instructionManager().loadInstructionSet(set);
        } else {
            ip.instructionManager().unloadInstructionSet(set);
        }
        ip.customStorage().put("mode", state ^ bit);
    }

    @Override
    public int code() {
        return 0x4d4f4445;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class HoverMode implements InstructionSet {
        @Instr('>')
        public static void east(ExecutionContext ctx) {
            ctx.IP().delta().add(1, 0, 0);
        }

        @Instr('v')
        public static void south(ExecutionContext ctx) {
            ctx.IP().delta().add(0, 1, 0);
        }

        @Instr('<')
        public static void west(ExecutionContext ctx) {
            ctx.IP().delta().add(-1, 0, 0);
        }

        @Instr('^')
        public static void north(ExecutionContext ctx) {
            ctx.IP().delta().add(0, -1, 0);
        }

        @Instr('h')
        public static void high(ExecutionContext ctx) {
            if (ctx.dimensions() == 3) {
                ctx.IP().delta().add(0, 0, 1);
            } else {
                ctx.interpret('r');
            }
        }

        @Instr('l')
        public static void low(ExecutionContext ctx) {
            if (ctx.dimensions() == 3) {
                ctx.IP().delta().add(0, 0, -1);
            } else {
                ctx.interpret('r');
            }
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class SwitchMode implements InstructionSet {
        private static void set(ExecutionContext ctx, int ch) {
            ctx.fungeSpace().set(ctx.IP().position(), ch);
        }

        @Instr('(')
        public static void loadFinger(ExecutionContext ctx) {
            Funge98.loadFinger(ctx);
            set(ctx, ')');
        }

        @Instr(')')
        public static void unloadFinger(ExecutionContext ctx) {
            Funge98.unloadFinger(ctx);
            set(ctx, '(');
        }

        @Instr('[')
        public static void turnLeft(ExecutionContext ctx) {
            Funge98.turnLeft(ctx);
            set(ctx, ']');
        }

        @Instr(']')
        public static void turnRight(ExecutionContext ctx) {
            Funge98.turnRight(ctx);
            set(ctx, '[');
        }

        @Instr('{')
        public static void blockStart(ExecutionContext ctx) {
            Funge98.blockStart(ctx);
            set(ctx, '}');
        }

        @Instr('}')
        public static void blockEnd(ExecutionContext ctx) {
            Funge98.blockEnd(ctx);
            set(ctx, '{');
        }
    }
}
