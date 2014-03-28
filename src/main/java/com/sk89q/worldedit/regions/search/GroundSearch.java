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

/**
 * Given a column of blocks in the world, finds the first ground block
 * starting from a given Y.
 */
public interface GroundSearch {

    /**
     * Find the ground block starting from the given position and traversing
     * downward until reaching minY (inclusive).
     * </p>
     * The highest ground block that may be returned is at the location of
     * the origin location. The lowest ground block that may be returned is
     * in the same column as the origin but with y = minY.
     * </p>
     * It is possible for no ground block to be found if the given origin
     * block is underground.
     *
     * @param origin the origin
     * @param minY the minimum Y to end the search at
     * @return the location of a ground block, or null if none was found
     */
    Vector findGround(Vector origin, int minY);

}
