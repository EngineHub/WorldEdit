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

package com.sk89q.worldedit.extent.cache;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;

/**
 * Returns the same cached {@link BaseBlock} for repeated calls to
 * {@link #getLazyBlock(Vector)} with the same position.
 */
public class LastAccessExtentCache extends AbstractDelegateExtent {

    private CachedBlock lastBlock;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     */
    public LastAccessExtentCache(Extent extent) {
        super(extent);
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        BlockVector blockVector = position.toBlockVector();
        CachedBlock lastBlock = this.lastBlock;
        if (lastBlock != null && lastBlock.position.equals(blockVector)) {
            return lastBlock.block;
        } else {
            BaseBlock block = super.getLazyBlock(position);
            this.lastBlock = new CachedBlock(blockVector, block);
            return block;
        }
    }

    private static class CachedBlock {
        private final BlockVector position;
        private final BaseBlock block;

        private CachedBlock(BlockVector position, BaseBlock block) {
            this.position = position;
            this.block = block;
        }
    }

}
