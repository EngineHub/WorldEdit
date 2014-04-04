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
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.patterns.Pattern;

import java.util.*;

/**
 * @author zml2008
 */
public class GravityBrush implements Brush {
    private final boolean fullHeight;
    
    public GravityBrush(boolean fullHeight) {
        this.fullHeight = fullHeight;
    }

    @Override
    public void build(EditSession editSession, Vector pos, Pattern mat, double size) throws MaxChangedBlocksException {
        final BaseBlock air = new BaseBlock(BlockID.AIR, 0);
        final double startY = fullHeight ? editSession.getWorld().getMaxY() : pos.getBlockY() + size;
        for (double x = pos.getBlockX() + size; x > pos.getBlockX() - size; --x) {
            for (double z = pos.getBlockZ() + size; z > pos.getBlockZ() - size; --z) {
                double y = startY;
                final List<BaseBlock> blockTypes = new ArrayList<BaseBlock>();
                for (; y > pos.getBlockY() - size; --y) {
                    final Vector pt = new Vector(x, y, z);
                    final BaseBlock block = editSession.getBlock(pt);
                    if (!block.isAir()) {
                        blockTypes.add(block);
                        editSession.setBlock(pt, air);
                    }
                }
                Vector pt = new Vector(x, y, z);
                Collections.reverse(blockTypes);
                for (int i = 0; i < blockTypes.size();) {
                    if (editSession.getBlock(pt).getType() == BlockID.AIR) {
                        editSession.setBlock(pt, blockTypes.get(i++));
                    }
                    pt = pt.add(0, 1, 0);
                }
            }
        }
    }
}
