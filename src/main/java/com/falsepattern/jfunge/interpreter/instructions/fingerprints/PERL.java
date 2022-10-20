package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Funge98;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PERL implements Fingerprint {
    public static final PERL INSTANCE = new PERL();

    private static String executePerl(ExecutionContext ctx) {
        String name;
        do {
            name = UUID.randomUUID() + ".pl";
        } while (Files.exists(Paths.get(name)));
        try {
            try (val os = Files.newOutputStream(Paths.get(name), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                os.write("print eval(".getBytes(StandardCharsets.UTF_8));
                os.write(ctx.IP().stackStack.TOSS().popString().getBytes(StandardCharsets.UTF_8));
                os.write(");".getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
                Funge98.reflect(ctx);
                return null;
            }
            try {
                val process = Runtime.getRuntime().exec(new String[]{"perl", name});
                int returnCode;
                while (true) {
                    try {
                        returnCode = process.waitFor();
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (returnCode != 0) {
                    return Integer.toString(returnCode);
                }
                val res = new ByteArrayOutputStream();
                val input = process.getInputStream();
                val buf = new byte[256];
                int read;
                while ((read = input.read(buf)) > 0) {
                    res.write(buf, 0, read);
                }
                return res.toString("UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
                Funge98.reflect(ctx);
                return null;
            }
        } finally {
            try {
                Files.deleteIfExists(Paths.get(name));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Instr('E')
    public static void exec(ExecutionContext ctx) {
        String res = executePerl(ctx);
        if (res != null) {
            ctx.IP().stackStack.TOSS().pushString(res);
        }
    }

    @Instr('S')
    public static void getShelled(ExecutionContext ctx) {
        ctx.IP().stackStack.TOSS().push(1);
    }

    @Instr('I')
    public static void execInt(ExecutionContext ctx) {
        String res = executePerl(ctx);
        if (res != null) {
            int intRes;
            try {
                intRes = Integer.parseInt(res);
            } catch (NumberFormatException e) {
                Funge98.reflect(ctx);
                return;
            }
            ctx.IP().stackStack.TOSS().push(intRes);
        }
    }

    @Override
    public int code() {
        return 0x5045524c;
    }
}
