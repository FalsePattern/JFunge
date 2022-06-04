package com.falsepattern.jfunge.storage;

import com.falsepattern.jfunge.Releasable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.var;
import org.joml.Vector3ic;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Chunk implements Releasable {
    public static final int CHUNK_EDGE_SIZE = 32;
    public static final int CHUNK_CAPACITY = CHUNK_EDGE_SIZE * CHUNK_EDGE_SIZE * CHUNK_EDGE_SIZE;

    private static final int BUFFER_CAPACITY = 64;
    private static final List<Chunk> buffer = new LinkedList<>();

    private int[] storage = new int[CHUNK_CAPACITY];
    private int defaultValue;
    private int populatedCells;

    public static Chunk allocate(int defaultValue) {
        Chunk instance;
        if (buffer.size() > 0) {
            instance = buffer.remove(0);
        } else {
            instance = new Chunk();
        }
        Arrays.fill(instance.storage, defaultValue);
        instance.defaultValue = defaultValue;
        instance.populatedCells = 0;
        return instance;
    }

    public static int toChunk(int pos) {
        return Math.floorDiv(pos, CHUNK_EDGE_SIZE);
    }

    public static int inChunk(int pos) {
        return Math.floorMod(pos, CHUNK_EDGE_SIZE);
    }

    public static int fromChunk(int cPos) {
        return cPos * CHUNK_EDGE_SIZE;
    }

    public boolean isEmpty() {
        return populatedCells == 0;
    }

    @Override
    public void release() {
        if (buffer.size() <= BUFFER_CAPACITY) {
            buffer.add(this);
        }
    }

    private static int toIndex(int x, int y, int z) {
        return (z * CHUNK_EDGE_SIZE + y) * CHUNK_EDGE_SIZE + x;
    }

    public int get(int x, int y, int z) {
        return storage[toIndex(x, y, z)];
    }

    public int get(Vector3ic v) {
        return get(v.x(), v.y(), v.z());
    }

    public boolean set(int x, int y, int z, int value) {
        int i = toIndex(x, y, z);
        int old = storage[i];
        storage[i] = value;
        int c;
        populatedCells += c = (old == defaultValue && value != defaultValue ? 1 : old != defaultValue && value == defaultValue ? -1 : 0);
        return c != 0;
    }

    public boolean set(Vector3ic v, int value) {
        return set(v.x(), v.y(), v.z(), value);
    }

    public int minX() {
        return min(Getter.gX);
    }

    public int maxX() {
        return max(Getter.gX);
    }

    public int minY() {
        return min(Getter.gY);
    }

    public int maxY() {
        return max(Getter.gY);
    }

    public int minZ() {
        return min(Getter.gZ);
    }

    public int maxZ() {
        return max(Getter.gZ);
    }

    private int min(Getter g) {
        for (var a = 0; a < CHUNK_EDGE_SIZE; a++)
            for (var b = 0; b < CHUNK_EDGE_SIZE; b++)
                for (var c = 0; c < CHUNK_EDGE_SIZE; c++)
                    if (storage[g.toIndex(a, b, c)] != defaultValue)
                        return a;
        return -1;
    }

    private int max(Getter g) {
        for (var a = CHUNK_EDGE_SIZE - 1; a >= 0; a--)
            for (var b = 0; b < CHUNK_EDGE_SIZE; b++)
                for (var c = 0; c < CHUNK_EDGE_SIZE; c++)
                    if (storage[g.toIndex(a, b, c)] != defaultValue)
                        return a;
        return -1;
    }

    private interface Getter {
        int toIndex(int a, int b, int c);

        Getter gX = new Getter() {
            public int toIndex(int a, int b, int c) {
                return Chunk.toIndex(a, b, c);
            }
        };
        Getter gY = new Getter() {
            public int toIndex(int a, int b, int c) {
                return Chunk.toIndex(b, a, c);
            }
        };
        Getter gZ = new Getter() {
            public int toIndex(int a, int b, int c) {
                return Chunk.toIndex(c, a, b);
            }
        };
    }
}
