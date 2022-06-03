package com.falsepattern.jfunge.storage;

import com.falsepattern.jfunge.Releasable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Chunk implements Releasable {
    public static final int CHUNK_EDGE_SIZE = 32;
    public static final int CHUNK_CAPACITY = CHUNK_EDGE_SIZE * CHUNK_EDGE_SIZE;

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

    private static int toIndex(int x, int y) {
        return y * CHUNK_EDGE_SIZE + x;
    }

    public boolean set(int x, int y, int value) {
        int i = toIndex(x, y);
        int old = storage[i];
        storage[i] = value;
        int c;
        populatedCells += c = (old == defaultValue && value != defaultValue ? 1 : old != defaultValue && value == defaultValue ? -1 : 0);
        return c != 0;
    }

    public int top() {
        for (int y = 0; y < CHUNK_EDGE_SIZE; y++) {
            for (int x = 0; x < CHUNK_EDGE_SIZE; x++) {
                if (get(x, y) != defaultValue) {
                    return y;
                }
            }
        }
        return -1;
    }

    public int bottom() {
        for (int y = CHUNK_EDGE_SIZE - 1; y >= 0; y--) {
            for (int x = 0; x < CHUNK_EDGE_SIZE; x++) {
                if (get(x, y) != defaultValue) {
                    return y;
                }
            }
        }
        return -1;
    }

    public int left() {
        for (int x = 0; x < CHUNK_EDGE_SIZE; x++) {
            for (int y = 0; y < CHUNK_EDGE_SIZE; y++) {
                if (get(x, y) != defaultValue) {
                    return x;
                }
            }
        }
        return -1;
    }

    public int right() {
        for (int x = CHUNK_EDGE_SIZE - 1; x >= 0; x--) {
            for (int y = 0; y < CHUNK_EDGE_SIZE; y++) {
                if (get(x, y) != defaultValue) {
                    return x;
                }
            }
        }
        return -1;
    }

    public int get(int x, int y) {
        return storage[toIndex(x, y)];
    }
}
