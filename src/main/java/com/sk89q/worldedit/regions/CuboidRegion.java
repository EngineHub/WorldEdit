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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.data.ChunkStore;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 *
 * @author sk89q
 */
public class CuboidRegion extends AbstractRegion implements FlatRegion {
    /**
     * Store the first point.
     */
    private Vector pos1;
    /**
     * Store the second point.
     */
    private Vector pos2;

    /**
     * Construct a new instance of this cuboid region.
     *
     * @param pos1
     * @param pos2
     */
    public CuboidRegion(Vector pos1, Vector pos2) {
        this(null, pos1, pos2);
    }

    /**
     * Construct a new instance of this cuboid region.
     *
     * @param world
     * @param pos1
     * @param pos2
     */
    public CuboidRegion(LocalWorld world, Vector pos1, Vector pos2) {
        super(world);
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    /**
     * Get the lower point of the cuboid.
     *
     * @return min point
     */
    public Vector getMinimumPoint() {
        return new Vector(Math.min(pos1.getX(), pos2.getX()),
                         Math.min(pos1.getY(), pos2.getY()),
                         Math.min(pos1.getZ(), pos2.getZ()));
    }

    /**
     * Get the upper point of the cuboid.
     *
     * @return max point
     */
    public Vector getMaximumPoint() {
        return new Vector(Math.max(pos1.getX(), pos2.getX()),
                         Math.max(pos1.getY(), pos2.getY()),
                         Math.max(pos1.getZ(), pos2.getZ()));
    }

    public int getMinimumY() {
        return Math.min(pos1.getBlockY(), pos2.getBlockY());
    }

    public int getMaximumY() {
        return Math.max(pos1.getBlockY(), pos2.getBlockY());
    }

    /**
     * Expands the cuboid in a direction.
     *
     * @param change
     */
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

    /**
     * Contracts the cuboid in a direction.
     *
     * @param change
     */
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

    /**
     * Get a list of chunks that this region is within.
     *
     * @return
     */
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

    /**
     * Returns true based on whether the region contains the point,
     *
     * @param pt
     */
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

    /**
     * Get the iterator.
     *
     * @return iterator of points inside the region
     */
    @Override
    public Iterator<BlockVector> iterator() {
        return new Iterator<BlockVector>() {
            private Vector min = getMinimumPoint();
            private Vector max = getMaximumPoint();
            private int nextX = min.getBlockX();
            private int nextY = min.getBlockY();
            private int nextZ = min.getBlockZ();

            public boolean hasNext() {
                return (nextX != Integer.MIN_VALUE);
            }

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

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Iterable<Vector2D> asFlatRegion() {
        return new Iterable<Vector2D>() {
            @Override
            public Iterator<Vector2D> iterator() {
                return new Iterator<Vector2D>() {
                    private Vector min = getMinimumPoint();
                    private Vector max = getMaximumPoint();
                    private int nextX = min.getBlockX();
                    private int nextZ = min.getBlockZ();

                    public boolean hasNext() {
                        return (nextX != Integer.MIN_VALUE);
                    }

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
        };
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

    public CuboidRegion clone() {
        return (CuboidRegion) super.clone();
    }

    /**
     * Make a cuboid region out of the given region using the minimum and maximum
     * bounds of the provided region.
     *
     * @param region the region
     * @return a new cuboid region
     */
    public static CuboidRegion makeCuboid(Region region) {
        return new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint());
    }

}
