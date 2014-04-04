/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.blocks;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an item, without an amount value. See {@link BaseItemStack} for an instance
 * with stack amount information.
 *
 * @author sk89q
 */
public class BaseItem {
    
    private int id;
    private short data;
    private final Map<Integer, Integer> enchantments = new HashMap<Integer, Integer>();

    /**
     * Construct the object.
     *
     * @param id ID of the item
     */
    public BaseItem(int id) {
        this.id = id;
        this.data = 0;
    }

    /**
     * Construct the object.
     *
     * @param id ID of the item
     * @param data data value of the item
     */
    public BaseItem(int id, short data) {
        this.id = id;
        this.data = data;
    }

    /**
     * Get the type of item.
     * 
     * @return the id
     */
    public int getType() {
        return id;
    }

    /**
     * Get the type of item.
     * 
     * @param id the id to set
     */
    public void setType(int id) {
        this.id = id;
    }

    /**
     * Get the damage value.
     * 
     * @return the damage
     */
    @Deprecated
    public short getDamage() {
        return data;
    }

    /**
     * Get the data value.
     * 
     * @return the data
     */
    public short getData() {
        return data;
    }

    /**
     * Set the data value.
     * 
     * @param data the damage to set
     */
    @Deprecated
    public void setDamage(short data) {
        this.data = data;
    }

    /**
     * Set the data value.
     * 
     * @param data the damage to set
     */
    public void setData(short data) {
        this.data = data;
    }

    /**
     * Get the map of enchantments.
     * 
     * @return map of enchantments
     */
    public Map<Integer, Integer> getEnchantments() {
        return enchantments;
    }
}
