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

package com.sk89q.worldedit.operation;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.regions.search.GroundSearch;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Accepts 2D coordinates to columns, finds the first ground block in each
 * column, and applies the given {@link RegionFunction} onto the ground blocks.
 */
public class GroundFunction implements FlatRegionFunction {

    private final RegionFunction function;
    private GroundSearch groundSearch;
    private int minY;
    private int maxY;

    /**
     * Create a new instance.
     *
     * @param groundSearch the ground search implementation
     * @param minY the minimum Y (inclusive)
     * @param maxY the maximum Y (inclusive)
     * @param function the function to apply on ground blocks
     */
    public GroundFunction(GroundSearch groundSearch, int minY, int maxY, RegionFunction function) {
        checkNotNull(function);
        checkNotNull(groundSearch);
        checkArgument(minY <= maxY, "minY <= maxY required");
        this.function = function;
        this.groundSearch = groundSearch;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public boolean apply(Vector2D pt) throws WorldEditException {
        Vector ground = groundSearch.findGround(pt.toVector(maxY), minY);
        return ground != null && function.apply(ground);
    }
}
