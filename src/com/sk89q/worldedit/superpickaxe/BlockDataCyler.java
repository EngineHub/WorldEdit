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
import com.sk89q.worldedit.blocks.BlockID;

/**
 * A mode that cycles the data values of supported blocks.
 * 
 * @author sk89q
 */
public class BlockDataCyler implements SuperPickaxeMode {
    @Override
    public boolean act(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session, WorldVector clicked) {
        
        LocalWorld world = clicked.getWorld();
        
        int type = world.getBlockType(clicked);
        int data = world.getBlockData(clicked);
        
        if (config.allowedDataCycleBlocks.size() > 0
                && !player.hasPermission("worldedit.override.data-cycler")
                && !config.allowedDataCycleBlocks.contains(type)) {
            player.printError("You are not permitted to cycle the data value of that block.");
            return true;
        }
        
        if (type == BlockID.LOG) {
            data = (data + 1) % 3;
        } else if (type == BlockID.LEAVES) {
            data = (data + 1) % 3;
        } else if (type == BlockID.CACTUS) {
            data = (data + 1) % 16;
        } else if (type == BlockID.SOIL) {
            data = (data + 1) % 9;
        } else if (type == BlockID.CROPS) {
            data = (data + 1) % 6;
        } else if (type == BlockID.MINECART_TRACKS) {
            if (data >= 6 && data <= 9) {
                data = (data + 1) % 4 + 6;
            } else {
                player.printError("This minecart track orientation is not supported.");
                return true;
            }
        } else if (type == BlockID.WOODEN_STAIRS || type == BlockID.COBBLESTONE_STAIRS) {
            data = (data + 1) % 4;
        } else if (type == BlockID.SIGN_POST) {
            data = (data + 1) % 16;
        } else if (type == BlockID.FURNACE || type == BlockID.BURNING_FURNACE
                || type == BlockID.DISPENSER) {
            data = (data + 1) % 4 + 2;
        } else if (type == BlockID.PUMPKIN || type == BlockID.JACKOLANTERN) {
            data = (data + 1) % 4;
        } else if (type == BlockID.CLOTH) {
            data = nextClothColor(data);
        } else {
            player.printError("That block's data cannot be cycled.");
            return true;
        }
        
        world.setBlockData(clicked, data);
        
        return true;
    }
    
    private static int nextClothColor(int data) {
        switch (data) {
            case 0: return 8;
            case 8: return 7;
            case 7: return 15;
            case 15: return 12;
            case 12: return 14;
            case 14: return 1;
            case 1: return 4;
            case 4: return 5;
            case 5: return 13;
            case 13: return 9;
            case 9: return 3;
            case 3: return 11;
            case 11: return 10;
            case 10: return 2;
            case 2: return 6;
            case 6: return 0;
        }
    
        return 0;
    }
}
