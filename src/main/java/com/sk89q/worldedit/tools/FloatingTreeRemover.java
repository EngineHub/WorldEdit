// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.tools;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * A pickaxe mode that removes floating treetops (logs and leaves not connected
 * to anything else)
 * 
 * @author Moo0
 */
public class FloatingTreeRemover implements BlockTool {
    private static final BaseBlock air = new BaseBlock(0);
    private int range;
    
    public FloatingTreeRemover() {
        this.range = 100;
    }
    
    public boolean canUse(LocalPlayer player) {
        return player.hasPermission("worldedit.tool.deltree");
    }
    
    public boolean actPrimary(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session, WorldVector clicked) {
        LocalWorld world = clicked.getWorld();
        
        int initialType = world.getBlockType(clicked);
        int block;

        EditSession editSession = session.createEditSession(player);

        if (initialType != BlockID.LEAVES && initialType != BlockID.LOG) {
            player.printError("That's not a floating tree.");
            return true;
        }
        
        HashSet<BlockVector> blockSet = new HashSet<BlockVector>();
        try {
            if (!recurse(server, editSession, world, clicked.toBlockVector(),
                    clicked, range, blockSet, 0)) {
                player.printError("That's not a floating tree.");
                return true;
            }
            for (Iterator<BlockVector> iterator = blockSet.iterator(); iterator.hasNext();) {
                BlockVector blockVector = iterator.next();
                block = editSession.getBlock(blockVector).getType();
                if (block == BlockID.LEAVES || block == BlockID.LOG) {
                    editSession.setBlock(blockVector, air);
                }
            }
        } catch (MaxChangedBlocksException e) {
            player.printError("Max blocks change limit reached.");
        } finally {
            session.remember(editSession);
        }

        return true;
    }

    /**
     * Helper method.
     * 
     * @param server
     * @param superPickaxeManyDrop
     * @param world
     * @param pos
     * @param origin
     * @param size
     * @param initialType
     * @param visited
     */
    private boolean recurse(ServerInterface server, EditSession editSession,
            LocalWorld world, BlockVector pos,
            Vector origin, int size,
            Set<BlockVector> visited, int lastBlock)
            throws MaxChangedBlocksException {
        
        if (origin.distance(pos) > size || visited.contains(pos)) {
            return true;
        }

        visited.add(pos);

        int block = editSession.getBlock(pos).getType();
        if (block == BlockID.AIR || block == BlockID.SNOW){
            return true;
        }
        if (block != BlockID.LEAVES && block != BlockID.LOG) {
            if (lastBlock == BlockID.LEAVES) {
                return true;
            }
            else {
                return false;
            }
            
        }

        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    if (Math.abs(i) + Math.abs(j) + Math.abs(k) == 1) {
                        if (!recurse(server, editSession, world, pos.add(i, j, k).toBlockVector(),
                                origin, size, visited, block)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

}
