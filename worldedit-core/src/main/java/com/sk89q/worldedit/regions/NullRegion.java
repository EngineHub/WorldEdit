/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A region that contains no points.
 */
public class NullRegion implements Region {

    private World world;

    @Override
    public BlockVector3 getMinimumPoint() {
        return BlockVector3.ZERO;
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return BlockVector3.ZERO;
    }

    @Override
    public Vector3 getCenter() {
        return Vector3.ZERO;
    }

    @Override
    public long getVolume() {
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
    public void expand(BlockVector3... changes) throws RegionOperationException {
        throw new RegionOperationException(TranslatableComponent.of("worldedit.selection.null.error.immutable"));
    }

    @Override
    public void contract(BlockVector3... changes) throws RegionOperationException {
        throw new RegionOperationException(TranslatableComponent.of("worldedit.selection.null.error.immutable"));
    }

    @Override
    public void shift(BlockVector3 change) throws RegionOperationException {
        throw new RegionOperationException(TranslatableComponent.of("worldedit.selection.null.error.immutable"));
    }

    @Override
    public boolean contains(BlockVector3 position) {
        return false;
    }

    @Override
    public Set<BlockVector2> getChunks() {
        return Collections.emptySet();
    }

    @Override
    public Set<BlockVector3> getChunkCubes() {
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
    public List<BlockVector2> polygonize(int maxPoints) {
        return Collections.emptyList();
    }

    @Override
    public Iterator<BlockVector3> iterator() {
        return new Iterator<BlockVector3>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public BlockVector3 next() {
                throw new NoSuchElementException();
            }
        };
    }

}
