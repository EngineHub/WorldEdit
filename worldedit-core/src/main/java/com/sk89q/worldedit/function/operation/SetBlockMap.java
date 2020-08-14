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

package com.sk89q.worldedit.function.operation;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class SetBlockMap implements Operation {

    private final Extent extent;
    private final BlockMap<BaseBlock> blocks;

    public SetBlockMap(Extent extent, BlockMap<BaseBlock> blocks) {
        this.extent = checkNotNull(extent);
        this.blocks = checkNotNull(blocks);
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        for (Map.Entry<BlockVector3, BaseBlock> entry : blocks.entrySet()) {
            extent.setBlock(entry.getKey(), entry.getValue());
        }
        return null;
    }

    @Override
    public void cancel() {
    }

}
