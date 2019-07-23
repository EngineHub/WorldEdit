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
import com.sk89q.worldedit.action.BlockPlacement;
import com.sk89q.worldedit.action.SideEffect;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.reorder.arrange.WorldActionOutputStream;
import com.sk89q.worldedit.reorder.buffer.MutableArrayWorldActionBuffer;
import com.sk89q.worldedit.reorder.buffer.MutableWorldActionBuffer;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.List;

/**
 * Extent that hands off block placements to a {@link WorldActionOutputStream}.
 */
public class ArrangerExtent extends AbstractDelegateExtent {

    private static final int BUFFER_AMOUNT = 1 << 16;

    private final BlockMap<BaseBlock> blockMap = BlockMap.createForBaseBlock();
    private final WorldActionOutputStream outputStream;
    private MutableWorldActionBuffer buffer;

    public ArrangerExtent(Extent extent, WorldActionOutputStream outputStream) {
        super(extent);
        this.outputStream = outputStream;
    }

    private MutableWorldActionBuffer buffer() {
        if (buffer == null) {
            buffer = MutableArrayWorldActionBuffer.allocate(BUFFER_AMOUNT);
        }
        return buffer;
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
        MutableWorldActionBuffer b = buffer();
        b.put(BlockPlacement.create(location, getExtent().getFullBlock(location),
            block.toBaseBlock(), SideEffect.getDefault()));

        // current buffer is full, pipe it and start a new buffer
        if (!b.hasRemaining()) {
            flushBuffer();
        }

        return true;
    }

    private void flushBuffer() {
        buffer.flip();
        outputStream.write(buffer);
        buffer = null;
    }

    @Override
    protected Operation commitBefore() {
        return new Operation() {
            @Override
            public Operation resume(RunContext run) {
                if (buffer != null) {
                    flushBuffer();
                }
                outputStream.flush();
                return null;
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
