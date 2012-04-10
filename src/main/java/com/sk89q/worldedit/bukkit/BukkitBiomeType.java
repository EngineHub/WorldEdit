package com.sk89q.worldedit.bukkit;

import java.util.Locale;

import org.bukkit.block.Biome;

import com.sk89q.worldedit.BiomeType;

public enum BukkitBiomeType implements BiomeType {

    SWAMPLAND(Biome.SWAMPLAND),
    FOREST(Biome.FOREST),
    TAIGA(Biome.TAIGA),
    DESERT(Biome.DESERT),
    PLAINS(Biome.PLAINS),
    HELL(Biome.HELL),
    SKY(Biome.SKY),
    RIVER(Biome.RIVER),
    EXTREME_HILLS(Biome.EXTREME_HILLS),
    OCEAN(Biome.OCEAN),
    FROZEN_OCEAN(Biome.FROZEN_OCEAN),
    FROZEN_RIVER(Biome.FROZEN_RIVER),
    ICE_PLAINS(Biome.ICE_PLAINS),
    ICE_MOUNTAINS(Biome.ICE_MOUNTAINS),
    MUSHROOM_ISLAND(Biome.MUSHROOM_ISLAND),
    MUSHROOM_SHORE(Biome.MUSHROOM_SHORE),
    BEACH(Biome.BEACH),
    DESERT_HILLS(Biome.DESERT_HILLS),
    FOREST_HILLS(Biome.FOREST_HILLS),
    TAIGA_HILLS(Biome.TAIGA_HILLS),
    SMALL_MOUNTAINS(Biome.SMALL_MOUNTAINS),
    JUNGLE(Biome.JUNGLE),
    JUNGLE_HILLS(Biome.JUNGLE_HILLS);

    private Biome bukkitBiome;

    private BukkitBiomeType(Biome biome) {
        this.bukkitBiome = biome;
    }

    @Override
    public String getName() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    public Biome getBukkitBiome() {
        return bukkitBiome;
    }
}
