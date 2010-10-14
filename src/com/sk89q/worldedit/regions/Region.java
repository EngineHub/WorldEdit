// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import com.sk89q.worldedit.Vector;

/**
 *
 * @author Albert
 */
public interface Region extends Iterable<Vector> {
    /**
     * Get the lower point of a region.
     * 
     * @return min. point
     */
    public Vector getMinimumPoint();
    /**
     * Get the upper point of a region.
     * 
     * @return max. point
     */
    public Vector getMaximumPoint();
    /**
     * Get the number of blocks in the region.
     * 
     * @return number of blocks
     */
    public int getSize();
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
     * Expand the region.
     *
     * @param change
     */
    public void expand(Vector change);
    /**
     * Contract the region.
     *
     * @param change
     */
    public void contract(Vector change);
}
