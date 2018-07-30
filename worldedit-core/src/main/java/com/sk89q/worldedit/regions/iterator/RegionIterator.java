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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.Region;

import java.util.Iterator;

public class RegionIterator implements Iterator<BlockVector> {

    private final Region region;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final Vector min;
    private int nextX;
    private int nextY;
    private int nextZ;

    public RegionIterator(Region region) {
        checkNotNull(region);

        this.region = region;

        Vector max = region.getMaximumPoint();
        this.maxX = max.getBlockX();
        this.maxY = max.getBlockY();
        this.maxZ = max.getBlockZ();

        this.min = region.getMinimumPoint();
        this.nextX = min.getBlockX();
        this.nextY = min.getBlockY();
        this.nextZ = min.getBlockZ();

        forward();
    }

    @Override
    public boolean hasNext() {
        return nextX != Integer.MIN_VALUE;
    }

    private void forward() {
        while (hasNext() && !region.contains(new BlockVector(nextX, nextY, nextZ))) {
            forwardOne();
        }
    }

    @Override
    public BlockVector next() {
        if (!hasNext()) throw new java.util.NoSuchElementException();

        BlockVector answer = new BlockVector(nextX, nextY, nextZ);

        forwardOne();
        forward();

        return answer;
    }

    private void forwardOne() {
        if (++nextX <= maxX) {
            return;
        }
        nextX = min.getBlockX();

        if (++nextY <= maxY) {
            return;
        }
        nextY = min.getBlockY();

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
