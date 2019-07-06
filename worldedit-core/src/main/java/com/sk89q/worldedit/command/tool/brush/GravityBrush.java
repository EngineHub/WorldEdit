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
import com.sk89q.worldedit.util.LocatedBlock;
import com.sk89q.worldedit.util.collection.LocatedBlockList;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.LinkedHashSet;
import java.util.Set;

public class GravityBrush implements Brush {

    private final boolean fullHeight;

    public GravityBrush(boolean fullHeight) {
        this.fullHeight = fullHeight;
    }

    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        double yMax = fullHeight ? editSession.getWorld().getMaxY() : position.getY() + size;
        LocatedBlockList column = new LocatedBlockList();
        Set<BlockVector3> removedBlocks = new LinkedHashSet<>();
        for (double x = position.getX() - size; x <= position.getX() + size; x++) {
            for (double z = position.getZ() - size; z <= position.getZ() + size; z++) {
                for (double y = position.getY() - size; y <= yMax; y++) {
                    BlockVector3 newPos = BlockVector3.at(x, y - 1, z);
                    BlockVector3 pt = BlockVector3.at(x, y, z);

                    BaseBlock block = editSession.getFullBlock(pt);

                    if (block.getBlockType().getMaterial().isAir()) {
                        continue;
                    }

                    if (!removedBlocks.remove(newPos)) {
                        // we have not moved the block below this one.
                        // is it free in the edit session?
                        if (!editSession.getBlock(newPos).getBlockType().getMaterial().isAir()) {
                            // no -- do not move this block
                            continue;
                        }
                    }

                    column.add(newPos, block);
                    removedBlocks.add(pt);
                }

                for (LocatedBlock block : column) {
                    editSession.setBlock(block.getLocation(), block.getBlock());
                }

                for (BlockVector3 removedBlock : removedBlocks) {
                    editSession.setBlock(removedBlock, BlockTypes.AIR.getDefaultState());
                }

                column.clear();
                removedBlocks.clear();
            }
        }
    }

}
