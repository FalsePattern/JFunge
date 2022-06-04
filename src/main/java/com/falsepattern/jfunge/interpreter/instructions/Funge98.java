package com.falsepattern.jfunge.interpreter.instructions;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.ip.Stack;
import lombok.val;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

public class Funge98 implements InstructionSet {
    @Override
    public void load(ObjIntConsumer<Instruction> instructionSet) {
        for (int i = 0; i <= 9; i++) {
            int finalI = i;
            instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().push(finalI), '0' + i);
        }
        for (int i = 0; i <= 5; i++) {
            int finalI = i;
            instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().push(10 + finalI), 'a' + i);
        }
        instructionSet.accept((ctx) -> ctx.IP().delta.set(1, 0, 0), '>');
        instructionSet.accept((ctx) -> ctx.IP().delta.set(0, 1, 0), 'v');
        instructionSet.accept((ctx) -> ctx.IP().delta.set(-1, 0, 0), '<');
        instructionSet.accept((ctx) -> ctx.IP().delta.set(0, -1, 0), '^');
        instructionSet.accept((ctx) -> ctx.IP().position.add(ctx.IP().delta), '#');
        instructionSet.accept((ctx) -> System.out.printf("%d ", ctx.IP().stackStack.TOSS().pop()), '.');
        instructionSet.accept((ctx) -> System.out.print((char)ctx.IP().stackStack.TOSS().pop()), ',');
        instructionSet.accept((ctx) -> ctx.IP().delta.mul(-1), 'r');
        instructionSet.accept((ctx) -> ctx.IP().die(), '@');
        instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().pop(), '$');
        instructionSet.accept((ctx) -> ctx.interpret(ctx.IP().stackStack.TOSS().pop() == 0 ? '>' : '<'), '_');
        instructionSet.accept((ctx) -> ctx.interpret(ctx.IP().stackStack.TOSS().pop() == 0 ? 'v' : '^'), '|');
        instructionSet.accept((ctx) -> binop(ctx, Integer::sum), '+');
        instructionSet.accept((ctx) -> binop(ctx, (a, b) -> a - b), '-');
        instructionSet.accept((ctx) -> binop(ctx, (a, b) -> a * b), '*');
        instructionSet.accept((ctx) -> binop(ctx, (a, b) -> a > b ? 1 : 0), '`');
        instructionSet.accept((ctx) -> stack(ctx, (toss) -> {
            int b = toss.pop();
            int a = toss.pop();
            toss.push(b);
            toss.push(a);
        }), '\\');
        instructionSet.accept((ctx) -> {
            val ip = ctx.IP();
            val toss = ip.stackStack.TOSS();
            if (ctx.dimensions() == 3) {
                val vec = toss.pop3().add(ip.storageOffset);
                val i = toss.pop();
                ctx.fungeSpace().set(vec, i);
            } else {
                val vec = toss.pop2().add(ip.storageOffset.x, ip.storageOffset.y);
                val i = toss.pop();
                ctx.fungeSpace().set(vec.x, vec.y, ip.storageOffset.z, i);
            }
        }, 'p');
        instructionSet.accept((ctx) -> {
            val ip = ctx.IP();
            val toss = ip.stackStack.TOSS();
            if (ctx.dimensions() == 3) {
                val vec = toss.pop3().add(ip.storageOffset);
                toss.push(ctx.fungeSpace().get(vec));
            } else {
                val vec = toss.pop2().add(ip.storageOffset.x, ip.storageOffset.y);
                toss.push(ctx.fungeSpace().get(vec.x, vec.y, ip.storageOffset.z));
            }
        }, 'g');
        instructionSet.accept((ctx) -> stack(ctx, (toss) -> toss.push(toss.pop() == 0 ? 1 : 0)), '!');
        instructionSet.accept((ctx) -> stack(ctx, (toss) -> {
            int x = toss.pop();
            toss.push(x);
            toss.push(x);
        }), ':');
    }

    private void stack(ExecutionContext ctx, Consumer<Stack> runner) {
        runner.accept(ctx.IP().stackStack.TOSS());
    }

    private void binop(ExecutionContext ctx, BinaryOperator op) {
        val TOSS = ctx.IP().stackStack.TOSS();
        int b = TOSS.pop();
        int a = TOSS.pop();
        TOSS.push(op.op(a, b));
    }

    private interface BinaryOperator {
        int op(int a, int b);
    }

    @Override
    public void unload(IntConsumer instructionSet) {

    }
}
