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

package com.sk89q.worldedit.world;

import com.sk89q.jnbt.CompoundTag;

import javax.annotation.Nullable;

/**
 * Indicates an object that contains extra data identified as an NBT structure.
 * This interface is used when saving and loading objects to a serialized
 * format, but may be used in other cases.
 */
public interface NbtValued {
    
    /**
     * Returns whether the block contains NBT data. {@link #getNbtData()}
     * must not return null if this method returns true.
     * 
     * @return true if there is NBT data
     */
    boolean hasNbtData();

    /**
     * Get the object's NBT data (tile entity data). The returned tag, if
     * modified in any way, should be sent to {@link #setNbtData(CompoundTag)}
     * so that the instance knows of the changes. Making changes without
     * calling {@link #setNbtData(CompoundTag)} could have unintended
     * consequences.
     *
     * <p>{@link #hasNbtData()} must return true if and only if method does
     * not return null.</p>
     * 
     * @return compound tag, or null
     */
    @Nullable
    CompoundTag getNbtData();

    /**
     * Set the object's NBT data (tile entity data).
     * 
     * @param nbtData NBT data, or null if no data
     */
    void setNbtData(@Nullable CompoundTag nbtData);

}
