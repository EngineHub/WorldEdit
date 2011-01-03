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

package com.sk89q.worldedit.superpickaxe;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * A super pickaxe mode that will remove blocks in an area.
 * 
 * @author sk89q
 */
public class AreaPickaxe implements SuperPickaxeMode {
    private static final BaseBlock air = new BaseBlock(0);
    private int range;
    
    public AreaPickaxe(int range) {
        this.range = range;
    }
    
    @Override
    public boolean act(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session, LocalWorld world,
            Vector clicked) {
        int ox = clicked.getBlockX();
        int oy = clicked.getBlockY();
        int oz = clicked.getBlockZ();
        int initialType = server.getBlockType(world, clicked);
        
        if (initialType == 0) {
            return true;
        }
    
        if (initialType == BlockID.BEDROCK && !player.canDestroyBedrock()) {
            return true;
        }
        
        EditSession editSession = new EditSession(server, world,
                session.getBlockChangeLimit());
        
        try {
            for (int x = ox - range; x <= ox + range; x++) {
                for (int y = oy - range; y <= oy + range; y++) {
                    for (int z = oz - range; z <= oz + range; z++) {
                        Vector pos = new Vector(x, y, z);
                        if (server.getBlockType(world, pos) == initialType) {
                            if (config.superPickaxeManyDrop) {
                                server.simulateBlockMine(world, pos);
                            }
                            
                            editSession.setBlock(pos, air);
                        }
                    }
                }
            }
        } catch (MaxChangedBlocksException e) {
            player.printError("Max blocks change limit reached.");
        } finally {
            session.remember(editSession);
        }
    
        return true;
    }
}
