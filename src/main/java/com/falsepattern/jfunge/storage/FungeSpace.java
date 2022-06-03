package com.falsepattern.jfunge.storage;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.*;

import java.awt.*;

import static com.falsepattern.jfunge.storage.Chunk.*;

@RequiredArgsConstructor
public class FungeSpace {
    private final TIntObjectMap<TIntObjectMap<Chunk>> storage = new TIntObjectHashMap<>();

    private int cacheCX;
    private int cacheCY;
    private Chunk cacheChunk;

    private boolean boundsRecheck = false;
    private Rectangle bounds = new Rectangle(0, 0, 0, 0);

    @NonNull
    private int defaultValue;

    public int get(int x, int y) {
        val cX = toChunk(x);
        val cY = toChunk(y);
        if (cacheChunk != null && cacheCX == cX && cacheCY == cY) {
            return cacheChunk.get(inChunk(x), inChunk(y));
        }
        val column = storage.get(cY);
        if (column != null) {
            val chunk = column.get(cX);
            if (chunk != null) {
                cacheChunk = chunk;
                cacheCX = cX;
                cacheCY = cY;
                return chunk.get(inChunk(x), inChunk(y));
            }
        }
        return defaultValue;
    }

    public void set(int x, int y, int value) {
        val cX = toChunk(x);
        val cY = toChunk(y);
        if (cacheChunk != null && cacheCX == cX && cacheCY == cY) {
            cacheChunk.set(inChunk(x), inChunk(y), value);
        }
        var row = storage.get(cY);
        if (row == null) {
            row = new TIntObjectHashMap<>();
            storage.put(cY, row);
        }
        var chunk = row.get(cX);
        if (chunk == null) {
            if (value == defaultValue) return;
            chunk = Chunk.allocate(defaultValue);
            row.put(cX, chunk);
        }
        boundsRecheck |= chunk.set(inChunk(x), inChunk(y), value);
        cacheChunk = chunk;
        cacheCX = cX;
        cacheCY = cY;
    }

    public void gc() {
        cacheChunk = null;
        storage.retainEntries((y, row) -> {
            row.retainEntries((x, cell) -> !cell.isEmpty());
            return row.size() > 0;
        });
    }

    private static int max(int[] arr) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < arr.length; i++) {
            max = Math.max(arr[i], max);
        }
        return max;
    }
    private static int min(int[] arr) {
        int max = Integer.MAX_VALUE;
        for (int i = 0; i < arr.length; i++) {
            max = Math.min(arr[i], max);
        }
        return max;
    }

    public Rectangle getBounds() {
        recheckBounds();
        return bounds;
    }

    public void recheckBounds() {
        if (!boundsRecheck) return;
        gc();
        boundsRecheck = false;
        if (storage.size() == 0) {
            bounds = new Rectangle(0, 0, 0, 0);
            return;
        }
        int top = Integer.MAX_VALUE;
        int bottom = Integer.MIN_VALUE;
        int left = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int[] cYArr = storage.keys();
        int cYTop = min(cYArr);
        int cYBottom = max(cYArr);
        {
            var topRow = storage.get(cYTop);
            for (int i : topRow.keys()) {
                val chunk = topRow.get(i);
                top = Math.min(top, fromChunk(cYTop) + chunk.top());
            }
        }
        {
            var bottomRow = storage.get(cYBottom);
            for (int i : bottomRow.keys()) {
                val chunk = bottomRow.get(i);
                bottom = Math.max(bottom, fromChunk(cYBottom) + chunk.bottom());
            }
        }
        int cXLeft = Integer.MAX_VALUE;
        int cXRight = Integer.MIN_VALUE;
        for (var cY: cYArr) {
            val row = storage.get(cY);
            int[] cXArr = row.keys();
            int cXLeftCurrent = min(cXArr);
            int cXRightCurrent = max(cXArr);
            if (cXLeftCurrent <= cXLeft) {
                cXLeft = cXLeftCurrent;
                val chunk = row.get(cXLeft);
                left = Math.min(left, fromChunk(cXLeft) + chunk.left());
            }
            if (cXRightCurrent >= cXRight) {
                cXRight = cXRightCurrent;
                val chunk = row.get(cXRight);
                right = Math.max(right, fromChunk(cXRight) + chunk.right());
            }
        }
        bounds = new Rectangle(left, top, right, bottom);
    }
}
