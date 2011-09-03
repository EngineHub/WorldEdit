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
import java.util.Set;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * A pickaxe mode that recursively finds adjacent blocks within range of
 * an initial block and of the same type.
 * 
 * @author sk89q
 */
public class RecursivePickaxe implements BlockTool {
    private static final BaseBlock air = new BaseBlock(0);
    private double range;
    
    public RecursivePickaxe(double range) {
        this.range = range;
    }
    
    public boolean canUse(LocalPlayer player) {
        return player.hasPermission("worldedit.superpickaxe.recursive");
    }
    
    public boolean actPrimary(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session, WorldVector clicked) {
        LocalWorld world = clicked.getWorld();
        
        int initialType = world.getBlockType(clicked);
        
        if (initialType == BlockID.AIR) {
            return true;
        }

        if (initialType == BlockID.BEDROCK && !player.canDestroyBedrock()) {
            return true;
        }

        EditSession editSession = session.createEditSession(player);

        try {
            recurse(server, editSession, world, clicked.toBlockVector(),
                    clicked, range, initialType, new HashSet<BlockVector>(),
                    config.superPickaxeManyDrop);
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
    private static void recurse(ServerInterface server, EditSession editSession,
            LocalWorld world, BlockVector pos,
            Vector origin, double size, int initialType,
            Set<BlockVector> visited, boolean drop)
            throws MaxChangedBlocksException {
        
        if (origin.distance(pos) > size || visited.contains(pos)) {
            return;
        }

        visited.add(pos);

        if (editSession.getBlock(pos).getType() == initialType) {
            if (drop) {
                world.simulateBlockMine(pos);
            }
            editSession.setBlock(pos, air);
        } else {
            return;
        }

        recurse(server, editSession, world, pos.add(1, 0, 0).toBlockVector(),
                origin, size, initialType, visited, drop);
        recurse(server, editSession, world, pos.add(-1, 0, 0).toBlockVector(),
                origin, size, initialType, visited, drop);
        recurse(server, editSession, world, pos.add(0, 0, 1).toBlockVector(),
                origin, size, initialType, visited, drop);
        recurse(server, editSession, world, pos.add(0, 0, -1).toBlockVector(),
                origin, size, initialType, visited, drop);
        recurse(server, editSession, world, pos.add(0, 1, 0).toBlockVector(),
                origin, size, initialType, visited, drop);
        recurse(server, editSession, world, pos.add(0, -1, 0).toBlockVector(),
                origin, size, initialType, visited, drop);
    }

}
