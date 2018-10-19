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

package com.sk89q.worldedit.regions.iterator;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class FlatRegionIterator implements Iterator<BlockVector2>  {

    private Region region;
    private int y;
    private int minX;
    private int nextX;
    private int nextZ;
    private int maxX;
    private int maxZ;

    public FlatRegionIterator(Region region) {
        checkNotNull(region);

        this.region = region;

        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        this.y = min.getBlockY();

        this.minX = min.getBlockX();

        this.nextX = minX;
        this.nextZ = min.getBlockZ();

        this.maxX = max.getBlockX();
        this.maxZ = max.getBlockZ();

        forward();
    }

    @Override
    public boolean hasNext() {
        return nextX != Integer.MIN_VALUE;
    }

    private void forward() {
        while (hasNext() && !region.contains(BlockVector3.at(nextX, y, nextZ))) {
            forwardOne();
        }
    }

    @Override
    public BlockVector2 next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        BlockVector2 answer = BlockVector2.at(nextX, nextZ);

        forwardOne();
        forward();

        return answer;
    }

    private void forwardOne() {
        if (++nextX <= maxX) {
            return;
        }
        nextX = minX;

        if (++nextZ <= maxZ) {
            return;
        }
        nextX = Integer.MIN_VALUE;
    }

}
