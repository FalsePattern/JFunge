package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import com.falsepattern.jfunge.util.MemoryStack;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.val;
import org.joml.Vector3i;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class REFC implements Fingerprint {
    public static final REFC INSTANCE = new REFC();

    private static Vectors getGlobal(ExecutionContext ctx) {
        if (!ctx.hasGlobal(INSTANCE.code(), "references")) {
            ctx.putGlobal(INSTANCE.code(), "references", new Vectors());
        }
        return ctx.getGlobal(INSTANCE.code(), "references");
    }

    @Instr('R')
    public static void reference(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val vecs = getGlobal(ctx);
        val stack = ctx.stack();
        val vec = stack.popVecDimProof(ctx.dimensions(), mem.vec3i());
        stack.push(vecs.reference(vec));
    }

    @Instr('D')
    public static void dereference(ExecutionContext ctx) {
        val vecs = getGlobal(ctx);
        val stack = ctx.stack();
        val vec = vecs.dereference(stack.pop());
        stack.pushVecDimProof(ctx.dimensions(), vec);
    }

    public static class Vectors {
        private final TIntObjectMap<Vector3i> references = new TIntObjectHashMap<>();
        private final TObjectIntMap<Vector3i> dereferences = new TObjectIntHashMap<>();
        private static int counter = 0;

        public int reference(Vector3i vec) {
            vec = new Vector3i(vec);
            if (dereferences.containsKey(vec)) {
                return dereferences.get(vec);
            } else {
                int ref = counter++;
                references.put(ref, vec);
                dereferences.put(vec, ref);
                return ref;
            }
        }

        public Vector3i dereference(int index) {
            if (references.containsKey(index)) {
                return references.get(index);
            } else {
                return null;
            }
        }
    }

    @Override
    public int code() {
        return 0x52454643;
    }
}
