package com.falsepattern.jfunge.storage;


import lombok.Cleanup;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junitpioneer.jupiter.DisableIfTestFails;

import java.util.Random;

@DisableIfTestFails
@TestMethodOrder(AsWrittenMethodOrderer.class)
public class TestChunk {
    @Test
    public void testSetGet() {
        val rngSeed = System.nanoTime();
        @Cleanup
        val chunk = Chunk.allocate(0);

        var rng = new Random(rngSeed);
        for (int z = 0; z < Chunk.CHUNK_EDGE_SIZE_Z; z++)
            for (int y = 0; y < Chunk.CHUNK_EDGE_SIZE_Y; y++)
                for (int x = 0; x < Chunk.CHUNK_EDGE_SIZE_X; x++)
                    chunk.set(x, y, z, rng.nextInt());

        rng = new Random(rngSeed);
        for (int z = 0; z < Chunk.CHUNK_EDGE_SIZE_Z; z++)
            for (int y = 0; y < Chunk.CHUNK_EDGE_SIZE_Y; y++)
                for (int x = 0; x < Chunk.CHUNK_EDGE_SIZE_X; x++)
                    Assertions.assertEquals(rng.nextInt(), chunk.get(x, y, z));
    }

    @Test
    public void testDefaultValue() {
        for (int i = -10; i < 10; i++) {
            @Cleanup
            val chunk = Chunk.allocate(i);
            for (int z = 0; z < Chunk.CHUNK_EDGE_SIZE_Z; z++)
                for (int y = 0; y < Chunk.CHUNK_EDGE_SIZE_Y; y++)
                    for (int x = 0; x < Chunk.CHUNK_EDGE_SIZE_X; x++)
                        Assertions.assertEquals(i, chunk.get(x, y, z));
        }
    }

    @Test
    public void testEmpty() {
        @Cleanup
        val chunk = Chunk.allocate(0);
        Assertions.assertTrue(chunk.isEmpty());
        chunk.set(1, 1, 0, 3);
        Assertions.assertFalse(chunk.isEmpty());
        chunk.set(3, 3, 0, 10);
        Assertions.assertFalse(chunk.isEmpty());
        chunk.set(1, 1, 0, 0);
        Assertions.assertFalse(chunk.isEmpty());
        chunk.set(3, 3, 0, 0);
        Assertions.assertTrue(chunk.isEmpty());
    }
}
