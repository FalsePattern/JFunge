package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BASE implements Fingerprint {
    public static final BASE INSTANCE = new BASE();

    @Override
    public int code() {
        return 0x42415345;
    }

    @SneakyThrows
    @Instr('B')
    public static void printBinary(ExecutionContext ctx) {
        ctx.output().write(Integer.toBinaryString(ctx.stack().pop()).getBytes());
    }

    @SneakyThrows
    @Instr('H')
    public static void printHex(ExecutionContext ctx) {
        ctx.output().write(Integer.toHexString(ctx.stack().pop()).getBytes());
    }

    @Instr('I')
    public static void readIntBase(ExecutionContext ctx) {
        int base = ctx.stack().pop();
        //Read input until non-digit character is encountered
        StringBuilder sb = new StringBuilder();
        int c;
        while (Character.isLetterOrDigit(c = ctx.input(true))) {
            sb.append((char) c);
            ctx.input(false);
        }
        ctx.stack().push(Integer.parseInt(sb.toString(), base));
    }

    @SneakyThrows
    @Instr('N')
    public static void printInBase(ExecutionContext ctx) {
        int base = ctx.stack().pop();
        int value = ctx.stack().pop();
        ctx.output().write(Integer.toString(value, base).getBytes());
    }

    @SneakyThrows
    @Instr('O')
    public static void printOctal(ExecutionContext ctx) {
        ctx.output().write(Integer.toOctalString(ctx.stack().pop()).getBytes());
    }

}
