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

package com.sk89q.worldedit.function.biome;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.FlatRegionFunction;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.pattern.BiomePattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Replaces the biome at the locations that this function is applied to.
 */
public class BiomeReplace implements FlatRegionFunction, RegionFunction {

    private final Extent extent;
    private final BiomePattern biome;

    /**
     * Create a new instance.
     *
     * @param extent an extent
     * @param biome a biome
     */
    public BiomeReplace(Extent extent, BiomeType biome) {
        this(extent, (BiomePattern) biome);
    }

    /**
     * Create a new instance.
     *
     * @param extent the extent to apply this function to
     * @param pattern the biome pattern to set
     */
    public BiomeReplace(Extent extent, BiomePattern pattern) {
        checkNotNull(extent);
        checkNotNull(pattern);
        this.extent = extent;
        this.biome = pattern;
    }

    @Override
    public boolean apply(BlockVector3 position) throws WorldEditException {
        return extent.setBiome(position, biome.applyBiome(position));
    }

    @Override
    @Deprecated
    public boolean apply(BlockVector2 position) throws WorldEditException {
        boolean success = false;
        for (int y = extent.getMinimumPoint().getY(); y <= extent.getMaximumPoint().getY(); y++) {
            success |= apply(position.toBlockVector3(y));
        }
        return success;
    }
}
