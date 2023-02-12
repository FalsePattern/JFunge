package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CPLI implements Fingerprint {
    public static final CPLI INSTANCE = new CPLI();

    @Instr('A')
    public static void add(ExecutionContext ctx) {
        val stack = ctx.stack();
        val bi = stack.pop();
        val br = stack.pop();
        val ai = stack.pop();
        val ar = stack.pop();
        stack.push(ar + br);
        stack.push(ai + bi);
    }

    @Instr('D')
    public static void div(ExecutionContext ctx) {
        val stack = ctx.stack();
        val bi = stack.pop();
        val br = stack.pop();
        val ai = stack.pop();
        val ar = stack.pop();
        val r = (ar * br + ai * bi) / (br * br + bi * bi);
        val i = (ai * br - ar * bi) / (br * br + bi * bi);
        stack.push(r);
        stack.push(i);
    }

    @Instr('M')
    public static void mul(ExecutionContext ctx) {
        val stack = ctx.stack();
        val bi = stack.pop();
        val br = stack.pop();
        val ai = stack.pop();
        val ar = stack.pop();
        val r = ar * br - ai * bi;
        val i = ar * bi + ai * br;
        stack.push(r);
        stack.push(i);
    }

    @SneakyThrows
    @Instr('O')
    public static void output(ExecutionContext ctx) {
        val stack = ctx.stack();
        val i = stack.pop();
        val r = stack.pop();
        ctx.output().write(("(" + r + " + " + i + "i)").getBytes());
    }

    @Instr('S')
    public static void sub(ExecutionContext ctx) {
        val stack = ctx.stack();
        val bi = stack.pop();
        val br = stack.pop();
        val ai = stack.pop();
        val ar = stack.pop();
        stack.push(ar - br);
        stack.push(ai - bi);
    }

    @Instr('V')
    public static void length(ExecutionContext ctx) {
        val stack = ctx.stack();
        val i = stack.pop();
        val r = stack.pop();
        stack.push((int) Math.sqrt(r * r + i * i));
    }

    @Override
    public int code() {
        return 0x43504C49;
    }
}
