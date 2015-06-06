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

package com.sk89q.worldedit.forge;

import com.google.common.collect.HashBiMap;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.registry.BiomeRegistry;

import net.minecraft.world.biome.BiomeGenBase;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides access to biome data in Forge.
 */
class ForgeBiomeRegistry implements BiomeRegistry {
    private static Map<Integer, BiomeGenBase> biomes = Collections.emptyMap();
    private static Map<Integer, BiomeData> biomeData = Collections.emptyMap();

    @Nullable
    @Override
    public BaseBiome createFromId(int id) {
        return new BaseBiome(id);
    }

    @Override
    public List<BaseBiome> getBiomes() {
        List<BaseBiome> list = new ArrayList<BaseBiome>();
        for (int biome : biomes.keySet()) {
            list.add(new BaseBiome(biome));
        }
        return list;
    }

    @Nullable
    @Override
    public BiomeData getData(BaseBiome biome) {
        return biomeData.get(biome.getId());
    }

    /**
     * Populate the internal static list of biomes.
     *
     * <p>If called repeatedly, the last call will overwrite all previous
     * calls.</p>
     */
    static void populate() {
        Map<Integer, BiomeGenBase> biomes = HashBiMap.create();
        Map<Integer, BiomeData> biomeData = new HashMap<Integer, BiomeData>();

        for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
            if ((biome == null) || (biomes.containsValue(biome))) {
                continue;
            }
            biomes.put(biome.biomeID, biome);
            biomeData.put(biome.biomeID, new ForgeBiomeData(biome));
        }

        ForgeBiomeRegistry.biomes = biomes;
        ForgeBiomeRegistry.biomeData = biomeData;
    }

    /**
     * Cached biome data information.
     */
    private static class ForgeBiomeData implements BiomeData {
        private final BiomeGenBase biome;

        /**
         * Create a new instance.
         *
         * @param biome the base biome
         */
        private ForgeBiomeData(BiomeGenBase biome) {
            this.biome = biome;
        }

        @Override
        public String getName() {
            return biome.biomeName;
        }
    }

}