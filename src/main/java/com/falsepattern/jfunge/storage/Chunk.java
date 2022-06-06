package com.falsepattern.jfunge.storage;

import com.falsepattern.jfunge.Copiable;
import com.falsepattern.jfunge.Releasable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.var;
import org.joml.Vector3ic;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Chunk implements Releasable, Copiable<Chunk> {
    public static final int CHUNK_EDGE_SIZE_X = 16;
    public static final int CHUNK_EDGE_SIZE_Y = 16;
    public static final int CHUNK_EDGE_SIZE_Z = 1;
    public static final int CHUNK_CAPACITY = CHUNK_EDGE_SIZE_X * CHUNK_EDGE_SIZE_Y * CHUNK_EDGE_SIZE_Z;

    private static final int BUFFER_CAPACITY = 64;
    private static final List<Chunk> buffer = new LinkedList<>();

    private final int[] storage = new int[CHUNK_CAPACITY];
    private final Bounds bounds = new Bounds();
    private int defaultValue;
    private int populatedCells;

    private Chunk(Chunk original) {
        System.arraycopy(original.storage, 0, storage, 0, CHUNK_CAPACITY);
        bounds.set(original.bounds);
        defaultValue = original.defaultValue;
        populatedCells = original.populatedCells;
    }

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
        instance.bounds.set(-1, -1, -1, -1, -1, -1);
        return instance;
    }

    public static int toChunkX(int pos) {
        return Math.floorDiv(pos, CHUNK_EDGE_SIZE_X);
    }

    public static int toChunkY(int pos) {
        return Math.floorDiv(pos, CHUNK_EDGE_SIZE_Y);
    }

    public static int toChunkZ(int pos) {
        return Math.floorDiv(pos, CHUNK_EDGE_SIZE_Z);
    }

    public static int inChunkX(int pos) {
        return Math.floorMod(pos, CHUNK_EDGE_SIZE_X);
    }

    public static int inChunkY(int pos) {
        return Math.floorMod(pos, CHUNK_EDGE_SIZE_Y);
    }

    public static int inChunkZ(int pos) {
        return Math.floorMod(pos, CHUNK_EDGE_SIZE_Z);
    }

    public static int fromChunkX(int cPos) {
        return cPos * CHUNK_EDGE_SIZE_X;
    }

    public static int fromChunkY(int cPos) {
        return cPos * CHUNK_EDGE_SIZE_Y;
    }

    public static int fromChunkZ(int cPos) {
        return cPos * CHUNK_EDGE_SIZE_Z;
    }

    private static int toIndex(int x, int y, int z) {
        return (z * CHUNK_EDGE_SIZE_Y + y) * CHUNK_EDGE_SIZE_X + x;
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
        if (c != 0) {
            bounds.set(-1, -1, -1, -1, -1, -1);
            return true;
        } else {
            return false;
        }
    }

    public boolean set(Vector3ic v, int value) {
        return set(v.x(), v.y(), v.z(), value);
    }

    public int minX() {
        return bounds.xMin == -1 ? bounds.xMin = min(Getter.gX) : bounds.xMin;
    }

    public int maxX() {
        return bounds.xMax == -1 ? bounds.xMax = max(Getter.gX) : bounds.xMax;
    }

    public int minY() {
        return bounds.yMin == -1 ? bounds.yMin = min(Getter.gY) : bounds.yMin;
    }

    public int maxY() {
        return bounds.yMax == -1 ? bounds.yMax = max(Getter.gY) : bounds.yMax;
    }

    public int minZ() {
        return bounds.zMin == -1 ? bounds.zMin = min(Getter.gZ) : bounds.zMin;
    }

    public int maxZ() {
        return bounds.zMax == -1 ? bounds.zMax = max(Getter.gZ) : bounds.zMax;
    }

    private int min(Getter g) {
        for (var a = 0; a < g.sa(); a++)
            for (var b = 0; b < g.sb(); b++)
                for (var c = 0; c < g.sc(); c++)
                    if (storage[g.toIndex(a, b, c)] != defaultValue)
                        return a;
        throw new IllegalStateException();
    }

    private int max(Getter g) {
        for (var a = g.sa() - 1; a >= 0; a--)
            for (var b = 0; b < g.sb(); b++)
                for (var c = 0; c < g.sc(); c++)
                    if (storage[g.toIndex(a, b, c)] != defaultValue)
                        return a;
        throw new IllegalStateException();
    }

    @Override
    public Chunk deepCopy() {
        return new Chunk(this);
    }

    private interface Getter {
        Getter gX = new Getter() {
            public int toIndex(int a, int b, int c) {
                return Chunk.toIndex(a, b, c);
            }

            @Override
            public int sa() {
                return CHUNK_EDGE_SIZE_X;
            }

            @Override
            public int sb() {
                return CHUNK_EDGE_SIZE_Y;
            }

            @Override
            public int sc() {
                return CHUNK_EDGE_SIZE_Z;
            }
        };
        Getter gY = new Getter() {
            public int toIndex(int a, int b, int c) {
                return Chunk.toIndex(b, a, c);
            }

            @Override
            public int sa() {
                return CHUNK_EDGE_SIZE_Y;
            }

            @Override
            public int sb() {
                return CHUNK_EDGE_SIZE_X;
            }

            @Override
            public int sc() {
                return CHUNK_EDGE_SIZE_Z;
            }
        };
        Getter gZ = new Getter() {
            public int toIndex(int a, int b, int c) {
                return Chunk.toIndex(b, c, a);
            }

            @Override
            public int sa() {
                return CHUNK_EDGE_SIZE_Z;
            }

            @Override
            public int sb() {
                return CHUNK_EDGE_SIZE_X;
            }

            @Override
            public int sc() {
                return CHUNK_EDGE_SIZE_Y;
            }
        };

        int toIndex(int a, int b, int c);

        int sa();

        int sb();

        int sc();
    }
}
