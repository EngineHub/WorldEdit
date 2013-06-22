// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com> and contributors
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

import java.util.Iterator;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector2D;

/**
 * A plane region that is parallel to the XZ plane, or at least has been projected
 * onto the XZ plane.
 */
public interface FlatRegion extends Region {

    /**
     * Get the minimum Y coordinate.
     * 
     * @return the minimum Y coordinate
     */
    public int getMinimumY();

    /**
     * Get the maximum Y coordinate.
     * 
     * @return the maximum Y coordinate
     */
    public int getMaximumY();

    /**
     * Get an {@link Iterable} over all the points in the XZ plane.
     * 
     * @return the iterator
     */
    public Iterable<Vector2D> asFlatRegion();

    /**
     * Get an iterator over all the points in the XZ plane.
     * 
     * @return the iterator
     */
    public Iterator<Vector2D> flatIterator();
    
    /**
     * Get an iterator over every column over the XZ plane, represented as an iterator
     * of the top most points in the region.
     * 
     * @return iterator of the top most inside the region
     */
    public Iterator<BlockVector> columnIterator();
}
