package com.sk89q.worldedit.regions.faces;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.EllipsoidRegion;

public class EllipsoidRegionFaces implements Iterable<Vector> {

    public static class EllipsoidRegionFacesIterator implements Iterator<Vector> {

        private final Vector center;
        private final Vector radius;
        private final Iterator<BlockVector> iterator;
        private Vector next;

        public EllipsoidRegionFacesIterator(EllipsoidRegion region, int thickness) {
            this.center = region.getCenter();
            this.radius = region.getRadius().subtract(
                    thickness - 0.5, thickness - 0.5, thickness - 0.5);

            this.iterator = region.iterator();
            next = iterator.next();
            forward();
        }

        private boolean contained() {
            return next.subtract(center).divide(radius).lengthSq() >= 1;
        }

        private void forward() {
            while (iterator.hasNext() && !contained()) {
                forwardOne();
            }
        }

        private void forwardOne() {
            next = iterator.next();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Vector next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            Vector current = next;
            if (iterator.hasNext()) {
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

    private final EllipsoidRegion region;
    private final int thickness;

    public EllipsoidRegionFaces(EllipsoidRegion region, int thickness) {
        this.region = region;
        this.thickness = thickness;
    }

    @Override
    public Iterator<Vector> iterator() {
        return new EllipsoidRegionFacesIterator(region, thickness);
    }
}
