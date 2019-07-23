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

package com.sk89q.worldedit.extension.platform;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;

public interface Locatable {

    /**
     * Get the location of this actor.
     *
     * @return the location of the actor
     */
    Location getLocation();

    /**
     * Get the location of this actor in block coordinates.
     *
     * @return the block location of the actor
     */
    default Location getBlockLocation() {
        Location location = getLocation();
        return location.setPosition(location.toVector().floor());
    }

    /**
     * Sets the location of this actor.
     *
     * @param location the new location of the actor
     * @return if the teleport succeeded
     */
    boolean setLocation(Location location);

    /**
     * Sets the position of this actor.
     *
     * @param pos where to move them
     */
    default void setPosition(Vector3 pos) {
        setLocation(new Location(getExtent(), pos));
    }

    /**
     * Get the extent that this actor is in.
     *
     * @return the extent
     */
    Extent getExtent();

}
