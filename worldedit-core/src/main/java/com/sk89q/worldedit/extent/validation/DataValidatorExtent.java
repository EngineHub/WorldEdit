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

package com.sk89q.worldedit.extent.validation;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Validates set data to prevent creating invalid blocks and such.
 */
public class DataValidatorExtent extends AbstractDelegateExtent {

    private final int minY;
    private final int maxY;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     * @param world the world
     */
    public DataValidatorExtent(Extent extent, World world) {
        this(extent, checkNotNull(world).getMinY(), world.getMaxY());
    }

    /**
     * Create a new instance.
     *
     * @param extent The extent
     * @param minY The minimum Y height to allow (inclusive)
     * @param maxY The maximum Y height to allow (inclusive)
     */
    public DataValidatorExtent(Extent extent, int minY, int maxY) {
        super(extent);
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
        final int y = location.getBlockY();
        final BlockType type = block.getBlockType();
        if (y < minY || y > maxY) {
            return false;
        }

        // No invalid blocks
        if (type == null) {
            return false;
        }

        return super.setBlock(location, block);
    }

    @Override
    public boolean setBiome(BlockVector3 location, BiomeType biome) {
        final int y = location.getBlockY();

        if (y < minY || y > maxY) {
            return false;
        }

        return super.setBiome(location, biome);
    }
}
