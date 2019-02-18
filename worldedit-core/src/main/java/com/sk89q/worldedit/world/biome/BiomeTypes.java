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

    @Nullable public static final BiomeType BADLANDS = get("minecraft:badlands");
    @Nullable public static final BiomeType BADLANDS_PLATEAU = get("minecraft:badlands_plateau");
    @Nullable public static final BiomeType BEACH = get("minecraft:beach");
    @Nullable public static final BiomeType BIRCH_FOREST = get("minecraft:birch_forest");
    @Nullable public static final BiomeType BIRCH_FOREST_HILLS = get("minecraft:birch_forest_hills");
    @Nullable public static final BiomeType COLD_OCEAN = get("minecraft:cold_ocean");
    @Nullable public static final BiomeType DARK_FOREST = get("minecraft:dark_forest");
    @Nullable public static final BiomeType DARK_FOREST_HILLS = get("minecraft:dark_forest_hills");
    @Nullable public static final BiomeType DEEP_COLD_OCEAN = get("minecraft:deep_cold_ocean");
    @Nullable public static final BiomeType DEEP_FROZEN_OCEAN = get("minecraft:deep_frozen_ocean");
    @Nullable public static final BiomeType DEEP_LUKEWARM_OCEAN = get("minecraft:deep_lukewarm_ocean");
    @Nullable public static final BiomeType DEEP_OCEAN = get("minecraft:deep_ocean");
    @Nullable public static final BiomeType DEEP_WARM_OCEAN = get("minecraft:deep_warm_ocean");
    @Nullable public static final BiomeType DESERT = get("minecraft:desert");
    @Nullable public static final BiomeType DESERT_HILLS = get("minecraft:desert_hills");
    @Nullable public static final BiomeType DESERT_LAKES = get("minecraft:desert_lakes");
    @Nullable public static final BiomeType END_BARRENS = get("minecraft:end_barrens");
    @Nullable public static final BiomeType END_HIGHLANDS = get("minecraft:end_highlands");
    @Nullable public static final BiomeType END_MIDLANDS = get("minecraft:end_midlands");
    @Nullable public static final BiomeType ERODED_BADLANDS = get("minecraft:eroded_badlands");
    @Nullable public static final BiomeType FLOWER_FOREST = get("minecraft:flower_forest");
    @Nullable public static final BiomeType FOREST = get("minecraft:forest");
    @Nullable public static final BiomeType FROZEN_OCEAN = get("minecraft:frozen_ocean");
    @Nullable public static final BiomeType FROZEN_RIVER = get("minecraft:frozen_river");
    @Nullable public static final BiomeType GIANT_SPRUCE_TAIGA = get("minecraft:giant_spruce_taiga");
    @Nullable public static final BiomeType GIANT_SPRUCE_TAIGA_HILLS = get("minecraft:giant_spruce_taiga_hills");
    @Nullable public static final BiomeType GIANT_TREE_TAIGA = get("minecraft:giant_tree_taiga");
    @Nullable public static final BiomeType GIANT_TREE_TAIGA_HILLS = get("minecraft:giant_tree_taiga_hills");
    @Nullable public static final BiomeType GRAVELLY_MOUNTAINS = get("minecraft:gravelly_mountains");
    @Nullable public static final BiomeType ICE_SPIKES = get("minecraft:ice_spikes");
    @Nullable public static final BiomeType JUNGLE = get("minecraft:jungle");
    @Nullable public static final BiomeType JUNGLE_EDGE = get("minecraft:jungle_edge");
    @Nullable public static final BiomeType JUNGLE_HILLS = get("minecraft:jungle_hills");
    @Nullable public static final BiomeType LUKEWARM_OCEAN = get("minecraft:lukewarm_ocean");
    @Nullable public static final BiomeType MODIFIED_BADLANDS_PLATEAU = get("minecraft:modified_badlands_plateau");
    @Nullable public static final BiomeType MODIFIED_GRAVELLY_MOUNTAINS = get("minecraft:modified_gravelly_mountains");
    @Nullable public static final BiomeType MODIFIED_JUNGLE = get("minecraft:modified_jungle");
    @Nullable public static final BiomeType MODIFIED_JUNGLE_EDGE = get("minecraft:modified_jungle_edge");
    @Nullable public static final BiomeType MODIFIED_WOODED_BADLANDS_PLATEAU = get("minecraft:modified_wooded_badlands_plateau");
    @Nullable public static final BiomeType MOUNTAIN_EDGE = get("minecraft:mountain_edge");
    @Nullable public static final BiomeType MOUNTAINS = get("minecraft:mountains");
    @Nullable public static final BiomeType MUSHROOM_FIELD_SHORE = get("minecraft:mushroom_field_shore");
    @Nullable public static final BiomeType MUSHROOM_FIELDS = get("minecraft:mushroom_fields");
    @Nullable public static final BiomeType NETHER = get("minecraft:nether");
    @Nullable public static final BiomeType OCEAN = get("minecraft:ocean");
    @Nullable public static final BiomeType PLAINS = get("minecraft:plains");
    @Nullable public static final BiomeType RIVER = get("minecraft:river");
    @Nullable public static final BiomeType SAVANNA = get("minecraft:savanna");
    @Nullable public static final BiomeType SAVANNA_PLATEAU = get("minecraft:savanna_plateau");
    @Nullable public static final BiomeType SHATTERED_SAVANNA = get("minecraft:shattered_savanna");
    @Nullable public static final BiomeType SHATTERED_SAVANNA_PLATEAU = get("minecraft:shattered_savanna_plateau");
    @Nullable public static final BiomeType SMALL_END_ISLANDS = get("minecraft:small_end_islands");
    @Nullable public static final BiomeType SNOWY_BEACH = get("minecraft:snowy_beach");
    @Nullable public static final BiomeType SNOWY_MOUNTAINS = get("minecraft:snowy_mountains");
    @Nullable public static final BiomeType SNOWY_TAIGA = get("minecraft:snowy_taiga");
    @Nullable public static final BiomeType SNOWY_TAIGA_HILLS = get("minecraft:snowy_taiga_hills");
    @Nullable public static final BiomeType SNOWY_TAIGA_MOUNTAINS = get("minecraft:snowy_taiga_mountains");
    @Nullable public static final BiomeType SNOWY_TUNDRA = get("minecraft:snowy_tundra");
    @Nullable public static final BiomeType STONE_SHORE = get("minecraft:stone_shore");
    @Nullable public static final BiomeType SUNFLOWER_PLAINS = get("minecraft:sunflower_plains");
    @Nullable public static final BiomeType SWAMP = get("minecraft:swamp");
    @Nullable public static final BiomeType SWAMP_HILLS = get("minecraft:swamp_hills");
    @Nullable public static final BiomeType TAIGA = get("minecraft:taiga");
    @Nullable public static final BiomeType TAIGA_HILLS = get("minecraft:taiga_hills");
    @Nullable public static final BiomeType TAIGA_MOUNTAINS = get("minecraft:taiga_mountains");
    @Nullable public static final BiomeType TALL_BIRCH_FOREST = get("minecraft:tall_birch_forest");
    @Nullable public static final BiomeType TALL_BIRCH_HILLS = get("minecraft:tall_birch_hills");
    @Nullable public static final BiomeType THE_END = get("minecraft:the_end");
    @Nullable public static final BiomeType THE_VOID = get("minecraft:the_void");
    @Nullable public static final BiomeType WARM_OCEAN = get("minecraft:warm_ocean");
    @Nullable public static final BiomeType WOODED_BADLANDS_PLATEAU = get("minecraft:wooded_badlands_plateau");
    @Nullable public static final BiomeType WOODED_HILLS = get("minecraft:wooded_hills");
    @Nullable public static final BiomeType WOODED_MOUNTAINS = get("minecraft:wooded_mountains");

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
