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

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.world.World;

import java.util.*;

/**
 * A region that contains no points.
 */
public class NullRegion implements Region {

    private World world;

    @Override
    public Vector getMinimumPoint() {
        return new Vector(0, 0, 0);
    }

    @Override
    public Vector getMaximumPoint() {
        return new Vector(0, 0, 0);
    }

    @Override
    public Vector getCenter() {
        return new Vector(0, 0, 0);
    }

    @Override
    public int getArea() {
        return 0;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public void expand(Vector... changes) throws RegionOperationException {
        throw new RegionOperationException("Cannot change NullRegion");
    }

    @Override
    public void contract(Vector... changes) throws RegionOperationException {
        throw new RegionOperationException("Cannot change NullRegion");
    }

    @Override
    public void shift(Vector change) throws RegionOperationException {
        throw new RegionOperationException("Cannot change NullRegion");
    }

    @Override
    public boolean contains(Vector position) {
        return false;
    }

    @Override
    public Set<Vector2D> getChunks() {
        return Collections.emptySet();
    }

    @Override
    public Set<Vector> getChunkCubes() {
        return Collections.emptySet();
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public NullRegion clone() {
        return new NullRegion();
    }

    @Override
    public List<BlockVector2D> polygonize(int maxPoints) {
        return Collections.emptyList();
    }

    @Override
    public Iterator<BlockVector> iterator() {
        return new Iterator<BlockVector>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public BlockVector next() {
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Cannot remove");
            }
        };
    }

}
