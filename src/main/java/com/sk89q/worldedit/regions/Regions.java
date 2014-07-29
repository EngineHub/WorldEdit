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

package com.sk89q.worldedit.regions;

/**
 * Utility methods relating to {@link Region}s.
 */
public final class Regions {

    private Regions() {
    }

    /**
     * Get the minimum Y coordinate of the given region using the region's
     * {@link Region#getMinimumPoint()} method.
     *
     * @param region the region
     * @return the Y coordinate
     */
    public static double minimumY(Region region) {
        return region.getMinimumPoint().getY();
    }

    /**
     * Get the maximum Y coordinate of the given region using the region's
     * {@link Region#getMaximumPoint()} method.
     *
     * @param region the region
     * @return the Y coordinate
     */
    public static double maximumY(Region region) {
        return region.getMaximumPoint().getY();
    }

    /**
     * Get the minimum Y coordinate of the given region using the region's
     * {@link Region#getMinimumPoint()} method.
     *
     * @param region the region
     * @return the Y coordinate
     */
    public static int minimumBlockY(Region region) {
        return region.getMinimumPoint().getBlockY();
    }

    /**
     * Get the maximum Y coordinate of the given region using the region's
     * {@link Region#getMaximumPoint()} method.
     *
     * @param region the region
     * @return the Y coordinate
     */
    public static int maximumBlockY(Region region) {
        return region.getMaximumPoint().getBlockY();
    }

    /**
     * Attempt to get a {@link FlatRegion} from the given region.
     *
     * <p>If the given region is already a {@link FlatRegion}, then the region
     * will be cast and returned. Otherwise, a new {@link CuboidRegion} will
     * be created covers the provided region's minimum and maximum extents.</p>
     *
     * @param region the region
     * @return a flat region
     */
    public static FlatRegion asFlatRegion(Region region) {
        if (region instanceof FlatRegion) {
            return (FlatRegion) region;
        } else {
            return CuboidRegion.makeCuboid(region);
        }
    }

}
