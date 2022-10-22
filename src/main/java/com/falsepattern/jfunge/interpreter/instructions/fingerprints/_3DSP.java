package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.util.MemoryStack;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.val;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class _3DSP implements Fingerprint {
    public static final _3DSP INSTANCE = new _3DSP();

    @Override
    public int code() {
        return 0x33445350;
    }

    private interface Op {
        void operate(Vector3fc a, Vector3fc b, Vector3f result);
    }

    private static Matrix4f getMatrix(ExecutionContext ctx, Vector3i origin, Matrix4f output) {
        val space = ctx.fungeSpace();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                output.set(x, y, Float.intBitsToFloat(space.get(origin.x + x, origin.y + y, origin.z)));
            }
        }
        output.determineProperties();
        return output;
    }

    private static void putMatrix(ExecutionContext ctx, Vector3i origin, Matrix4f matrix) {
        val space = ctx.fungeSpace();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                space.set(origin.x + x, origin.y + y, origin.z, Float.floatToRawIntBits(matrix.get(x, y)));
            }
        }
    }

    private static void binOp(ExecutionContext ctx, Op op) {
        @Cleanup val mem = MemoryStack.stackPush();
        val stack = ctx.stack();
        val b = stack.popF3(mem.vec3f());
        val a = stack.popF3(mem.vec3f());
        val res = mem.vec3f();
        op.operate(a, b, res);
        stack.pushF3(res);
    }

    @Instr('A')
    public static void add(ExecutionContext ctx) {
        binOp(ctx, (a, b, res) -> res.set(a).add(b));
    }

    @Instr('B')
    public static void sub(ExecutionContext ctx) {
        binOp(ctx, (a, b, res) -> res.set(a).sub(b));
    }

    @Instr('C')
    public static void cross(ExecutionContext ctx) {
        binOp(ctx, (a, b, res) -> res.set(a).cross(b));
    }

    @Instr('D')
    public static void dot(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val stack = ctx.stack();
        val b = stack.popF3(mem.vec3f());
        val a = stack.popF3(mem.vec3f());
        stack.pushF(a.dot(b));
    }

    @Instr('L')
    public static void length(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val v = ctx.stack().popF3(mem.vec3f());
        ctx.stack().pushF(v.length());
    }

    @Instr('M')
    public static void mul(ExecutionContext ctx) {
        binOp(ctx, (a, b, res) -> res.set(a).mul(b));
    }

    @Instr('N')
    public static void normalize(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val v = ctx.stack().popF3(mem.vec3f());
        v.normalize();
        ctx.stack().pushF3(v);
    }

    @Instr('P')
    public static void copyMatrix(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val stack = ctx.stack();
        val source = stack.popVecDimProof(ctx.dimensions(), mem.vec3i());
        val target = stack.popVecDimProof(ctx.dimensions(), mem.vec3i());
        val matrix = getMatrix(ctx, source, mem.mat4f());
        putMatrix(ctx, target, matrix);
    }

    @Instr('R')
    public static void genRotMatrix(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val stack = ctx.stack();
        val angle = stack.popF();
        val axis = stack.pop();
        val pos = stack.popVecDimProof(ctx.dimensions(), mem.vec3i());
        if (axis <= 0 || axis >= 4) {
            ctx.interpret('r');
            return;
        }
        val matrix = mem.mat4f();
        matrix.rotation(Math.toRadians(angle), axis == 1 ? 1 : 0, axis == 2 ? 1 : 0, axis == 3 ? 1 : 0);
        putMatrix(ctx, pos, matrix);
    }

    @Instr('S')
    public static void genScaleMatrix(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val stack = ctx.stack();
        val scale = stack.popF3(mem.vec3f());
        val pos = stack.popVecDimProof(ctx.dimensions(), mem.vec3i());
        val matrix = mem.mat4f();
        matrix.scaling(scale);
        putMatrix(ctx, pos, matrix);
    }

    @Instr('T')
    public static void genTranslationMatrix(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val stack = ctx.stack();
        val translation = stack.popF3(mem.vec3f());
        val pos = stack.popVecDimProof(ctx.dimensions(), mem.vec3i());
        val matrix = mem.mat4f();
        matrix.translation(translation);
        putMatrix(ctx, pos, matrix);
    }

    @Instr('U')
    public static void duplicateVector(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val stack = ctx.stack();
        val vec = stack.popF3(mem.vec3f());
        stack.pushF3(vec);
        stack.pushF3(vec);
    }

    @Instr('V')
    public static void mapTo2D(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val stack = ctx.stack();
        val vec = stack.popF3(mem.vec3f());
        if (vec.z == 0) {
            vec.z = 1;
        }
        stack.pushF(vec.x / vec.z);
        stack.pushF(vec.y / vec.z);
    }

    @Instr('X')
    public static void transformVector(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val stack = ctx.stack();
        val matrix = getMatrix(ctx, stack.popVecDimProof(ctx.dimensions(), mem.vec3i()), mem.mat4f());
        val vector = stack.popF3(mem.vec3f());
        matrix.transformPosition(vector);
        stack.pushF3(vector);
    }

    @Instr('Y')
    public static void multiplyMatrices(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val stack = ctx.stack();
        val tmp = mem.vec3i();
        val matB = getMatrix(ctx, stack.popVecDimProof(ctx.dimensions(), tmp), mem.mat4f());
        val matA = getMatrix(ctx, stack.popVecDimProof(ctx.dimensions(), tmp), mem.mat4f());
        putMatrix(ctx, stack.popVecDimProof(ctx.dimensions(), tmp), matB.mul(matA));
    }

    @Instr('Z')
    public static void scaleVector(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val stack = ctx.stack();
        val vec = stack.popF3(mem.vec3f());
        vec.mul(stack.popF());
        stack.pushF3(vec);
    }
}
