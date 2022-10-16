package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Funge98;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.joml.Vector2i;
import org.joml.Vector3i;

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
        ctx.IP().position.x = ctx.IP().stackStack.TOSS().pop();
    }

    @Instr('Y')
    public static void setY(ExecutionContext ctx) {
        ctx.IP().position.y = ctx.IP().stackStack.TOSS().pop();
    }

    @Instr('V')
    public static void setDX(ExecutionContext ctx) {
        ctx.IP().delta.x = ctx.IP().stackStack.TOSS().pop();
    }

    @Instr('W')
    public static void setDY(ExecutionContext ctx) {
        ctx.IP().delta.y = ctx.IP().stackStack.TOSS().pop();
    }

    @Instr('G')
    public static void get(ExecutionContext ctx) {
        val ip = ctx.IP();
        val toss = ip.stackStack.TOSS();
        if (ctx.dimensions() == 3) {
            val vec = new Vector3i();
            vec.x = toss.pop();
            vec.y = toss.pop();
            vec.z = toss.pop();
            vec.add(ip.storageOffset);
            toss.push(ctx.fungeSpace().get(vec));
        } else {
            val vec = new Vector2i();
            vec.x = toss.pop();
            vec.y = toss.pop();
            vec.add(ip.storageOffset.x, ip.storageOffset.y);
            toss.push(ctx.fungeSpace().get(vec.x, vec.y, ip.storageOffset.z));
        }
    }

    @Instr('P')
    public static void put(ExecutionContext ctx) {
        val ip = ctx.IP();
        val toss = ip.stackStack.TOSS();
        if (ctx.dimensions() == 3) {
            val vec = new Vector3i();
            vec.x = toss.pop();
            vec.y = toss.pop();
            vec.z = toss.pop();
            vec.add(ip.storageOffset);
            val i = toss.pop();
            ctx.fungeSpace().set(vec, i);
        } else {
            val vec = new Vector2i();
            vec.x = toss.pop();
            vec.y = toss.pop();
            vec.add(ip.storageOffset.x, ip.storageOffset.y);
            val i = toss.pop();
            ctx.fungeSpace().set(vec.x, vec.y, ip.storageOffset.z, i);
        }
    }

    @Instr('Z')
    public static void rampIfZero(ExecutionContext ctx) {
        if (ctx.IP().stackStack.TOSS().pop() == 0) {
            Funge98.trampoline(ctx);
        }
    }

    @SneakyThrows
    @Instr('S')
    public static void outputString(ExecutionContext ctx) {
        val str = ctx.IP().stackStack.TOSS().popString();
        ctx.output().write(str.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int code() {
        return 0x4f525448;
    }
}
