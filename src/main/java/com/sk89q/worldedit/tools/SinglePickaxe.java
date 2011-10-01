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

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * A super pickaxe mode that removes one block.
 * 
 * @author sk89q
 */
public class SinglePickaxe implements BlockTool {
    
    public boolean canUse(LocalPlayer player) {
        return player.hasPermission("worldedit.superpickaxe");
    }
    
    public boolean actPrimary(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session, WorldVector clicked) {
        LocalWorld world = clicked.getWorld();
        
        if (world.getBlockType(clicked) == BlockID.BEDROCK
                && !player.canDestroyBedrock()) {
            return true;
        }

        if (config.superPickaxeDrop) {
            world.simulateBlockMine(clicked);
        }
        
        world.setBlockType(clicked, BlockID.AIR);
        
        return true;
    }

}
