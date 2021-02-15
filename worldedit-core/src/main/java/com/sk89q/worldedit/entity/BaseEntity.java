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

package com.sk89q.worldedit.entity;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.world.NbtValued;
import com.sk89q.worldedit.world.entity.EntityType;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a mutable "snapshot" of an entity.
 *
 * <p>An instance of this class contains all the information needed to
 * accurately reproduce the entity, provided that the instance was
 * made correctly. In some implementations, it may not be possible to get a
 * snapshot of entities correctly, so, for example, the NBT data for an entity
 * may be missing.</p>
 *
 * <p>This class identifies entities using its entity type string, although
 * this is not very efficient as the types are currently not interned. This
 * may be changed in the future.</p>
 */
public class BaseEntity implements NbtValued {

    private final EntityType type;
    @Nullable
    private LazyReference<CompoundBinaryTag> nbtData;

    /**
     * Create a new base entity.
     *
     * @param type the entity type
     * @param nbtData NBT data
     * @deprecated Use {@link BaseEntity#BaseEntity(EntityType, LazyReference)}
     */
    @Deprecated
    public BaseEntity(EntityType type, CompoundTag nbtData) {
        this(type);
        setNbtData(nbtData);
    }

    /**
     * Create a new base entity.
     *
     * @param type the entity type
     * @param nbtData NBT data
     */
    public BaseEntity(EntityType type, LazyReference<CompoundBinaryTag> nbtData) {
        this(type);
        setNbtReference(nbtData);
    }

    /**
     * Create a new base entity with no NBT data.
     *
     * @param type the entity type
     */
    public BaseEntity(EntityType type) {
        this.type = type;
    }

    /**
     * Make a clone of a {@link BaseEntity}.
     *
     * @param other the object to clone
     */
    public BaseEntity(BaseEntity other) {
        checkNotNull(other);
        this.type = other.getType();
        setNbtReference(other.getNbtReference());
    }

    @Nullable
    @Override
    public LazyReference<CompoundBinaryTag> getNbtReference() {
        return nbtData;
    }

    @Override
    public void setNbtReference(@Nullable LazyReference<CompoundBinaryTag> nbtData) {
        this.nbtData = nbtData;
    }

    /**
     * Get the type of entity.
     *
     * @return the entity type
     */
    public EntityType getType() {
        return this.type;
    }

}
