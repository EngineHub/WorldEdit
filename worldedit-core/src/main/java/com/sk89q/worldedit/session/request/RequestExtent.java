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

package com.sk89q.worldedit.session.request;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import javax.annotation.Nullable;
import java.util.List;

public class RequestExtent implements Extent {

    private Request request;

    protected Extent getExtent() {
        if (request == null || !request.isValid()) {
            request = Request.request();
        }
        return request.getEditSession();
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        return getExtent().getMinimumPoint();
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return getExtent().getMaximumPoint();
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return getExtent().getEntities(region);
    }

    @Override
    public List<? extends Entity> getEntities() {
        return getExtent().getEntities();
    }

    @Override
    @Nullable
    public Entity createEntity(Location location, BaseEntity entity) {
        return getExtent().createEntity(location, entity);
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        return getExtent().getBlock(position);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        return getExtent().getFullBlock(position);
    }

    @Override
    public BiomeType getBiome(BlockVector2 position) {
        return getExtent().getBiome(position);
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
        return getExtent().setBlock(position, block);
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        return getExtent().setBiome(position, biome);
    }

    @Override
    @Nullable
    public Operation commit() {
        Operation commit = getExtent().commit();
        request = null;
        return commit;
    }
}
