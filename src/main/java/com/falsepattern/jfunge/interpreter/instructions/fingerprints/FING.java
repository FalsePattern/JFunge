package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import com.falsepattern.jfunge.interpreter.instructions.Instruction;
import com.falsepattern.jfunge.interpreter.instructions.InstructionSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntFunction;
import java.util.function.ObjIntConsumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FING implements Fingerprint {
    public static final FING INSTANCE = new FING();

    private static Instruction popInstr(ExecutionContext ctx, int instruction) {
        val result = new AtomicReference<Instruction>();
        ctx.IP().instructionManager().unloadInstructionSet(new InstructionSet() {
            @Override
            public void unload(IntFunction<Instruction> unLoader) {
                result.set(unLoader.apply(instruction <= 25 ? instruction + 'A' : instruction));
            }
        });
        return result.get();
    }

    private static void pushInstr(ExecutionContext ctx, int instruction, Instruction value) {
        ctx.IP().instructionManager().loadInstructionSet(new InstructionSet() {
            @Override
            public void load(ObjIntConsumer<Instruction> loader) {
                loader.accept(value, instruction <= 25 ? instruction + 'A' : instruction);
            }
        });
    }

    private static boolean invalidValue(int value) {
        return (value < 0 || value > 25) && (value < 'A' || value > 'Z');
    }

    @SuppressWarnings("DuplicatedCode")
    @Instr('X')
    public static void swap(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        val b = stack.pop();
        if (invalidValue(a) || invalidValue(b)) {
            ctx.IP().reflect();
            return;
        }
        val aI = Optional.ofNullable(popInstr(ctx, a)).orElse((c) -> c.IP().reflect());
        val bI = Optional.ofNullable(popInstr(ctx, b)).orElse((c) -> c.IP().reflect());
        pushInstr(ctx, a, bI);
        pushInstr(ctx, b, aI);
    }

    @Instr('Y')
    public static void pop(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        if (invalidValue(a)) {
            ctx.IP().reflect();
            return;
        }
        popInstr(ctx, a);
    }

    @SuppressWarnings("DuplicatedCode")
    @Instr('Z')
    public static void move(ExecutionContext ctx) {
        val stack = ctx.stack();
        val b = stack.pop();
        val a = stack.pop();
        if (invalidValue(a) || invalidValue(b)) {
            ctx.IP().reflect();
            return;
        }
        val aI = popInstr(ctx, a);
        if (aI != null) {
            pushInstr(ctx, a, aI);
        }
        pushInstr(ctx, b, Optional.ofNullable(aI).orElse((c) -> c.IP().reflect()));
    }

    @Override
    public int code() {
        return 0x46494e47;
    }
}
