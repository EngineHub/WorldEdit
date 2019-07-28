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

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractBufferingExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A special extent that batches changes into Minecraft chunks. This helps
 * improve the speed of setting the blocks, since chunks do not need to be
 * loaded repeatedly, however it does take more memory due to caching the
 * blocks.
 */
public class ChunkBatchingExtent extends AbstractBufferingExtent {

    /**
     * Comparator optimized for sorting chunks by the region file they reside
     * in. This allows for file caches to be used while loading the chunk.
     */
    private static final Comparator<BlockVector2> REGION_OPTIMIZED_SORT =
            Comparator.comparing((BlockVector2 vec) -> vec.shr(5), BlockVector2.COMPARING_GRID_ARRANGEMENT)
                    .thenComparing(BlockVector2.COMPARING_GRID_ARRANGEMENT);

    private final Table<BlockVector2, BlockVector3, BaseBlock> batches =
        TreeBasedTable.create(REGION_OPTIMIZED_SORT, BlockVector3.sortByCoordsYzx());
    private final Set<BlockVector3> containedBlocks = new HashSet<>();
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

    private BlockVector2 getChunkPos(BlockVector3 location) {
        return location.shr(4).toBlockVector2();
    }

    private BlockVector3 getInChunkPos(BlockVector3 location) {
        return BlockVector3.at(location.getX() & 15, location.getY(), location.getZ() & 15);
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
        if (!enabled) {
            return setDelegateBlock(location, block);
        }
        BlockVector2 chunkPos = getChunkPos(location);
        BlockVector3 inChunkPos = getInChunkPos(location);
        batches.put(chunkPos, inChunkPos, block.toBaseBlock());
        containedBlocks.add(location);
        return true;
    }

    @Override
    protected Optional<BaseBlock> getBufferedBlock(BlockVector3 position) {
        if (!containedBlocks.contains(position)) {
            return Optional.empty();
        }
        return Optional.of(batches.get(getChunkPos(position), getInChunkPos(position)));
    }

    @Override
    protected Operation commitBefore() {
        if (!commitRequired()) {
            return null;
        }
        return new Operation() {

            // we get modified between create/resume -- only create this on resume to prevent CME
            private Iterator<Map.Entry<BlockVector2, Map<BlockVector3, BaseBlock>>> batchIterator;

            @Override
            public Operation resume(RunContext run) throws WorldEditException {
                if (batchIterator == null) {
                    batchIterator = batches.rowMap().entrySet().iterator();
                }
                if (!batchIterator.hasNext()) {
                    return null;
                }
                Map.Entry<BlockVector2, Map<BlockVector3, BaseBlock>> next = batchIterator.next();
                BlockVector3 chunkOffset = next.getKey().toBlockVector3().shl(4);
                for (Map.Entry<BlockVector3, BaseBlock> block : next.getValue().entrySet()) {
                    getExtent().setBlock(block.getKey().add(chunkOffset), block.getValue());
                    containedBlocks.remove(block.getKey());
                }
                batchIterator.remove();
                return this;
            }

            @Override
            public void cancel() {
            }

            @Override
            public void addStatusMessages(List<String> messages) {
            }
        };
    }

}
