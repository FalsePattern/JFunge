package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import com.falsepattern.jfunge.util.MemoryStack;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JSTR implements Fingerprint {
    public static final JSTR INSTANCE = new JSTR();

    @Instr('P')
    public static void putString(ExecutionContext ctx) {
        val stack = ctx.stack();
        val ip = ctx.IP();
        val fs = ctx.fungeSpace();
        @Cleanup val mStack = MemoryStack.stackPush();
        val position = mStack.vec3i();
        val delta = mStack.vec3i();

        val count = stack.pop();
        stack.popVecDimProof(ctx.dimensions(), position);
        stack.popVecDimProof(ctx.dimensions(), delta);

        position.add(ip.storageOffset());
        for (int i = 0; i < count; i++) {
            fs.set(position, stack.pop());
            position.add(delta);
        }
    }

    @Instr('G')
    public static void getString(ExecutionContext ctx) {
        val stack = ctx.stack();
        val ip = ctx.IP();
        val fs = ctx.fungeSpace();
        @Cleanup val mStack = MemoryStack.stackPush();
        val position = mStack.vec3i();
        val delta = mStack.vec3i();

        val count = stack.pop();
        stack.popVecDimProof(ctx.dimensions(), position);
        stack.popVecDimProof(ctx.dimensions(), delta);

        position.add(ip.storageOffset());
        stack.push(0);
        for (int i = 0; i < count; i++) {
            stack.push(fs.get(position));
            position.add(delta);
        }
    }

    @Override
    public int code() {
        return 0x4a535452;
    }
}
