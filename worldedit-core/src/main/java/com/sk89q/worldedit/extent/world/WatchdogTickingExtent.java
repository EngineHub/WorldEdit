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

package com.sk89q.worldedit.extent.world;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Watchdog;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import javax.annotation.Nullable;

/**
 * Extent that ticks the watchdog before every world-affecting action.
 */
public class WatchdogTickingExtent extends AbstractDelegateExtent {

    private final Watchdog watchdog;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     * @param watchdog the watchdog to reset
     */
    public WatchdogTickingExtent(Extent extent, Watchdog watchdog) {
        super(extent);
        this.watchdog = watchdog;
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
        watchdog.tick();
        return super.setBlock(location, block);
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        watchdog.tick();
        return super.createEntity(location, entity);
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        watchdog.tick();
        return super.setBiome(position, biome);
    }
}
