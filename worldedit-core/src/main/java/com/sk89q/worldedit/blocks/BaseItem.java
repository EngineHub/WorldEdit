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

import com.sk89q.worldedit.blocks.type.ItemType;
import com.sk89q.worldedit.blocks.type.ItemTypes;
import com.sk89q.worldedit.world.registry.BundledItemData;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an item, without an amount value. See {@link BaseItemStack}
 * for an instance with stack amount information.
 *
 * <p>This class may be removed in the future.</p>
 */
public class BaseItem {
    
    private ItemType itemType;
    private short damage;
    private final Map<Integer, Integer> enchantments = new HashMap<>();

    /**
     * Construct the object.
     *
     * @param id ID of the item
     */
    @Deprecated
    public BaseItem(int id) {
        this(id, (short) 0);
    }

    /**
     * Construct the object.
     *
     * @param itemType Type of the item
     */
    public BaseItem(ItemType itemType) {
        this.itemType = itemType;
    }

    /**
     * Construct the object.
     *
     * @param id ID of the item
     * @param data data value of the item
     */
    @Deprecated
    public BaseItem(int id, short data) {
        setType(id);
        this.damage = data;
    }

    /**
     * Construct the object.
     *
     * @param itemType Type of the item
     * @param damage Damage value of the item
     */
    public BaseItem(ItemType itemType, short damage) {
        this.itemType = itemType;
        this.damage = damage;
    }

    /**
     * Get the type of item.
     * 
     * @return the id
     */
    @Deprecated
    public int getType() {
        return this.itemType.getLegacyId();
    }

    /**
     * Set the type of item.
     * 
     * @param id the id to set
     */
    @Deprecated
    public void setType(int id) {
        ItemType type = ItemTypes.getItemType(BundledItemData.getInstance().fromLegacyId(id));
        setItemType(type);
    }

    /**
     * Set the type of the item.
     *
     * @param itemType The type to set
     */
    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    /**
     * Get the damage value.
     * 
     * @return the damage
     */
    public short getDamage() {
        return this.damage;
    }

    /**
     * Get the data value.
     * 
     * @return the data
     */
    @Deprecated
    public short getData() {
        return this.damage;
    }

    /**
     * Set the data value.
     * 
     * @param damage the damage to set
     */
    public void setDamage(short damage) {
        this.damage = damage;
    }

    /**
     * Set the data value.
     * 
     * @param data the damage to set
     */
    @Deprecated
    public void setData(short data) {
        this.damage = data;
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
