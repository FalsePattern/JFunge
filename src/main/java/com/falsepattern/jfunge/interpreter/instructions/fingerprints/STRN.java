package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import com.falsepattern.jfunge.util.MemoryStack;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class STRN implements Fingerprint {
    public static final STRN INSTANCE = new STRN();
    @Override
    public int code() {
        return 0x5354524E;
    }

    @Instr('A')
    public static void append(ExecutionContext ctx) {
        val stack = ctx.stack();
        val b = stack.popString();
        val a = stack.popString();
        stack.pushString(b + a);
    }

    @Instr('C')
    public static void compare(ExecutionContext ctx) {
        val stack = ctx.stack();
        val b = stack.popString();
        val a = stack.popString();
        stack.push(b.compareTo(a));
    }

    @SneakyThrows
    @Instr('D')
    public static void display(ExecutionContext ctx) {
        val stack = ctx.stack();
        val str = stack.popString();
        ctx.output().write(str.getBytes());
    }

    @Instr('F')
    public static void find(ExecutionContext ctx) {
        val stack = ctx.stack();
        val b = stack.popString();
        val a = stack.popString();
        val index = b.indexOf(a);
        if (index < 0)
            stack.push(0);
        else
            stack.pushString(b.substring(index));
    }

    @Instr('G')
    public static void getString(ExecutionContext ctx) {
        val stack = ctx.stack();

        @Cleanup val mStack = MemoryStack.stackPush();
        val vec = mStack.vec3i();
        stack.popVecDimProof(ctx.dimensions(), vec);

        vec.add(ctx.IP().storageOffset());

        val fs = ctx.fungeSpace();
        val bounds = fs.bounds();

        val str = new StringBuilder();
        while (bounds.inBounds(vec)) {
            val c = fs.get(vec);
            if (c == 0) break;
            str.append((char)c);
            vec.add(1, 0, 0);
        }
        stack.pushString(str.toString());
    }

    @Instr('I')
    public static void readStringFromInput(ExecutionContext ctx) {
        val stack = ctx.stack();
        val str = new StringBuilder();
        while (ctx.input(true) == '\n') {
            ctx.input(false);
        }
        int c;
        while ((c = ctx.input(true)) != -1 && c != 0 && c != '\n') {
            str.append((char)ctx.input(false));
        }
        stack.pushString(str.toString());
    }

    @Instr('L')
    public static void leftMost(ExecutionContext ctx) {
        val stack = ctx.stack();
        val offset = stack.pop();
        val str = stack.popString();
        subString(ctx, str, 0, offset);
    }

    @Instr('M')
    public static void section(ExecutionContext ctx) {
        val stack = ctx.stack();
        val count = stack.pop();
        val offset = stack.pop();
        val str = stack.popString();
        if (offset < 0) {
            ctx.IP().reflect();
            return;
        }
        subString(ctx, str, offset, offset + count);
    }

    @Instr('N')
    public static void length(ExecutionContext ctx) {
        val stack = ctx.stack();
        val str = stack.popString();
        stack.pushString(str);
        stack.push(str.length());
    }

    @Instr('P')
    public static void putString(ExecutionContext ctx) {
        val stack = ctx.stack();

        @Cleanup val mStack = MemoryStack.stackPush();
        val vec = mStack.vec3i();
        stack.popVecDimProof(ctx.dimensions(), vec);
        vec.add(ctx.IP().storageOffset());

        val str = stack.popString();

        val fs = ctx.fungeSpace();
        for (int i = 0; i < str.length(); i++) {
            fs.set(vec, str.charAt(i));
            vec.add(1, 0, 0);
        }
        fs.set(vec, 0);
    }

    @Instr('R')
    public static void rightMost(ExecutionContext ctx) {
        val stack = ctx.stack();
        val offset = stack.pop();
        val str = stack.popString();
        subString(ctx, str, str.length() - offset, str.length());
    }

    @Instr('S')
    public static void intToString(ExecutionContext ctx) {
        val stack = ctx.stack();
        val num = stack.pop();
        stack.pushString(Integer.toString(num));
    }

    @Instr('V')
    public static void parseInt(ExecutionContext ctx) {
        val stack = ctx.stack();
        val str = stack.popString();
        try {
            stack.push(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            ctx.IP().reflect();
        }
    }

    private static void subString(ExecutionContext ctx, String str, int start, int end) {
        val stack = ctx.stack();
        val length = str.length();
        if (start < 0) start = 0;
        if (end > length) end = length;
        if (start > end) {
            ctx.IP().reflect();
        } else {
            stack.pushString(str.substring(start, end));
        }
    }
}
