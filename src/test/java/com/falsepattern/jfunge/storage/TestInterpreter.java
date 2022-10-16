package com.falsepattern.jfunge.storage;

import com.falsepattern.jfunge.interpreter.Interpreter;
import lombok.val;
import lombok.var;
import org.apache.commons.io.output.TeeOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TestInterpreter {
    private static final Interpreter.FileIOSupplier fakeSupplier = new Interpreter.FileIOSupplier() {

        private final Map<String, byte[]> files = new HashMap<>();

        @Override
        public byte[] readFile(String file) throws IOException {
            if (files.containsKey(file)) {
                val b = files.get(file);
                return Arrays.copyOf(b, b.length);
            } else {
                val s = TestInterpreter.class.getResourceAsStream("/" + file);
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

    private static byte[] readProgram(String path) {
        val program = new ByteArrayOutputStream();
        Assertions.assertDoesNotThrow(() -> {
            val reader = TestInterpreter.class.getResourceAsStream(path);
            Assertions.assertNotNull(reader);
            var read = 0;
            val b = new byte[4096];
            while ((read = reader.read(b)) > 0) {
                program.write(b, 0, read);
            }
        });
        return program.toByteArray();
    }

    private static int interpret(String[] args, byte[] code, int iterLimit, InputStream input, OutputStream output) {
        return Assertions.assertDoesNotThrow(() -> Interpreter.executeProgram(false, args, code, iterLimit, input, output, fakeSupplier));
    }

    private static InputStream nullStream() {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Test
    public void testMycology() {
        val checkingOutput = new ByteArrayOutputStream();
        val output = new TeeOutputStream(checkingOutput, System.out);
        val program = readProgram("/mycology.b98");
        val returnCode = interpret(new String[]{"mycology.b98"}, program, 300000, nullStream(), output);
        val txt = checkingOutput.toString();
        Assertions.assertTrue(Arrays.stream(txt.split("\n")).noneMatch((line) -> {
            if (line.startsWith("BAD")) {
                System.err.println("Found BAD check in Mycology! Interpreter is NOT standard-compliant");
                return true;
            } else {
                return false;
            }
        }));
        Assertions.assertEquals(15, returnCode);
    }

    @Test
    public void testSemicolonAtStart() {
        System.out.println("Testing edge case ;;.@");
        val output = new ByteArrayOutputStream();
        val returnCode = interpret(new String[0], ";;.@".getBytes(StandardCharsets.UTF_8), 50, nullStream(), output);
        val txt = output.toString();
        Assertions.assertEquals("0 ", txt);
        Assertions.assertEquals(0, returnCode);
    }

    @Test
    public void testPutCharAtStart() {
        System.out.println("Testing edge case 'a,@");
        val output = new ByteArrayOutputStream();
        val returnCode = interpret(new String[0], "'a,@".getBytes(StandardCharsets.UTF_8), 50, nullStream(), output);
        val txt = output.toString();
        Assertions.assertEquals("a", txt);
        Assertions.assertEquals(0, returnCode);
    }
}
