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
    private static BiMap biomes = HashBiMap.create();

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
        Iterator it = biomes.keySet().iterator();
        while (it.hasNext()) {
            BiomeType test = (BiomeType) it.next();
            if (test.getName().equalsIgnoreCase(name)) {
                return test;
            }
        }
        throw new UnknownBiomeTypeException(name);
    }

    public List all() {
        if (biomes.isEmpty()) {
            biomes = HashBiMap.create(new HashMap());
            for (BiomeGenBase biome : BiomeGenBase.biomeList) {
                if ((biome == null) || (biomes.containsValue(biome))) {
                    continue;
                }
                biomes.put(new ForgeBiomeType(biome), biome);
            }
        }
        List retBiomes = new ArrayList();
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