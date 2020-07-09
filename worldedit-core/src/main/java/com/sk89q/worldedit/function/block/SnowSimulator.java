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

package com.sk89q.worldedit.function.block;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

public class SnowSimulator implements RegionFunction {

    private final BlockState ice = BlockTypes.ICE.getDefaultState();
    private final BlockState snow = BlockTypes.SNOW.getDefaultState();
    private final BlockState snowBlock = BlockTypes.SNOW_BLOCK.getDefaultState();

    private final IntegerProperty snowLayersProperty = (IntegerProperty) (Object) BlockTypes.SNOW.getProperty("layers");

    private final EditSession editSession;
    private final boolean stack;

    public SnowSimulator(EditSession editSession, boolean stack) {
        this.editSession = editSession;
        this.stack = stack;
    }

    @Override
    public boolean apply(BlockVector3 position) throws WorldEditException {
        BlockState block = this.editSession.getBlock(position);

        if (block.getBlockType() == BlockTypes.WATER) {
            return this.editSession.setBlock(position, ice);
        }

        // Can only replace air (or snow in stack mode)
        if (!block.getBlockType().getMaterial().isAir() && (!stack || block.getBlockType() != BlockTypes.SNOW)) {
            return false;
        }

        // Can't put snow this far down
        if (position.getBlockY() == this.editSession.getMinimumPoint().getBlockY()) {
            return false;
        }

        BlockState below = this.editSession.getBlock(position.subtract(0, 1, 0));

        // Can't place snow on translucent blocks
        if (below.getBlockType().getMaterial().isTranslucent()) {
            // But still add snow on leaves
            if (!BlockCategories.LEAVES.contains(below.getBlockType())) {
                return false;
            }
        }

        if (stack && block.getBlockType() == BlockTypes.SNOW) {
            int currentHeight = block.getState(snowLayersProperty);
            // We've hit the highest layer (If it doesn't contain current + 2 it means it's 1 away from full)
            if (!snowLayersProperty.getValues().contains(currentHeight + 2)) {
                return this.editSession.setBlock(position, snowBlock);
            } else {
                return this.editSession.setBlock(position, block.with(snowLayersProperty, currentHeight + 1));
            }
        }
        return this.editSession.setBlock(position, snow);
    }
}
