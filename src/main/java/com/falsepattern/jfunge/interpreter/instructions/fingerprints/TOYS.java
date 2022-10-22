package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.storage.FungeSpace;
import com.falsepattern.jfunge.util.MemoryStack;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.val;
import org.joml.Vector3i;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TOYS implements Fingerprint {
    public static final TOYS INSTANCE = new TOYS();

    private interface TransferOperation {
        void operate(FungeSpace space, Vector3i src, Vector3i dst, int x, int y, int z);
    }

    private interface IterationOperation {
        void operate(FungeSpace space, Vector3i dst, Vector3i delta, Vector3i src, TransferOperation op);
    }

    private static void copy(FungeSpace space, Vector3i src, Vector3i dst, int x, int y, int z) {
        space.set(dst.x + x, dst.y + y, dst.z + z, space.get(src.x + x, src.y + y, src.z + z));
    }

    private static void move(FungeSpace space, Vector3i src, Vector3i dst, int x, int y, int z) {
        copy(space, src, dst, x, y, z);
        space.set(src.x + x, src.y + y, src.z + z, ' ');
    }

    private static void lowOrder(FungeSpace space, Vector3i dst, Vector3i delta, Vector3i src, TransferOperation op) {
        for (int z = 0; z < delta.z; z++)
            for (int y = 0; y < delta.y; y++)
                for (int x = 0; x < delta.x; x++)
                    op.operate(space, src, dst, x, y, z);
    }

    private static void highOrder(FungeSpace space, Vector3i dst, Vector3i delta, Vector3i src, TransferOperation op) {
        for (int z = delta.z - 1; z >= 0; z--)
            for (int y = delta.y - 1; y >= 0; y--)
                for (int x = delta.x - 1; x >= 0; x--)
                    op.operate(space, src, dst, x, y, z);
    }

    private static void executeOperation(ExecutionContext ctx, IterationOperation iter, TransferOperation op) {
        @Cleanup val mem = MemoryStack.stackPush();
        val dst = mem.vec3i();
        val delta = mem.vec3i();
        val src = mem.vec3i();
        val stack = ctx.stack();
        stack.popVecDimProof(ctx.dimensions(), dst);
        stack.popVecDimProof(ctx.dimensions(), delta);
        stack.popVecDimProof(ctx.dimensions(), src);
        if (ctx.dimensions() != 3) {
            delta.z = 1;
        }
        val space = ctx.fungeSpace();
        iter.operate(space, dst, delta, src, op);
    }

    @Instr('C')
    public static void lowOrderCopy(ExecutionContext ctx) {
        executeOperation(ctx, TOYS::lowOrder, TOYS::copy);
    }

    @Instr('K')
    public static void highOrderCopy(ExecutionContext ctx) {
        executeOperation(ctx, TOYS::highOrder, TOYS::copy);
    }

    @Instr('M')
    public static void lowOrderMove(ExecutionContext ctx) {
        executeOperation(ctx, TOYS::lowOrder, TOYS::move);
    }

    @Instr('V')
    public static void highOrderMove(ExecutionContext ctx) {
        executeOperation(ctx, TOYS::highOrder, TOYS::move);
    }

    @Instr('S')
    public static void fill(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val dst = mem.vec3i();
        val delta = mem.vec3i();
        val stack = ctx.stack();
        stack.popVecDimProof(ctx.dimensions(), dst);
        stack.popVecDimProof(ctx.dimensions(), delta);
        if (ctx.dimensions() != 3) {
            delta.z = 1;
        }
        val cellValue = stack.pop();
        val space = ctx.fungeSpace();
        lowOrder(space, dst, delta, null, (space1, src, dst1, x, y, z) -> space.set(dst1.x + x, dst1.y + y, dst1.z + z, cellValue));
    }

    @Instr('J')
    public static void shiftY(ExecutionContext ctx) {
        int shift = ctx.stack().pop();
        if (shift != 0) {
            val pos = ctx.IP().position();
            val x = pos.x();
            val z = pos.z();
            val space = ctx.fungeSpace();
            val bounds = space.bounds();
            if (shift < 0) {
                for (int y = bounds.yMin(); y <= bounds.yMax() - shift; y++) {
                    space.set(x, y + shift, z, space.get(x, y, z));
                }
            } else {
                for (int y = bounds.yMax(); y >= bounds.yMin() - shift; y--) {
                    space.set(x, y + shift, z, space.get(x, y, z));
                }
            }
        }
    }

    @Instr('O')
    public static void shiftX(ExecutionContext ctx) {
        int shift = ctx.stack().pop();
        if (shift != 0) {
            val pos = ctx.IP().position();
            val y = pos.y();
            val z = pos.z();
            val space = ctx.fungeSpace();
            val bounds = space.bounds();
            if (shift < 0) {
                for (int x = bounds.xMin(); x <= bounds.xMax() - shift; x++) {
                    space.set(x + shift, y, z, space.get(x, y, z));
                }
            } else {
                for (int x = bounds.xMax(); x >= bounds.xMin() - shift; x--) {
                    space.set(x + shift, y, z, space.get(x, y, z));
                }
            }
        }
    }

    @Instr('L')
    public static void getLeft(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val pos = mem.vec3i().set(ctx.IP().position());
        ctx.interpret('[');
        ctx.interpret('\'');
        ctx.interpret(']');
        ctx.IP().position().set(pos);
    }

    @Instr('R')
    public static void getRight(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val pos = mem.vec3i().set(ctx.IP().position());
        ctx.interpret(']');
        ctx.interpret('\'');
        ctx.interpret('[');
        ctx.IP().position().set(pos);
    }

    @Instr('I')
    public static void increment(ExecutionContext ctx) {
        val stack = ctx.stack();
        stack.push(stack.pop() + 1);
    }

    @Instr('D')
    public static void decrement(ExecutionContext ctx) {
        val stack = ctx.stack();
        stack.push(stack.pop() - 1);
    }

    @Instr('N')
    public static void negate(ExecutionContext ctx) {
        val stack = ctx.stack();
        stack.push(~stack.pop());
    }

    @Instr('H')
    public static void shift(ExecutionContext ctx) {
        val stack = ctx.stack();
        val b = stack.pop();
        val a = stack.pop();
        if (b >= 0) {
            stack.push(a << b);
        } else {
            //UNDEF signed right shift because unsigned shift fails mycology check for some reason
            //TODO inspect the check inside mycology
            stack.push(a >> (-b));
        }
    }

    @Instr('A')
    public static void replicate(ExecutionContext ctx) {
        val stack = ctx.stack();
        val n = stack.pop();
        val x = stack.pop();
        for (int i = 0; i < n; i++) {
            stack.push(x);
        }
    }

    //UNDEF I don't know what "butterfly operation" means, so I just checked how rcfunge does it :shrug:
    @Instr('B')
    public static void butterfly(ExecutionContext ctx) {
        val stack = ctx.stack();
        val a = stack.pop();
        val b = stack.pop();
        stack.push(a + b);
        stack.push(a - b);
    }

    @Instr('E')
    public static void sum(ExecutionContext ctx) {
        val stack = ctx.stack();
        int sum = 0;
        while (stack.size() > 0) {
            sum += stack.pop();
        }
        stack.push(sum);
    }

    @Instr('P')
    public static void product(ExecutionContext ctx) {
        val stack = ctx.stack();
        int prod = 1;
        while (stack.size() > 0) {
            prod *= stack.pop();
        }
        stack.push(prod);
    }

    private static void operateMatrix(ExecutionContext ctx, boolean get) {
        @Cleanup val mem = MemoryStack.stackPush();
        val stack = ctx.stack();
        val pos = stack.popVecDimProof(ctx.dimensions(), mem.vec3i());
        //UNDEF F and G pop i and j in the style of a vector (first Y then X)
        val scale = stack.pop2(mem.vec2i());
        val space = ctx.fungeSpace();
        if (get) {
            for (int i = scale.y - 1; i >= 0; i--) {
                for (int j = scale.x - 1; j >= 0; j--) {
                    stack.push(space.get(pos.x + j, pos.y + i, pos.z));
                }
            }
        } else {
            for (int i = 0; i < scale.y; i++) {
                for (int j = 0; j < scale.x; j++) {
                    space.set(pos.x + j, pos.y + i, pos.z, stack.pop());
                }
            }
        }
    }

    @Instr('F')
    public static void putMatrix(ExecutionContext ctx) {
        operateMatrix(ctx, false);
    }

    @Instr('G')
    public static void getMatrix(ExecutionContext ctx) {
        operateMatrix(ctx, true);
    }

    @Instr('Q')
    public static void storeBehind(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val pos = mem.vec3i().set(ctx.IP().position());
        ctx.interpret('r');
        ctx.interpret('s');
        ctx.interpret('r');
        ctx.IP().position().set(pos);
    }

    @Instr('T')
    public static void dimBranch(ExecutionContext ctx) {
        int dim = ctx.stack().pop();
        switch (dim) {
            case 0:
                ctx.interpret('_');
                break;
            case 1:
                ctx.interpret('|');
                break;
            case 2:
                if (ctx.dimensions() != 3) {
                    ctx.interpret('r');
                } else {
                    ctx.interpret('m');
                }
                break;
        }
    }

    private static final int[] branchDirs = new int[]{'<', '>', '^', 'v', 'h', 'l'};

    @Instr('U')
    public static void transmutableBranch(ExecutionContext ctx) {
        int random = ctx.IP().nextRandom() & 0xffff;
        random %= ctx.dimensions() * 2;
        val dir = branchDirs[random];
        val pos = ctx.IP().position();
        ctx.fungeSpace().set(pos.x, pos.y, pos.z, dir);
        ctx.interpret(dir);
    }

    @Instr('W')
    public static void waitForValue(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val stack = ctx.stack();
        val pos = stack.popVecDimProof(ctx.dimensions(), mem.vec3i());
        int value = stack.pop();
        val space = ctx.fungeSpace();
        int valueAtCell = space.get(mem.vec3i().set(pos).add(ctx.IP().storageOffset()));
        if (valueAtCell < value) {
            stack.push(value);
            stack.pushVecDimProof(ctx.dimensions(), pos);
            ctx.IP().position().sub(ctx.IP().delta());
        } else if (valueAtCell > value) {
            ctx.interpret('r');
        }
    }

    @Instr('X')
    public static void incrementX(ExecutionContext ctx) {
        ctx.IP().position().x++;
    }

    @Instr('Y')
    public static void incrementY(ExecutionContext ctx) {
        ctx.IP().position().y++;
    }

    @Instr('Z')
    public static void incrementZ(ExecutionContext ctx) {
        if (ctx.dimensions() == 3) {
            ctx.IP().position().z++;
        } else {
            ctx.interpret('r');
        }
    }

    @Override
    public int code() {
        return 0x544f5953;
    }
}
