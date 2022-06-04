package com.falsepattern.jfunge.interpreter.instructions;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.ip.Stack;
import lombok.val;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.io.File;
import java.time.LocalDateTime;
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
        instructionSet.accept((ctx) -> {
            val d = ctx.IP().delta;
            d.set(d.y, -d.x, d.z);
        }, '[');
        instructionSet.accept((ctx) -> {
            val d = ctx.IP().delta;
            d.set(-d.y, d.x, d.z);
        }, ']');
        instructionSet.accept((ctx) -> ctx.IP().position.add(ctx.IP().delta), '#');
        instructionSet.accept((ctx) -> {
            int n = ctx.IP().stackStack.TOSS().pop();
            ctx.IP().position.add(new Vector3i(ctx.IP().delta).mul(n));
        }, 'j');
        instructionSet.accept((ctx) -> {
            val s = ctx.IP().stackStack.TOSS();
            if (ctx.dimensions() == 3) {
                s.pop3(ctx.IP().delta);
            } else {
                val tmp = s.pop2();
                ctx.IP().delta.set(tmp.x, tmp.y, 0);
            }
        }, 'x');
        instructionSet.accept((ctx) -> {
            val s = ctx.IP().stackStack.TOSS();
            int n = s.pop();
            if (n > 0) {
                val snapshot = new Vector3i(ctx.IP().position);
                ctx.step(ctx.IP());
                int i = ctx.fungeSpace().get(ctx.IP().position);
                ctx.IP().position.set(snapshot);
                for (int j = 0; j < n; j++) {
                    ctx.interpret(i);
                }
            } else {
                ctx.interpret('#');
            }
        }, 'k');
        instructionSet.accept((ctx) -> System.out.printf("%d ", ctx.IP().stackStack.TOSS().pop()), '.');
        instructionSet.accept((ctx) -> System.out.print((char)ctx.IP().stackStack.TOSS().pop()), ',');
        instructionSet.accept((ctx) -> ctx.IP().delta.mul(-1), 'r');
        instructionSet.accept((ctx) -> ctx.IP().die(), '@');
        instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().pop(), '$');
        instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().clear(), 'n');
        instructionSet.accept((ctx) -> ctx.interpret(ctx.IP().stackStack.TOSS().pop() == 0 ? '>' : '<'), '_');
        instructionSet.accept((ctx) -> ctx.interpret(ctx.IP().stackStack.TOSS().pop() == 0 ? 'v' : '^'), '|');
        instructionSet.accept((ctx) -> {
            int i = ctx.fungeSpace().get(new Vector3i(ctx.IP().position).add(ctx.IP().delta));
            ctx.IP().stackStack.TOSS().push(i);
            ctx.interpret('#');
        }, '\'');
        instructionSet.accept((ctx) -> {
            ctx.fungeSpace().set(new Vector3i(ctx.IP().position).add(ctx.IP().delta), ctx.IP().stackStack.TOSS().pop());
            ctx.interpret('#');
        }, 's');
        instructionSet.accept((ctx) -> {
            val s = ctx.IP().stackStack.TOSS();
            val b = s.pop();
            val a = s.pop();
            if (a > b) {
                ctx.interpret(']');
            } else if (a < b) {
                ctx.interpret('[');
            } else {
                ctx.interpret('z');
            }
        }, 'w');
        instructionSet.accept((ctx) -> {}, 'z');
        instructionSet.accept((ctx) -> binop(ctx, Integer::sum), '+');
        instructionSet.accept((ctx) -> binop(ctx, (a, b) -> a - b), '-');
        instructionSet.accept((ctx) -> binop(ctx, (a, b) -> a * b), '*');
        instructionSet.accept((ctx) -> binop(ctx, (a, b) -> a > b ? 1 : 0), '`');
        instructionSet.accept((ctx) -> binop(ctx, (a, b) -> b == 0 ? 0 : a / b), '/');
        instructionSet.accept((ctx) -> binop(ctx, (a, b) -> b == 0 ? 0 : a % b), '%');
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
        instructionSet.accept((ctx) -> {
            if (!ctx.IP().stackStack.pushStackStack()) {
                ctx.interpret('r');
                return;
            }
            val SOSS = ctx.IP().stackStack.SOSS().get();
            val TOSS = ctx.IP().stackStack.TOSS();
            int n = SOSS.pop();
            if (n > 0) {
                val tmp = new Stack();
                for (int i = 0; i < n; i++) {
                    tmp.push(SOSS.pop());
                }
                for (int i = 0; i < n; i++) {
                    TOSS.push(tmp.pop());
                }
            }
            if (n < 0) {
                for (int i = 0; i < -n; i++) {
                    SOSS.push(0);
                }
            }
            if (ctx.dimensions() == 3) {
                SOSS.push3(ctx.IP().storageOffset);
            } else {
                SOSS.push2(new Vector2i(ctx.IP().storageOffset.x, ctx.IP().storageOffset.y));
            }
            val snapshot = new Vector3i(ctx.IP().position);
            ctx.step(ctx.IP());
            ctx.IP().storageOffset.set(ctx.IP().position);
            ctx.IP().position.set(snapshot);
        }, '{');
        instructionSet.accept((ctx) -> {
            val TOSS = ctx.IP().stackStack.TOSS();
            val SOSSt = ctx.IP().stackStack.SOSS();
            if (!SOSSt.isPresent() || !ctx.IP().stackStack.popStackStack()) {
                ctx.interpret('r');
                return;
            }
            val SOSS = SOSSt.get();
            int n = TOSS.pop();

            if (ctx.dimensions() == 3) {
                SOSS.pop3(ctx.IP().storageOffset);
            } else {
                val tmp2 = SOSS.pop2();
                ctx.IP().storageOffset.x = tmp2.x;
                ctx.IP().storageOffset.y = tmp2.y;
            }
            val tmp = new Stack();
            if (n > 0) {
                for (int i = 0; i < n; i++) {
                    tmp.push(TOSS.pop());
                }
                for (int i = 0; i < n; i++) {
                    SOSS.push(tmp.pop());
                }
            } else if (n < 0) {
                for (int i = 0; i < -n; i++) {
                    SOSS.pop();
                }
            }

        }, '}');
        instructionSet.accept((ctx) -> {
            val TOSS = ctx.IP().stackStack.TOSS();
            val SOSSt = ctx.IP().stackStack.SOSS();
            if (!SOSSt.isPresent()) {
                ctx.interpret('r');
                return;
            }
            val SOSS = SOSSt.get();
            val n = TOSS.pop();
            if (n > 0) {
                for (int i = 0; i < n; i++) {
                    TOSS.push(SOSS.pop());
                }
            } else if (n < 0) {
                for (int i = 0; i < -n; i++) {
                    SOSS.push(TOSS.pop());
                }
            }
        }, 'u');
        instructionSet.accept(this::sysInfo, 'y');
        instructionSet.accept((ctx) -> {
            int q = ctx.IP().stackStack.TOSS().pop();
            System.exit(q);
        }, 'q');
    }

    private void sysInfo(ExecutionContext ctx) {
        val s = ctx.IP().stackStack.TOSS();
        val tossSize = ctx.IP().stackStack.TOSS().size();
        val n = s.pop();
        val sizes = ctx.IP().stackStack.sizes();
        //20 envs
        ctx.env().forEach((key, value) -> {
            s.pushString(key + "=" + value);
        });
        //19 args
        ctx.args().forEach(s::pushString);
        s.push(0);
        s.push(0);
        //18 sizes of stack stack
        for (int i = 0; i < sizes.length; i++) {
            s.push(sizes[i]);
        }
        //17 size of stack stack
        s.push(ctx.IP().stackStack.size());
        //16 time
        val time = LocalDateTime.now();
        s.push(time.getHour() * 256 * 256 + time.getMinute() * 256 + time.getSecond());
        //15 date
        s.push((time.getYear() - 1900) * 256 * 256 + time.getMonthValue() * 256 + time.getDayOfMonth());
        val bounds = ctx.fungeSpace().bounds();
        if (ctx.dimensions() == 3) {
            //14 greatest point
            s.push3(new Vector3i(bounds.xMax() - bounds.xMin(), bounds.yMax() - bounds.yMin(), bounds.zMax() - bounds.zMin()));
            //13 least point
            s.push3(new Vector3i(bounds.xMin(), bounds.yMin(), bounds.zMin()));
            //12 storage offset
            s.push3(ctx.IP().storageOffset);
            //11 delta
            s.push3(ctx.IP().delta);
            //10 position
            s.push3(ctx.IP().position);
        } else {
            //14 greatest point
            s.push2(new Vector2i(bounds.xMax() - bounds.xMin(), bounds.yMax() - bounds.yMin()));
            //13 least point
            s.push2(new Vector2i(bounds.xMin(), bounds.yMin()));
            //12 storage offset
            s.push2(new Vector2i(ctx.IP().storageOffset.x, ctx.IP().storageOffset.y));
            //11 delta
            s.push2(new Vector2i(ctx.IP().delta.x, ctx.IP().delta.y));
            //10 position
            s.push2(new Vector2i(ctx.IP().position.x, ctx.IP().position.y));
        }
        //9 teamnumber
        s.push(0);
        //8 uuid
        s.push(0);
        //7 dimensions
        s.push(ctx.dimensions());
        //6 separator
        s.push(File.separatorChar);
        //5 operating paradigm
        s.push(ctx.paradigm());
        //4 version
        s.push(ctx.version());
        //3 handprint
        s.push(ctx.handprint());
        //2 bpc
        s.push(4);
        //1 flags
        s.push(0);
        if (n > 0) {
            int curr = s.pick(n - 1);
            for (int i = s.size(); i >= tossSize; i--) {
                s.pop();
            }
            s.push(curr);
        }
    }

    public static void stack(ExecutionContext ctx, Consumer<Stack> runner) {
        runner.accept(ctx.IP().stackStack.TOSS());
    }

    public static void binop(ExecutionContext ctx, BinaryOperator op) {
        val TOSS = ctx.IP().stackStack.TOSS();
        int b = TOSS.pop();
        int a = TOSS.pop();
        TOSS.push(op.op(a, b));
    }

    public interface BinaryOperator {
        int op(int a, int b);
    }

    @Override
    public void unload(IntConsumer instructionSet) {

    }
}
