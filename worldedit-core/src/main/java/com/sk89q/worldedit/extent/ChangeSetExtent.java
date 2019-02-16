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

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.history.change.BiomeChange;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.EntityCreate;
import com.sk89q.worldedit.history.change.EntityRemove;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Stores changes to a {@link ChangeSet}.
 */
public class ChangeSetExtent extends AbstractDelegateExtent {

    private final ChangeSet changeSet;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     * @param changeSet the change set
     */
    public ChangeSetExtent(Extent extent, ChangeSet changeSet) {
        super(extent);
        checkNotNull(changeSet);
        this.changeSet = changeSet;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
        BaseBlock previous = getFullBlock(location);
        changeSet.add(new BlockChange(location, previous, block));
        return super.setBlock(location, block);
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        BiomeType previous = getBiome(position);
        changeSet.add(new BiomeChange(position, previous, biome));
        return super.setBiome(position, biome);
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity state) {
        Entity entity = super.createEntity(location, state);
        if (entity != null) {
            changeSet.add(new EntityCreate(location, state, entity));
        }
        return entity;
    }

    @Override
    public List<? extends Entity> getEntities() {
        return wrapEntities(super.getEntities());
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return wrapEntities(super.getEntities(region));
    }

    private List<? extends Entity> wrapEntities(List<? extends Entity> entities) {
        List<Entity> newList = new ArrayList<>(entities.size());
        for (Entity entity : entities) {
            newList.add(new TrackedEntity(entity));
        }
        return newList;
    }

    private class TrackedEntity implements Entity {
        private final Entity entity;

        private TrackedEntity(Entity entity) {
            this.entity = entity;
        }

        @Override
        public BaseEntity getState() {
            return entity.getState();
        }

        @Override
        public Location getLocation() {
            return entity.getLocation();
        }

        @Override
        public boolean setLocation(Location location) {
            // TODO Add a changeset for this.
            return entity.setLocation(location);
        }

        @Override
        public Extent getExtent() {
            return entity.getExtent();
        }

        @Override
        public boolean remove() {
            Location location = entity.getLocation();
            BaseEntity state = entity.getState();
            boolean success = entity.remove();
            if (state != null && success) {
                changeSet.add(new EntityRemove(location, state));
            }
            return success;
        }

        @Nullable
        @Override
        public <T> T getFacet(Class<? extends T> cls) {
            return entity.getFacet(cls);
        }
    }
}
