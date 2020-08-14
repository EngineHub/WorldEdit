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

package com.sk89q.worldedit.function.pattern;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;

import java.util.Map.Entry;

/**
 * Applies a block type while retaining all possible states.
 */
public class TypeApplyingPattern extends AbstractExtentPattern {
    private final BlockState blockState;

    public TypeApplyingPattern(Extent extent, BlockState blockState) {
        super(extent);
        this.blockState = blockState;
    }

    @Override
    public BaseBlock applyBlock(BlockVector3 position) {
        BlockState oldBlock = getExtent().getBlock(position);
        BlockState newBlock = blockState;
        for (Entry<Property<?>, Object> entry : oldBlock.getStates().entrySet()) {
            @SuppressWarnings("unchecked")
            Property<Object> prop = (Property<Object>) entry.getKey();
            newBlock = newBlock.with(prop, entry.getValue());
        }
        return newBlock.toBaseBlock();
    }
}
