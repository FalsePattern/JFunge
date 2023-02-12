package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import gnu.trove.function.TDoubleFunction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.joml.Math;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FPDP implements Fingerprint {
    public static final FPDP INSTANCE = new FPDP();
    private static final DecimalFormat PRINT_FORMAT = new DecimalFormat("0.###### ");

    private static void binop(ExecutionContext ctx, BinOp op) {
        val stack = ctx.stack();
        val b = stack.popD();
        val a = stack.popD();
        stack.pushD(op.op(a, b));
    }

    private static void unop(ExecutionContext ctx, TDoubleFunction op) {
        val stack = ctx.stack();
        stack.pushD(op.execute(stack.popD()));
    }

    @Instr('A')
    public static void add(ExecutionContext ctx) {
        binop(ctx, Double::sum);
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
        stack.pushD(stack.pop());
    }

    @Instr('G')
    public static void atan(ExecutionContext ctx) {
        unop(ctx, java.lang.Math::atan);
    }

    @Instr('H')
    public static void acos(ExecutionContext ctx) {
        unop(ctx, Math::acos);
    }

    @Instr('I')
    public static void fToI(ExecutionContext ctx) {
        val stack = ctx.stack();
        stack.push((int) stack.popD());
    }

    @Instr('K')
    public static void logE(ExecutionContext ctx) {
        unop(ctx, java.lang.Math::log);
    }

    @Instr('L')
    public static void log10(ExecutionContext ctx) {
        unop(ctx, java.lang.Math::log10);
    }

    @Instr('M')
    public static void mul(ExecutionContext ctx) {
        binop(ctx, (a, b) -> a * b);
    }

    @Instr('N')
    public static void negate(ExecutionContext ctx) {
        unop(ctx, (x) -> -x);
    }

    @SneakyThrows
    @Instr('P')
    public static void print(ExecutionContext ctx) {
        ctx.output()
           .write(PRINT_FORMAT.format(ctx.stack().popD())
                              .replace("\uFFFD", "NaN ")
                              .replace("\u221E", "infinity")
                              .getBytes(StandardCharsets.UTF_8));
    }

    @Instr('Q')
    public static void sqrt(ExecutionContext ctx) {
        unop(ctx, Math::sqrt);
    }

    @Instr('R')
    public static void parseDouble(ExecutionContext ctx) {
        val stack = ctx.stack();
        val str = stack.popString();
        try {
            stack.pushD(Double.parseDouble(str));
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
        unop(ctx, Math::exp);
    }

    @Instr('Y')
    public static void pow(ExecutionContext ctx) {
        binop(ctx, java.lang.Math::pow);
    }

    @Override
    public int code() {
        return 0x46504450;
    }

    private interface BinOp {
        double op(double a, double b);
    }
}
