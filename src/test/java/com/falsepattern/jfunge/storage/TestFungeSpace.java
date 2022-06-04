package com.falsepattern.jfunge.storage;

import lombok.val;
import org.joml.Vector4i;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.nio.charset.StandardCharsets;

import static com.falsepattern.jfunge.storage.Chunk.*;

public class TestFungeSpace {
    private static int toPos(int fragment) {
        int sign = -((fragment >>> 2) & 1);
        return sign + (1 + 2 * sign) * (((fragment >>> 1) & 1) * CHUNK_EDGE_SIZE + (fragment & 1));
    }
    @Test
    public void testSetGet() {
        val fungeSpace = new FungeSpace(0);
        val count = (int) Math.pow(Math.pow(2, 3), 3);
        for (int i = 0; i < count; i++) {
            System.out.println(toPos(i) + ", " + toPos(i >>> 3) + ", " + toPos(i >>> 6));
            fungeSpace.set(toPos(i), toPos(i >>> 3), toPos(i >>> 6), i + 1);
        }
        for (int i = 0; i < count; i++) {
            Assertions.assertEquals(i + 1, fungeSpace.get(toPos(i), toPos(i >>> 3), toPos(i >>> 6)));
        }
    }

    @Test
    public void testBounds() {
        val space = new FungeSpace(0);
        val expected = new Bounds();
        Assertions.assertEquals(expected.set(0, 0, 0, 0, 0, 0), space.bounds());
        space.set(0, 0, 0, 1);
        Assertions.assertEquals(expected.set(0, 0, 0, 0, 0, 0), space.bounds());
        space.set(1, 0, 0, 1);
        space.set(0, 1, 0, 2);
        space.set(0, 0, 1, 3);
        Assertions.assertEquals(expected.set(0, 0, 0, 1, 1, 1), space.bounds());
        space.set(0, 0, 0, 0);
        space.set(1, 0, 0, 0);
        space.set(0, 1, 0, 0);
        space.set(0, 0, 1, 0);
        Assertions.assertEquals(expected.set(0, 0, 0, 0, 0, 0), space.bounds());
        space.set(-10, -5, -20, 10);
        space.set(127, 5, 5, 8);
        space.set(5, 127, 5, 5);
        space.set(5, 5, 127, 5);
        Assertions.assertEquals(expected.set(-10, -5, -20, 127, 127, 127), space.bounds());
        space.set(-10, -5, -20, 0);
        Assertions.assertEquals(expected.set(5, 5, 5, 127, 127, 127), space.bounds());
        space.set(127, 5, 5, 0);
        Assertions.assertEquals(expected.set(5, 5, 5, 5, 127, 127), space.bounds());
        space.set(5, 127, 5, 0);
        Assertions.assertEquals(expected.set(5, 5, 127, 5, 5, 127), space.bounds());
        space.set(5, 5, 127, 0);
        Assertions.assertEquals(expected.set(0, 0, 0, 0, 0, 0), space.bounds());
    }

    @Test
    public void testLoadFile() {
        val space = new FungeSpace(32);
        space.loadFileAt(0, 0, 0, "abc\ndef\fghi".getBytes(StandardCharsets.UTF_8));
        Assertions.assertEquals('a', space.get(0, 0, 0));
        Assertions.assertEquals('b', space.get(1, 0, 0));
        Assertions.assertEquals('c', space.get(2, 0, 0));
        Assertions.assertEquals('d', space.get(0, 1, 0));
        Assertions.assertEquals('e', space.get(1, 1, 0));
        Assertions.assertEquals('f', space.get(2, 1, 0));
        Assertions.assertEquals('g', space.get(0, 0, 1));
        Assertions.assertEquals('h', space.get(1, 0, 1));
        Assertions.assertEquals('i', space.get(2, 0, 1));
    }
}
