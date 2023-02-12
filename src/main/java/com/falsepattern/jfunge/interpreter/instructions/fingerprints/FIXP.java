package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.Random;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class FIXP implements Fingerprint {
    public static final FIXP INSTANCE = new FIXP();
    @Override
    public int code() {
        return 0x46495850;
    }

    @Instr('A')
    public static void and(ExecutionContext ctx) {
        val stack = ctx.stack();
        val b = stack.pop();
        val a = stack.pop();
        stack.push(a & b);
    }

    @Instr('B')
    public static void acos(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        val result = Math.toDegrees(Math.acos(a / 10000D));
        if (Double.isNaN(result)) {
            ctx.IP().reflect();
        } else {
            stack.push((int) (result * 10000));
        }
    }

    @Instr('C')
    public static void cos(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        stack.push((int)(Math.cos(Math.toRadians(a / 10000D)) * 10000));
    }

    @Instr('D')
    public static void random(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        stack.push(new Random().nextInt(3));
    }

    @Instr('I')
    public static void sin(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        stack.push((int)(Math.sin(Math.toRadians(a / 10000D)) * 10000));
    }

    @Instr('J')
    public static void asin(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        val result = Math.toDegrees(Math.asin(a / 10000D));
        if (Double.isNaN(result)) {
            ctx.IP().reflect();
        } else {
            stack.push((int) (result * 10000));
        }
    }

    @Instr('N')
    public static void negate(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        stack.push(-a);
    }

    @Instr('O')
    public static void or(ExecutionContext ctx) {
        val stack = ctx.stack();
        val b = stack.pop();
        val a = stack.pop();
        stack.push(a | b);
    }

    @Instr('P')
    public static void mulPi(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        stack.push((int)(a * Math.PI));
    }

    @Instr('Q')
    public static void sqrt(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        stack.push((int)Math.sqrt(a));
    }

    @Instr('R')
    public static void pow(ExecutionContext ctx) {
        val stack = ctx.stack();
        val b = stack.pop();
        val a = stack.pop();
        val result = Math.pow(a, b);
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            ctx.IP().reflect();
        } else {
            stack.push((int) result);
        }
    }

    @Instr('S')
    public static void sign(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        stack.push(Integer.compare(a, 0));
    }

    @Instr('T')
    public static void tan(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        stack.push((int)(Math.tan(Math.toRadians(a / 10000D)) * 10000));
    }

    @Instr('U')
    public static void atan(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        stack.push((int)(Math.toDegrees(Math.atan(a / 10000D)) * 10000));
    }

    @Instr('V')
    public static void abs(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        stack.push(Math.abs(a));
    }

    @Instr('X')
    public static void xor(ExecutionContext ctx) {
        val stack = ctx.stack();
        val b = stack.pop();
        val a = stack.pop();
        stack.push(a ^ b);
    }
}
