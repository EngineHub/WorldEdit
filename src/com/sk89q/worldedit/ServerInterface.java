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

import com.sk89q.worldedit.blocks.BaseItemStack;

/**
 * 
 * @author sk89q
 */
public abstract class ServerInterface {
    /**
     * Set block type.
     * 
     * @param pt
     * @param type
     * @return
     */
    public abstract boolean setBlockType(LocalWorld world, Vector pt, int type);

    /**
     * Get block type.
     * 
     * @param pt
     * @return
     */
    public abstract int getBlockType(LocalWorld world, Vector pt);

    /**
     * Set block data.
     * 
     * @param pt
     * @param data
     * @return
     */
    public abstract void setBlockData(LocalWorld world, Vector pt, int data);

    /**
     * Get block data.
     * 
     * @param pt
     * @return
     */
    public abstract int getBlockData(LocalWorld world, Vector pt);

    /**
     * Set sign text.
     * 
     * @param pt
     * @param text
     */
    public abstract void setSignText(LocalWorld world, Vector pt, String[] text);

    /**
     * Get sign text.
     * 
     * @param pt
     * @return
     */
    public abstract String[] getSignText(LocalWorld world, Vector pt);

    /**
     * Gets the contents of chests. Will return null if the chest does not
     * really exist or it is the second block for a double chest.
     * 
     * @param pt
     * @return
     */
    public abstract BaseItemStack[] getChestContents(LocalWorld world, Vector pt);

    /**
     * Sets a chest slot.
     * 
     * @param pt
     * @param contents
     * @return
     */
    public abstract boolean setChestContents(LocalWorld world, Vector pt,
            BaseItemStack[] contents);

    /**
     * Clear a chest's contents.
     * 
     * @param pt
     */
    public abstract boolean clearChest(LocalWorld world, Vector pt);

    /**
     * Checks if a mob type is valid.
     * 
     * @param type
     * @return
     */
    public abstract boolean isValidMobType(String type);

    /**
     * Set mob spawner mob type.
     * 
     * @param pt
     * @param mobType
     */
    public abstract void setMobSpawnerType(LocalWorld world, Vector pt,
            String mobType);

    /**
     * Get mob spawner mob type. May return an empty string.
     * 
     * @param pt
     * @param mobType
     */
    public abstract String getMobSpawnerType(LocalWorld world, Vector pt);

    /**
     * Generate a tree at a location.
     * 
     * @param pt
     * @return
     */
    public abstract boolean generateTree(EditSession editSession,
            LocalWorld world, Vector pt);

    /**
     * Drop an item.
     * 
     * @param pt
     * @param type
     * @param count
     * @param times
     */
    public abstract void dropItem(LocalWorld world, Vector pt, int type,
            int count, int times);

    /**
     * Drop an item.
     * 
     * @param pt
     * @param type
     * @param count
     * @param times
     */
    public abstract void dropItem(LocalWorld world, Vector pt, int type,
            int count);

    /**
     * Drop an item.
     * 
     * @param pt
     * @param type
     * @param count
     * @param times
     */
    public abstract void dropItem(LocalWorld world, Vector pt, int type);

    /**
     * Simulate a block being mined.
     * 
     * @param pt
     */
    public abstract void simulateBlockMine(LocalWorld world, Vector pt);

    /**
     * Resolves an item name to its ID.
     * 
     * @param name
     * @return
     */
    public abstract int resolveItem(String name);

    /**
     * Kill mobs in an area.
     * 
     * @param origin
     * @param radius
     * @return
     */
    public abstract int killMobs(LocalWorld world, Vector origin, int radius);
}
