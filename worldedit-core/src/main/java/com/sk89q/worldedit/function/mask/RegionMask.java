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

package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A mask that tests whether given positions are contained within a region.
 */
public class RegionMask extends AbstractMask {

    private Region region;

    /**
     * Create a new region mask.
     *
     * @param region the region
     */
    public RegionMask(Region region) {
        setRegion(region);
    }

    /**
     * Get the region.
     *
     * @return the region
     */
    public Region getRegion() {
        return region;
    }

    /**
     * Set the region that positions must be contained within.
     *
     * @param region the region
     */
    public void setRegion(Region region) {
        checkNotNull(region);
        this.region = region;
    }

    @Override
    public boolean test(BlockVector3 vector) {
        return region.contains(vector);
    }

    @Nullable
    @Override
    public Mask2D toMask2D() {
        return null;
    }

}
