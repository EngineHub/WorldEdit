// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.regions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.data.ChunkStore;

/**
 * An axis-aligned bounding box.
 */
public class CuboidRegion extends AbstractFlatRegion implements FlatRegion {

    private Vector pos1;
    private Vector pos2;
    
    /**
     * Create a new AABB.
     *
     * @param pos1 the first point
     * @param pos2 the second point
     */
    public CuboidRegion(Vector pos1, Vector pos2) {
        this(null, pos1, pos2);
    }

    /**
     * Construct a AABB.
     *
     * @param world the world
     * @param pos1 the first point
     * @param pos2 the second point
     */
    public CuboidRegion(LocalWorld world, Vector pos1, Vector pos2) {
        super(world);
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    /**
     * Construct an instance from an existing instance.
     *
     * @param region the existing region
     */
    public CuboidRegion(CuboidRegion region) {
        this(region.getWorld(), region.getPos1(), region.getPos2());
    }

    @Override
    public Vector getMinimumPoint() {
        return new Vector(Math.min(pos1.getX(), pos2.getX()),
                         Math.min(pos1.getY(), pos2.getY()),
                         Math.min(pos1.getZ(), pos2.getZ()));
    }

    @Override
    public Vector getMaximumPoint() {
        return new Vector(Math.max(pos1.getX(), pos2.getX()),
                         Math.max(pos1.getY(), pos2.getY()),
                         Math.max(pos1.getZ(), pos2.getZ()));
    }

    @Override
    public int getMinimumY() {
        return Math.min(pos1.getBlockY(), pos2.getBlockY());
    }

    @Override
    public int getMaximumY() {
        return Math.max(pos1.getBlockY(), pos2.getBlockY());
    }

    @Override
    public int getArea() {
        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        return (int)((max.getX() - min.getX() + 1) *
                     (max.getY() - min.getY() + 1) *
                     (max.getZ() - min.getZ() + 1));
    }

    @Override
    public int getWidth() {
        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        return (int) (max.getX() - min.getX() + 1);
    }

    @Override
    public int getHeight() {
        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        return (int) (max.getY() - min.getY() + 1);
    }

    @Override
    public int getLength() {
        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        return (int) (max.getZ() - min.getZ() + 1);
    }

    @Override
    public void expand(Vector... changes) {
        for (Vector change : changes) {
            if (change.getX() > 0) {
                if (Math.max(pos1.getX(), pos2.getX()) == pos1.getX()) {
                    pos1 = pos1.add(new Vector(change.getX(), 0, 0));
                } else {
                    pos2 = pos2.add(new Vector(change.getX(), 0, 0));
                }
            } else {
                if (Math.min(pos1.getX(), pos2.getX()) == pos1.getX()) {
                    pos1 = pos1.add(new Vector(change.getX(), 0, 0));
                } else {
                    pos2 = pos2.add(new Vector(change.getX(), 0, 0));
                }
            }

            if (change.getY() > 0) {
                if (Math.max(pos1.getY(), pos2.getY()) == pos1.getY()) {
                    pos1 = pos1.add(new Vector(0, change.getY(), 0));
                } else {
                    pos2 = pos2.add(new Vector(0, change.getY(), 0));
                }
            } else {
                if (Math.min(pos1.getY(), pos2.getY()) == pos1.getY()) {
                    pos1 = pos1.add(new Vector(0, change.getY(), 0));
                } else {
                    pos2 = pos2.add(new Vector(0, change.getY(), 0));
                }
            }

            if (change.getZ() > 0) {
                if (Math.max(pos1.getZ(), pos2.getZ()) == pos1.getZ()) {
                    pos1 = pos1.add(new Vector(0, 0, change.getZ()));
                } else {
                    pos2 = pos2.add(new Vector(0, 0, change.getZ()));
                }
            } else {
                if (Math.min(pos1.getZ(), pos2.getZ()) == pos1.getZ()) {
                    pos1 = pos1.add(new Vector(0, 0, change.getZ()));
                } else {
                    pos2 = pos2.add(new Vector(0, 0, change.getZ()));
                }
            }
        }

        recalculate();
    }

    @Override
    public void contract(Vector... changes) {
        for (Vector change : changes) {
            if (change.getX() < 0) {
                if (Math.max(pos1.getX(), pos2.getX()) == pos1.getX()) {
                    pos1 = pos1.add(new Vector(change.getX(), 0, 0));
                } else {
                    pos2 = pos2.add(new Vector(change.getX(), 0, 0));
                }
            } else {
                if (Math.min(pos1.getX(), pos2.getX()) == pos1.getX()) {
                    pos1 = pos1.add(new Vector(change.getX(), 0, 0));
                } else {
                    pos2 = pos2.add(new Vector(change.getX(), 0, 0));
                }
            }

            if (change.getY() < 0) {
                if (Math.max(pos1.getY(), pos2.getY()) == pos1.getY()) {
                    pos1 = pos1.add(new Vector(0, change.getY(), 0));
                } else {
                    pos2 = pos2.add(new Vector(0, change.getY(), 0));
                }
            } else {
                if (Math.min(pos1.getY(), pos2.getY()) == pos1.getY()) {
                    pos1 = pos1.add(new Vector(0, change.getY(), 0));
                } else {
                    pos2 = pos2.add(new Vector(0, change.getY(), 0));
                }
            }

            if (change.getZ() < 0) {
                if (Math.max(pos1.getZ(), pos2.getZ()) == pos1.getZ()) {
                    pos1 = pos1.add(new Vector(0, 0, change.getZ()));
                } else {
                    pos2 = pos2.add(new Vector(0, 0, change.getZ()));
                }
            } else {
                if (Math.min(pos1.getZ(), pos2.getZ()) == pos1.getZ()) {
                    pos1 = pos1.add(new Vector(0, 0, change.getZ()));
                } else {
                    pos2 = pos2.add(new Vector(0, 0, change.getZ()));
                }
            }
        }

        recalculate();
    }

    private void recalculate() {
        pos1 = pos1.clampY(0, world == null ? 255 : world.getMaxY());
        pos2 = pos2.clampY(0, world == null ? 255 : world.getMaxY());
    }

    @Override
    public void shift(Vector change) throws RegionOperationException {
        pos1 = pos1.add(change);
        pos2 = pos2.add(change);

        recalculate();
    }

    /**
     * Get position 1.
     *
     * @return position 1
     */
    public Vector getPos1() {
        return pos1;
    }

    /**
     * Set position 1.
     *
     * @param pos1
     */
    public void setPos1(Vector pos1) {
        this.pos1 = pos1;
    }

    /**
     * Get position 2.
     *
     * @return position 2
     */
    public Vector getPos2() {
        return pos2;
    }

    /**
     * Set position 2.
     *
     * @param pos2
     */
    public void setPos2(Vector pos2) {
        this.pos2 = pos2;
    }

    @Override
    public Set<Vector2D> getChunks() {
        Set<Vector2D> chunks = new HashSet<Vector2D>();

        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                chunks.add(new BlockVector2D(x >> ChunkStore.CHUNK_SHIFTS,
                        z >> ChunkStore.CHUNK_SHIFTS));
            }
        }

