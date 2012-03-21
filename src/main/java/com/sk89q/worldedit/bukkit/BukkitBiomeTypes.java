package com.sk89q.worldedit.bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.block.Biome;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.UnknownBiomeTypeException;

public class BukkitBiomeTypes implements BiomeTypes {

    public BukkitBiomeTypes() {
    }

    @Override
    public boolean has(String name) {
        try {
            Biome.valueOf(name.toUpperCase(Locale.ENGLISH));
            return true;
        } catch (IllegalArgumentException exc) {
            return false;
        }
    }

    @Override
    public BiomeType get(String name) throws UnknownBiomeTypeException {
        try {
            Biome biome = Biome.valueOf(name.toUpperCase(Locale.ENGLISH));
            return new BiomeType(biome.name());
        } catch (IllegalArgumentException exc) {
            throw new UnknownBiomeTypeException(name);
        }
    }

    @Override
    public List<BiomeType> all() {
        List<BiomeType> biomes = new ArrayList<BiomeType>();
        for (Biome biome : Biome.values()) {
            biomes.add(new BiomeType(biome.name()));
        }
        return biomes;
    }

}
