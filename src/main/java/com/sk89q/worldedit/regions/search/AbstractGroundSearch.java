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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.regions.search;

import com.sk89q.worldedit.Vector;

import static com.google.common.base.Preconditions.checkArgument;
import static net.minecraft.util.com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for finding the first ground block starting from a certain
 * position and traversing down.
 */
public abstract class AbstractGroundSearch implements GroundSearch {

    /**
     * Returns whether the given block should be "passed through" when
     * conducting the ground search.
     *
     * @param position return whether the given block is the ground
     * @return true if the search should stop
     */
    protected abstract boolean isGround(Vector position);

    /**
     * Find the ground block starting from the given position and traversing
     * downward until reaching minY (inclusive).
     * </p>
     * The highest ground block that may be returned is at the location of
     * the origin location. The lowest ground block that may be returned is
     * in the same column as the origin but with y = minY.
     * </p>
     * It is possible for no ground block to be found if the given origin
     * block is underground to begin with.
     *
     * @param origin the origin
     * @param minY the minimum Y to end the search at
     * @return the location of a ground block, or null if none was found
     */
    public Vector findGround(Vector origin, int minY) {
        checkNotNull(origin);
        checkArgument(minY <= origin.getBlockY(), "minY <= origin Y");

        // Don't want to be in the ground
        if (isGround(origin.add(0, 1, 0))) {
            return null;
        }

        for (int y = origin.getBlockY() + 1; y >= minY; --y) {
            Vector test = origin.setY(y);
            if (isGround(test)) {
                return test;
            }
        }

        return null;
    }

}
