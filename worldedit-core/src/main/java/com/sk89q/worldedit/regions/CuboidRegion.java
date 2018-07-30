/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.regions;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.storage.ChunkStore;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An axis-aligned cuboid. It can be defined using two corners of the cuboid.
 */
public class CuboidRegion extends AbstractRegion implements FlatRegion {

    private Vector pos1;
    private Vector pos2;

    /**
     * Construct a new instance of this cuboid using two corners of the cuboid.
     *
     * @param pos1 the first position
     * @param pos2 the second position
     */
    public CuboidRegion(Vector pos1, Vector pos2) {
        this(null, pos1, pos2);
    }

    /**
     * Construct a new instance of this cuboid using two corners of the cuboid.
     *
     * @param world the world
     * @param pos1  the first position
     * @param pos2  the second position
     */
    public CuboidRegion(World world, Vector pos1, Vector pos2) {
        super(world);
        checkNotNull(pos1);
        checkNotNull(pos2);
        this.pos1 = pos1;
        this.pos2 = pos2;
        recalculate();
    }

    /**
     * Get the first cuboid-defining corner.
     *
     * @return a position
     */
    public Vector getPos1() {
        return pos1;
    }

    /**
     * Set the first cuboid-defining corner.
     *
     * @param pos1 a position
     */
    public void setPos1(Vector pos1) {
        this.pos1 = pos1;
    }

    /**
     * Get the second cuboid-defining corner.
     *
     * @return a position
     */
    public Vector getPos2() {
        return pos2;
    }

    /**
     * Set the second cuboid-defining corner.
     *
     * @param pos2 a position
     */
    public void setPos2(Vector pos2) {
        this.pos2 = pos2;
    }

    /**
     * Clamps the cuboid according to boundaries of the world.
     */
    private void recalculate() {
        pos1 = pos1.clampY(0, world == null ? 255 : world.getMaxY());
        pos2 = pos2.clampY(0, world == null ? 255 : world.getMaxY());
    }

    /**
     * Get a region that contains the faces of this cuboid.
     *
     * @return a new complex region
     */
    public Region getFaces() {
        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        return new RegionIntersection(
                // Project to Z-Y plane
                new CuboidRegion(pos1.setX(min.getX()), pos2.setX(min.getX())),
                new CuboidRegion(pos1.setX(max.getX()), pos2.setX(max.getX())),

                // Project to X-Y plane
                new CuboidRegion(pos1.setZ(min.getZ()), pos2.setZ(min.getZ())),
                new CuboidRegion(pos1.setZ(max.getZ()), pos2.setZ(max.getZ())),

                // Project to the X-Z plane
                new CuboidRegion(pos1.setY(min.getY()), pos2.setY(min.getY())),
                new CuboidRegion(pos1.setY(max.getY()), pos2.setY(max.getY())));
    }

    /**
     * Get a region that contains the walls (all faces but the ones parallel to
     * the X-Z plane) of this cuboid.
     *
     * @return a new complex region
     */
    public Region getWalls() {
        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        return new RegionIntersection(
                // Project to Z-Y plane
                new CuboidRegion(pos1.setX(min.getX()), pos2.setX(min.getX())),
                new CuboidRegion(pos1.setX(max.getX()), pos2.setX(max.getX())),

                // Project to X-Y plane
                new CuboidRegion(pos1.setZ(min.getZ()), pos2.setZ(min.getZ())),
                new CuboidRegion(pos1.setZ(max.getZ()), pos2.setZ(max.getZ())));
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
    public void expand(Vector... changes) {
        checkNotNull(changes);

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
        checkNotNull(changes);

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

    @Override
    public void shift(Vector change) throws RegionOperationException {
        pos1 = pos1.add(change);
        pos2 = pos2.add(change);

        recalculate();
    }

    @Override
    public Set<Vector2D> getChunks() {
        Set<Vector2D> chunks = new HashSet<>();

        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        for (int x = min.getBlockX() >> ChunkStore.CHUNK_SHIFTS; x <= max.getBlockX() >> ChunkStore.CHUNK_SHIFTS; ++x) {
            for (int z = min.getBlockZ() >> ChunkStore.CHUNK_SHIFTS; z <= max.getBlockZ() >> ChunkStore.CHUNK_SHIFTS; ++z) {
                chunks.add(new BlockVector2D(x, z));
            }
        }

        return chunks;
    }

    @Override
    public Set<Vector> getChunkCubes() {
        Set<Vector> chunks = new HashSet<>();

        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        for (int x = min.getBlockX() >> ChunkStore.CHUNK_SHIFTS; x <= max.getBlockX() >> ChunkStore.CHUNK_SHIFTS; ++x) {
            for (int z = min.getBlockZ() >> ChunkStore.CHUNK_SHIFTS; z <= max.getBlockZ() >> ChunkStore.CHUNK_SHIFTS; ++z) {
                for (int y = min.getBlockY() >> ChunkStore.CHUNK_SHIFTS; y <= max.getBlockY() >> ChunkStore.CHUNK_SHIFTS; ++y) {
                    chunks.add(new BlockVector(x, y, z));
                }
            }
        }

        return chunks;
    }

    @Override
    public boolean contains(Vector position) {
        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();

        Vector min = getMinimumPoint();
        Vector max = getMaximumPoint();

        return x >= min.getBlockX() && x <= max.getBlockX()
                && y >= min.getBlockY() && y <= max.getBlockY()
                && z >= min.getBlockZ() && z <= max.getBlockZ();
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
    public Iterable<Vector2D> asFlatRegion() {
        return () -> new Iterator<Vector2D>() {
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
    public String toString() {
        return getMinimumPoint() + " - " + getMaximumPoint();
    }

    @Override
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
        checkNotNull(region);
        return new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint());
    }

    /**
     * Make a cuboid from the center.
     *
     * @param origin the origin
     * @param apothem the apothem, where 0 is the minimum value to make a 1x1 cuboid
     * @return a cuboid region
     */
    public static CuboidRegion fromCenter(Vector origin, int apothem) {
        checkNotNull(origin);
        checkArgument(apothem >= 0, "apothem => 0 required");
        Vector size = new Vector(1, 1, 1).multiply(apothem);
        return new CuboidRegion(origin.subtract(size), origin.add(size));
    }
}
