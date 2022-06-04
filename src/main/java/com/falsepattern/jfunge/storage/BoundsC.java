package com.falsepattern.jfunge.storage;

import org.joml.Vector3ic;

public interface BoundsC {
    int xMin();
    int yMin();
    int zMin();
    int xMax();
    int yMax();
    int zMax();


    default boolean inBounds(int x, int y, int z) {
        return x >= xMin() && x <= xMax() && y >= yMin() && y <= yMax() && z >= zMin() && z <= zMax();
    }

    default boolean inBounds(Vector3ic pos) {
        return inBounds(pos.x(), pos.y(), pos.z());
    }
}
