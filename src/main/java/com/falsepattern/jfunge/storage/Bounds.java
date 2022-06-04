package com.falsepattern.jfunge.storage;

import lombok.Data;
import lombok.experimental.Accessors;
import org.joml.Vector3i;
import org.joml.Vector3ic;

@Accessors(fluent = true, chain = true)
@Data
public class Bounds implements BoundsC {
    private int xMin;
    private int yMin;
    private int zMin;
    private int xMax;
    private int yMax;
    private int zMax;

    public Bounds zero() {
        xMin = yMin = zMin = xMax = yMax = zMax = 0;
        return this;
    }

    public Bounds set(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.zMin = zMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.zMax = zMax;
        return this;
    }

    public boolean inBounds(int x, int y, int z) {
        return x >= xMin && x <= xMax && y >= yMin && y <= yMax && z >= zMin && z <= zMax;
    }

    public boolean inBounds(Vector3ic pos) {
        return inBounds(pos.x(), pos.y(), pos.z());
    }
}
