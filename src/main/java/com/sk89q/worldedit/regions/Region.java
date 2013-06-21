// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit.regions;

import java.util.Set;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

/**
 * Represents a 3-dimensional region that has a given area of 0 or greater.
 */
public interface Region extends Iterable<BlockVector>, Cloneable {
    
    /**
     * Get the lower point of a region. The minimum point as the smallest X, Y, and Z
     * coordinates of the region when the region is represented as a bounding box
     * aligned with the world that perfect contains this region with no space between
     * the boundaries of the bounding box and the farthest out points of the region.
     *
     * @return minimum point
     */
    public Vector getMinimumPoint();

    /**
     * Get the upper point of a region. The minimum point as the smallest X, Y, and Z
     * coordinates of the region when the region is represented as a bounding box
     * aligned with the world that perfect contains this region with no space between
     * the boundaries of the bounding box and the farthest out points of the region.
     *
     * @return maximum point
     */
    public Vector getMaximumPoint();

    /**
     * Get the center point of a region. Coordinates may not consist of only integers.
     *
     * @return center point
     */
    public Vector getCenter();

    /**
     * Get the number of blocks contained fully by the region. This number must
     * coincide with {@link #contains(Vector)}.
     *
     * @return number of blocks
     */
    public int getArea();

    /**
     * Get the width of the region (X-axis), as measured from the lowest and highest
     * boundaries of the region.
     *
     * @return width
     */
    public int getWidth();

    /**
     * Get the width of the region (Y-axis), as measured from the lowest and highest
     * boundaries of the region.
     *
     * @return height
     */
    public int getHeight();

    /**
     * Get the width of the region (Z-axis), as measured from the lowest and highest
     * boundaries of the region.
     *
     * @return length
     */
    public int getLength();

    /**
     * Expand the region.
     *
     * @param changes array/arguments with multiple related changes
     * @throws RegionOperationException
     */
    public void expand(Vector... changes) throws RegionOperationException;

    /**
     * Contract the region.
     *
     * @param changes array/arguments with multiple related changes
     * @throws RegionOperationException
     */
    public void contract(Vector... changes) throws RegionOperationException;

    /**
     * Shift the region.
     *
     * @param change the change
     * @throws RegionOperationException
     */
    public void shift(Vector change) throws RegionOperationException;

    /**
     * Returns true based on whether the region contains the point.
     *
     * @param position position to check
     * @return true if the region contains the position
     */
    public boolean contains(Vector position);

    /**
     * Get a list of chunks that intersect with this region, partially or fully.
     *
     * @return set of 2-dimension vectors specifying the chunks
     */
    public Set<Vector2D> getChunks();

    /**
     * Return a list of 16*16*16 chunks in a region.
     *
     * @return the chunk cubes this region overlaps with
     */
    public Set<Vector> getChunkCubes();

    /**
     * Get the world the selection is in.
     *
     * @return the world
     */
    public LocalWorld getWorld();

    /**
     * Sets the world the selection is in
     *
     * @param world world to set
     */
    public void setWorld(LocalWorld world);

    /**
     * Make a copy of the region.
     * 
     * @return a new region
     */
    public Region clone();
}
