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

import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.Tameable;
import com.sk89q.worldedit.internal.util.AbstractAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;

/**
 * An adapter to adapt a Bukkit entity into a WorldEdit one.
 */
class BukkitEntity extends AbstractAdapter<org.bukkit.entity.Entity> implements Entity {

    /**
     * Create a new instance.
     *
     * @param entity the entity
     */
    BukkitEntity(org.bukkit.entity.Entity entity) {
        super(entity);
    }

    @SuppressWarnings("unchecked")
    <T> T getMetaData(Class<T> metaDataClass) {
        if (metaDataClass == Tameable.class && getHandle() instanceof org.bukkit.entity.Tameable) {
            return (T) new TameableAdapter((org.bukkit.entity.Tameable) getHandle());
        } else {
            return null;
        }
    }

    @Override
    public World getWorld() {
        return BukkitAdapter.adapt(getHandle().getWorld());
    }

    @Override
    public Location getLocation() {
        return BukkitAdapter.adapt(getHandle().getLocation());
    }
}
