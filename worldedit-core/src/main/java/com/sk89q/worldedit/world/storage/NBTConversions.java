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

package com.sk89q.worldedit.world.storage;

import com.sk89q.jnbt.ListTag;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinListTag;

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
     * @deprecated Use {@link #toLocation(Extent, LinListTag, LinListTag)} instead.
     */
    @Deprecated
    public static Location toLocation(Extent extent, ListTag positionTag, ListTag directionTag) {
        checkNotNull(extent);
        checkNotNull(positionTag);
        checkNotNull(directionTag);
        return new Location(
            extent,
            positionTag.asDouble(0), positionTag.asDouble(1), positionTag.asDouble(2),
            directionTag.getFloat(0), directionTag.getFloat(1));
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
     * @param rotationTag the rotation tag
     * @return a location
     */
    public static Location toLocation(Extent extent, LinListTag<LinDoubleTag> positionTag, LinListTag<LinFloatTag> rotationTag) {
        int posTagSize = positionTag.value().size();
        int rotTagSize = rotationTag.value().size();
        return new Location(
            extent,
            posTagSize > 0 ? positionTag.get(0).valueAsDouble() : 0,
            posTagSize > 1 ? positionTag.get(1).valueAsDouble() : 0,
            posTagSize > 2 ? positionTag.get(2).valueAsDouble() : 0,
            rotTagSize > 0 ? rotationTag.get(0).valueAsFloat() : 0,
            rotTagSize > 1 ? rotationTag.get(1).valueAsFloat() : 0
        );
    }

}
