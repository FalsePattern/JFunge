package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import com.falsepattern.jfunge.storage.FungeSpace;
import com.falsepattern.jfunge.util.MemoryStack;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.val;
import org.joml.Vector3i;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class INDV implements Fingerprint {
    public static final INDV INSTANCE = new INDV();

    private static void getVector(FungeSpace fs, int dimensions, int x, int y, int z, Vector3i output) {
        switch (dimensions) {
            default:
                throw new IllegalStateException("Pointer logic only works on 3d or lower funge");
            case 3:
                output.z = fs.get(x, y, z);
                x++;
            case 2:
                output.y = fs.get(x, y, z);
                x++;
            case 1:
                output.x = fs.get(x, y, z);
        }
    }

    private static void putVector(FungeSpace fs, int dimensions, int x, int y, int z, Vector3i input) {
        switch (dimensions) {
            default:
                throw new IllegalStateException("Pointer logic only works on 3d or lower funge");
            case 3:
                fs.set(x, y, z, input.z);
                x++;
            case 2:
                fs.set(x, y, z, input.y);
                x++;
            case 1:
                fs.set(x, y, z, input.x);
        }
    }

    private static void retrievePointer(ExecutionContext ctx, Vector3i output) {
        val stack = ctx.stack();
        val ip = ctx.IP();
        val fs = ctx.fungeSpace();
        @Cleanup val mStack = MemoryStack.stackPush();

        val pointer = mStack.vec3i();
        stack.popVecDimProof(ctx.dimensions(), pointer);
        pointer.add(ip.storageOffset());
        getVector(fs, ctx.dimensions(), pointer.x, pointer.y, pointer.z, output);
    }

    @Instr('G')
    public static void getNumberAtPointer(ExecutionContext ctx) {
        val stack = ctx.stack();
        val ip = ctx.IP();

        @Cleanup val mStack = MemoryStack.stackPush();
        val pointer = mStack.vec3i();
        retrievePointer(ctx, pointer);
        pointer.add(ip.storageOffset());
        stack.push(ctx.fungeSpace().get(pointer));
    }

    @Instr('P')
    public static void putNumberAtPointer(ExecutionContext ctx) {
        val stack = ctx.stack();
        val ip = ctx.IP();

        @Cleanup val mStack = MemoryStack.stackPush();
        val pointer = mStack.vec3i();
        retrievePointer(ctx, pointer);
        pointer.add(ip.storageOffset());
        ctx.fungeSpace().set(pointer, stack.pop());
    }

    @Instr('V')
    public static void getVectorAtPointer(ExecutionContext ctx) {
        val stack = ctx.stack();
        val ip = ctx.IP();

        @Cleanup val mStack = MemoryStack.stackPush();
        val pointer = mStack.vec3i();
        retrievePointer(ctx, pointer);
        pointer.add(ip.storageOffset());
        getVector(ctx.fungeSpace(), ctx.dimensions(), pointer.x, pointer.y, pointer.z, pointer);
        stack.pushVecDimProof(ctx.dimensions(), pointer);
    }

    @Instr('W')
    public static void putVectorAtPointer(ExecutionContext ctx) {
        val stack = ctx.stack();
        val ip = ctx.IP();

        @Cleanup val mStack = MemoryStack.stackPush();
        val pointer = mStack.vec3i();
        retrievePointer(ctx, pointer);
        pointer.add(ip.storageOffset());
        val vec = mStack.vec3i();
        stack.popVecDimProof(ctx.dimensions(), vec);
        putVector(ctx.fungeSpace(), ctx.dimensions(), pointer.x, pointer.y, pointer.z, vec);
    }

    @Override
    public int code() {
        return 0x494e4456;
    }
}
