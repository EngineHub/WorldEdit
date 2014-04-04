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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.UnknownBiomeTypeException;

public class ForgeBiomeTypes implements BiomeTypes {
    private static BiMap<BiomeType, BiomeGenBase> biomes = HashBiMap.create();

    public ForgeBiomeTypes() {
        all();
    }

    public boolean has(String name) {
        for (BiomeGenBase biome : BiomeGenBase.biomeList) {
            if ((biome != null) && (biome.biomeName.equalsIgnoreCase(name))) {
                return true;
            }
        }
        return false;
    }

    public BiomeType get(String name) throws UnknownBiomeTypeException {
        if (biomes == null) {
            all();
        }
        Iterator<BiomeType> it = biomes.keySet().iterator();
        while (it.hasNext()) {
            BiomeType test = (BiomeType) it.next();
            if (test.getName().equalsIgnoreCase(name)) {
                return test;
            }
        }
        throw new UnknownBiomeTypeException(name);
    }

    public List<BiomeType> all() {
        if (biomes.isEmpty()) {
            biomes = HashBiMap.create(new HashMap<BiomeType, BiomeGenBase>());
            for (BiomeGenBase biome : BiomeGenBase.biomeList) {
                if ((biome == null) || (biomes.containsValue(biome))) {
                    continue;
                }
                biomes.put(new ForgeBiomeType(biome), biome);
            }
        }
        List<BiomeType> retBiomes = new ArrayList<BiomeType>();
        retBiomes.addAll(biomes.keySet());
        return retBiomes;
    }

    public static BiomeType getFromBaseBiome(BiomeGenBase biome) {
        return biomes.containsValue(biome) ? (BiomeType) biomes.inverse().get(biome) : BiomeType.UNKNOWN;
    }

    public static BiomeGenBase getFromBiomeType(BiomeType biome) {
        return (BiomeGenBase) biomes.get(biome);
    }
}