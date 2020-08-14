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

package com.sk89q.worldedit.session.request;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.NullRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.world.World;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A region that mirrors the current selection according to the current
 * {@link LocalSession} and {@link World} set on the current
 * {@link Request}.
 *
 * <p>If a selection cannot be taken, then the selection will be assumed to be
 * that of a {@link NullRegion}.</p>
 */
public class RequestSelection implements Region {

    /**
     * Get the delegate region.
     *
     * @return the delegate region
     */
    protected Region getRegion() {
        LocalSession session = Request.request().getSession();
        World world = Request.request().getWorld();

        if (session != null && world != null) {
            try {
                return session.getSelection(world);
            } catch (IncompleteRegionException ignored) {
            }
        }

        return new NullRegion();
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        return getRegion().getMinimumPoint();
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return getRegion().getMaximumPoint();
    }

    @Override
    public Vector3 getCenter() {
        return getRegion().getCenter();
    }

    @Override
    public long getVolume() {
        return getRegion().getVolume();
    }

    @Override
    public int getWidth() {
        return getRegion().getWidth();
    }

    @Override
    public int getHeight() {
        return getRegion().getHeight();
    }

    @Override
    public int getLength() {
        return getRegion().getLength();
    }

    @Override
    public void expand(BlockVector3... changes) throws RegionOperationException {
        getRegion().expand(changes);
    }

    @Override
    public void contract(BlockVector3... changes) throws RegionOperationException {
        getRegion().contract(changes);
    }

    @Override
    public void shift(BlockVector3 change) throws RegionOperationException {
        getRegion().shift(change);
    }

    @Override
    public boolean contains(BlockVector3 position) {
        return getRegion().contains(position);
    }

    @Override
    public Set<BlockVector2> getChunks() {
        return getRegion().getChunks();
    }

    @Override
    public Set<BlockVector3> getChunkCubes() {
        return getRegion().getChunkCubes();
    }

    @Override
    public World getWorld() {
        return getRegion().getWorld();
    }

    @Override
    public void setWorld(World world) {
        getRegion().setWorld(world);
    }

    @Override
    public Region clone() {
        return this;
    }

    @Override
    public List<BlockVector2> polygonize(int maxPoints) {
        return getRegion().polygonize(maxPoints);
    }

    @Override
    public Iterator<BlockVector3> iterator() {
        return getRegion().iterator();
    }

}
