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
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.iterator.RegionIterator;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.storage.ChunkStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class AbstractRegion implements Region {

    protected World world;

    public AbstractRegion(World world) {
        this.world = world;
    }

    @Override
    public Vector3 getCenter() {
        return getMinimumPoint().add(getMaximumPoint()).toVector3().divide(2);
    }

    /**
     * Get the iterator.
     *
     * @return iterator of points inside the region
     */
    @Override
    public Iterator<BlockVector3> iterator() {
        return new RegionIterator(this);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public void shift(BlockVector3 change) throws RegionOperationException {
        expand(change);
        contract(change);
    }

    @Override
    public AbstractRegion clone() {
        try {
            return (AbstractRegion) super.clone();
        } catch (CloneNotSupportedException exc) {
            return null;
        }
    }

    @Override
    public List<BlockVector2> polygonize(int maxPoints) {
        if (maxPoints >= 0 && maxPoints < 4) {
            throw new IllegalArgumentException("Cannot polygonize an AbstractRegion with no overridden polygonize method into less than 4 points.");
        }

        final BlockVector3 min = getMinimumPoint();
        final BlockVector3 max = getMaximumPoint();

        final List<BlockVector2> points = new ArrayList<>(4);

        points.add(BlockVector2.at(min.x(), min.z()));
        points.add(BlockVector2.at(min.x(), max.z()));
        points.add(BlockVector2.at(max.x(), max.z()));
        points.add(BlockVector2.at(max.x(), min.z()));

        return points;
    }

    @Override
    public long getVolume() {
        BlockVector3 min = getMinimumPoint();
        BlockVector3 max = getMaximumPoint();

        return (max.x() - min.x() + 1L)
            * (max.y() - min.y() + 1L)
            * (max.z() - min.z() + 1L);
    }

    /**
     * Get X-size.
     *
     * @return width
     */
    @Override
    public int getWidth() {
        BlockVector3 min = getMinimumPoint();
        BlockVector3 max = getMaximumPoint();

        return max.x() - min.x() + 1;
    }

    /**
     * Get Y-size.
     *
     * @return height
     */
    @Override
    public int getHeight() {
        BlockVector3 min = getMinimumPoint();
        BlockVector3 max = getMaximumPoint();

        return max.y() - min.y() + 1;
    }

    /**
     * Get Z-size.
     *
     * @return length
     */
    @Override
    public int getLength() {
        BlockVector3 min = getMinimumPoint();
        BlockVector3 max = getMaximumPoint();

        return max.z() - min.z() + 1;
    }

    /**
     * Get a list of chunks.
     *
     * @return a set of chunks
     */
    @Override
    public Set<BlockVector2> getChunks() {
        final Set<BlockVector2> chunks = new HashSet<>();

        final BlockVector3 min = getMinimumPoint();
        final BlockVector3 max = getMaximumPoint();

        final int minY = min.y();

        for (int x = min.x(); x <= max.x(); ++x) {
            for (int z = min.z(); z <= max.z(); ++z) {
                if (!contains(BlockVector3.at(x, minY, z))) {
                    continue;
                }

                chunks.add(BlockVector2.at(
                    x >> ChunkStore.CHUNK_SHIFTS,
                    z >> ChunkStore.CHUNK_SHIFTS
                ));
            }
        }

        return chunks;
    }

    @Override
    public Set<BlockVector3> getChunkCubes() {
        final Set<BlockVector3> chunks = new HashSet<>();

        final BlockVector3 min = getMinimumPoint();
        final BlockVector3 max = getMaximumPoint();

        for (int x = min.x(); x <= max.x(); ++x) {
            for (int y = min.y(); y <= max.y(); ++y) {
                for (int z = min.z(); z <= max.z(); ++z) {
                    if (!contains(BlockVector3.at(x, y, z))) {
                        continue;
                    }

                    chunks.add(BlockVector3.at(
                        x >> ChunkStore.CHUNK_SHIFTS,
                        y >> ChunkStore.CHUNK_SHIFTS,
                        z >> ChunkStore.CHUNK_SHIFTS
                    ));
                }
            }
        }

        return chunks;
    }

    // Sub-class utilities

    protected final int getWorldMinY() {
        return world == null ? Integer.MIN_VALUE : world.getMinY();
    }

    protected final int getWorldMaxY() {
        return world == null ? Integer.MAX_VALUE : world.getMaxY();
    }

}
