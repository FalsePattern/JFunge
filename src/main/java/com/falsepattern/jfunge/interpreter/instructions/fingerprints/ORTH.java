package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import com.falsepattern.jfunge.interpreter.instructions.Funge98;
import com.falsepattern.jfunge.util.MemoryStack;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ORTH implements Fingerprint {
    public static final ORTH INSTANCE = new ORTH();

    @Instr('A')
    public static void and(ExecutionContext ctx) {
        Funge98.binop(ctx, (a, b) -> a & b);
    }

    @Instr('O')
    public static void or(ExecutionContext ctx) {
        Funge98.binop(ctx, (a, b) -> a | b);
    }

    @Instr('E')
    public static void xor(ExecutionContext ctx) {
        Funge98.binop(ctx, (a, b) -> a ^ b);
    }

    @Instr('X')
    public static void setX(ExecutionContext ctx) {
        ctx.IP().position().x = ctx.stack().pop();
    }

    @Instr('Y')
    public static void setY(ExecutionContext ctx) {
        ctx.IP().position().y = ctx.stack().pop();
    }

    @Instr('V')
    public static void setDX(ExecutionContext ctx) {
        ctx.IP().delta().x = ctx.stack().pop();
    }

    @Instr('W')
    public static void setDY(ExecutionContext ctx) {
        ctx.IP().delta().y = ctx.stack().pop();
    }

    @Instr('G')
    public static void get(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val ip = ctx.IP();
        val stack = ctx.stack();
        if (ctx.dimensions() == 3) {
            val vec = mem.vec3i();
            vec.x = stack.pop();
            vec.y = stack.pop();
            vec.z = stack.pop();
            vec.add(ip.storageOffset());
            stack.push(ctx.fungeSpace().get(vec));
        } else {
            val vec = mem.vec2i();
            vec.x = stack.pop();
            vec.y = stack.pop();
            vec.add(ip.storageOffset().x, ip.storageOffset().y);
            stack.push(ctx.fungeSpace().get(vec.x, vec.y, ip.storageOffset().z));
        }
    }

    @Instr('P')
    public static void put(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val ip = ctx.IP();
        val stack = ctx.stack();
        if (ctx.dimensions() == 3) {
            val vec = mem.vec3i();
            vec.x = stack.pop();
            vec.y = stack.pop();
            vec.z = stack.pop();
            vec.add(ip.storageOffset());
            val i = stack.pop();
            ctx.fungeSpace().set(vec, i);
        } else {
            val vec = mem.vec2i();
            vec.x = stack.pop();
            vec.y = stack.pop();
            vec.add(ip.storageOffset().x, ip.storageOffset().y);
            val i = stack.pop();
            ctx.fungeSpace().set(vec.x, vec.y, ip.storageOffset().z, i);
        }
    }

    @Instr('Z')
    public static void rampIfZero(ExecutionContext ctx) {
        if (ctx.stack().pop() == 0) {
            Funge98.trampoline(ctx);
        }
    }

    @SneakyThrows
    @Instr('S')
    public static void outputString(ExecutionContext ctx) {
        val str = ctx.stack().popString();
        ctx.output().write(str.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int code() {
        return 0x4f525448;
    }
}
