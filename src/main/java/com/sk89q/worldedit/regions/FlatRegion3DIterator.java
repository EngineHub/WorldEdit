package com.sk89q.worldedit.regions;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector2D;

public class FlatRegion3DIterator implements Iterator<BlockVector> {

    private Iterator<Vector2D> flatIterator;
    private int minY;
    private int maxY;

    private Vector2D next2D;
    private int nextY;

    public FlatRegion3DIterator(FlatRegion region, Iterator<Vector2D> flatIterator) {
        this.flatIterator = flatIterator;
        this.minY = region.getMinimumY();
        this.maxY = region.getMaximumY();

        if (flatIterator.hasNext()) {
            this.next2D = flatIterator.next();
        } else {
            this.next2D = null;
        }
        this.nextY = minY;
    }

    public FlatRegion3DIterator(FlatRegion region) {
        this(region, region.asFlatRegion().iterator());
    }

    @Override
    public boolean hasNext() {
        return next2D != null;
    }

    @Override
    public BlockVector next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        BlockVector current = new BlockVector(next2D.getBlockX(), nextY, next2D.getBlockZ());
        if (nextY < maxY) {
            nextY++;
        } else if (flatIterator.hasNext()) {
            next2D = flatIterator.next();
            nextY = minY;
        } else {
            next2D = null;
        }

        return current;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
