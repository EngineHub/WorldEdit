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

package com.sk89q.worldedit.world.biome;

import javax.annotation.Nullable;

/**
 * Stores a list of common Biome String IDs.
 */
public class BiomeTypes {

    public static final BiomeType BADLANDS = register("minecraft:badlands");
    public static final BiomeType BADLANDS_PLATEAU = register("minecraft:badlands_plateau");
    public static final BiomeType BEACH = register("minecraft:beach");
    public static final BiomeType BIRCH_FOREST = register("minecraft:birch_forest");
    public static final BiomeType BIRCH_FOREST_HILLS = register("minecraft:birch_forest_hills");
    public static final BiomeType COLD_OCEAN = register("minecraft:cold_ocean");
    public static final BiomeType DARK_FOREST = register("minecraft:dark_forest");
    public static final BiomeType DARK_FOREST_HILLS = register("minecraft:dark_forest_hills");
    public static final BiomeType DEEP_COLD_OCEAN = register("minecraft:deep_cold_ocean");
    public static final BiomeType DEEP_FROZEN_OCEAN = register("minecraft:deep_frozen_ocean");
    public static final BiomeType DEEP_LUKEWARM_OCEAN = register("minecraft:deep_lukewarm_ocean");
    public static final BiomeType DEEP_OCEAN = register("minecraft:deep_ocean");
    public static final BiomeType DEEP_WARM_OCEAN = register("minecraft:deep_warm_ocean");
    public static final BiomeType DESERT = register("minecraft:desert");
    public static final BiomeType DESERT_HILLS = register("minecraft:desert_hills");
    public static final BiomeType DESERT_LAKES = register("minecraft:desert_lakes");
    public static final BiomeType END_BARRENS = register("minecraft:end_barrens");
    public static final BiomeType END_HIGHLANDS = register("minecraft:end_highlands");
    public static final BiomeType END_MIDLANDS = register("minecraft:end_midlands");
    public static final BiomeType ERODED_BADLANDS = register("minecraft:eroded_badlands");
    public static final BiomeType FLOWER_FOREST = register("minecraft:flower_forest");
    public static final BiomeType FOREST = register("minecraft:forest");
    public static final BiomeType FROZEN_OCEAN = register("minecraft:frozen_ocean");
    public static final BiomeType FROZEN_RIVER = register("minecraft:frozen_river");
    public static final BiomeType GIANT_SPRUCE_TAIGA = register("minecraft:giant_spruce_taiga");
    public static final BiomeType GIANT_SPRUCE_TAIGA_HILLS = register("minecraft:giant_spruce_taiga_hills");
    public static final BiomeType GIANT_TREE_TAIGA = register("minecraft:giant_tree_taiga");
    public static final BiomeType GIANT_TREE_TAIGA_HILLS = register("minecraft:giant_tree_taiga_hills");
    public static final BiomeType GRAVELLY_MOUNTAINS = register("minecraft:gravelly_mountains");
    public static final BiomeType ICE_SPIKES = register("minecraft:ice_spikes");
    public static final BiomeType JUNGLE = register("minecraft:jungle");
    public static final BiomeType JUNGLE_EDGE = register("minecraft:jungle_edge");
    public static final BiomeType JUNGLE_HILLS = register("minecraft:jungle_hills");
    public static final BiomeType LUKEWARM_OCEAN = register("minecraft:lukewarm_ocean");
    public static final BiomeType MODIFIED_BADLANDS_PLATEAU = register("minecraft:modified_badlands_plateau");
    public static final BiomeType MODIFIED_GRAVELLY_MOUNTAINS = register("minecraft:modified_gravelly_mountains");
    public static final BiomeType MODIFIED_JUNGLE = register("minecraft:modified_jungle");
    public static final BiomeType MODIFIED_JUNGLE_EDGE = register("minecraft:modified_jungle_edge");
    public static final BiomeType MODIFIED_WOODED_BADLANDS_PLATEAU = register("minecraft:modified_wooded_badlands_plateau");
    public static final BiomeType MOUNTAIN_EDGE = register("minecraft:mountain_edge");
    public static final BiomeType MOUNTAINS = register("minecraft:mountains");
    public static final BiomeType MUSHROOM_FIELD_SHORE = register("minecraft:mushroom_field_shore");
    public static final BiomeType MUSHROOM_FIELDS = register("minecraft:mushroom_fields");
    public static final BiomeType NETHER = register("minecraft:nether");
    public static final BiomeType OCEAN = register("minecraft:ocean");
    public static final BiomeType PLAINS = register("minecraft:plains");
    public static final BiomeType RIVER = register("minecraft:river");
    public static final BiomeType SAVANNA = register("minecraft:savanna");
    public static final BiomeType SAVANNA_PLATEAU = register("minecraft:savanna_plateau");
    public static final BiomeType SHATTERED_SAVANNA = register("minecraft:shattered_savanna");
    public static final BiomeType SHATTERED_SAVANNA_PLATEAU = register("minecraft:shattered_savanna_plateau");
    public static final BiomeType SMALL_END_ISLANDS = register("minecraft:small_end_islands");
    public static final BiomeType SNOWY_BEACH = register("minecraft:snowy_beach");
    public static final BiomeType SNOWY_MOUNTAINS = register("minecraft:snowy_mountains");
    public static final BiomeType SNOWY_TAIGA = register("minecraft:snowy_taiga");
    public static final BiomeType SNOWY_TAIGA_HILLS = register("minecraft:snowy_taiga_hills");
    public static final BiomeType SNOWY_TAIGA_MOUNTAINS = register("minecraft:snowy_taiga_mountains");
    public static final BiomeType SNOWY_TUNDRA = register("minecraft:snowy_tundra");
    public static final BiomeType STONE_SHORE = register("minecraft:stone_shore");
    public static final BiomeType SUNFLOWER_PLAINS = register("minecraft:sunflower_plains");
    public static final BiomeType SWAMP = register("minecraft:swamp");
    public static final BiomeType SWAMP_HILLS = register("minecraft:swamp_hills");
    public static final BiomeType TAIGA = register("minecraft:taiga");
    public static final BiomeType TAIGA_HILLS = register("minecraft:taiga_hills");
    public static final BiomeType TAIGA_MOUNTAINS = register("minecraft:taiga_mountains");
    public static final BiomeType TALL_BIRCH_FOREST = register("minecraft:tall_birch_forest");
    public static final BiomeType TALL_BIRCH_HILLS = register("minecraft:tall_birch_hills");
    public static final BiomeType THE_END = register("minecraft:the_end");
    public static final BiomeType THE_VOID = register("minecraft:the_void");
    public static final BiomeType WARM_OCEAN = register("minecraft:warm_ocean");
    public static final BiomeType WOODED_BADLANDS_PLATEAU = register("minecraft:wooded_badlands_plateau");
    public static final BiomeType WOODED_HILLS = register("minecraft:wooded_hills");
    public static final BiomeType WOODED_MOUNTAINS = register("minecraft:wooded_mountains");

    private BiomeTypes() {
    }

    private static BiomeType register(final String id) {
        return register(new BiomeType(id));
    }

    public static BiomeType register(final BiomeType biome) {
        return BiomeType.REGISTRY.register(biome.getId(), biome);
    }

    public static @Nullable BiomeType get(final String id) {
        return BiomeType.REGISTRY.get(id);
    }
}
