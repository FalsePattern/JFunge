package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.joml.Vector2i;
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
        val vecs = getGlobal(ctx);
        val vec = new Vector3i();
        val stack = ctx.stack();
        if (ctx.dimensions() == 3) {
            stack.pop3(vec);
        } else {
            val vec2 = stack.pop2();
            vec.x = vec2.x;
            vec.y = vec2.y;
        }
        stack.push(vecs.reference(vec));
    }

    @Instr('D')
    public static void dereference(ExecutionContext ctx) {
        val vecs = getGlobal(ctx);
        val stack = ctx.stack();
        Vector3i vec = vecs.dereference(stack.pop());
        if (ctx.dimensions() == 3) {
            stack.push3(vec);
        } else {
            stack.push2(new Vector2i(vec.x, vec.y));
        }
    }

    public static class Vectors {
        private final TIntObjectMap<Vector3i> references = new TIntObjectHashMap<>();
        private final TObjectIntMap<Vector3i> dereferences = new TObjectIntHashMap<>();
        private static int counter = 0;

        public int reference(Vector3i vec) {
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
