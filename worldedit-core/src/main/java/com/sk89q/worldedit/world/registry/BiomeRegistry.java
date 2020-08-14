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

package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.biome.BiomeType;

import javax.annotation.Nullable;

/**
 * Provides information on biomes.
 */
public interface BiomeRegistry {

    /**
     * Get the name of the biome, usually as a translatable component.
     *
     * @param biomeType the biome type
     * @return the name of the biome
     */
    Component getRichName(BiomeType biomeType);

    /**
     * Get data about a biome.
     *
     * @param biome the biome
     * @return a data object or null if information is not known
     * @deprecated This method no longer returns any useful information.
     *     Use {@link #getRichName(BiomeType)} for the name of the biome.
     */
    @Deprecated
    @Nullable
    BiomeData getData(BiomeType biome);

}
