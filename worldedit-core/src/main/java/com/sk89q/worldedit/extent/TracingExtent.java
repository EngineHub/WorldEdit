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

package com.sk89q.worldedit.extent;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * An extent that can report back if an operation fails due to the extent(s) below it.
 *
 * <em>Internal use only.</em>
 */
public class TracingExtent extends AbstractDelegateExtent {

    public enum Action {
        SET_BLOCK,
        SET_BIOME,
        CREATE_ENTITY,
    }

    private final Set<BlockVector3> touchedLocations = Collections.newSetFromMap(BlockMap.create());
    private final SetMultimap<BlockVector3, Action> failedActions = Multimaps.newSetMultimap(
            BlockMap.create(), () -> EnumSet.noneOf(Action.class)
    );

    /**
     * Create a new instance.
     *
     * @param extent the extent
     */
    public TracingExtent(Extent extent) {
        super(extent);
    }

    public boolean isActive() {
        return !touchedLocations.isEmpty();
    }

    public Set<BlockVector3> getTouchedLocations() {
        return Collections.unmodifiableSet(touchedLocations);
    }

    public SetMultimap<BlockVector3, Action> getFailedActions() {
        return Multimaps.unmodifiableSetMultimap(failedActions);
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
        touchedLocations.add(location);
        boolean result = super.setBlock(location, block);
        if (!result) {
            failedActions.put(location, Action.SET_BLOCK);
        }
        return result;
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        touchedLocations.add(position);
        boolean result = super.setBiome(position, biome);
        if (!result) {
            failedActions.put(position, Action.SET_BIOME);
        }
        return result;
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        BlockVector3 blockVector3 = location.toVector().toBlockPoint();
        touchedLocations.add(blockVector3);
        Entity result = super.createEntity(location, entity);
        if (result == null) {
            failedActions.put(blockVector3, Action.CREATE_ENTITY);
        }
        return result;
    }

    @Override
    public String toString() {
        return "TracingExtent{delegate=" + getExtent() + "}";
    }
}
