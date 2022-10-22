package com.falsepattern.jfunge.interpreter.instructions;

import com.falsepattern.jfunge.Globals;
import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.FPSP;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.HRTI;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.MODE;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.MODU;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.NULL;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.ORTH;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.PERL;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.REFC;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.ROMA;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.TOYS;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.TURT;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints._3DSP;
import com.falsepattern.jfunge.ip.IStack;
import com.falsepattern.jfunge.ip.impl.Stack;
import com.falsepattern.jfunge.util.MemoryStack;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;

import java.io.File;
import java.io.IOException;
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
        addFingerprint(_3DSP.INSTANCE);
        addFingerprint(FPSP.INSTANCE);
        addFingerprint(HRTI.INSTANCE);
        addFingerprint(MODE.INSTANCE);
        addFingerprint(MODU.INSTANCE);
        addFingerprint(NULL.INSTANCE);
        addFingerprint(ORTH.INSTANCE);
        addFingerprint(PERL.INSTANCE);
        addFingerprint(REFC.INSTANCE);
        addFingerprint(ROMA.INSTANCE);
        addFingerprint(TOYS.INSTANCE);
        addFingerprint(TURT.INSTANCE);
    }

    private static void addFingerprint(Fingerprint print) {
        fingerprints.put(print.code(), print);
    }

    private static int getFingerCode(IStack s) {
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
        val stack = ctx.stack();
        val code = getFingerCode(stack);
        if (fingerprints.containsKey(code) && ctx.fingerprintAllowed(code)) {
            stack.push(code);
            stack.push(1);
            ctx.IP().instructionManager().loadInstructionSet(fingerprints.get(code));
        } else {
            ctx.interpret('r');
        }
    }

    @Instr(')')
    public static void unloadFinger(ExecutionContext ctx) {
        val code = getFingerCode(ctx.stack());
        if (fingerprints.containsKey(code)) {
            ctx.IP().instructionManager().unloadInstructionSet(fingerprints.get(code));
        } else {
            ctx.interpret('r');
        }
    }

    @Instr('>')
    public static void east(ExecutionContext ctx) {
        ctx.IP().delta().set(1, 0, 0);
    }

    @Instr('v')
    public static void south(ExecutionContext ctx) {
        ctx.IP().delta().set(0, 1, 0);
    }

    @Instr('h')
    public static void high(ExecutionContext ctx) {
        if (ctx.dimensions() == 3)
            ctx.IP().delta().set(0, 0, 1);
        else
            ctx.interpret('r');
    }

    @Instr('l')
    public static void low(ExecutionContext ctx) {
        if (ctx.dimensions() == 3)
            ctx.IP().delta().set(0, 0, -1);
        else
            ctx.interpret('r');
    }

    @Instr('?')
    public static void away(ExecutionContext ctx) {
        var random = Math.abs(ctx.IP().nextRandom());
        if (ctx.dimensions() == 3) {
            switch (random % 6) {
                case 0:
                    east(ctx);
                    break;
                case 1:
                    south(ctx);
                    break;
                case 2:
                    west(ctx);
                    break;
                case 3:
                    north(ctx);
                    break;
                case 4:
                    high(ctx);
                    break;
                case 5:
                    low(ctx);
                    break;
            }
        } else {
            switch (random % 4) {
                case 0:
                    east(ctx);
                    break;
                case 1:
                    south(ctx);
                    break;
                case 2:
                    west(ctx);
                    break;
                case 3:
                    north(ctx);
                    break;
            }
        }
    }

    @Instr('<')
    public static void west(ExecutionContext ctx) {
        ctx.IP().delta().set(-1, 0, 0);
    }

    @Instr('^')
    public static void north(ExecutionContext ctx) {
        ctx.IP().delta().set(0, -1, 0);
    }

    @Instr('[')
    public static void turnLeft(ExecutionContext ctx) {
        val d = ctx.IP().delta();
        d.set(d.y, -d.x, d.z);
    }

    @Instr(']')
    public static void turnRight(ExecutionContext ctx) {
        val d = ctx.IP().delta();
        d.set(-d.y, d.x, d.z);
    }

    @Instr('#')
    public static void trampoline(ExecutionContext ctx) {
        ctx.IP().step();
    }

    @Instr('j')
    public static void jumpNTimes(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        int n = ctx.stack().pop();
        ctx.IP().position().add(mem.vec3i().set(ctx.IP().delta()).mul(n));
    }

    @Instr('x')
    public static void absoluteDelta(ExecutionContext ctx) {
        val stack = ctx.stack();
        stack.popVecDimProof(ctx.dimensions(), ctx.IP().delta());
    }

    @Instr('k')
    public static void doNTimes(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val s = ctx.stack();
        int n = s.pop();
        if (n > 0) {
            val snapshot = mem.vec3i().set(ctx.IP().position());
            ctx.step(ctx.IP());
            int i = ctx.fungeSpace().get(ctx.IP().position());
            ctx.IP().position().set(snapshot);
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
        ctx.output().write(Integer.toString(ctx.stack().pop()).getBytes(StandardCharsets.UTF_8));
        ctx.output().write(' ');
    }

    @SneakyThrows
    @Instr(',')
    public static void printChar(ExecutionContext ctx) {
        ctx.output().write(ctx.stack().pop());
    }

    @Instr('r')
    public static void reflect(ExecutionContext ctx) {
        ctx.IP().reflect();
    }

    @Instr('@')
    public static void die(ExecutionContext ctx) {
        ctx.IP().die();
    }

    @Instr('$')
    public static void pop(ExecutionContext ctx) {
        ctx.stack().pop();
    }

    @Instr('n')
    public static void clearStack(ExecutionContext ctx) {
        ctx.stack().clear();
    }

    @Instr('_')
    public static void branchEastWest(ExecutionContext ctx) {
        ctx.interpret(ctx.stack().pop() == 0 ? '>' : '<');
    }

    @Instr('|')
    public static void branchNorthSouth(ExecutionContext ctx) {
        ctx.interpret(ctx.stack().pop() == 0 ? 'v' : '^');
    }

    @Instr('m')
    public static void branchHighLow(ExecutionContext ctx) {
        if (ctx.dimensions() == 3) {
            ctx.interpret(ctx.stack().pop() == 0 ? 'h' : 'l');
        } else {
            ctx.interpret('r');
        }
    }

    @Instr('\'')
    public static void getNext(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        int i = ctx.fungeSpace().get(mem.vec3i().set(ctx.IP().position()).add(ctx.IP().delta()));
        ctx.stack().push(i);
        ctx.interpret('#');
    }

    @Instr('s')
    public static void putNext(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        ctx.fungeSpace().set(mem.vec3i().set(ctx.IP().position()).add(ctx.IP().delta()), ctx.stack().pop());
        ctx.interpret('#');
    }

    @Instr('w')
    public static void conditionalTurn(ExecutionContext ctx) {
        val s = ctx.stack();
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
    public static void noop(ExecutionContext ctx) {
    }

    @Instr('+')
    public static void add(ExecutionContext ctx) {
        binop(ctx, Integer::sum);
    }

    @Instr('-')
    public static void sub(ExecutionContext ctx) {
        binop(ctx, (a, b) -> a - b);
    }

    @Instr('*')
    public static void mul(ExecutionContext ctx) {
        binop(ctx, (a, b) -> a * b);
    }

    @Instr('`')
    public static void greater(ExecutionContext ctx) {
        binop(ctx, (a, b) -> a > b ? 1 : 0);
    }

    @Instr('/')
    public static void div(ExecutionContext ctx) {
        binop(ctx, (a, b) -> b == 0 ? 0 : a / b);
    }

    @Instr('%')
    public static void mod(ExecutionContext ctx) {
        binop(ctx, (a, b) -> b == 0 ? 0 : a % b);
    }

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
        @Cleanup val mem = MemoryStack.stackPush();
        val ip = ctx.IP();
        val stack = ip.stackStack().TOSS();
        ctx.fungeSpace().set(stack.popVecDimProof(ctx.dimensions(), mem.vec3i()).add(ip.storageOffset()), stack.pop());
    }

    @Instr('g')
    public static void get(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val ip = ctx.IP();
        val stack = ctx.stack();
        stack.push(ctx.fungeSpace().get(stack.popVecDimProof(ctx.dimensions(), mem.vec3i()).add(ip.storageOffset())));
    }

    @Instr('!')
    public static void logicalNot(ExecutionContext ctx) {
        stack(ctx, (toss) -> toss.push(toss.pop() == 0 ? 1 : 0));
    }

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
        @Cleanup val mem = MemoryStack.stackPush();
        if (!ctx.IP().stackStack().push()) {
            ctx.interpret('r');
            return;
        }
        val SOSS = ctx.IP().stackStack().SOSS().get();
        val TOSS = ctx.stack();
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
        SOSS.pushVecDimProof(ctx.dimensions(), ctx.IP().storageOffset());
        val snapshot = mem.vec3i().set(ctx.IP().position());
        ctx.step(ctx.IP());
        ctx.IP().storageOffset().set(ctx.IP().position());
        ctx.IP().position().set(snapshot);
    }

    @Instr('}')
    public static void blockEnd(ExecutionContext ctx) {
        val TOSS = ctx.stack();
        val SOSSt = ctx.IP().stackStack().SOSS();
        if (!SOSSt.isPresent() || !ctx.IP().stackStack().pop()) {
            ctx.interpret('r');
            return;
        }
        val SOSS = SOSSt.get();
        int n = TOSS.pop();

        SOSS.popVecDimProof(ctx.dimensions(), ctx.IP().storageOffset());
        val tmp = (IStack) new Stack();
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
        val TOSS = ctx.stack();
        val SOSSt = ctx.IP().stackStack().SOSS();
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
        @Cleanup val mem = MemoryStack.stackPush();
        val s = ctx.stack();
        val tossSize = ctx.stack().size();
        val n = s.pop();
        val sizes = ctx.IP().stackStack().stackSizes();
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
        for (int size : sizes) {
            s.push(size);
        }
        //17 size of stack stack
        s.push(ctx.IP().stackStack().size());
        //16 time
        val time = LocalDateTime.now();
        s.push(time.getHour() * 256 * 256 + time.getMinute() * 256 + time.getSecond());
        //15 date
        s.push((time.getYear() - 1900) * 256 * 256 + time.getMonthValue() * 256 + time.getDayOfMonth());
        val bounds = ctx.fungeSpace().bounds();
        //14 greatest point
        s.pushVecDimProof(ctx.dimensions(), mem.vec3i().set(bounds.xMax() - bounds.xMin(), bounds.yMax() - bounds.yMin(), bounds.zMax() - bounds.zMin()));
        //13 least point
        s.pushVecDimProof(ctx.dimensions(), mem.vec3i().set(bounds.xMin(), bounds.yMin(), bounds.zMin()));
        //12 storage offset
        s.pushVecDimProof(ctx.dimensions(), ctx.IP().storageOffset());
        //11 delta
        s.pushVecDimProof(ctx.dimensions(), ctx.IP().delta());
        //10 position
        s.pushVecDimProof(ctx.dimensions(), ctx.IP().position());
        //9 teamnumber
        s.push(0);
        //8 uuid
        s.push(ctx.IP().UUID());
        //7 dimensions
        s.push(ctx.dimensions());
        //6 separator
        s.push(File.separatorChar);
        //5 operating paradigm
        s.push((ctx.envFlags() & 0x08) != 0 ? 1 : 0);
        //4 version
        s.push(Globals.FUNGE_VERSION);
        //3 handprint
        s.push(Globals.HANDPRINT);
        //2 bpc
        s.push(4);
        //1 flags
        s.push(ctx.envFlags());
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
        int q = ctx.stack().pop();
        ctx.stop(q);
    }

    @Instr('t')
    public static void split(ExecutionContext ctx) {
        if (!ctx.concurrentAllowed()) {
            System.err.println("Program tried to execute concurrent funge when it was disabled!");
            reflect(ctx);
            return;
        }
        val clone = ctx.cloneIP();
        clone.delta().mul(-1);
    }

    @Instr('i')
    public static void input(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        if ((ctx.envFlags() & 0x02) == 0) {
            reflect(ctx);
            return;
        }
        val s = ctx.stack();
        val filename = s.popString();
        val flags = s.pop();
        val pos = s.popVecDimProof(ctx.dimensions(), mem.vec3i());
        pos.add(ctx.IP().storageOffset());
        try {
            if (!ctx.fileInputAllowed(filename)) {
                System.err.println("Code tried to read file not on whitelist: " + filename);
                reflect(ctx);
                return;
            }
        } catch (IOException e) {
            reflect(ctx);
            return;
        }
        val file = ctx.readFile(filename);
        if (file == null) {
            ctx.interpret('r');
            return;
        }
        val delta = ((flags & 1) == 1) ? ctx.fungeSpace().loadBinaryFileAt(pos.x, pos.y, pos.z, file) : ctx.fungeSpace().loadFileAt(pos.x, pos.y, pos.z, file, ctx.dimensions() == 3);
        pos.sub(ctx.IP().storageOffset());
        s.pushVecDimProof(ctx.dimensions(), delta);
        s.pushVecDimProof(ctx.dimensions(), pos);
    }

    @Instr('o')
    public static void output(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        if ((ctx.envFlags() & 0x04) == 0) {
            reflect(ctx);
            return;
        }
        val s = ctx.stack();
        val filename = s.popString();
        val flags = s.pop();
        val pos = s.popVecDimProof(ctx.dimensions(), mem.vec3i());
        val delta = s.popVecDimProof(ctx.dimensions(), mem.vec3i());
        if (ctx.dimensions() != 3) {
            delta.z = 1;
        }
        pos.add(ctx.IP().storageOffset());
        try {
            if (!ctx.fileOutputAllowed(filename)) {
                System.err.println("Code tried to write file not on whitelist: " + filename);
                reflect(ctx);
                return;
            }
        } catch (IOException e) {
            reflect(ctx);
            return;
        }
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
            ctx.stack().push(counter);
        } else {
            ctx.interpret('r');
        }
    }

    @Instr('~')
    @SneakyThrows
    public static void readChar(ExecutionContext ctx) {
        ctx.output().flush();
        ctx.stack().push(ctx.input(false));
    }

    @Instr('=')
    public static void sysCall(ExecutionContext ctx) {
        if (!ctx.syscallAllowed()) {
            System.err.println("Program tried to syscall while it was disabled!");
            reflect(ctx);
            return;
        }
        val command = ctx.stack().popString();
        try {
            val proc = Runtime.getRuntime().exec(command);
            ctx.stack().push(proc.waitFor());
        } catch (Exception e) {
            ctx.stack().push(-1);
        }
    }

    public static void stack(ExecutionContext ctx, Consumer<IStack> runner) {
        runner.accept(ctx.stack());
    }

    public static void binop(ExecutionContext ctx, BinaryOperator op) {
        val TOSS = ctx.stack();
        int b = TOSS.pop();
        int a = TOSS.pop();
        TOSS.push(op.op(a, b));
    }

    @Override
    public void load(ObjIntConsumer<Instruction> instructionSet) {
        InstructionSet.super.load(instructionSet);
        for (int i = 0; i <= 9; i++) {
            int finalI = i;
            instructionSet.accept((ctx) -> ctx.stack().push(finalI), '0' + i);
        }
        for (int i = 0; i <= 5; i++) {
            int finalI = i;
            instructionSet.accept((ctx) -> ctx.stack().push(10 + finalI), 'a' + i);
        }
    }

    @Override
    public void unload(IntConsumer instructionSet) {
        throw new UnsupportedOperationException("Cannot unload the base syntax");
    }

    public interface BinaryOperator {
        int op(int a, int b);
    }
}
