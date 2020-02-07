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

package com.sk89q.worldedit.extent;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Base extent class for buffering changes between {@link #setBlock(BlockVector3, BlockStateHolder)}
 * and the delegate extent. This class ensures that {@link #getBlock(BlockVector3)} is properly
 * handled, by returning buffered blocks.
 */
public abstract class AbstractBufferingExtent extends AbstractDelegateExtent {
    /**
     * Create a new instance.
     *
     * @param extent the extent
     */
    protected AbstractBufferingExtent(Extent extent) {
        super(extent);
    }

    @Override
    public abstract <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException;

    protected final <T extends BlockStateHolder<T>> boolean setDelegateBlock(BlockVector3 location, T block) throws WorldEditException {
        return super.setBlock(location, block);
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        BaseBlock block = getBufferedFullBlock(position);
        if (block == null) {
            return super.getBlock(position);
        }
        return block.toImmutableState();
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        BaseBlock block = getBufferedFullBlock(position);
        if (block == null) {
            return super.getFullBlock(position);
        }
        return block;
    }

    @Deprecated
    protected Optional<BaseBlock> getBufferedBlock(BlockVector3 position) {
        throw new IllegalStateException("Invalid BufferingExtent provided. Must override `getBufferedFullBlock(BlockVector3)`.");
    }

    //TODO make below abstract
    /**
     * Gets a block from the buffer, or null if not buffered.
     *
     * This **must** be overridden, and will be abstract in WorldEdit 8.
     *
     * @param position The position
     * @return The buffered block, or null
     */
    @Nullable
    protected BaseBlock getBufferedFullBlock(BlockVector3 position) {
        return getBufferedBlock(position).orElse(null);
    }

}
