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

package com.sk89q.worldedit.spout.selections;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.RegionSelector;
import org.spout.api.geo.World;
import org.spout.api.geo.discrete.Point;

public interface Selection {
    /**
     * Get the lower point of a region.
     *
     * @return min. point
     */
    public Point getMinimumPoint();

    /**
     * Get the lower point of a region.
     *
     * @return min. point
     */
    public Vector getNativeMinimumPoint();

    /**
     * Get the upper point of a region.
     *
     * @return max. point
     */
    public Point getMaximumPoint();

    /**
     * Get the upper point of a region.
     *
     * @return max. point
     */
    public Vector getNativeMaximumPoint();

    /**
     * Get the region selector. This is for internal use.
     *
     * @return
     */
    public RegionSelector getRegionSelector();

    /**
     * Get the world.
     *
     * @return
     */
    public World getWorld();

    /**
     * Get the number of blocks in the region.
     *
     * @return number of blocks
     */
    public int getArea();

    /**
     * Get X-size.
     *
     * @return width
     */
    public int getWidth();

    /**
     * Get Y-size.
     *
     * @return height
     */
    public int getHeight();

    /**
     * Get Z-size.
     *
     * @return length
     */
    public int getLength();

    /**
     * Returns true based on whether the region contains the point,
     *
     * @param pt
     * @return
     */
    public boolean contains(Point pt);
}
