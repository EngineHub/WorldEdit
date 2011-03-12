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

package com.sk89q.worldedit;

import java.util.Random;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;

/**
 * Represents a world.
 * 
 * @author sk89q
 */
public abstract class LocalWorld {
    /**
     * List of removable entity types.
     */
    public enum EntityType {
        ARROWS,
        ITEMS,
        PAINTINGS,
        BOATS,
        MINECARTS,
        TNT,
    }
    
    /**
     * Random generator.
     */
    protected Random random = new Random();

    /**
     * Set block type.
     * 
     * @param pt
     * @param type
     * @return
     */
    public abstract boolean setBlockType(Vector pt, int type);

    /**
     * Get block type.
     * 
     * @param pt
     * @return
     */
    public abstract int getBlockType(Vector pt);

    /**
     * Set block data.
     * 
     * @param pt
     * @param data
     */
    public abstract void setBlockData(Vector pt, int data);

    /**
     * Get block data.
     * 
     * @param pt
     * @return
     */
    public abstract int getBlockData(Vector pt);

    /**
     * Attempts to accurately copy a BaseBlock's extra data to the world.
     * 
     * @param pt
     * @param block
     * @return
     */
    public abstract boolean copyToWorld(Vector pt, BaseBlock block);

    /**
     * Attempts to read a BaseBlock's extra data from the world.
     * 
     * @param pt
     * @param block
     * @return
     */
    public abstract boolean copyFromWorld(Vector pt, BaseBlock block);

    /**
     * Clear a chest's contents.
     * 
     * @param pt
     * @return
     */
    public abstract boolean clearContainerBlockContents(Vector pt);

    /**
     * Generate a tree at a location.
     * 
     * @param editSession
     * @param pt
     * @return
     * @throws MaxChangedBlocksException
     */
    public abstract boolean generateTree(EditSession editSession, Vector pt)
            throws MaxChangedBlocksException;

    /**
     * Generate a big tree at a location.
     * 
     * @param editSession
     * @param pt
     * @return
     * @throws MaxChangedBlocksException
     */
    public abstract boolean generateBigTree(EditSession editSession, Vector pt)
            throws MaxChangedBlocksException;

    /**
     * Generate a birch tree at a location.
     * 
     * @param editSession
     * @param pt
     * @return
     * @throws MaxChangedBlocksException
     */
    public abstract boolean generateBirchTree(EditSession editSession, Vector pt)
            throws MaxChangedBlocksException;

    /**
     * Generate a redwood tree at a location.
     * 
     * @param editSession
     * @param pt
     * @return
     * @throws MaxChangedBlocksException
     */
    public abstract boolean generateRedwoodTree(EditSession editSession,
            Vector pt) throws MaxChangedBlocksException;

    /**
     * Generate a tall redwood tree at a location.
     * 
     * @param editSession 
     * @param pt
     * @return
     * @throws MaxChangedBlocksException 
     */
    public abstract boolean generateTallRedwoodTree(EditSession editSession,
            Vector pt) throws MaxChangedBlocksException;

    /**
     * Drop an item.
     * 
     * @param pt
     * @param item 
     * @param times
     */
    public void dropItem(Vector pt, BaseItemStack item, int times) {
        for (int i = 0; i < times; i++) {
            dropItem(pt, item);
        }
    }

    /**
     * Drop an item.
     * 
     * @param pt
     * @param item
     */
    public abstract void dropItem(Vector pt, BaseItemStack item);

