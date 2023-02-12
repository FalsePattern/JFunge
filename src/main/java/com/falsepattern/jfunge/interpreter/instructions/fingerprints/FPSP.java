package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import gnu.trove.function.TFloatFunction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.joml.Math;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FPSP implements Fingerprint {
    public static final FPSP INSTANCE = new FPSP();
    @Override
    public int code() {
        return 0x46505350;
    }

    private interface BinOp {
        float op(float a, float b);
    }

    private static void binop(ExecutionContext ctx, BinOp op) {
        val stack = ctx.stack();
        val b = stack.popF();
        val a = stack.popF();
        stack.pushF(op.op(a, b));
    }

    private static void unop(ExecutionContext ctx, TFloatFunction op) {
        val stack = ctx.stack();
        stack.pushF(op.execute(stack.popF()));
    }

    @Instr('A')
    public static void add(ExecutionContext ctx) {
        binop(ctx, Float::sum);
    }

    @Instr('B')
    public static void sin(ExecutionContext ctx) {
       unop(ctx, Math::sin);
    }

    @Instr('C')
    public static void cos(ExecutionContext ctx) {
        unop(ctx, Math::cos);
    }

    @Instr('D')
    public static void div(ExecutionContext ctx) {
        binop(ctx, (a, b) -> a / b);
    }

    @Instr('E')
    public static void aSin(ExecutionContext ctx) {
        unop(ctx, Math::asin);
    }

    @Instr('F')
    public static void iToF(ExecutionContext ctx) {
        val stack = ctx.stack();
        stack.pushF(stack.pop());
    }

    @Instr('G')
    public static void atan(ExecutionContext ctx) {
        unop(ctx, (x) -> (float) java.lang.Math.atan(x));
    }

    @Instr('H')
    public static void acos(ExecutionContext ctx) {
        unop(ctx, Math::acos);
    }

    @Instr('I')
    public static void fToI(ExecutionContext ctx) {
        val stack = ctx.stack();
        stack.push((int)stack.popF());
    }

    @Instr('K')
    public static void logE(ExecutionContext ctx) {
        unop(ctx, (x) -> (float) java.lang.Math.log(x));
    }

    @Instr('L')
    public static void log10(ExecutionContext ctx) {
        unop(ctx, (x) -> (float) java.lang.Math.log10(x));
    }

    @Instr('M')
    public static void mul(ExecutionContext ctx) {
        binop(ctx, (a, b) -> a * b);
    }

    @Instr('N')
    public static void negate(ExecutionContext ctx) {
        unop(ctx, (x) -> -x);
    }

    private static final DecimalFormat PRINT_FORMAT = new DecimalFormat("0.###### ");

    @SneakyThrows
    @Instr('P')
    public static void print(ExecutionContext ctx) {
        ctx.output()
           .write(PRINT_FORMAT.format(ctx.stack().popF())
                              .replace("\uFFFD", "NaN ")
                              .replace("\u221E", "infinity")
                              .getBytes(StandardCharsets.UTF_8));
    }

    @Instr('Q')
    public static void sqrt(ExecutionContext ctx) {
        unop(ctx, Math::sqrt);
    }

    @Instr('R')
    public static void parseFloat(ExecutionContext ctx) {
        val stack = ctx.stack();
        val str = stack.popString();
        try {
            stack.pushF(Float.parseFloat(str));
        } catch (NumberFormatException e) {
            ctx.IP().reflect();
        }
    }

    @Instr('S')
    public static void sub(ExecutionContext ctx) {
        binop(ctx, (a, b) -> a - b);
    }

    @Instr('T')
    public static void tan(ExecutionContext ctx) {
        unop(ctx, Math::tan);
    }

    @Instr('V')
    public static void abs(ExecutionContext ctx) {
        unop(ctx, Math::abs);
    }

    @Instr('X')
    public static void exp(ExecutionContext ctx) {
        unop(ctx, (x) -> (float) Math.exp(x));
    }

    @Instr('Y')
    public static void pow(ExecutionContext ctx) {
        binop(ctx, (a, b) -> (float) java.lang.Math.pow(a, b));
    }
}
