/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.storage.ChunkStore;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An axis-aligned cuboid. It can be defined using two corners of the cuboid.
 */
public class CuboidRegion extends AbstractRegion implements FlatRegion {

    private BlockVector3 pos1;
    private BlockVector3 pos2;

    /**
     * Construct a new instance of this cuboid using two corners of the cuboid.
     *
     * @param pos1 the first position
     * @param pos2 the second position
     */
    public CuboidRegion(BlockVector3 pos1, BlockVector3 pos2) {
        this(null, pos1, pos2);
    }

    /**
     * Construct a new instance of this cuboid using two corners of the cuboid.
     *
     * @param world the world
     * @param pos1  the first position
     * @param pos2  the second position
     */
    public CuboidRegion(World world, BlockVector3 pos1, BlockVector3 pos2) {
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
    public BlockVector3 getPos1() {
        return pos1;
    }

    /**
     * Set the first cuboid-defining corner.
     *
     * @param pos1 a position
     */
    public void setPos1(BlockVector3 pos1) {
        this.pos1 = pos1;
    }

    /**
     * Get the second cuboid-defining corner.
     *
     * @return a position
     */
    public BlockVector3 getPos2() {
        return pos2;
    }

    /**
     * Set the second cuboid-defining corner.
     *
     * @param pos2 a position
     */
    public void setPos2(BlockVector3 pos2) {
        this.pos2 = pos2;
    }

    /**
     * Clamps the cuboid according to boundaries of the world.
     */
    private void recalculate() {
        pos1 = pos1.clampY(getWorldMinY(), getWorldMaxY());
        pos2 = pos2.clampY(getWorldMinY(), getWorldMaxY());
    }

    /**
     * Get a region that contains the faces of this cuboid.
     *
     * @return a new complex region
     */
    public Region getFaces() {
        BlockVector3 min = getMinimumPoint();
        BlockVector3 max = getMaximumPoint();

        return new RegionIntersection(
                // Project to Z-Y plane
                new CuboidRegion(pos1.withX(min.x()), pos2.withX(min.x())),
                new CuboidRegion(pos1.withX(max.x()), pos2.withX(max.x())),

                // Project to X-Y plane
                new CuboidRegion(pos1.withZ(min.z()), pos2.withZ(min.z())),
                new CuboidRegion(pos1.withZ(max.z()), pos2.withZ(max.z())),

                // Project to the X-Z plane
                new CuboidRegion(pos1.withY(min.y()), pos2.withY(min.y())),
                new CuboidRegion(pos1.withY(max.y()), pos2.withY(max.y())));
    }

    /**
     * Get a region that contains the walls (all faces but the ones parallel to
     * the X-Z plane) of this cuboid.
     *
     * @return a new complex region
     */
    public Region getWalls() {
        BlockVector3 min = getMinimumPoint();
        BlockVector3 max = getMaximumPoint();

        return new RegionIntersection(
                // Project to Z-Y plane
                new CuboidRegion(pos1.withX(min.x()), pos2.withX(min.x())),
                new CuboidRegion(pos1.withX(max.x()), pos2.withX(max.x())),

                // Project to X-Y plane
                new CuboidRegion(pos1.withZ(min.z()), pos2.withZ(min.z())),
                new CuboidRegion(pos1.withZ(max.z()), pos2.withZ(max.z())));
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        return pos1.getMinimum(pos2);
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return pos1.getMaximum(pos2);
    }

    @Override
    public CuboidRegion getBoundingBox() {
        return this;
    }

    @Override
    public int getMinimumY() {
        return Math.min(pos1.y(), pos2.y());
    }

    @Override
    public int getMaximumY() {
        return Math.max(pos1.y(), pos2.y());
    }

    @Override
    public void expand(BlockVector3... changes) {
        checkNotNull(changes);

        for (BlockVector3 change : changes) {
            if (change.x() > 0) {
                if (Math.max(pos1.x(), pos2.x()) == pos1.x()) {
                    pos1 = pos1.add(change.x(), 0, 0);
                } else {
                    pos2 = pos2.add(change.x(), 0, 0);
                }
            } else {
                if (Math.min(pos1.x(), pos2.x()) == pos1.x()) {
                    pos1 = pos1.add(change.x(), 0, 0);
                } else {
                    pos2 = pos2.add(change.x(), 0, 0);
                }
            }

            if (change.y() > 0) {
                if (Math.max(pos1.y(), pos2.y()) == pos1.y()) {
                    pos1 = pos1.add(0, change.y(), 0);
                } else {
                    pos2 = pos2.add(0, change.y(), 0);
                }
            } else {
                if (Math.min(pos1.y(), pos2.y()) == pos1.y()) {
                    pos1 = pos1.add(0, change.y(), 0);
                } else {
                    pos2 = pos2.add(0, change.y(), 0);
                }
            }

            if (change.z() > 0) {
                if (Math.max(pos1.z(), pos2.z()) == pos1.z()) {
                    pos1 = pos1.add(0, 0, change.z());
                } else {
                    pos2 = pos2.add(0, 0, change.z());
                }
            } else {
                if (Math.min(pos1.z(), pos2.z()) == pos1.z()) {
                    pos1 = pos1.add(0, 0, change.z());
                } else {
                    pos2 = pos2.add(0, 0, change.z());
                }
            }
        }

        recalculate();
    }

    @Override
    public void contract(BlockVector3... changes) {
        checkNotNull(changes);

        for (BlockVector3 change : changes) {
            if (change.x() < 0) {
                if (Math.max(pos1.x(), pos2.x()) == pos1.x()) {
                    pos1 = pos1.add(change.x(), 0, 0);
                } else {
                    pos2 = pos2.add(change.x(), 0, 0);
                }
            } else {
                if (Math.min(pos1.x(), pos2.x()) == pos1.x()) {
                    pos1 = pos1.add(change.x(), 0, 0);
                } else {
                    pos2 = pos2.add(change.x(), 0, 0);
                }
            }

            if (change.y() < 0) {
                if (Math.max(pos1.y(), pos2.y()) == pos1.y()) {
                    pos1 = pos1.add(0, change.y(), 0);
                } else {
                    pos2 = pos2.add(0, change.y(), 0);
                }
            } else {
                if (Math.min(pos1.y(), pos2.y()) == pos1.y()) {
                    pos1 = pos1.add(0, change.y(), 0);
                } else {
                    pos2 = pos2.add(0, change.y(), 0);
                }
            }

            if (change.z() < 0) {
                if (Math.max(pos1.z(), pos2.z()) == pos1.z()) {
                    pos1 = pos1.add(0, 0, change.z());
                } else {
                    pos2 = pos2.add(0, 0, change.z());
                }
            } else {
                if (Math.min(pos1.z(), pos2.z()) == pos1.z()) {
                    pos1 = pos1.add(0, 0, change.z());
                } else {
                    pos2 = pos2.add(0, 0, change.z());
                }
            }
        }

        recalculate();
    }

    @Override
    public void shift(BlockVector3 change) throws RegionOperationException {
        pos1 = pos1.add(change);
        pos2 = pos2.add(change);

        recalculate();
    }

    @Override
    public Set<BlockVector2> getChunks() {
        Set<BlockVector2> chunks = new HashSet<>();

        BlockVector3 min = getMinimumPoint();
        BlockVector3 max = getMaximumPoint();

        for (int x = min.x() >> ChunkStore.CHUNK_SHIFTS; x <= max.x() >> ChunkStore.CHUNK_SHIFTS; ++x) {
            for (int z = min.z() >> ChunkStore.CHUNK_SHIFTS; z <= max.z() >> ChunkStore.CHUNK_SHIFTS; ++z) {
                chunks.add(BlockVector2.at(x, z));
            }
        }

        return chunks;
    }

    @Override
    public Set<BlockVector3> getChunkCubes() {
        Set<BlockVector3> chunks = new HashSet<>();

        BlockVector3 min = getMinimumPoint();
        BlockVector3 max = getMaximumPoint();

        for (int x = min.x() >> ChunkStore.CHUNK_SHIFTS; x <= max.x() >> ChunkStore.CHUNK_SHIFTS; ++x) {
            for (int z = min.z() >> ChunkStore.CHUNK_SHIFTS; z <= max.z() >> ChunkStore.CHUNK_SHIFTS; ++z) {
                for (int y = min.y() >> ChunkStore.CHUNK_SHIFTS; y <= max.y() >> ChunkStore.CHUNK_SHIFTS; ++y) {
                    chunks.add(BlockVector3.at(x, y, z));
                }
            }
        }

        return chunks;
    }

    @Override
    public boolean contains(BlockVector3 position) {
        BlockVector3 min = getMinimumPoint();
        BlockVector3 max = getMaximumPoint();

        return position.containedWithin(min, max);
    }

    @Override
    public Iterator<BlockVector3> iterator() {
        return new Iterator<BlockVector3>() {
            private final BlockVector3 min = getMinimumPoint();
            private final BlockVector3 max = getMaximumPoint();
            private int nextX = min.x();
            private int nextY = min.y();
            private int nextZ = min.z();

            @Override
            public boolean hasNext() {
                return (nextX != Integer.MIN_VALUE);
            }

            @Override
            public BlockVector3 next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                BlockVector3 answer = BlockVector3.at(nextX, nextY, nextZ);
                if (++nextX > max.x()) {
                    nextX = min.x();
                    if (++nextZ > max.z()) {
                        nextZ = min.z();
                        if (++nextY > max.y()) {
                            nextX = Integer.MIN_VALUE;
                        }
                    }
                }
                return answer;
            }
        };
    }

    @Override
    public Iterable<BlockVector2> asFlatRegion() {
        return () -> new Iterator<BlockVector2>() {
            private final BlockVector3 min = getMinimumPoint();
            private final BlockVector3 max = getMaximumPoint();
            private int nextX = min.x();
            private int nextZ = min.z();

            @Override
            public boolean hasNext() {
                return (nextX != Integer.MIN_VALUE);
            }

            @Override
            public BlockVector2 next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                BlockVector2 answer = BlockVector2.at(nextX, nextZ);
                if (++nextX > max.x()) {
                    nextX = min.x();
                    if (++nextZ > max.z()) {
                        nextX = Integer.MIN_VALUE;
                    }
                }
                return answer;
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
    public static CuboidRegion fromCenter(BlockVector3 origin, int apothem) {
        checkNotNull(origin);
        checkArgument(apothem >= 0, "apothem => 0 required");
        BlockVector3 size = BlockVector3.ONE.multiply(apothem);
        return new CuboidRegion(origin.subtract(size), origin.add(size));
    }
}
