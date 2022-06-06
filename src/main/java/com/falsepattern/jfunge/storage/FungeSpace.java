package com.falsepattern.jfunge.storage;

import com.falsepattern.jfunge.Copiable;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.*;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import static com.falsepattern.jfunge.storage.Chunk.*;

@RequiredArgsConstructor
public class FungeSpace implements Copiable<FungeSpace> {
    private final TIntObjectMap<TIntObjectMap<TIntObjectMap<Chunk>>> storage = new TIntObjectHashMap<>();

    private final Vector3i cachePos = new Vector3i();
    private Chunk cacheChunk;

    private boolean boundsRecheck = false;
    private final Bounds bounds = new Bounds();

    private final int defaultValue;

    private FungeSpace(FungeSpace original) {
        original.storage.forEachEntry((z, oPlane) -> {
            val nPlane = new TIntObjectHashMap<TIntObjectMap<Chunk>>();
            storage.put(z, nPlane);
            oPlane.forEachEntry((y, oRow) -> {
                val nRow = new TIntObjectHashMap<Chunk>();
                nPlane.put(y, nRow);
                oRow.forEachEntry((x, oChunk) -> {
                    val nChunk = oChunk.deepCopy();
                    nRow.put(x, nChunk);
                    return true;
                });
                return true;
            });
            return true;
        });
        boundsRecheck = original.boundsRecheck;
        bounds.set(original.bounds);
        defaultValue = original.defaultValue;
        cachePos.set(0);
        cacheChunk = null;
    }

    public int get(int x, int y, int z) {
        val cX = toChunkX(x);
        val cY = toChunkY(y);
        val cZ = toChunkZ(z);
        if (cacheChunk != null && cachePos.equals(cX, cY, cZ)) {
            return cacheChunk.get(inChunkX(x), inChunkY(y), inChunkZ(z));
        }
        val plane = storage.get(cZ);
        if (plane != null) {
            val row = plane.get(cY);
            if (row != null) {
                val chunk = row.get(cX);
                if (chunk != null) {
                    cacheChunk = chunk;
                    cachePos.set(cX, cY, cZ);
                    return chunk.get(inChunkX(x), inChunkY(y), inChunkZ(z));
                }
            }
        }
        return defaultValue;
    }

    public int get(Vector3ic v) {
        return get(v.x(), v.y(), v.z());
    }

    public void set(int x, int y, int z, int value) {
        val cX = toChunkX(x);
        val cY = toChunkY(y);
        val cZ = toChunkZ(z);
        if (cacheChunk != null && cachePos.equals(cX, cY, cZ)) {
            cacheChunk.set(inChunkX(x), inChunkY(y), inChunkZ(z), value);
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
        boundsRecheck |= chunk.set(inChunkX(x), inChunkY(y), inChunkZ(z), value);
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
                        chunk.release();
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
        int best = Integer.MAX_VALUE;
        var plane = storage.get(cZMin);
        for (val iRow : plane.keys()) {
            val row = plane.get(iRow);
            for (val iChunk : row.keys()) {
                val chunk = row.get(iChunk);
                int minZ = chunk.minZ();
                if (minZ < best) {
                    best = minZ;
                }
            }
        }
        zMinFinal = fromChunkZ(cZMin) + best;
        best = Integer.MIN_VALUE;
        plane = storage.get(cZMax);
        for (val iRow : plane.keys()) {
            val row = plane.get(iRow);
            for (val iChunk : row.keys()) {
                val chunk = row.get(iChunk);
                int maxZ = chunk.maxZ();
                if (maxZ > best) {
                    best = maxZ;
                }
            }
        }
        zMaxFinal = fromChunkZ(cZMax) + best;
        for (var cZ: cZArr) {
            plane = storage.get(cZ);
            int yMin;
            int yMax;
            int[] cYArr = plane.keys();
            minMax(cYArr, mm);
            int cYMin = mm[0];
            int cYMax = mm[1];
            best = Integer.MAX_VALUE;
            var row = plane.get(cYMin);
            for (val iChunk : row.keys()) {
                val chunk = row.get(iChunk);
                int minY = chunk.minY();
                if (minY < best) {
                    best = minY;
                }
            }
            yMin = fromChunkY(cYMin) + best;
            best = Integer.MIN_VALUE;
            row = plane.get(cYMax);
            for (val iChunk : row.keys()) {
                val chunk = row.get(iChunk);
                int maxY = chunk.maxY();
                if (maxY > best) {
                    best = maxY;
                }
            }
            yMax = fromChunkY(cYMax) + best;
            yMinFinal = Math.min(yMinFinal, yMin);
            yMaxFinal = Math.max(yMaxFinal, yMax);
            for (val cY: cYArr) {
                row = plane.get(cY);
                int xMin;
                int xMax;
                int[] cXArr = row.keys();
                minMax(cXArr, mm);
                int cXMin = mm[0];
                int cXMax = mm[1];
                xMin = fromChunkX(cXMin) + row.get(cXMin).minX();
                xMax = fromChunkX(cXMax) + row.get(cXMax).maxX();
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

    @Override
    public FungeSpace deepCopy() {
        return new FungeSpace(this);
    }
}
