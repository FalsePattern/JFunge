package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HRTI implements Fingerprint {
    public static final HRTI INSTANCE = new HRTI();

    private static TIntLongMap getMarkMap(ExecutionContext ctx) {
        if (!ctx.hasGlobal(INSTANCE.code(), "marks")) {
            ctx.putGlobal(INSTANCE.code(), "marks", new TIntLongHashMap());
        }
        return ctx.getGlobal(INSTANCE.code(), "marks");
    }

    @Instr('G')
    public static void granularity(ExecutionContext ctx) {
        ctx.stack().push(1);
    }

    @Instr('M')
    public static void mark(ExecutionContext ctx) {
        val marks = getMarkMap(ctx);
        marks.put(ctx.IP().UUID(), System.nanoTime());
    }

    @Instr('T')
    public static void timer(ExecutionContext ctx) {
        val marks = getMarkMap(ctx);
        val ip = ctx.IP();
        if (!marks.containsKey(ip.UUID())) {
            ctx.IP().reflect();
            return;
        }
        ctx.stack().push((int) ((System.nanoTime() - marks.get(ip.UUID())) / 1000L));
    }

    @Instr('E')
    public static void erase(ExecutionContext ctx) {
        val marks = getMarkMap(ctx);
        val ip = ctx.IP();
        marks.remove(ip.UUID());
    }

    @Instr('S')
    public static void second(ExecutionContext ctx) {
        ctx.stack().push((int) ((System.nanoTime() % 1000000000L) / 1000L));
    }

    @Override
    public int code() {
        return 0x48525449;
    }
}
