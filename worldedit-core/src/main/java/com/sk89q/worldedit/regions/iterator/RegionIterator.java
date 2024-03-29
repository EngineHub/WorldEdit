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

package com.sk89q.worldedit.regions.iterator;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

public class RegionIterator implements Iterator<BlockVector3> {

    private final Region region;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final BlockVector3 min;
    private int nextX;
    private int nextY;
    private int nextZ;

    public RegionIterator(Region region) {
        checkNotNull(region);

        this.region = region;

        BlockVector3 max = region.getMaximumPoint();
        this.maxX = max.x();
        this.maxY = max.y();
        this.maxZ = max.z();

        this.min = region.getMinimumPoint();
        this.nextX = min.x();
        this.nextY = min.y();
        this.nextZ = min.z();

        forward();
    }

    @Override
    public boolean hasNext() {
        return nextX != Integer.MIN_VALUE;
    }

    private void forward() {
        while (hasNext() && !region.contains(BlockVector3.at(nextX, nextY, nextZ))) {
            forwardOne();
        }
    }

    @Override
    public BlockVector3 next() {
        if (!hasNext()) {
            throw new java.util.NoSuchElementException();
        }

        BlockVector3 answer = BlockVector3.at(nextX, nextY, nextZ);

        forwardOne();
        forward();

        return answer;
    }

    private void forwardOne() {
        if (++nextX <= maxX) {
            return;
        }
        nextX = min.x();

        if (++nextY <= maxY) {
            return;
        }
        nextY = min.y();

        if (++nextZ <= maxZ) {
            return;
        }
        nextX = Integer.MIN_VALUE;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
