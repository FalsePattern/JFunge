package com.falsepattern.jfunge.storage;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.*;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.ArrayList;

import static com.falsepattern.jfunge.storage.Chunk.*;

@RequiredArgsConstructor
public class FungeSpace {
    private final TIntObjectMap<TIntObjectMap<TIntObjectMap<Chunk>>> storage = new TIntObjectHashMap<>();

    private final Vector3i cachePos = new Vector3i();
    private Chunk cacheChunk;

    private boolean boundsRecheck = false;
    private final Bounds bounds = new Bounds();

    private final int defaultValue;

    public int get(int x, int y, int z) {
        val cX = toChunk(x);
        val cY = toChunk(y);
        val cZ = toChunk(z);
        if (cacheChunk != null && cachePos.equals(cX, cY, cZ)) {
            return cacheChunk.get(inChunk(x), inChunk(y), inChunk(z));
        }
        val plane = storage.get(cZ);
        if (plane != null) {
            val row = plane.get(cY);
            if (row != null) {
                val chunk = row.get(cX);
                if (chunk != null) {
                    cacheChunk = chunk;
                    cachePos.set(cX, cY, cZ);
                    return chunk.get(inChunk(x), inChunk(y), inChunk(z));
                }
            }
        }
        return defaultValue;
    }

    public int get(Vector3ic v) {
        return get(v.x(), v.y(), v.z());
    }

    public void set(int x, int y, int z, int value) {
        val cX = toChunk(x);
        val cY = toChunk(y);
        val cZ = toChunk(z);
        if (cacheChunk != null && cachePos.equals(cX, cY, cZ)) {
            cacheChunk.set(inChunk(x), inChunk(y), inChunk(z), value);
        }
        var plane = storage.get(cZ);
        if (plane == null) {
            plane = new TIntObjectHashMap<>();
            storage.put(cZ, plane);
        }
        var row = plane.get(cY);
        if (row == null) {
            row = new TIntObjectHashMap<>();
            plane.put(cY, row);
        }
        var chunk = row.get(cX);
        if (chunk == null) {
            if (value == defaultValue) return;
            chunk = Chunk.allocate(defaultValue);
            row.put(cX, chunk);
        }
        boundsRecheck |= chunk.set(inChunk(x), inChunk(y), inChunk(z), value);
        cacheChunk = chunk;
        cachePos.set(cX, cY, cZ);
    }

    public void set(Vector3ic v, int value) {
        set(v.x(), v.y(), v.z(), value);
    }

    public void gc() {
        cacheChunk = null;
        val planes = storage.keys();
        for (val iPlane: planes) {
            val plane = storage.get(iPlane);
            val rows = plane.keys();
            for (val iRow: rows) {
                val row = plane.get(iRow);
                val chunks = row.keys();
                for (val iChunk: chunks) {
                    val chunk = row.get(iChunk);
                    if (chunk.isEmpty()) {
                        row.remove(iChunk);
                    }
                }
                if (row.isEmpty()) {
                    plane.remove(iRow);
                }
            }
            if (plane.isEmpty()) {
                storage.remove(iPlane);
            }
        }
    }

    public void loadFileAt(int x, int y, int z, byte[] data, boolean trefunge) {
        int X = x;
        int Y = y;
        int Z = z;
        for (int i = 0; i < data.length; i++) {
            int c = Byte.toUnsignedInt(data[i]);
            switch (c) {
                case '\r':
                    if (i < data.length - 1 && data[i + 1] == '\n') continue;
                case '\n':
                    X = x;
                    Y++;
                    continue;
                case '\f':
                    if (trefunge) {
                        X = x;
                        Y = y;
                        Z++;
                    }
                    continue;
            }
            set(X, Y, Z, c);
            X++;
        }
    }

    public void wipe() {
        storage.clear();
        boundsRecheck = false;
        bounds.zero();
    }

    public BoundsC bounds() {
        recheckBounds();
        return bounds;
    }

    public void recheckBounds() {
        if (!boundsRecheck) return;
        gc();
        boundsRecheck = false;
        if (storage.size() == 0) {
            bounds.zero();
            return;
        }
        int zMinFinal;
        int zMaxFinal;
        int yMinFinal = Integer.MAX_VALUE;
        int yMaxFinal = Integer.MIN_VALUE;
        int xMinFinal = Integer.MAX_VALUE;
        int xMaxFinal = Integer.MIN_VALUE;
        int[] mm = new int[2];
        int[] cZArr = storage.keys();
        minMax(cZArr, mm);
        int cZMin = mm[0];
        int cZMax = mm[1];
        zMinFinal = cZMin * CHUNK_EDGE_SIZE + storage.get(cZMin).valueCollection().stream().flatMap((row) -> row.valueCollection().stream()).mapToInt(Chunk::minZ).min().getAsInt();
        zMaxFinal = cZMax * CHUNK_EDGE_SIZE + storage.get(cZMax).valueCollection().stream().flatMap((row) -> row.valueCollection().stream()).mapToInt(Chunk::maxZ).max().getAsInt();
        for (var cZ: cZArr) {
            val plane = storage.get(cZ);
            int yMin;
            int yMax;
            int[] cYArr = plane.keys();
            minMax(cYArr, mm);
            int cYMin = mm[0];
            int cYMax = mm[1];
            yMin = cYMin * CHUNK_EDGE_SIZE + plane.get(cYMin).valueCollection().stream().mapToInt(Chunk::minY).min().getAsInt();
            yMax = cYMax * CHUNK_EDGE_SIZE + plane.get(cYMax).valueCollection().stream().mapToInt(Chunk::maxY).max().getAsInt();
            yMinFinal = Math.min(yMinFinal, yMin);
            yMaxFinal = Math.max(yMaxFinal, yMax);
            for (val cY: cYArr) {
                val row = plane.get(cY);
                int xMin;
                int xMax;
                int[] cXArr = row.keys();
                minMax(cXArr, mm);
                int cXMin = mm[0];
                int cXMax = mm[1];
                xMin = cXMin * CHUNK_EDGE_SIZE + row.get(cXMin).minX();
                xMax = cXMax * CHUNK_EDGE_SIZE + row.get(cXMax).maxX();
                xMinFinal = Math.min(xMinFinal, xMin);
                xMaxFinal = Math.max(xMaxFinal, xMax);
            }
        }
        bounds.set(xMinFinal, yMinFinal, zMinFinal, xMaxFinal, yMaxFinal, zMaxFinal);
    }

    private static void minMax(int[] arr, int[] buf) {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < arr.length; i++) {
            max = Math.max(arr[i], max);
            min = Math.min(arr[i], min);
        }
        buf[0] = min;
        buf[1] = max;
    }
}
