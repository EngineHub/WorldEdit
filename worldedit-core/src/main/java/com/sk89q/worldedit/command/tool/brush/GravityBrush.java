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

package com.sk89q.worldedit.command.tool.brush;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GravityBrush implements Brush {

    private final boolean fullHeight;
    
    public GravityBrush(boolean fullHeight) {
        this.fullHeight = fullHeight;
    }

    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        final double startY = fullHeight ? editSession.getWorld().getMaxY() : position.getBlockY() + size;
        for (double x = position.getBlockX() + size; x > position.getBlockX() - size; --x) {
            for (double z = position.getBlockZ() + size; z > position.getBlockZ() - size; --z) {
                double y = startY;
                final List<BlockState> blockTypes = new ArrayList<>();
                for (; y > position.getBlockY() - size; --y) {
                    final BlockVector3 pt = BlockVector3.at(x, y, z);
                    final BlockState block = editSession.getBlock(pt);
                    if (!block.getBlockType().getMaterial().isAir()) {
                        blockTypes.add(block);
                        editSession.setBlock(pt, BlockTypes.AIR.getDefaultState());
                    }
                }
                BlockVector3 pt = BlockVector3.at(x, y, z);
                Collections.reverse(blockTypes);
                for (int i = 0; i < blockTypes.size();) {
                    if (editSession.getBlock(pt).getBlockType().getMaterial().isAir()) {
                        editSession.setBlock(pt, blockTypes.get(i++));
                    }
                    pt = pt.add(0, 1, 0);
                }
            }
        }
    }

}
