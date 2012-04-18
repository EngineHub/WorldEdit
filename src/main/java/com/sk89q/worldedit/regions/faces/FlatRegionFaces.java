package com.sk89q.worldedit.regions.faces;

import java.util.Iterator;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.FlatRegion;

public class FlatRegionFaces implements Iterable<Vector> {

    private static class FlatRegionFacesIterator implements Iterator<Vector> {

        private final Iterator<Vector> wallsIterator;

        private final Iterator<Vector2D> flatIterator;
        private final int minY;
        private final int maxY;
        private final int thickness;

        private boolean top;
        private Vector2D next2D;
        private int nextY;

        public FlatRegionFacesIterator(FlatRegion region,
                Iterable<Vector> walls, int thickness) {
            this.wallsIterator = walls.iterator();

            this.minY = region.getMinimumPoint().getBlockY();
            this.maxY = region.getMaximumPoint().getBlockY();
            this.thickness = thickness;
            this.flatIterator = region.asFlatRegion().iterator();

            this.top = true;
            this.next2D = flatIterator.next();
            this.nextY = maxY - thickness + 1;
        }

        @Override
        public boolean hasNext() {
            return wallsIterator.hasNext() || flatIterator.hasNext() || top;
        }

        @Override
        public Vector next() {
            if (wallsIterator.hasNext()) {
                return wallsIterator.next();
            } else {
                Vector current = next2D.toVector(nextY);;
                if (top) {
                    if (nextY >= maxY) {
                        nextY = minY + thickness - 1;
                        top = false;
                    } else {
                        nextY++;
                    }
                } else {
                    if (nextY <= minY) {
                        next2D = flatIterator.next();
                        nextY = maxY - thickness + 1;
                        top = true;
                    } else {
                        nextY--;
                    }
                }
                return current;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final FlatRegion region;
    private final Iterable<Vector> walls;
    private final int thickness;

    public FlatRegionFaces(FlatRegion region, int thickness) {
        this.region = region;
        this.walls = region.walls(thickness);
        this.thickness = thickness;
    }

    @Override
    public Iterator<Vector> iterator() {
        return new FlatRegionFacesIterator(region, walls, thickness);
    }
}
