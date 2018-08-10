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

package com.sk89q.worldedit.extent.clipboard;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Stores block data as a multi-dimensional array of {@link BaseBlock}s and
 * other data as lists or maps.
 */
public class BlockArrayClipboard implements Clipboard {

    private final Region region;
    private Vector origin;
    private final BlockStateHolder[][][] blocks;
    private final List<ClipboardEntity> entities = new ArrayList<>();

    /**
     * Create a new instance.
     *
     * <p>The origin will be placed at the region's lowest minimum point.</p>
     *
     * @param region the bounding region
     */
    public BlockArrayClipboard(Region region) {
        checkNotNull(region);
        this.region = region.clone();
        this.origin = region.getMinimumPoint();

        Vector dimensions = getDimensions();
        blocks = new BlockStateHolder[dimensions.getBlockX()][dimensions.getBlockY()][dimensions.getBlockZ()];
    }

    @Override
    public Region getRegion() {
        return region.clone();
    }

    @Override
    public Vector getOrigin() {
        return origin;
    }

    @Override
    public void setOrigin(Vector origin) {
        this.origin = origin;
    }

    @Override
    public Vector getDimensions() {
        return region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1);
    }

    @Override
    public Vector getMinimumPoint() {
        return region.getMinimumPoint();
    }

    @Override
    public Vector getMaximumPoint() {
        return region.getMaximumPoint();
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        List<Entity> filtered = new ArrayList<>();
        for (Entity entity : entities) {
            if (region.contains(entity.getLocation().toVector())) {
                filtered.add(entity);
            }
        }
        return Collections.unmodifiableList(filtered);
    }

    @Override
    public List<? extends Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        ClipboardEntity ret = new ClipboardEntity(location, entity);
        entities.add(ret);
        return ret;
    }

    @Override
    public BlockState getBlock(Vector position) {
        if (region.contains(position)) {
            Vector v = position.subtract(region.getMinimumPoint());
            BlockStateHolder block = blocks[v.getBlockX()][v.getBlockY()][v.getBlockZ()];
            if (block != null) {
                return block.toImmutableState();
            }
        }

        return BlockTypes.AIR.getDefaultState();
    }

    @Override
    public BaseBlock getFullBlock(Vector position) {
        if (region.contains(position)) {
            Vector v = position.subtract(region.getMinimumPoint());
            BlockStateHolder block = blocks[v.getBlockX()][v.getBlockY()][v.getBlockZ()];
            if (block != null) {
                return block.toBaseBlock();
            }
        }

        return BlockTypes.AIR.getDefaultState().toBaseBlock();
    }

    @Override
    public boolean setBlock(Vector position, BlockStateHolder block) throws WorldEditException {
        if (region.contains(position)) {
            Vector v = position.subtract(region.getMinimumPoint());
            blocks[v.getBlockX()][v.getBlockY()][v.getBlockZ()] = block;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public BaseBiome getBiome(Vector2D position) {
        return new BaseBiome(0);
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        return false;
    }

    @Nullable
    @Override
    public Operation commit() {
        return null;
    }

    /**
     * Stores entity data.
     */
    private class ClipboardEntity extends StoredEntity {
        ClipboardEntity(Location location, BaseEntity entity) {
            super(location, entity);
        }

        @Override
        public boolean remove() {
            return entities.remove(this);
        }

        @Nullable
        @Override
        public <T> T getFacet(Class<? extends T> cls) {
            return null;
        }
    }

}
