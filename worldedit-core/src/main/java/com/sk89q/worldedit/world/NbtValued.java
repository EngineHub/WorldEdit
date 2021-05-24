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

package com.sk89q.worldedit.world;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;

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
     * @deprecated See {@link #getNbt()}
     */
    @Deprecated
    default boolean hasNbtData() {
        return getNbt() != null;
    }

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
     * @deprecated See {@link #getNbt()}
     */
    @Deprecated
    @Nullable
    default CompoundTag getNbtData() {
        CompoundBinaryTag tag = getNbt();
        return tag == null ? null : new CompoundTag(tag);
    }

    /**
     * Set the object's NBT data (tile entity data).
     *
     * @param nbtData NBT data, or null if no data
     * @deprecated See {@link #setNbtReference(LazyReference)}
     */
    @Deprecated
    default void setNbtData(@Nullable CompoundTag nbtData) {
        setNbtReference(nbtData == null ? null : LazyReference.from(nbtData::asBinaryTag));
    }

    /**
     * Get the object's NBT data (tile entity data).
     *
     * <p>
     * This only needs to be used if you don't want to immediately resolve the data.
     * Otherwise, you probably want {@link #getNbt()}.
     * </p>
     *
     * @return compound tag, or null
     * @apiNote This must be overridden by new subclasses. See {@link NonAbstractForCompatibility}
     *          for details
     */
    @NonAbstractForCompatibility(
        delegateName = "getNbtData",
        delegateParams = { }
    )
    @Nullable
    default LazyReference<CompoundBinaryTag> getNbtReference() {
        DeprecationUtil.checkDelegatingOverride(getClass());

        CompoundTag nbtData = getNbtData();
        return nbtData == null ? null : LazyReference.from(nbtData::asBinaryTag);
    }

    /**
     * Get the object's NBT data (tile entity data).
     *
     * @return compound tag, or null
     * @apiNote This must be overridden by new subclasses. See {@link NonAbstractForCompatibility}
     *          for details
     */
    @Nullable
    default CompoundBinaryTag getNbt() {
        LazyReference<CompoundBinaryTag> ref = getNbtReference();
        return ref == null ? null : ref.getValue();
    }

    /**
     * Set the object's NBT data (tile entity data).
     *
     * @param nbtData NBT data, or null if no data
     * @apiNote This must be overridden by new subclasses. See {@link NonAbstractForCompatibility}
     *          for details
     */
    @NonAbstractForCompatibility(
        delegateName = "setNbtData",
        delegateParams = { CompoundTag.class }
    )
    default void setNbtReference(@Nullable LazyReference<CompoundBinaryTag> nbtData) {
        DeprecationUtil.checkDelegatingOverride(getClass());

        setNbtData(nbtData == null ? null : new CompoundTag(nbtData.getValue()));
    }

    /**
     * Set the object's NBT data (tile entity data).
     *
     * @param nbtData NBT data, or null if no data
     */
    default void setNbt(@Nullable CompoundBinaryTag nbtData) {
        setNbtReference(nbtData == null ? null : LazyReference.computed(nbtData));
    }

}
