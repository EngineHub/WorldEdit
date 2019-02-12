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

package com.sk89q.worldedit.extent.buffer;

import com.google.common.collect.Maps;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Buffers changes to an {@link Extent} and allows retrieval of the changed blocks,
 * without modifying the underlying extent.
 */
public class ExtentBuffer extends AbstractDelegateExtent {

    private final Map<BlockVector3, BaseBlock> buffer = Maps.newHashMap();
    private final Mask mask;

    /**
     * Create a new extent buffer that will buffer every change.
     *
     * @param delegate the delegate extent for {@link Extent#getBlock(BlockVector3)}, etc. calls
     */
    public ExtentBuffer(Extent delegate) {
        this(delegate, Masks.alwaysTrue());
    }

    /**
     * Create a new extent buffer that will buffer changes that meet the criteria
     * of the given mask.
     *
     * @param delegate the delegate extent for {@link Extent#getBlock(BlockVector3)}, etc. calls
     * @param mask the mask
     */
    public ExtentBuffer(Extent delegate, Mask mask) {
        super(delegate);
        checkNotNull(delegate);
        checkNotNull(mask);
        this.mask = mask;
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        if (mask.test(position)) {
            return getOrDefault(position).toImmutableState();
        }
        return super.getBlock(position);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        if (mask.test(position)) {
            return getOrDefault(position);
        }
        return super.getFullBlock(position);
    }

    private BaseBlock getOrDefault(BlockVector3 position) {
        return buffer.computeIfAbsent(position, (pos -> getExtent().getFullBlock(pos)));
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
        if (mask.test(location)) {
            buffer.put(location, block.toBaseBlock());
            return true;
        }
        return false;
    }
}
