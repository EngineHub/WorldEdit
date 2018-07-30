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

package com.sk89q.worldedit.extent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;

import java.util.List;

import javax.annotation.Nullable;

/**
 * A world, portion of a world, clipboard, or other object that can have blocks
 * set or entities placed.
 *
 * @see InputExtent the get____() portion
 * @see OutputExtent the set____() portion
 */
public interface Extent extends InputExtent, OutputExtent {

    /**
     * Get the minimum point in the extent.
     *
     * <p>If the extent is unbounded, then a large (negative) value may
     * be returned.</p>
     *
     * @return the minimum point
     */
    Vector getMinimumPoint();

    /**
     * Get the maximum point in the extent.
     *
     * <p>If the extent is unbounded, then a large (positive) value may
     * be returned.</p>
     *
     * @return the maximum point
     */
    Vector getMaximumPoint();

    /**
     * Get a list of all entities within the given region.
     *
     * <p>If the extent is not wholly loaded (i.e. a world being simulated in the
     * game will not have every chunk loaded), then this list may not be
     * incomplete.</p>
     *
     * @param region the region in which entities must be contained
     * @return a list of entities
     */
    List<? extends Entity> getEntities(Region region);

    /**
     * Get a list of all entities.
     *
     * <p>If the extent is not wholly loaded (i.e. a world being simulated in the
     * game will not have every chunk loaded), then this list may not be
     * incomplete.</p>
     *
     * @return a list of entities
     */
    List<? extends Entity> getEntities();

    /**
     * Create an entity at the given location.
     *
     * @param entity the entity
     * @param location the location
     * @return a reference to the created entity, or null if the entity could not be created
     */
    @Nullable Entity createEntity(Location location, BaseEntity entity);

}
