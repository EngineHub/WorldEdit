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

package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.collection.BlockMap;

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

    @Override
    public Mask2D toMask2D() {
        return new MaskMemoizer2D(this.mask.toMask2D());
    }

    @Override
    public void close() throws Exception {
        clear();
    }
}
