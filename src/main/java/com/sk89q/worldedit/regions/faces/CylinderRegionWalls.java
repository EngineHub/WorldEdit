package com.sk89q.worldedit.regions.faces;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.FlatRegion3DIterator;

public class CylinderRegionWalls implements Iterable<Vector> {

    public static class CylinderRegion2DWallsIterator implements Iterator<Vector2D> {

        private final Vector2D center;
        private final Vector2D radius;
        private final Iterator<Vector2D> flatIterator;
        private Vector2D next;

        public CylinderRegion2DWallsIterator(CylinderRegion region, int thickness) {
            this.center = region.getCenter().toVector2D();
            this.radius = region.getRadius().subtract(thickness - 0.5, thickness - 0.5);

            this.flatIterator = region.asFlatRegion().iterator();
            forwardOne();
            forward();
        }


        private boolean contained() {
            return next.subtract(center).divide(radius).lengthSq() >= 1;
        }

        private void forward() {
            while (flatIterator.hasNext() && !contained()) {
                forwardOne();
            }
        }

        private void forwardOne() {
            next = flatIterator.next();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Vector2D next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            Vector2D current = next;
            if (flatIterator.hasNext()) {
                forwardOne();
                forward();
            } else {
                next = null;
            }
            return current;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final CylinderRegion region;
    private final int thickness;

    public CylinderRegionWalls(CylinderRegion region, int thickness) {
        this.region = region;
        this.thickness = thickness;
    }

    @Override
    public Iterator<Vector> iterator() {
        return new Iterator<Vector>() {

            private Iterator<BlockVector> iterator =
                    new FlatRegion3DIterator(region, 
                            new CylinderRegion2DWallsIterator(region, thickness));

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Vector next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }
}
