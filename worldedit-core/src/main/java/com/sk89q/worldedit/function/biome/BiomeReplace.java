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

package com.sk89q.worldedit.function.biome;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.FlatRegionFunction;
import com.sk89q.worldedit.function.pattern.BiomePattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.biome.BiomeType;

/**
 * Replaces the biome at the locations that this function is applied to.
 */
public class BiomeReplace implements FlatRegionFunction {

    private final Extent extent;
    private BiomePattern biome;

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
    public boolean apply(BlockVector2 position) throws WorldEditException {
        return extent.setBiome(position, biome.apply(position));
    }

}
