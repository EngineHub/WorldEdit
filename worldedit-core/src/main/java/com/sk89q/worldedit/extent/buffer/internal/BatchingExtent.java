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

package com.sk89q.worldedit.extent.buffer.internal;

import com.google.common.base.Throwables;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractBufferingExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;

/**
 * An extent that buffers all changes until completed.
 */
public class BatchingExtent extends AbstractBufferingExtent {

    private final BlockMap<BaseBlock> blockMap = BlockMap.createForBaseBlock();
    private boolean enabled;

    public BatchingExtent(Extent extent) {
        this(extent, true);
    }

    public BatchingExtent(Extent extent, boolean enabled) {
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
    protected BaseBlock getBufferedFullBlock(BlockVector3 position) {
        if (!enabled) {
            // Early exit if we're not enabled.
            return null;
        }
        return blockMap.get(position);
    }

    @Override
    protected Operation commitBefore() {
        if (!commitRequired()) {
            return null;
        }
        return new Operation() {

            @Override
            public Operation resume(RunContext run) throws WorldEditException {
                try {
                    blockMap.forEach((position, block) -> {
                        try {
                            getExtent().setBlock(position, block);
                        } catch (WorldEditException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (RuntimeException e) {
                    Throwables.throwIfInstanceOf(e.getCause(), WorldEditException.class);
                    throw e;
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
