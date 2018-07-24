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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.world.NbtValued;
import com.sk89q.worldedit.world.item.ItemType;

import javax.annotation.Nullable;

/**
 * Represents an item, without an amount value. See {@link BaseItemStack}
 * for an instance with stack amount information.
 *
 * <p>This class may be removed in the future.</p>
 */
public class BaseItem implements NbtValued {
    
    private ItemType itemType;
    @Nullable
    private CompoundTag nbtData;

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
     * @param itemType Type of the item
     * @param tag NBT Compound tag
     */
    public BaseItem(ItemType itemType, CompoundTag tag) {
        this.itemType = itemType;
        this.nbtData = tag;
    }

    /**
     * Get the type of item.
     *
     * @return the type
     */
    public ItemType getType() {
        return this.itemType;
    }

    /**
     * Set the type of the item.
     *
     * @param itemType The type to set
     */
    public void setType(ItemType itemType) {
        this.itemType = itemType;
    }

    @Override
    public boolean hasNbtData() {
        return this.nbtData != null;
    }

    @Nullable
    @Override
    public CompoundTag getNbtData() {
        return this.nbtData;
    }

    @Override
    public void setNbtData(@Nullable CompoundTag nbtData) {
        this.nbtData = nbtData;
    }
}
