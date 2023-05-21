package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.collection.BlockMap;

import javax.annotation.Nullable;

/**
 * A utility mask wrapper that memoizes the results of the given mask.
 *
 * <p>
 * This should not be kept around long-term for memory usage reasons. It's intended for usage within a single operation.
 * The function is auto-closeable to make this simpler.
 * </p>
 */
public class MaskMemoizer extends AbstractMask implements AutoCloseable {

    private final Mask mask;
    private final BlockMap<Boolean> cache;

    public MaskMemoizer(Mask mask) {
        this.mask = mask;
        this.cache = BlockMap.create();
    }

    @Override
    public boolean test(BlockVector3 vector) {
        return this.cache.computeIfAbsent(vector, mask::test);
    }

    public void clear() {
        this.cache.clear();
    }

    @Nullable
    @Override
    public Mask2D toMask2D() {
        return new MaskMemoizer2D(this.mask.toMask2D());
    }

    @Override
    public void close() throws Exception {
        clear();
    }
}
