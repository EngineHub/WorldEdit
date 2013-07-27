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
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;

/**
 * An abstract implementation of the world.
 */
public abstract class AbstractRegion implements Region {
    
    protected LocalWorld world;

    public AbstractRegion(LocalWorld world) {
        this.world = world;
    }

    @Override
    public Vector getCenter() {
        return getMinimumPoint().add(getMaximumPoint()).divide(2);
    }
    
    @Override
    public Iterator<BlockVector> iterator() {
        return new RegionIterator(this);
    }

    @Override
    public LocalWorld getWorld() {
        return world;
    }

    @Override
    public void setWorld(LocalWorld world) {
        this.world = world;
    }

    @Override
    public void shift(Vector change) throws RegionOperationException {
        expand(change);
        contract(change);
    }

    @Override
    public AbstractRegion clone() {
        try {
            return (AbstractRegion) super.clone();
        } catch (CloneNotSupportedException exc) {
            return null;
        }
    }
}
