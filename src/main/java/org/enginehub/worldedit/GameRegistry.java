// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package org.enginehub.worldedit;

import org.bukkit.block.Block;
import org.enginehub.common.WorldObject;

import com.sk89q.worldedit.UnknownItemException;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ClothColor;

/**
 * Knows about blocks, items, entities, and other {@link WorldObject}s.
 */
public final class GameRegistry {

    GameRegistry() {
    }
    
    public BlockDefinition lookup(Block block) {
        
    }
    
    public Block getBlockByName(String name, boolean fuzzy) {
        BlockType blockType;
        name = name.replace("_", " ");
        name = name.replace(";", "|");
        String[] blockAndExtraData = name.split("\\|");
        String[] typeAndData = blockAndExtraData[0].split(":", 2);
        String testID = typeAndData[0];
        int blockId = -1;

        int data = -1;

        // Attempt to parse the item ID or otherwise resolve an item/block
        // name to its numeric ID
        try {
            blockId = Integer.parseInt(testID);
            blockType = BlockType.fromID(blockId);
        } catch (NumberFormatException e) {
            blockType = BlockType.lookup(testID);
            if (blockType == null) {
                int t = server.resolveItem(testID);
                if (t > 0) {
                    blockType = BlockType.fromID(t); // Could be null
                    blockId = t;
                }
            }
        }

        if (blockId == -1 && blockType == null) {
            // Maybe it's a cloth
            ClothColor col = ClothColor.lookup(testID);

            if (col != null) {
                blockType = BlockType.CLOTH;
                data = col.getID();
            } else {
                throw new UnknownItemException(name);
            }
        }
    }

}
