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

package com.sk89q.worldedit.fabric;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeArray;

/**
 * Interface over a {@link BiomeArray} as a mutable object.
 */
public interface MutableBiomeArray {

    /**
     * Hook into the given biome array, to allow edits on it.
     * @param biomeArray the biome array to edit
     * @return the mutable interface to the biome array
     */
    static MutableBiomeArray inject(BiomeArray biomeArray) {
        // It's Mixin'd
        return (MutableBiomeArray) biomeArray;
    }

    void setBiome(int x, int y, int z, Biome biome);

}
