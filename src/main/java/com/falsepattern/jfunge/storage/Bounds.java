package com.falsepattern.jfunge.storage;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
@Data
@NoArgsConstructor
public class Bounds implements BoundsC<Bounds> {
    public int xMin;
    public int yMin;
    public int zMin;
    public int xMax;
    public int yMax;
    public int zMax;

    private Bounds(Bounds original) {
        xMin = original.xMin;
        yMin = original.yMin;
        zMin = original.zMin;
        xMax = original.xMax;
        yMax = original.yMax;
        zMax = original.zMax;
    }

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

    public Bounds set(Bounds other) {
        set(other.xMin, other.yMin, other.zMin, other.xMax, other.yMax, other.zMax);
        return this;
    }

    @Override
    public Bounds deepCopy() {
        return new Bounds(this);
    }
}