        return chunks;
    }

    @Override
    public Set<Vector> getChunkCubes() {
        Set<Vector> chunks = new HashSet<Vector>();

        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                    chunks.add(new BlockVector(x >> ChunkStore.CHUNK_SHIFTS,
                            y >> ChunkStore.CHUNK_SHIFTS, z >> ChunkStore.CHUNK_SHIFTS));
                }
            }
        }

        return chunks;
    }

    @Override
    public boolean contains(Vector pt) {
        double x = pt.getX();
        double y = pt.getY();
        double z = pt.getZ();

        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        return x >= min.getBlockX() && x <= max.getBlockX()
                && y >= min.getBlockY() && y <= max.getBlockY()
                && z >= min.getBlockZ() && z <= max.getBlockZ();
    }

    @Override
    public Iterator<BlockVector> columnIterator() {
        return new Iterator<BlockVector>() {
            private Vector min = getMinimumPoint();
            private Vector max = getMaximumPoint();
            private int maxY = max.getBlockY();
            private int nextX = min.getBlockX();
            private int nextZ = min.getBlockZ();

            @Override
            public boolean hasNext() {
                return (nextX != Integer.MIN_VALUE);
            }

            @Override
            public BlockVector next() {
                if (!hasNext()) throw new java.util.NoSuchElementException();
                BlockVector answer = new BlockVector(nextX, maxY, nextZ);
                if (++nextX > max.getBlockX()) {
                    nextX = min.getBlockX();
                    if (++nextZ > max.getBlockZ()) {
                        nextX = Integer.MIN_VALUE;
                    }
                }
                return answer;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Iterator<BlockVector> iterator() {
        return new Iterator<BlockVector>() {
            private Vector min = getMinimumPoint();
            private Vector max = getMaximumPoint();
            private int nextX = min.getBlockX();
            private int nextY = min.getBlockY();
            private int nextZ = min.getBlockZ();

            @Override
            public boolean hasNext() {
                return (nextX != Integer.MIN_VALUE);
            }

            @Override
            public BlockVector next() {
                if (!hasNext()) throw new java.util.NoSuchElementException();
                BlockVector answer = new BlockVector(nextX, nextY, nextZ);
                if (++nextX > max.getBlockX()) {
                    nextX = min.getBlockX();
                    if (++nextY > max.getBlockY()) {
                        nextY = min.getBlockY();
                        if (++nextZ > max.getBlockZ()) {
                            nextX = Integer.MIN_VALUE;
                        }
                    }
                }
                return answer;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Iterator<Vector2D> flatIterator() {
        return new Iterator<Vector2D>() {
            private Vector min = getMinimumPoint();
            private Vector max = getMaximumPoint();
            private int nextX = min.getBlockX();
            private int nextZ = min.getBlockZ();

            @Override
            public boolean hasNext() {
                return (nextX != Integer.MIN_VALUE);
            }

            @Override
            public Vector2D next() {
                if (!hasNext()) throw new java.util.NoSuchElementException();
                Vector2D answer = new Vector2D(nextX, nextZ);
                if (++nextX > max.getBlockX()) {
                    nextX = min.getBlockX();
                    if (++nextZ > max.getBlockZ()) {
                        nextX = Integer.MIN_VALUE;
                    }
                }
                return answer;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public FlatRegion toFlatRegion() {
        return this;
    }

    /**
     * Returns string representation in the format
     * "(minX, minY, minZ) - (maxX, maxY, maxZ)".
     *
     * @return string
     */
    @Override
    public String toString() {
        return getMinimumPoint() + " - " + getMaximumPoint();
    }

    @Override
    public CuboidRegion clone() {
        return (CuboidRegion) super.clone();
    }
}