    /**
     * Simulate a block being mined.
     * 
     * @param pt
     */
    public void simulateBlockMine(Vector pt) {
        int type = getBlockType(pt);
        //setBlockType(pt, 0);

        if (type == 1) { dropItem(pt, new BaseItemStack(4)); } // Stone
        else if (type == 2) { dropItem(pt, new BaseItemStack(3)); } // Grass
        else if (type == 7) { } // Bedrock
        else if (type == 8) { } // Water
        else if (type == 9) { } // Water
        else if (type == 10) { } // Lava
        else if (type == 11) { } // Lava
        else if (type == 13) { // Gravel
            dropItem(pt, new BaseItemStack(type));

            if (random.nextDouble() >= 0.9) {
                dropItem(pt, new BaseItemStack(318));
            }
        }
        else if (type == 16) { dropItem(pt, new BaseItemStack(263)); } // Coal ore
        else if (type == 17) { dropItem(pt, new BaseItemStack(17, 1, (short)getBlockData(pt))); } // Log
        else if (type == 18) { // Leaves
            if (random.nextDouble() > 0.95) {
                dropItem(pt, new BaseItemStack(6));
            }
        }
        else if (type == 20) { } // Glass
        else if (type == 21) { dropItem(pt, new BaseItemStack(351, 1, (short)4), (random.nextInt(5)+4)); }
        else if (type == 26) { dropItem(pt, new BaseItemStack(355)); } // Bed
        else if (type == 35) { dropItem(pt, new BaseItemStack(35, 1, (short)getBlockData(pt))); } // Cloth
        else if (type == 43) { // Double step
            dropItem(pt, new BaseItemStack(44, 1, (short)getBlockData(pt)), 2);
        }
        else if (type == 44) { dropItem(pt, new BaseItemStack(44, 1, (short)getBlockData(pt))); } // Step
        else if (type == 47) { } // Bookshelves
        else if (type == 51) { } // Fire
        else if (type == 52) { } // Mob spawner
        else if (type == 53) { dropItem(pt, new BaseItemStack(5)); } // Wooden stairs
        else if (type == 55) { dropItem(pt, new BaseItemStack(331)); } // Redstone wire
        else if (type == 56) { dropItem(pt, new BaseItemStack(264)); } // Diamond ore
        else if (type == 59) { dropItem(pt, new BaseItemStack(295)); } // Crops
        else if (type == 60) { dropItem(pt, new BaseItemStack(3)); } // Soil
        else if (type == 62) { dropItem(pt, new BaseItemStack(61)); } // Furnace
        else if (type == 63) { dropItem(pt, new BaseItemStack(323)); } // Sign post
        else if (type == 64) { dropItem(pt, new BaseItemStack(324)); } // Wood door
        else if (type == 67) { dropItem(pt, new BaseItemStack(4)); } // Cobblestone stairs
        else if (type == 68) { dropItem(pt, new BaseItemStack(323)); } // Wall sign
        else if (type == 71) { dropItem(pt, new BaseItemStack(330)); } // Iron door
        else if (type == 73) { dropItem(pt, new BaseItemStack(331), (random.nextInt(2)+4)); } // Redstone ore
        else if (type == 74) { dropItem(pt, new BaseItemStack(331), (random.nextInt(2)+4)); } // Glowing redstone ore
        else if (type == 75) { dropItem(pt, new BaseItemStack(76)); } // Redstone torch
        else if (type == 78) { } // Snow
        else if (type == 79) { } // Ice
        else if (type == 82) { dropItem(pt, new BaseItemStack(337), 4); } // Clay
        else if (type == 83) { dropItem(pt, new BaseItemStack(338)); } // Reed
        else if (type == 89) { dropItem(pt, new BaseItemStack(348)); } // Lightstone
        else if (type == 90) { } // Portal
        else if (type == 93) { dropItem(pt, new BaseItemStack(356)); } // Repeater
        else if (type == 94) { dropItem(pt, new BaseItemStack(356)); } // Repeater
        else if (type != 0) {
            dropItem(pt, new BaseItemStack(type));
        }
    }

    /**
     * Kill mobs in an area.
     * 
     * @param origin
     * @param radius
     * @return
     */
    public abstract int killMobs(Vector origin, int radius);

    /**
     * Remove entities in an area.
     * 
     * @param type 
     * @param origin
     * @param radius
     * @return
     */
    public abstract int removeEntities(EntityType type, Vector origin, int radius);

    /**
     * Compare if the other world is equal.
     * 
     * @param other
     * @return
     */
    @Override
    public abstract boolean equals(Object other);

    /**
     * Hash code.
     * 
     * @return
     */
    @Override
    public abstract int hashCode();
}
