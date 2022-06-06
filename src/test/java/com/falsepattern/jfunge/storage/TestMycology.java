package com.falsepattern.jfunge.storage;

import com.falsepattern.jfunge.interpreter.Interpreter;
import lombok.val;
import lombok.var;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class TestMycology {
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
        val returnCode = Assertions.assertDoesNotThrow(() -> Interpreter.executeProgram(false, new String[]{"mycology.b98"}, program.toByteArray(), 100000, input, output));
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
