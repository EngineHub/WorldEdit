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

package com.sk89q.worldedit.world.storage;

import com.sk89q.jnbt.ListTag;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility methods for working with NBT data used in Minecraft.
 */
public final class NBTConversions {

    private NBTConversions() {
    }

    /**
     * Read a {@code Location} from two list tags, the first of which contains
     * three numbers for the X, Y, and Z components, and the second of
     * which contains two numbers, the yaw and pitch in degrees.
     *
     * <p>For values that are unavailable, their values will be 0.</p>
     *
     * @param extent the extent
     * @param positionTag the position tag
     * @param directionTag the direction tag
     * @return a location
     */
    public static Location toLocation(Extent extent, ListTag positionTag, ListTag directionTag) {
        checkNotNull(extent);
        checkNotNull(positionTag);
        checkNotNull(directionTag);
        return new Location(
                extent,
                positionTag.asDouble(0), positionTag.asDouble(1), positionTag.asDouble(2),
                (float) directionTag.asDouble(0), (float) directionTag.asDouble(1));
    }

}
