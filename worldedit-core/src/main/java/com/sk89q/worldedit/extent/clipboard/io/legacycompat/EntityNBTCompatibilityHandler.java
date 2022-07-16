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

package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.world.entity.EntityType;
import org.enginehub.linbus.tree.LinCompoundTag;

public interface EntityNBTCompatibilityHandler {
    /**
     * Check if this is an entity affected by this handler.
     *
     * @deprecated this was never used, just return the same tag from
     *     {@link #updateNbt(EntityType, LinCompoundTag)} if it's not affected
     */
    @Deprecated
    default boolean isAffectedEntity(EntityType type, CompoundTag entityTag) {
        var original = entityTag.toLinTag();
        var updated = updateNbt(type, original);
        return !original.equals(updated);
    }

    @Deprecated
    default CompoundTag updateNBT(EntityType type, CompoundTag entityTag) {
        return new CompoundTag(updateNbt(type, entityTag.toLinTag()));
    }

    /**
     * Given an entity type and data, update the data if needed.
     *
     * @param type the entity type
     * @param entityTag the entity tag
     * @return the updated tag, or the same tag if no update was needed
     * @apiNote This must be overridden by new subclasses. See {@link NonAbstractForCompatibility}
     *          for details
     */
    @SuppressWarnings("deprecation")
    @NonAbstractForCompatibility(
        delegateName = "updateNBT",
        delegateParams = { EntityType.class, CompoundTag.class }
    )
    default LinCompoundTag updateNbt(EntityType type, LinCompoundTag entityTag) {
        DeprecationUtil.checkDelegatingOverride(getClass());

        return updateNBT(type, new CompoundTag(entityTag)).toLinTag();
    }
}
