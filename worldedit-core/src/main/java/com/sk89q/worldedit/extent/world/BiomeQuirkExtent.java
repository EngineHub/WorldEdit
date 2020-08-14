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

import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;

/**
 * Handles quirks when placing biomes.
 */
public class BiomeQuirkExtent extends AbstractDelegateExtent {

    /**
     * Create a new instance.
     *
     * @param extent the extent
     */
    public BiomeQuirkExtent(Extent extent) {
        super(extent);
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        boolean success = false;
        if (!fullySupports3DBiomes()) {
            // Also place at Y = 0 for proper handling
            success = super.setBiome(position.withY(0), biome);
        }
        return super.setBiome(position, biome) || success;
    }
}
