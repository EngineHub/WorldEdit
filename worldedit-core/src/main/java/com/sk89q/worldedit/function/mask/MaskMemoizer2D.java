package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.util.collection.BlockMap;

/**
 * A utility mask wrapper that memoizes the results of the given mask.
 *
 * <p>
 * This should not be kept around long-term for memory usage reasons. It's intended for usage within a single operation.
 * The function is auto-closeable to make this simpler.
 * </p>
 */
public class MaskMemoizer2D extends AbstractMask2D implements AutoCloseable {

    private final Mask2D mask;
    private final BlockMap<Boolean> cache;

    public MaskMemoizer2D(Mask2D mask) {
        this.mask = mask;
        this.cache = BlockMap.create();
    }

    @Override
    public boolean test(BlockVector2 vector) {
        // Use a Y=0 BlockVector3 to avoid creating a new BlockMap implementation
        return this.cache.computeIfAbsent(vector.toBlockVector3(), ignored -> mask.test(vector));
    }

    public void clear() {
        this.cache.clear();
    }

    @Override
    public void close() throws Exception {
        clear();
    }
}
