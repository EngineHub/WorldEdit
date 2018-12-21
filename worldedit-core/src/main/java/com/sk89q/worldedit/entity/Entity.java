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

package com.sk89q.worldedit.entity;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Faceted;
import com.sk89q.worldedit.util.Location;

import javax.annotation.Nullable;

/**
 * A reference to an instance of an entity that exists in an {@link Extent}
 * and thus would have position and similar details.
 *
 * <p>This object cannot be directly cloned because it represents a particular
 * instance of an entity, but a {@link BaseEntity} can be created from
 * this entity by calling {@link #getState()}.</p>
 */
public interface Entity extends Faceted {

    /**
     * Get a copy of the entity's state.
     *
     * <p>In some cases, this method may return {@code null} if a snapshot
     * of the entity can't be created. It may not be possible, for example,
     * to get a snapshot of a player.</p>
     *
     * @return the entity's state or null if one cannot be created
     */
    @Nullable
    BaseEntity getState();

    /**
     * Get the location of this entity.
     *
     * @return the location of the entity
     */
    Location getLocation();

    /**
     * Sets the location of this entity.
     *
     * @param location the new location of the entity
     * @return if the teleport worked
     */
    boolean setLocation(Location location);

    /**
     * Get the extent that this entity is on.
     *
     * @return the extent
     */
    Extent getExtent();

    /**
     * Remove this entity from it container.
     *
     * @return true if removal was successful
     */
    boolean remove();

}
