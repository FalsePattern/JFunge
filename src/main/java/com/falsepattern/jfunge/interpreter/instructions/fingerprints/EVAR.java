package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class EVAR implements Fingerprint {
    public static final EVAR INSTANCE = new EVAR();

    @Instr('G')
    public static void getEnvironmentVariable(ExecutionContext ctx) {
        val stack = ctx.stack();
        val key = stack.popString();
        val value = ctx.env().get(key);
        if (value == null) {
            ctx.IP().reflect();
        } else {
            stack.pushString(value);
        }
    }

    @Instr('N')
    public static void getEnvironmentVariableCount(ExecutionContext ctx) {
        ctx.stack().push(ctx.envKeys().size());
    }

    @Instr('P')
    public static void setEnvironmentVariable(ExecutionContext ctx) {
        val stack = ctx.stack();
        val keyValuePar = stack.popString();
        val equalsIndex = keyValuePar.indexOf('=');
        if (equalsIndex == -1) {
            ctx.IP().reflect();
            return;
        }
        val key = keyValuePar.substring(0, equalsIndex);
        val value = keyValuePar.substring(equalsIndex + 1);
        if (!ctx.env().containsKey(key)) {
            ctx.envKeys().add(key);
        }
        ctx.env().put(key, value);
    }

    @Instr('V')
    public static void getEnvironmentVariableAtIndex(ExecutionContext ctx) {
        val stack = ctx.stack();
        val index = stack.pop();
        val keys = ctx.envKeys();
        if (index < 0 || index >= keys.size()) {
            ctx.IP().reflect();
            return;
        }
        val key = keys.get(index);
        stack.pushString(key + "=" + ctx.env().get(key));
    }

    @Override
    public int code() {
        return 0x45564152;
    }
}
