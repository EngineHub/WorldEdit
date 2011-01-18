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
import com.sk89q.worldedit.blocks.BaseItemStack;

/**
 * Represents a world.
 * 
 * @author sk89q
 */
public abstract class LocalWorld {
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
     * @return
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
     * Set sign text.
     * 
     * @param pt
     * @param text
     */
    public abstract void setSignText(Vector pt, String[] text);

    /**
     * Get sign text.
     * 
     * @param pt
     * @return
     */
    public abstract String[] getSignText(Vector pt);

    /**
     * Gets the contents of chests. Will return null if the chest does not
     * really exist or it is the second block for a double chest.
     * 
     * @param pt
     * @return
     */
    public abstract BaseItemStack[] getChestContents(Vector pt);

    /**
     * Sets a chest slot.
     * 
     * @param pt
     * @param contents
     * @return
     */
    public abstract boolean setChestContents(Vector pt,
            BaseItemStack[] contents);

    /**
     * Clear a chest's contents.
     * 
     * @param pt
     */
    public abstract boolean clearChest(Vector pt);

    /**
     * Set mob spawner mob type.
     * 
     * @param pt
     * @param mobType
     */
    public abstract void setMobSpawnerType(Vector pt,
            String mobType);

    /**
     * Get mob spawner mob type. May return an empty string.
     * 
     * @param pt
     * @param mobType
     */
    public abstract String getMobSpawnerType(Vector pt);

    /**
     * Generate a tree at a location.
     * 
     * @param pt
     * @return
     */
    public abstract boolean generateTree(EditSession editSession, Vector pt);

    /**
     * Generate a big tree at a location.
     * 
     * @param pt
     * @return
     */
    public abstract boolean generateBigTree(EditSession editSession, Vector pt);

    /**
     * Drop an item.
     *
     * @param pt
     * @param type
     * @param count
     * @param times
     */
    public void dropItem(Vector pt,BaseItemStack item, int times) {
        for (int i = 0; i < times; i++) {
            dropItem(pt, item);
        }
    }

    /**
     * Drop an item.
     * 
     * @param pt
     * @param item
     * @param count
     * @param times
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
        else if (type == 18) { // Leaves
            if (random.nextDouble() > 0.95) {
                dropItem(pt, new BaseItemStack(6));
            }
        }
        else if (type == 20) { } // Glass
        else if (type == 35) { dropItem(pt, new BaseItemStack(35, 1, (short)getBlockData(pt))); } // Cloth
        else if (type == 43) { dropItem(pt, new BaseItemStack(44)); } // Double step
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
        else if (type == 73) { dropItem(pt, new BaseItemStack(331), 4); } // Redstone ore
        else if (type == 74) { dropItem(pt, new BaseItemStack(331), 4); } // Glowing redstone ore
        else if (type == 75) { dropItem(pt, new BaseItemStack(76)); } // Redstone torch
        else if (type == 78) { } // Snow
        else if (type == 79) { } // Ice
        else if (type == 82) { dropItem(pt, new BaseItemStack(337), 4); } // Clay
        else if (type == 83) { dropItem(pt, new BaseItemStack(338)); } // Reed
        else if (type == 89) { dropItem(pt, new BaseItemStack(348)); } // Lightstone
        else if (type == 90) { } // Portal
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
     * Compare if the other world is equal.
     * 
     * @param other
     * @return
     */
    public abstract boolean equals(Object other);
    /**
     * Hash code.
     * 
     * @return
     */
    public abstract int hashCode();
}
