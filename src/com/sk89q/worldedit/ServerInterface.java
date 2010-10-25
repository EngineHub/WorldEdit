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

import java.util.Map;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseItem;

/**
 *
 * @author sk89q
 */
public interface ServerInterface {
    /**
     * Set block type.
     * 
     * @param pt
     * @param type
     * @return
     */
    public boolean setBlockType(Vector pt, int type);
    /**
     * Get block type.
     * 
     * @param pt
     * @return
     */
    public int getBlockType(Vector pt);
    /**
     * Set block data.
     * 
     * @param pt
     * @param data
     * @return
     */
    public void setBlockData(Vector pt, int data);
    /**
     * Get block data.
     * 
     * @param pt
     * @return
     */
    public int getBlockData(Vector pt);
    /**
     * Set sign text.
     * 
     * @param pt
     * @param text
     */
    public void setSignText(Vector pt, String[] text);
    /**
     * Get sign text.
     * 
     * @param pt
     * @return
     */
    public String[] getSignText(Vector pt);
    /**
     * Gets the contents of chests.
     *
     * @param pt
     * @return
     */
    public Map<Byte,Countable<BaseItem>> getChestContents(Vector pt);
    /**
     * Sets a chest slot.
     *
     * @param pt
     * @param slot
     * @param item
     * @param amount
     * @return
     */
    public boolean setChestSlot(Vector pt, byte slot, BaseItem item, int amount);
}
