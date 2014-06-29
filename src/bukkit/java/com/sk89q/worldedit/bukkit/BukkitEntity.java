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

package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.Tameable;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to adapt a Bukkit entity into a WorldEdit one.
 */
class BukkitEntity implements Entity {

    private final org.bukkit.entity.Entity entity;

    /**
     * Create a new instance.
     *
     * @param entity the entity
     */
    BukkitEntity(org.bukkit.entity.Entity entity) {
        checkNotNull(entity);
        this.entity = entity;
    }

    /**
     * Get the underlying Bukkit entity.
     *
     * @return the Bukkit entity
     */
    protected org.bukkit.entity.Entity getEntity() {
        return entity;
    }

    @SuppressWarnings("unchecked")
    <T> T getMetaData(Class<T> metaDataClass) {
        if (metaDataClass == Tameable.class && getEntity() instanceof org.bukkit.entity.Tameable) {
            return (T) new TameableAdapter((org.bukkit.entity.Tameable) getEntity());
        } else {
            return null;
        }
    }

    @Override
    public Extent getExtent() {
        return BukkitAdapter.adapt(getEntity().getWorld());
    }

    @Override
    public Location getLocation() {
        return BukkitAdapter.adapt(getEntity().getLocation());
    }

    @Override
    public BaseEntity getState() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean remove() {
        return false;
    }

}
