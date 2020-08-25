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

package com.sk89q.worldedit.extent.world;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Watchdog;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.List;
import javax.annotation.Nullable;

/**
 * Extent that ticks the watchdog before every world-affecting action.
 */
public class WatchdogTickingExtent extends AbstractDelegateExtent {

    // Number of operations we run per tick to the watchdog
    private static final int OPS_PER_TICK = 100;

    private final Watchdog watchdog;
    private boolean enabled;
    private int ops;

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private void onOperation() {
        if (enabled) {
            ops++;
            if (ops == OPS_PER_TICK) {
                watchdog.tick();
                ops = 0;
            }
        }
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
        onOperation();
        return super.setBlock(location, block);
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        onOperation();
        return super.createEntity(location, entity);
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        onOperation();
        return super.setBiome(position, biome);
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        onOperation();
        return super.getBlock(position);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        onOperation();
        return super.getFullBlock(position);
    }

    @Override
    public BiomeType getBiome(BlockVector3 position) {
        onOperation();
        return super.getBiome(position);
    }

    @Override
    public List<? extends Entity> getEntities() {
        onOperation();
        return super.getEntities();
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        onOperation();
        return super.getEntities(region);
    }
}
