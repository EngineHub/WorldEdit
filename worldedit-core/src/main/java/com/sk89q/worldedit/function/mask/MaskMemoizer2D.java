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
