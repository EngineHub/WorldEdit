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

package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.biome.BiomeData;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Provides information on biomes.
 */
public interface BiomeRegistry {

    /**
     * Create a new biome given its biome ID.
     *
     * @param id its biome ID
     * @return a new biome or null if it can't be created
     */
    @Nullable
    BaseBiome createFromId(int id);

    /**
     * Get a list of available biomes.
     *
     * @return a list of biomes
     */
    List<BaseBiome> getBiomes();

    /**
     * Get data about a biome.
     *
     * @param biome the biome
     * @return a data object or null if information is not known
     */
    @Nullable
    BiomeData getData(BaseBiome biome);

}
