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
import static com.sk89q.worldedit.math.BitMath.unpackY;
import static com.sk89q.worldedit.math.BitMath.unpackZ;

/**
 * Sort packed block positions by region, chunk, and finally Y-Z-X.
 */
public class RegionOptimizedLongComparator implements LongComparator {

    public static final RegionOptimizedLongComparator INSTANCE
        = new RegionOptimizedLongComparator();

    private RegionOptimizedLongComparator() {
    }

    @Override
    public int compare(long a, long b) {
        int cmp = RegionOptimizedChunkLongComparator.INSTANCE.compare(a, b);
        if (cmp != 0) {
            return cmp;
        }
        cmp = Integer.compare(unpackY(a), unpackY(b));
        if (cmp != 0) {
            return cmp;
        }
        cmp = Integer.compare(unpackZ(a), unpackZ(b));
        if (cmp != 0) {
            return cmp;
        }
        return Integer.compare(unpackX(a), unpackX(b));
    }
}
