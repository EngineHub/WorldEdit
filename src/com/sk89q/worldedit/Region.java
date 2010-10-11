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

package com.sk89q.worldedit;

/**
 *
 * @author Albert
 */
public interface Region extends Iterable<Point> {
    /**
     * Get the lower point of a region.
     * 
     * @return
     */
    public Point getMinimumPoint();
    /**
     * Get the upper point of a region.
     * 
     * @return
     */
    public Point getMaximumPoint();
    /**
     * Get the number of blocks in the region.
     * 
     * @return
     */
    public int getSize();
    /**
     * Get X-size.
     *
     * @return
     */
    public int getWidth();
    /**
     * Get Y-size.
     *
     * @return
     */
    public int getHeight();
    /**
     * Get Z-size.
     *
     * @return
     */
    public int getLength();
}
