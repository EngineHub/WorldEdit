package com.sk89q.worldedit.regions;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector2D;

public class FlatRegion3DColumnIterator implements Iterator<BlockVector> {

    private Iterator<Vector2D> flatIterator;
    private int y;

    private Vector2D next2D;

    public FlatRegion3DColumnIterator(FlatRegion region, Iterator<Vector2D> flatIterator) {
        this.flatIterator = flatIterator;
        this.y = region.getMaximumY();

        if (flatIterator.hasNext()) {
            this.next2D = flatIterator.next();
        } else {
            this.next2D = null;
        }
    }

    public FlatRegion3DColumnIterator(FlatRegion region) {
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

        BlockVector current = new BlockVector(next2D.getBlockX(), y, next2D.getBlockZ());
        if (flatIterator.hasNext()) {
            next2D = flatIterator.next();
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
