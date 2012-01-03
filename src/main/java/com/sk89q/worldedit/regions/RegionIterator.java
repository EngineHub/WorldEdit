package com.sk89q.worldedit.regions;

import java.util.Iterator;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;

public class RegionIterator implements Iterator<BlockVector> {
    private final Region region;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final Vector min;
    private int nextX;
    private int nextY;
    private int nextZ;

    public RegionIterator(Region region) {
        this.region = region;

        Vector max = region.getMaximumPoint();
        this.maxX = max.getBlockX();
        this.maxY = max.getBlockY();
        this.maxZ = max.getBlockZ();

        this.min = region.getMinimumPoint();
        this.nextX = min.getBlockX();
        this.nextY = min.getBlockY();
        this.nextZ = min.getBlockZ();

        forward();
    }

    public boolean hasNext() {
        return nextX != Integer.MIN_VALUE;
    }

    private void forward() {
        while (hasNext() && !region.contains(new BlockVector(nextX, nextY, nextZ))) {
            forwardOne();
        }
    }

    public BlockVector next() {
        if (!hasNext()) throw new java.util.NoSuchElementException();

        BlockVector answer = new BlockVector(nextX, nextY, nextZ);

        forwardOne();
        forward();

        return answer;
    }

    private void forwardOne() {
        if (++nextX <= maxX) {
            return;
        }
        nextX = min.getBlockX();

        if (++nextY <= maxY) {
            return;
        }
        nextY = min.getBlockY();

        if (++nextZ <= maxZ) {
            return;
        }
        nextX = Integer.MIN_VALUE;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
