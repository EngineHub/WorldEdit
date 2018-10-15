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

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.LocatedBlock;

import java.util.List;

public class SetLocatedBlocks implements Operation {

    private final Extent extent;
    private final Iterable<LocatedBlock> blocks;

    public SetLocatedBlocks(Extent extent, Iterable<LocatedBlock> blocks) {
        this.extent = checkNotNull(extent);
        this.blocks = checkNotNull(blocks);
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        for (LocatedBlock block : blocks) {
            extent.setBlock(block.getLocation(), block.getBlock());
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
