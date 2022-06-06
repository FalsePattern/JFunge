package com.falsepattern.jfunge.interpreter.instructions;

import com.falsepattern.jfunge.Globals;
import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.*;
import com.falsepattern.jfunge.ip.Stack;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.*;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Funge98 implements InstructionSet {
    public static final Funge98 INSTANCE = new Funge98();
    private static final TIntObjectMap<Fingerprint> fingerprints = new TIntObjectHashMap<>();

    static {
        addFingerprint(MODE.INSTANCE);
        addFingerprint(MODU.INSTANCE);
        addFingerprint(NULL.INSTANCE);
        addFingerprint(ROMA.INSTANCE);
    }

    private static void addFingerprint(Fingerprint print) {
        fingerprints.put(print.code(), print);
    }

    private static int getFingerCode(Stack s) {
        val n = s.pop();
        var sum = 0;
        for (var i = 0; i < n; i++) {
            sum *= 256;
            sum += s.pop();
        }
        return sum;
    }

    @Instr('(')
    public static void loadFinger(ExecutionContext ctx) {
        val s = ctx.IP().stackStack.TOSS();
        val code = getFingerCode(s);
        if (fingerprints.containsKey(code)) {
            s.push(code);
            s.push(1);
            ctx.IP().instructionManager.loadInstructionSet(fingerprints.get(code));
        } else {
            ctx.interpret('r');
        }
    }

    @Instr(')')
    public static void unloadFinger(ExecutionContext ctx) {
        val code = getFingerCode(ctx.IP().stackStack.TOSS());
        if (fingerprints.containsKey(code)) {
            ctx.IP().instructionManager.unloadInstructionSet(fingerprints.get(code));
        } else {
            ctx.interpret('r');
        }
    }

    @Instr('>')
    public static void east(ExecutionContext ctx) {ctx.IP().delta.set(1, 0, 0);}

    @Instr('v')
    public static void south(ExecutionContext ctx) {ctx.IP().delta.set(0, 1, 0);}

    @Instr('h')
    public static void high(ExecutionContext ctx) {
        if (ctx.dimensions() == 3)
            ctx.IP().delta.set(0, 0, 1);
        else
            ctx.interpret('r');
    }

    @Instr('l')
    public static void low(ExecutionContext ctx) {
        if (ctx.dimensions() == 3)
            ctx.IP().delta.set(0, 0, -1);
        else
            ctx.interpret('r');
    }

    @Instr('?')
    public static void away(ExecutionContext ctx) {
        var random = Math.abs(ctx.IP().nextRandom());
        if (ctx.dimensions() == 3) {
            switch (random % 6) {
                case 0: east(ctx); break;
                case 1: south(ctx); break;
                case 2: west(ctx); break;
                case 3: north(ctx); break;
                case 4: high(ctx); break;
                case 5: low(ctx); break;
            }
        } else {
            switch (random % 4) {
                case 0: east(ctx); break;
                case 1: south(ctx); break;
                case 2: west(ctx); break;
                case 3: north(ctx); break;
            }
        }
    }

    @Instr('<')
    public static void west(ExecutionContext ctx) {ctx.IP().delta.set(-1, 0, 0);}

    @Instr('^')
    public static void north(ExecutionContext ctx) {ctx.IP().delta.set(0, -1, 0);}

    @Instr('[')
    public static void turnLeft(ExecutionContext ctx) {
        val d = ctx.IP().delta;
        d.set(d.y, -d.x, d.z);
    }

    @Instr(']')
    public static void turnRight(ExecutionContext ctx) {
        val d = ctx.IP().delta;
        d.set(-d.y, d.x, d.z);
    }

    @Instr('#')
    public static void trampoline(ExecutionContext ctx) {ctx.IP().position.add(ctx.IP().delta);}

    @Instr('j')
    public static void jumpNTimes(ExecutionContext ctx) {
        int n = ctx.IP().stackStack.TOSS().pop();
        ctx.IP().position.add(new Vector3i(ctx.IP().delta).mul(n));
    }

    @Instr('x')
    public static void absoluteDelta(ExecutionContext ctx) {
        val s = ctx.IP().stackStack.TOSS();
        if (ctx.dimensions() == 3) {
            s.pop3(ctx.IP().delta);
        } else {
            val tmp = s.pop2();
            ctx.IP().delta.set(tmp.x, tmp.y, 0);
        }
    }

    @Instr('k')
    public static void doNTimes(ExecutionContext ctx) {
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
    }

    @SneakyThrows
    @Instr('.')
    public static void printNumber(ExecutionContext ctx) {
        ctx.output().write(Integer.toString(ctx.IP().stackStack.TOSS().pop()).getBytes(StandardCharsets.UTF_8));
        ctx.output().write(' ');
    }

    @SneakyThrows
    @Instr(',')
    public static void printChar(ExecutionContext ctx) {
        ctx.output().write(ctx.IP().stackStack.TOSS().pop());
    }

    @Instr('r')
    public static void reflect(ExecutionContext ctx) {ctx.IP().delta.mul(-1);}

    @Instr('@')
    public static void die(ExecutionContext ctx) {ctx.IP().die();}

    @Instr('$')
    public static void pop(ExecutionContext ctx) {ctx.IP().stackStack.TOSS().pop();}

    @Instr('n')
    public static void clearStack(ExecutionContext ctx) {ctx.IP().stackStack.TOSS().clear();}

    @Instr('_')
    public static void branchEastWest(ExecutionContext ctx) {ctx.interpret(ctx.IP().stackStack.TOSS().pop() == 0 ? '>' : '<');}

    @Instr('|')
    public static void branchNorthSouth(ExecutionContext ctx) {ctx.interpret(ctx.IP().stackStack.TOSS().pop() == 0 ? 'v' : '^');}

    @Instr('m')
    public static void branchHighLow(ExecutionContext ctx) {
        if (ctx.dimensions() == 3) {
            ctx.interpret(ctx.IP().stackStack.TOSS().pop() == 0 ? 'h' : 'l');
        } else {
            ctx.interpret('r');
        }
    }

    @Instr('\'')
    public static void getNext(ExecutionContext ctx) {
        int i = ctx.fungeSpace().get(new Vector3i(ctx.IP().position).add(ctx.IP().delta));
        ctx.IP().stackStack.TOSS().push(i);
        ctx.interpret('#');
    }

    @Instr('s')
    public static void putNext(ExecutionContext ctx) {
        ctx.fungeSpace().set(new Vector3i(ctx.IP().position).add(ctx.IP().delta), ctx.IP().stackStack.TOSS().pop());
        ctx.interpret('#');
    }

    @Instr('w')
    public static void conditionalTurn(ExecutionContext ctx) {
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
    }

    @Instr('z')
    public static void noop(ExecutionContext ctx) {}

    @Instr('+')
    public static void add(ExecutionContext ctx) {binop(ctx, Integer::sum);}

    @Instr('-')
    public static void sub(ExecutionContext ctx) {binop(ctx, (a, b) -> a - b);}

    @Instr('*')
    public static void mul(ExecutionContext ctx) {binop(ctx, (a, b) -> a * b);}

    @Instr('`')
    public static void greater(ExecutionContext ctx) {binop(ctx, (a, b) -> a > b ? 1 : 0);}

    @Instr('/')
    public static void div(ExecutionContext ctx) {binop(ctx, (a, b) -> b == 0 ? 0 : a / b);}

    @Instr('%')
    public static void mod(ExecutionContext ctx) {binop(ctx, (a, b) -> b == 0 ? 0 : a % b);}

    @Instr('\\')
    public static void swap(ExecutionContext ctx) {
        stack(ctx, (toss) -> {
            int b = toss.pop();
            int a = toss.pop();
            toss.push(b);
            toss.push(a);
        });
    }

    @Instr('p')
    public static void put(ExecutionContext ctx) {
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
    }

    @Instr('g')
    public static void get(ExecutionContext ctx) {
        val ip = ctx.IP();
        val toss = ip.stackStack.TOSS();
        if (ctx.dimensions() == 3) {
            val vec = toss.pop3().add(ip.storageOffset);
            toss.push(ctx.fungeSpace().get(vec));
        } else {
            val vec = toss.pop2().add(ip.storageOffset.x, ip.storageOffset.y);
            toss.push(ctx.fungeSpace().get(vec.x, vec.y, ip.storageOffset.z));
        }
    }

    @Instr('!')
    public static void logicalNot(ExecutionContext ctx) {stack(ctx, (toss) -> toss.push(toss.pop() == 0 ? 1 : 0));}

    @Instr(':')
    public static void duplicate(ExecutionContext ctx) {
        stack(ctx, (toss) -> {
            int x = toss.pop();
            toss.push(x);
            toss.push(x);
        });
    }

    @Instr('{')
    public static void blockStart(ExecutionContext ctx) {
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
    }

    @Instr('}')
    public static void blockEnd(ExecutionContext ctx) {
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

    }

    @Instr('u')
    public static void stackUnderStack(ExecutionContext ctx) {
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
    }

    @Instr('y')
    public static void sysInfo(ExecutionContext ctx) {
        val s = ctx.IP().stackStack.TOSS();
        val tossSize = ctx.IP().stackStack.TOSS().size();
        val n = s.pop();
        val sizes = ctx.IP().stackStack.sizes();
        //20 envs
        for (Map.Entry<String, String> entry : ctx.env().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            s.pushString(key + "=" + value);
        }
        //19 args
        for (String s1 : ctx.args()) {
            s.pushString(s1);
        }
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
        s.push(ctx.IP().UUID);
        //7 dimensions
        s.push(ctx.dimensions());
        //6 separator
        s.push(File.separatorChar);
        //5 operating paradigm
        s.push(1);
        //4 version
        s.push(Globals.FUNGE_VERSION);
        //3 handprint
        s.push(Globals.HANDPRINT);
        //2 bpc
        s.push(4);
        //1 flags
        s.push(0b00001111);
        if (n > 0) {
            int curr = s.pick(n - 1);
            for (int i = s.size(); i >= tossSize; i--) {
                s.pop();
            }
            s.push(curr);
        }
    }

    @Instr('q')
    public static void quit(ExecutionContext ctx) {
        int q = ctx.IP().stackStack.TOSS().pop();
        ctx.stop(q);
    }

    @Instr('t')
    public static void split(ExecutionContext ctx) {
        val clone = ctx.cloneIP();
        clone.delta.mul(-1);
    }

    @Instr('i')
    public static void input(ExecutionContext ctx) {
        val s = ctx.IP().stackStack.TOSS();
        val filename = s.popString();
        val flags = s.pop();
        val pos = new Vector3i();
        if (ctx.dimensions() == 3) {
            s.pop3(pos);
        } else {
            pos.set(s.pop2(), 0);
        }
        pos.add(ctx.IP().storageOffset);
        val file = ctx.readFile(filename);
        if (file == null) {
            ctx.interpret('r');
            return;
        }
        val delta = ((flags & 1) == 1) ? ctx.fungeSpace().loadBinaryFileAt(pos.x, pos.y, pos.z, file) : ctx.fungeSpace().loadFileAt(pos.x, pos.y, pos.z, file, ctx.dimensions() == 3);
        pos.sub(ctx.IP().storageOffset);
        if (ctx.dimensions() == 3) {
            s.push3(delta);
            s.push3(pos);
        } else {
            s.push2(new Vector2i(delta.x, delta.y));
            s.push2(new Vector2i(pos.x, pos.y));
        }
    }

    @Instr('o')
    public static void output(ExecutionContext ctx) {
        val s = ctx.IP().stackStack.TOSS();
        val filename = s.popString();
        val flags = s.pop();
        val pos = new Vector3i();
        val delta = new Vector3i();
        if (ctx.dimensions() == 3) {
            s.pop3(pos);
            s.pop3(delta);
        } else {
            pos.set(s.pop2(), 0);
            delta.set(s.pop2(), 1);
        }
        pos.add(ctx.IP().storageOffset);
        val data = ctx.fungeSpace().readDataAt(pos.x, pos.y, pos.z, delta.x, delta.y, delta.z, (flags & 1) == 1);
        if (!ctx.writeFile(filename, data)) {
            ctx.interpret('r');
        }
    }

    @Instr('&')
    @SneakyThrows
    public static void readInt(ExecutionContext ctx) {
        ctx.output().flush();
        var counter = 0;
        var found = false;
        var next = 0;
        while ((next = ctx.input(true)) < '0' || next > '9') {
            ctx.input(false);
        }
        while (true) {
            next = ctx.input(true);
            if (next >= '0' && next <= '9' && (counter * 10 >= counter)) {
                found = true;
                counter *= 10;
                counter += next - '0';
                ctx.input(false);
            } else {
                if (next == '\n') {
                    ctx.input(false);
                }
                break;
            }
        }
        if (found) {
            ctx.IP().stackStack.TOSS().push(counter);
        } else {
            ctx.interpret('r');
        }
    }

    @Instr('~')
    @SneakyThrows
    public static void readChar(ExecutionContext ctx) {
        ctx.output().flush();
        ctx.IP().stackStack.TOSS().push(ctx.input(false));
    }

    @Instr('=')
    public static void sysCall(ExecutionContext ctx) {
        val command = ctx.IP().stackStack.TOSS().popString();
        try {
            val proc = Runtime.getRuntime().exec(command);
            ctx.IP().stackStack.TOSS().push(proc.waitFor());
        } catch (Exception e) {
            ctx.IP().stackStack.TOSS().push(-1);
        }
    }

    @Override
    public void load(ObjIntConsumer<Instruction> instructionSet) {
        InstructionSet.super.load(instructionSet);
        for (int i = 0; i <= 9; i++) {
            int finalI = i;
            instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().push(finalI), '0' + i);
        }
        for (int i = 0; i <= 5; i++) {
            int finalI = i;
            instructionSet.accept((ctx) -> ctx.IP().stackStack.TOSS().push(10 + finalI), 'a' + i);
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
        throw new UnsupportedOperationException("Cannot unload the base syntax");
    }
}
