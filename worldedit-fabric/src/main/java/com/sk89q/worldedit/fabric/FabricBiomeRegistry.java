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

package com.sk89q.worldedit.fabric;

import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.registry.BiomeRegistry;
import net.minecraft.world.biome.Biome;

/**
 * Provides access to biome data in Fabric.
 */
class FabricBiomeRegistry implements BiomeRegistry {

    @Override
    public BiomeData getData(BiomeType biome) {
        return new FabricBiomeData(FabricAdapter.adapt(biome));
    }

    /**
     * Cached biome data information.
     */
    private static class FabricBiomeData implements BiomeData {
        private final Biome biome;

        /**
         * Create a new instance.
         *
         * @param biome the base biome
         */
        private FabricBiomeData(Biome biome) {
            this.biome = biome;
        }

        @Override
        public String getName() {
            return biome.getName().asFormattedString();
        }
    }

}