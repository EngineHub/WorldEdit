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
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.adventure.text.Component;
import com.sk89q.worldedit.world.item.ItemType;
import org.enginehub.linbus.tree.LinCompoundTag;

/**
 * Represents a stack of BaseItems.
 *
 * <p>This class may be removed in the future.</p>
 */
public class BaseItemStack extends BaseItem {

    private int amount = 1;

    /**
     * Construct the object with default stack size of one, with damage value of 0.
     *
     * @param itemType The item type
     */
    public BaseItemStack(ItemType itemType) {
        super(itemType);
    }

    /**
     * Construct the object.
     *
     * @param itemType The item type
     * @param amount amount in the stack
     */
    public BaseItemStack(ItemType itemType, int amount) {
        super(itemType);
        this.amount = amount;
    }

    /**
     * Construct the object.
     *
     * @param id The item type
     * @param tag Tag value
     * @param amount amount in the stack
     * @deprecated Use {@link #BaseItemStack(ItemType, LazyReference, int)}
     */
    @Deprecated
    public BaseItemStack(ItemType id, CompoundTag tag, int amount) {
        super(id, tag);
        this.amount = amount;
    }

    /**
     * Construct the object.
     *
     * @param id The item type
     * @param tag Tag value
     * @param amount amount in the stack
     */
    public BaseItemStack(ItemType id, LazyReference<LinCompoundTag> tag, int amount) {
        super(id, tag);
        this.amount = amount;
    }

    /**
     * Get the number of items in the stack.
     *
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Set the amount of items in the stack.
     *
     * @param amount the amount to set
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Gets the rich name of the item stack.
     *
     * @deprecated Use {@link BaseItemStack#getDisplayName()} instead
     * @return the rich name
     */
    @Deprecated
    public com.sk89q.worldedit.util.formatting.text.Component getRichName() {
        return WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS)
            .getRegistries().getItemRegistry().getRichName(this);
    }

    /**
     * Gets the display name of the item stack.
     *
     * @return the display name
     */
    public Component getDisplayName() {
        return WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS)
                .getRegistries().getItemRegistry().getDisplayName(this);
    }

}
