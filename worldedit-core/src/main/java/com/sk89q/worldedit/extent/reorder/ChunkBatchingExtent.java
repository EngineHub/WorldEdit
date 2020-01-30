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

package com.sk89q.worldedit.extent.reorder;

import com.google.common.collect.ImmutableSortedSet;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractBufferingExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.RegionOptimizedComparator;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * A special extent that batches changes into Minecraft chunks. This helps
 * improve the speed of setting the blocks, since chunks do not need to be
 * loaded repeatedly, however it does take more memory due to caching the
 * blocks.
 */
public class ChunkBatchingExtent extends AbstractBufferingExtent {

    private final BlockMap<BaseBlock> blockMap = BlockMap.createForBaseBlock();
    private boolean enabled;

    public ChunkBatchingExtent(Extent extent) {
        this(extent, true);
    }

    public ChunkBatchingExtent(Extent extent, boolean enabled) {
        super(extent);
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean commitRequired() {
        return enabled;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
        if (!enabled) {
            return setDelegateBlock(location, block);
        }
        blockMap.put(location, block.toBaseBlock());
        return true;
    }

    @Override
    protected Optional<BaseBlock> getBufferedBlock(BlockVector3 position) {
        return Optional.ofNullable(blockMap.get(position));
    }

    @Override
    protected Operation commitBefore() {
        if (!commitRequired()) {
            return null;
        }
        return new Operation() {

            // we get modified between create/resume -- only create this on resume to prevent CME
            private Iterator<BlockVector3> iterator;

            @Override
            public Operation resume(RunContext run) throws WorldEditException {
                if (iterator == null) {
                    iterator = ImmutableSortedSet.copyOf(RegionOptimizedComparator.INSTANCE,
                        blockMap.keySet()).iterator();
                }
                while (iterator.hasNext()) {
                    BlockVector3 position = iterator.next();
                    BaseBlock block = blockMap.get(position);
                    getExtent().setBlock(position, block);
                }
                blockMap.clear();
                return null;
            }

            @Override
            public void cancel() {
            }
        };
    }

}
