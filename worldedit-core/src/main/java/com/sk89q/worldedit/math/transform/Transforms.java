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

package com.sk89q.worldedit.math.transform;

import com.sk89q.worldedit.util.Location;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Various utility methods related to {@link Transform}s.
 */
public final class Transforms {

    private Transforms() {
    }

    /**
     * Transform a location's position with a given transform.
     *
     * <p>Direction is unaffected.</p>
     *
     * @param location the location
     * @param transform the transform
     * @return the transformed location
     */
    public static Location transform(Location location, Transform transform) {
        checkNotNull(location);
        checkNotNull(transform);
        return new Location(location.getExtent(), transform.apply(location.toVector()), location.getDirection());
    }

}
