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

import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;

/**
 * Utility class to apply region functions to {@link com.sk89q.worldedit.regions.Region}.
 */
public class FlatRegionApplicator implements Operation {

    private final FlatRegion flatRegion;
    private final FlatRegionFunction function;
    private int affected = 0;

    public FlatRegionApplicator(Region region, FlatRegionFunction function) {
        this.function = function;

        if (region instanceof FlatRegion) {
            flatRegion = (FlatRegion) region;
        } else {
            flatRegion = CuboidRegion.makeCuboid(region);
        }
    }

    /**
     * Get the number of affected objects.
     *
     * @return the number of affected
     */
    public int getAffected() {
        return affected;
    }

    @Override
    public Operation resume() throws WorldEditException {
        for (Vector2D pt : flatRegion.asFlatRegion()) {
            if (function.apply(pt)) {
                affected++;
            }
        }

        return null;
    }

    @Override
    public void cancel() {
    }

}

