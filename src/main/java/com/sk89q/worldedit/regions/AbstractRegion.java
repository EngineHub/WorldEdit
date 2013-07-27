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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.data.ChunkStore;

public abstract class AbstractRegion implements Region {
    /**
     * Stores the world.
     */
    protected LocalWorld world;

    public AbstractRegion(LocalWorld world) {
        this.world = world;
    }

    @Override
    public Vector getCenter() {
        return getMinimumPoint().add(getMaximumPoint()).divide(2);
    }

    /**
     * Get the iterator.
     *
     * @return iterator of points inside the region
     */
    public Iterator<BlockVector> iterator() {
        return new RegionIterator(this);
    }

    public LocalWorld getWorld() {
        return world;
    }

    public void setWorld(LocalWorld world) {
        this.world = world;
    }

    public void shift(Vector change) throws RegionOperationException {
        expand(change);
        contract(change);
    }

    public AbstractRegion clone() {
        try {
            return (AbstractRegion) super.clone();
        } catch (CloneNotSupportedException exc) {
            return null;
        }
    }

    @Override
    public List<BlockVector2D> polygonize(int maxPoints) {
        if (maxPoints >= 0 && maxPoints < 4) {
            throw new IllegalArgumentException("Cannot polygonize an AbstractRegion with no overridden polygonize method into less than 4 points.");
        }

        final BlockVector min = getMinimumPoint().toBlockVector();
        final BlockVector max = getMaximumPoint().toBlockVector();

        final List<BlockVector2D> points = new ArrayList<BlockVector2D>(4);

        points.add(new BlockVector2D(min.getX(), min.getZ()));
        points.add(new BlockVector2D(min.getX(), max.getZ()));
        points.add(new BlockVector2D(max.getX(), max.getZ()));
        points.add(new BlockVector2D(max.getX(), min.getZ()));

        return points;
    }
}
