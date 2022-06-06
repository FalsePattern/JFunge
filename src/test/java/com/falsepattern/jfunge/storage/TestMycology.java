package com.falsepattern.jfunge.storage;

import com.falsepattern.jfunge.interpreter.Interpreter;
import lombok.val;
import lombok.var;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TestMycology {
    private static final Interpreter.FileIOSupplier fakeSupplier = new Interpreter.FileIOSupplier() {

        private final Map<String, byte[]> files = new HashMap<>();

        @Override
        public byte[] readFile(String file) throws IOException {
            if (files.containsKey(file)) {
                val b = files.get(file);
                return Arrays.copyOf(b, b.length);
            } else {
                val s = TestMycology.class.getResourceAsStream("/" + file);
                if (s == null) {
                    throw new FileNotFoundException("Could not find resource " + file);
                }
                val ret = new ByteArrayOutputStream();
                val b = new byte[4096];
                var r = 0;
                while ((r = s.read(b)) > 0) {
                    ret.write(b, 0, r);
                }
                val bytes = ret.toByteArray();
                files.put(file, bytes);
                return Arrays.copyOf(bytes, bytes.length);
            }
        }

        @Override
        public boolean writeFile(String file, byte[] data) throws IOException {
            files.put(file, Arrays.copyOf(data, data.length));
            return true;
        }
    };

    @Test
    public void testMycology() {
        val input = new ByteArrayInputStream(new byte[0]);
        val output = new ByteArrayOutputStream();
        val program = new ByteArrayOutputStream();
        Assertions.assertDoesNotThrow(() -> {
            val reader = TestMycology.class.getResourceAsStream("/mycology.b98");
            Assertions.assertNotNull(reader);
            var read = 0;
            val b = new byte[4096];
            while ((read = reader.read(b)) > 0) {
                program.write(b, 0, read);
            }
        });
        val returnCode = Assertions.assertDoesNotThrow(() -> Interpreter.executeProgram(false, new String[]{"mycology.b98"}, program.toByteArray(), 300000, input, output, fakeSupplier));
        val txt = output.toString();
        Assertions.assertTrue(Arrays.stream(txt.split("\n")).noneMatch((line) -> {
            if (line.startsWith("BAD")) {
                System.out.println(line);
                return true;
            } else {
                return false;
            }
        }));
        Assertions.assertEquals(15, returnCode);
    }
}
