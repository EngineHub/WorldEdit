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

import java.util.Iterator;

/**
 *
 * @author Albert
 */
public class CuboidRegion implements Region {
    /**
     * Store the first point.
     */
    private Point pos1;
    /**
     * Store the second point.
     */
    private Point pos2;

    /**
     * Construct a new instance of this cuboid region.
     * 
     * @param pos1
     * @param pos2
     */
    public CuboidRegion(Point pos1, Point pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    /**
     * Get the lower point of the cuboid.
     *
     * @return min point
     */
    @Override
    public Point getMinimumPoint() {
        return new Point(Math.min(pos1.getX(), pos2.getX()),
                         Math.min(pos1.getY(), pos2.getY()),
                         Math.min(pos1.getZ(), pos2.getZ()));
    }

    /**
     * Get the upper point of the cuboid.
     *
     * @return max point
     */
    @Override
    public Point getMaximumPoint() {
        return new Point(Math.max(pos1.getX(), pos2.getX()),
                         Math.max(pos1.getY(), pos2.getY()),
                         Math.max(pos1.getZ(), pos2.getZ()));
    }

    /**
     * Get the number of blocks in the region.
     * 
     * @return number of blocks
     */
    public int getSize() {
        Point min = getMinimumPoint();
        Point max = getMaximumPoint();

        return (int)((max.getX() - min.getX() + 1) *
                     (max.getY() - min.getY() + 1) *
                     (max.getZ() - min.getZ() + 1));
    }

    /**
     * Get X-size.
     *
     * @return width
     */
    public int getWidth() {
        Point min = getMinimumPoint();
        Point max = getMaximumPoint();

        return (int)(max.getX() - min.getX() + 1);
    }

    /**
     * Get Y-size.
     *
     * @return height
     */
    public int getHeight() {
        Point min = getMinimumPoint();
        Point max = getMaximumPoint();

        return (int)(max.getY() - min.getY() + 1);
    }

    /**
     * Get Z-size.
     *
     * @return length
     */
    public int getLength() {
        Point min = getMinimumPoint();
        Point max = getMaximumPoint();

        return (int)(max.getZ() - min.getZ() + 1);
    }

    /**
     * Get the iterator.
     * 
     * @return iterator of Points
     */
    public Iterator<Point> iterator() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
