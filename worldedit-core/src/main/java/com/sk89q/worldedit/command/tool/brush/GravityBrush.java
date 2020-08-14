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

    private final boolean overrideHeight;
    private final int heightOffset;

    public GravityBrush(Integer heightOffset) {
        this.overrideHeight = heightOffset != null;
        this.heightOffset = heightOffset != null ? heightOffset : Integer.MIN_VALUE;
    }

    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        double sizeOffset = overrideHeight ? heightOffset : size;
        double yMax = Math.min(position.getY() + sizeOffset, editSession.getWorld().getMaxY());
        double yMin = Math.max(position.getY() - sizeOffset, editSession.getWorld().getMinY());
        LocatedBlockList column = new LocatedBlockList();
        Set<BlockVector3> removedBlocks = new LinkedHashSet<>();
        for (double x = position.getX() - size; x <= position.getX() + size; x++) {
            for (double z = position.getZ() - size; z <= position.getZ() + size; z++) {
                /*
                 * Algorithm:
                 * 1. Find lowest air block in the selection -> $lowestAir = position
                 * 2. Move the first non-air block above it down to $lowestAir
                 * 3. Add 1 to $lowestAir's y-coord.
                 * 4. If more blocks above current position, repeat from 2
                 */

                BlockVector3 lowestAir = null;
                for (double y = yMin; y <= yMax; y++) {
                    BlockVector3 pt = BlockVector3.at(x, y, z);

                    BaseBlock block = editSession.getFullBlock(pt);

                    if (block.getBlockType().getMaterial().isAir()) {
                        if (lowestAir == null) {
                            // we found the lowest air block
                            lowestAir = pt;
                        }
                        continue;
                    }

                    if (lowestAir == null) {
                        // no place to move the block to
                        continue;
                    }

                    BlockVector3 newPos = lowestAir;
                    // we know the block above must be air,
                    // since either this block is being moved into it,
                    // or there has been more air before this block
                    lowestAir = lowestAir.add(0, 1, 0);

                    removedBlocks.remove(newPos);
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
