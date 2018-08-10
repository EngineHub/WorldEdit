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

package com.sk89q.worldedit.function.operation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Sets block from an iterator of {@link Map.Entry} containing a
 * {@link BlockVector} as the key and a {@link BaseBlock} as the value.
 */
public class BlockMapEntryPlacer implements Operation {

    private final Extent extent;
    private final Iterator<Map.Entry<BlockVector, BlockStateHolder>> iterator;

    /**
     * Create a new instance.
     *
     * @param extent the extent to set the blocks on
     * @param iterator the iterator
     */
    public BlockMapEntryPlacer(Extent extent, Iterator<Map.Entry<BlockVector, BlockStateHolder>> iterator) {
        checkNotNull(extent);
        checkNotNull(iterator);
        this.extent = extent;
        this.iterator = iterator;
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        while (iterator.hasNext()) {
            Map.Entry<BlockVector, BlockStateHolder> entry = iterator.next();
            extent.setBlock(entry.getKey(), entry.getValue());
        }

        return null;
    }

    @Override
    public void cancel() {
    }

    @Override
    public void addStatusMessages(List<String> messages) {
    }

}
