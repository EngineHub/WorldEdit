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

package com.sk89q.worldedit.extent;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.OperationQueue;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.List;

import javax.annotation.Nullable;

/**
 * A base class for {@link Extent}s that merely passes extents onto another.
 */
public abstract class AbstractDelegateExtent implements Extent {

    private final Extent extent;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     */
    protected AbstractDelegateExtent(Extent extent) {
        checkNotNull(extent);
        this.extent = extent;
    }

    /**
     * Get the extent.
     *
     * @return the extent
     */
    public Extent getExtent() {
        return extent;
    }

    @Override
    public BlockState getBlock(Vector position) {
        return extent.getBlock(position);
    }

    @Override
    public BaseBlock getFullBlock(Vector position) {
        return extent.getFullBlock(position);
    }

    @Override
    public boolean setBlock(Vector location, BlockStateHolder block) throws WorldEditException {
        return extent.setBlock(location, block);
    }

    @Override
    @Nullable
    public Entity createEntity(Location location, BaseEntity entity) {
        return extent.createEntity(location, entity);
    }

    @Override
    public List<? extends Entity> getEntities() {
        return extent.getEntities();
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return extent.getEntities(region);
    }

    @Override
    public BaseBiome getBiome(Vector2D position) {
        return extent.getBiome(position);
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        return extent.setBiome(position, biome);
    }

    @Override
    public Vector getMinimumPoint() {
        return extent.getMinimumPoint();
    }

    @Override
    public Vector getMaximumPoint() {
        return extent.getMaximumPoint();
    }

    protected Operation commitBefore() {
        return null;
    }

    @Override
    public final @Nullable Operation commit() {
        Operation ours = commitBefore();
        Operation other = extent.commit();
        if (ours != null && other != null) {
            return new OperationQueue(ours, other);
        } else if (ours != null) {
            return ours;
        } else if (other != null) {
            return other;
        } else {
            return null;
        }
    }

}
