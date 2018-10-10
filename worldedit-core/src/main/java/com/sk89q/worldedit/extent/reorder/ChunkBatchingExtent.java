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

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.function.operation.SetLocatedBlocks;
import com.sk89q.worldedit.util.collection.LocatedBlockList;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A special extent that batches changes into Minecraft chunks. This helps
 * improve the speed of setting the blocks, since chunks do not need to be
 * loaded repeatedly, however it does take more memory due to caching the
 * blocks.
 */
public class ChunkBatchingExtent extends AbstractDelegateExtent {

    /**
     * Comparator optimized for sorting chunks by the region file they reside
     * in. This allows for file caches to be used while loading the chunk.
     */
    private static final Comparator<Vector2D> REGION_OPTIMIZED_SORT =
            Comparator.<Vector2D, Vector2D>comparing(vec -> vec.divide(32).floor(), Vector2D.COMPARING_GRID_ARRANGEMENT)
                    .thenComparing(Vector2D.COMPARING_GRID_ARRANGEMENT);

    private final SortedMap<BlockVector2D, LocatedBlockList> batches = new TreeMap<>(REGION_OPTIMIZED_SORT);
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

    @Override
    public boolean setBlock(Vector location, BlockStateHolder block) throws WorldEditException {
        if (!enabled) {
            return getExtent().setBlock(location, block);
        }
        BlockVector2D chunkPos = new BlockVector2D(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        batches.computeIfAbsent(chunkPos, k -> new LocatedBlockList()).add(location, block);
        return true;
    }

    @Override
    protected Operation commitBefore() {
        if (!enabled) {
            return null;
        }
        return new Operation() {

            // we get modified between create/resume -- only create this on resume to prevent CME
            private Iterator<LocatedBlockList> batchIterator;

            @Override
            public Operation resume(RunContext run) throws WorldEditException {
                if (batchIterator == null) {
                    batchIterator = batches.values().iterator();
                }
                if (!batchIterator.hasNext()) {
                    return null;
                }
                new SetLocatedBlocks(getExtent(), batchIterator.next()).resume(run);
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
