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

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

/**
 * Returns the same cached {@link BlockState} for repeated calls to
 * {@link #getBlock(BlockVector3)} with the same position.
 */
public class LastAccessExtentCache extends AbstractDelegateExtent {

    private CachedBlock<BlockState> lastBlock;
    private CachedBlock<BaseBlock> lastFullBlock;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     */
    public LastAccessExtentCache(Extent extent) {
        super(extent);
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        CachedBlock<BlockState> lastBlock = this.lastBlock;
        if (lastBlock != null && lastBlock.position.equals(position)) {
            return lastBlock.block;
        } else {
            BlockState block = super.getBlock(position);
            this.lastBlock = new CachedBlock<>(position, block);
            return block;
        }
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        CachedBlock<BaseBlock> lastFullBlock = this.lastFullBlock;
        if (lastFullBlock != null && lastFullBlock.position.equals(position)) {
            return lastFullBlock.block;
        } else {
            BaseBlock block = super.getFullBlock(position);
            this.lastFullBlock = new CachedBlock<>(position, block);
            return block;
        }
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
        if (super.setBlock(location, block)) {
            if (lastFullBlock != null && lastFullBlock.position.equals(location)) {
                this.lastFullBlock = new CachedBlock<>(location, block.toBaseBlock());
            }
            if (lastBlock != null && lastBlock.position.equals(location)) {
                this.lastBlock = new CachedBlock<>(location, block.toImmutableState());
            }

            return true;
        }
        return false;
    }

    private static class CachedBlock<B extends BlockStateHolder<B>> {
        private final BlockVector3 position;
        private final B block;

        private CachedBlock(BlockVector3 position, B block) {
            this.position = position;
            this.block = block;
        }
    }

}
