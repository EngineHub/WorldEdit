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
    JUNGLE_HILLS(Biome.JUNGLE_HILLS),
    JUNGLE_EDGE(Biome.JUNGLE_EDGE),
    DEEP_OCEAN(Biome.DEEP_OCEAN),
    STONE_BEACH(Biome.STONE_BEACH),
    COLD_BEACH(Biome.COLD_BEACH),
    BIRCH_FOREST(Biome.BIRCH_FOREST),
    BIRCH_FOREST_HILLS(Biome.BIRCH_FOREST_HILLS),
    ROOFED_FOREST(Biome.ROOFED_FOREST),
    COLD_TAIGA(Biome.COLD_TAIGA),
    COLD_TAIGA_HILLS(Biome.COLD_TAIGA_HILLS),
    MEGA_TAIGA(Biome.MEGA_TAIGA),
    MEGA_TAIGA_HILLS(Biome.MEGA_TAIGA_HILLS),
    EXTREME_HILLS_PLUS(Biome.EXTREME_HILLS_PLUS),
    SAVANNA(Biome.SAVANNA),
    SAVANNA_PLATEAU(Biome.SAVANNA_PLATEAU),
    MESA(Biome.MESA),
    MESA_PLATEAU_FOREST(Biome.MESA_PLATEAU_FOREST),
    MESA_PLATEAU(Biome.MESA_PLATEAU),
    SUNFLOWER_PLAINS(Biome.SUNFLOWER_PLAINS),
    DESERT_MOUNTAINS(Biome.DESERT_MOUNTAINS),
    FLOWER_FOREST(Biome.FLOWER_FOREST),
    TAIGA_MOUNTAINS(Biome.TAIGA_MOUNTAINS),
    SWAMPLAND_MOUNTAINS(Biome.SWAMPLAND_MOUNTAINS),
    ICE_PLAINS_SPIKES(Biome.ICE_PLAINS_SPIKES),
    JUNGLE_MOUNTAINS(Biome.JUNGLE_MOUNTAINS),
    JUNGLE_EDGE_MOUNTAINS(Biome.JUNGLE_EDGE_MOUNTAINS),
    COLD_TAIGA_MOUNTAINS(Biome.COLD_TAIGA_MOUNTAINS),
    SAVANNA_MOUNTAINS(Biome.SAVANNA_MOUNTAINS),
    SAVANNA_PLATEAU_MOUNTAINS(Biome.SAVANNA_PLATEAU_MOUNTAINS),
    MESA_BRYCE(Biome.MESA_BRYCE),
    MESA_PLATEAU_FOREST_MOUNTAINS(Biome.MESA_PLATEAU_FOREST_MOUNTAINS),
    MESA_PLATEAU_MOUNTAINS(Biome.MESA_PLATEAU_MOUNTAINS),
    BIRCH_FOREST_MOUNTAINS(Biome.BIRCH_FOREST_MOUNTAINS),
    BIRCH_FOREST_HILLS_MOUNTAINS(Biome.BIRCH_FOREST_HILLS_MOUNTAINS),
    ROOFED_FOREST_MOUNTAINS(Biome.ROOFED_FOREST_MOUNTAINS),
    MEGA_SPRUCE_TAIGA(Biome.MEGA_SPRUCE_TAIGA),
    EXTREME_HILLS_MOUNTAINS(Biome.EXTREME_HILLS_MOUNTAINS),
    EXTREME_HILLS_PLUS_MOUNTAINS(Biome.EXTREME_HILLS_PLUS_MOUNTAINS),
    MEGA_SPRUCE_TAIGA_HILLS(Biome.MEGA_SPRUCE_TAIGA_HILLS);

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
