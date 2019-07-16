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

package com.sk89q.worldedit.math;

import it.unimi.dsi.fastutil.longs.LongComparator;

import static com.sk89q.worldedit.math.BitMath.unpackX;
import static com.sk89q.worldedit.math.BitMath.unpackZ;

/**
 * Sort packed block positions by region, then chunk.
 */
public class RegionOptimizedChunkLongComparator implements LongComparator {

    public static final RegionOptimizedChunkLongComparator INSTANCE
        = new RegionOptimizedChunkLongComparator();

    private RegionOptimizedChunkLongComparator() {
    }

    @Override
    public int compare(long a, long b) {
        int acz = unpackZ(a) >> 4;
        int bcz = unpackZ(b) >> 4;
        // Region Z
        int cmp = Integer.compare(acz >> 5, bcz >> 5);
        if (cmp != 0) {
            return cmp;
        }
        int acx = unpackX(a) >> 4;
        int bcx = unpackX(b) >> 4;
        // Region X
        cmp = Integer.compare(acx >> 5, bcx >> 5);
        if (cmp != 0) {
            return cmp;
        }
        // Chunk Z
        cmp = Integer.compare(acz, bcz);
        if (cmp != 0) {
            return cmp;
        }
        // Chunk X
        return Integer.compare(acx, bcx);
    }
}
