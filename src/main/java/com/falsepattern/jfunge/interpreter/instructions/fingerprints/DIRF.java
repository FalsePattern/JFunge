package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.PermissionException;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DIRF implements Fingerprint {
    public static final DIRF INSTANCE = new DIRF();
    @Override
    public int code() {
        return 0x44495246;
    }

    @Instr('C')
    public static void changeDirectory(ExecutionContext ctx) {
        val stack = ctx.stack();
        val path = stack.popString();
        try {
            if (!ctx.changeDirectory(path)) {
                ctx.interpret('r');
            }
        } catch (PermissionException e) {
            System.err.println(e.getMessage());
            ctx.interpret('r');
        }
    }

    @Instr('M')
    public static void makeDirectory(ExecutionContext ctx) {
        val stack = ctx.stack();
        val path = stack.popString();
        try {
            if (!ctx.makeDirectory(path)) {
                ctx.interpret('r');
            }
        } catch (PermissionException e) {
            System.err.println(e.getMessage());
            ctx.interpret('r');
        }
    }

    @Instr('R')
    public static void removeDirectory(ExecutionContext ctx) {
        val stack = ctx.stack();
        val path = stack.popString();
        try {
            if (!ctx.removeDirectory(path)) {
                ctx.interpret('r');
            }
        } catch (PermissionException e) {
            System.err.println(e.getMessage());
            ctx.interpret('r');
        }
    }
}
