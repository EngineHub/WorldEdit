/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.util.nbt.TagStringIO;
import com.sk89q.worldedit.world.NbtValued;
import com.sk89q.worldedit.world.item.ItemType;

import java.io.IOException;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents an item, without an amount value. See {@link BaseItemStack}
 * for an instance with stack amount information.
 *
 * <p>This class may be removed in the future.</p>
 */
public class BaseItem implements NbtValued {

    private ItemType itemType;
    @Nullable
    private LazyReference<CompoundBinaryTag> nbtData;

    /**
     * Construct the object.
     *
     * @param itemType Type of the item
     */
    public BaseItem(ItemType itemType) {
        checkNotNull(itemType);
        this.itemType = itemType;
    }

    /**
     * Construct the object.
     *
     * @param itemType Type of the item
     * @param nbtData NBT Compound tag
     */
    @Deprecated
    public BaseItem(ItemType itemType, @Nullable CompoundTag nbtData) {
        this(itemType, nbtData == null ? null : LazyReference.from(nbtData::asBinaryTag));
    }

    /**
     * Construct the object.
     *
     * @param itemType Type of the item
     * @param tag NBT Compound tag
     */
    public BaseItem(ItemType itemType, @Nullable LazyReference<CompoundBinaryTag> tag) {
        checkNotNull(itemType);
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

    @Nullable
    @Override
    public LazyReference<CompoundBinaryTag> getNbtReference() {
        return this.nbtData;
    }

    @Override
    public void setNbtReference(@Nullable LazyReference<CompoundBinaryTag> nbtData) {
        this.nbtData = nbtData;
    }

    @Override
    public String toString() {
        String nbtString = "";
        LazyReference<CompoundBinaryTag> nbtData = this.nbtData;
        if (nbtData != null) {
            try {
                nbtString = TagStringIO.get().asString(nbtData.getValue());
            } catch (IOException e) {
                WorldEdit.logger.error("Failed to serialize NBT of Item", e);
            }
        }

        return getType().getId() + nbtString;
    }
}
