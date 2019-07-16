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
