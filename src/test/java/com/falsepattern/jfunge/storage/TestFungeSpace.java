package com.falsepattern.jfunge.storage;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static com.falsepattern.jfunge.storage.Chunk.*;

public class TestFungeSpace {
    @Test
    public void testSetGet() {
        val fungeSpace = new FungeSpace(0);
        fungeSpace.set(0, 0, 10);
        fungeSpace.set(1, 1, 9);
        fungeSpace.set(CHUNK_EDGE_SIZE, 0, 8);
        fungeSpace.set(CHUNK_EDGE_SIZE + 1, 1, 7);
        fungeSpace.set(CHUNK_EDGE_SIZE, CHUNK_EDGE_SIZE, 6);
        fungeSpace.set(CHUNK_EDGE_SIZE + 1, CHUNK_EDGE_SIZE + 1, 5);
        fungeSpace.set(0, CHUNK_EDGE_SIZE, 4);
        fungeSpace.set(1, CHUNK_EDGE_SIZE + 1, 3);
        fungeSpace.set(-CHUNK_EDGE_SIZE, -CHUNK_EDGE_SIZE, 2);
        fungeSpace.set(-CHUNK_EDGE_SIZE + 1, -CHUNK_EDGE_SIZE + 1, 1);

        Assertions.assertEquals(10, fungeSpace.get(0, 0));
        Assertions.assertEquals(9, fungeSpace.get(1, 1));
        Assertions.assertEquals(8, fungeSpace.get(CHUNK_EDGE_SIZE, 0));
        Assertions.assertEquals(7, fungeSpace.get(CHUNK_EDGE_SIZE + 1, 1));
        Assertions.assertEquals(6, fungeSpace.get(CHUNK_EDGE_SIZE, CHUNK_EDGE_SIZE));
        Assertions.assertEquals(5, fungeSpace.get(CHUNK_EDGE_SIZE + 1, CHUNK_EDGE_SIZE + 1));
        Assertions.assertEquals(4, fungeSpace.get(0, CHUNK_EDGE_SIZE));
        Assertions.assertEquals(3, fungeSpace.get(1, CHUNK_EDGE_SIZE + 1));
        Assertions.assertEquals(2, fungeSpace.get(-CHUNK_EDGE_SIZE, -CHUNK_EDGE_SIZE));
        Assertions.assertEquals(1, fungeSpace.get(-CHUNK_EDGE_SIZE + 1, -CHUNK_EDGE_SIZE + 1));
    }

    @Test
    public void testBounds() {
        val space = new FungeSpace(0);
        Assertions.assertEquals(new Rectangle(0, 0, 0, 0), space.getBounds());
        space.set(0, 0, 1);
        Assertions.assertEquals(new Rectangle(0, 0, 0, 0), space.getBounds());
        space.set(0, 1, 1);
        space.set(1, 0, 2);
        Assertions.assertEquals(new Rectangle(0, 0, 1, 1), space.getBounds());
        space.set(0, 0, 0);
        space.set(0, 1, 0);
        space.set(1, 0, 0);
        Assertions.assertEquals(new Rectangle(0, 0, 0, 0), space.getBounds());
        space.set(-10, -5, 10);
        space.set(127, 5, 8);
        space.set(5, 127, 5);
        Assertions.assertEquals(new Rectangle(-10, -5, 127, 127), space.getBounds());
        space.set(-10, -5, 0);
        Assertions.assertEquals(new Rectangle(5, 5, 127, 127), space.getBounds());
        space.set(127, 5, 0);
        Assertions.assertEquals(new Rectangle(5, 127, 5, 127), space.getBounds());
        space.set(5, 127, 0);
        Assertions.assertEquals(new Rectangle(0, 0, 0, 0), space.getBounds());
    }
}
