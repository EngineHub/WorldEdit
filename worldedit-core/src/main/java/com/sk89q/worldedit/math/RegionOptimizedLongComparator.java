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
