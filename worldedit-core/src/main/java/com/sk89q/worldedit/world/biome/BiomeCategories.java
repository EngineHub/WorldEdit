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

package com.sk89q.worldedit.world.biome;

/**
 * Stores a list of common {@link BiomeCategory BiomeCategories}.
 *
 * @see BiomeCategory
 */
@SuppressWarnings("unused")
public final class BiomeCategories {
    public static final BiomeCategory ALLOWS_SURFACE_SLIME_SPAWNS = get("minecraft:allows_surface_slime_spawns");
    public static final BiomeCategory ALLOWS_TROPICAL_FISH_SPAWNS_AT_ANY_HEIGHT = get("minecraft:allows_tropical_fish_spawns_at_any_height");
    public static final BiomeCategory HAS_CLOSER_WATER_FOG = get("minecraft:has_closer_water_fog");
    public static final BiomeCategory HAS_STRUCTURE_ANCIENT_CITY = get("minecraft:has_structure/ancient_city");
    public static final BiomeCategory HAS_STRUCTURE_BASTION_REMNANT = get("minecraft:has_structure/bastion_remnant");
    public static final BiomeCategory HAS_STRUCTURE_BURIED_TREASURE = get("minecraft:has_structure/buried_treasure");
    public static final BiomeCategory HAS_STRUCTURE_DESERT_PYRAMID = get("minecraft:has_structure/desert_pyramid");
    public static final BiomeCategory HAS_STRUCTURE_END_CITY = get("minecraft:has_structure/end_city");
    public static final BiomeCategory HAS_STRUCTURE_IGLOO = get("minecraft:has_structure/igloo");
    public static final BiomeCategory HAS_STRUCTURE_JUNGLE_TEMPLE = get("minecraft:has_structure/jungle_temple");
    public static final BiomeCategory HAS_STRUCTURE_MINESHAFT = get("minecraft:has_structure/mineshaft");
    public static final BiomeCategory HAS_STRUCTURE_MINESHAFT_MESA = get("minecraft:has_structure/mineshaft_mesa");
    public static final BiomeCategory HAS_STRUCTURE_NETHER_FORTRESS = get("minecraft:has_structure/nether_fortress");
    public static final BiomeCategory HAS_STRUCTURE_NETHER_FOSSIL = get("minecraft:has_structure/nether_fossil");
    public static final BiomeCategory HAS_STRUCTURE_OCEAN_MONUMENT = get("minecraft:has_structure/ocean_monument");
    public static final BiomeCategory HAS_STRUCTURE_OCEAN_RUIN_COLD = get("minecraft:has_structure/ocean_ruin_cold");
    public static final BiomeCategory HAS_STRUCTURE_OCEAN_RUIN_WARM = get("minecraft:has_structure/ocean_ruin_warm");
    public static final BiomeCategory HAS_STRUCTURE_PILLAGER_OUTPOST = get("minecraft:has_structure/pillager_outpost");
    public static final BiomeCategory HAS_STRUCTURE_RUINED_PORTAL_DESERT = get("minecraft:has_structure/ruined_portal_desert");
    public static final BiomeCategory HAS_STRUCTURE_RUINED_PORTAL_JUNGLE = get("minecraft:has_structure/ruined_portal_jungle");
    public static final BiomeCategory HAS_STRUCTURE_RUINED_PORTAL_MOUNTAIN = get("minecraft:has_structure/ruined_portal_mountain");
    public static final BiomeCategory HAS_STRUCTURE_RUINED_PORTAL_NETHER = get("minecraft:has_structure/ruined_portal_nether");
    public static final BiomeCategory HAS_STRUCTURE_RUINED_PORTAL_OCEAN = get("minecraft:has_structure/ruined_portal_ocean");
    public static final BiomeCategory HAS_STRUCTURE_RUINED_PORTAL_STANDARD = get("minecraft:has_structure/ruined_portal_standard");
    public static final BiomeCategory HAS_STRUCTURE_RUINED_PORTAL_SWAMP = get("minecraft:has_structure/ruined_portal_swamp");
    public static final BiomeCategory HAS_STRUCTURE_SHIPWRECK = get("minecraft:has_structure/shipwreck");
    public static final BiomeCategory HAS_STRUCTURE_SHIPWRECK_BEACHED = get("minecraft:has_structure/shipwreck_beached");
    public static final BiomeCategory HAS_STRUCTURE_STRONGHOLD = get("minecraft:has_structure/stronghold");
    public static final BiomeCategory HAS_STRUCTURE_SWAMP_HUT = get("minecraft:has_structure/swamp_hut");
    public static final BiomeCategory HAS_STRUCTURE_TRAIL_RUINS = get("minecraft:has_structure/trail_ruins");
    public static final BiomeCategory HAS_STRUCTURE_TRIAL_CHAMBERS = get("minecraft:has_structure/trial_chambers");
    public static final BiomeCategory HAS_STRUCTURE_VILLAGE_DESERT = get("minecraft:has_structure/village_desert");
    public static final BiomeCategory HAS_STRUCTURE_VILLAGE_PLAINS = get("minecraft:has_structure/village_plains");
    public static final BiomeCategory HAS_STRUCTURE_VILLAGE_SAVANNA = get("minecraft:has_structure/village_savanna");
    public static final BiomeCategory HAS_STRUCTURE_VILLAGE_SNOWY = get("minecraft:has_structure/village_snowy");
    public static final BiomeCategory HAS_STRUCTURE_VILLAGE_TAIGA = get("minecraft:has_structure/village_taiga");
    public static final BiomeCategory HAS_STRUCTURE_WOODLAND_MANSION = get("minecraft:has_structure/woodland_mansion");
    public static final BiomeCategory INCREASED_FIRE_BURNOUT = get("minecraft:increased_fire_burnout");
    public static final BiomeCategory IS_BADLANDS = get("minecraft:is_badlands");
    public static final BiomeCategory IS_BEACH = get("minecraft:is_beach");
    public static final BiomeCategory IS_DEEP_OCEAN = get("minecraft:is_deep_ocean");
    public static final BiomeCategory IS_END = get("minecraft:is_end");
    public static final BiomeCategory IS_FOREST = get("minecraft:is_forest");
    public static final BiomeCategory IS_HILL = get("minecraft:is_hill");
    public static final BiomeCategory IS_JUNGLE = get("minecraft:is_jungle");
    public static final BiomeCategory IS_MOUNTAIN = get("minecraft:is_mountain");
    public static final BiomeCategory IS_NETHER = get("minecraft:is_nether");
    public static final BiomeCategory IS_OCEAN = get("minecraft:is_ocean");
    public static final BiomeCategory IS_OVERWORLD = get("minecraft:is_overworld");
    public static final BiomeCategory IS_RIVER = get("minecraft:is_river");
    public static final BiomeCategory IS_SAVANNA = get("minecraft:is_savanna");
    public static final BiomeCategory IS_TAIGA = get("minecraft:is_taiga");
    public static final BiomeCategory MINESHAFT_BLOCKING = get("minecraft:mineshaft_blocking");
    public static final BiomeCategory MORE_FREQUENT_DROWNED_SPAWNS = get("minecraft:more_frequent_drowned_spawns");
    public static final BiomeCategory PLAYS_UNDERWATER_MUSIC = get("minecraft:plays_underwater_music");
    public static final BiomeCategory POLAR_BEARS_SPAWN_ON_ALTERNATE_BLOCKS = get("minecraft:polar_bears_spawn_on_alternate_blocks");
    public static final BiomeCategory PRODUCES_CORALS_FROM_BONEMEAL = get("minecraft:produces_corals_from_bonemeal");
    public static final BiomeCategory REDUCE_WATER_AMBIENT_SPAWNS = get("minecraft:reduce_water_ambient_spawns");
    public static final BiomeCategory REQUIRED_OCEAN_MONUMENT_SURROUNDING = get("minecraft:required_ocean_monument_surrounding");
    public static final BiomeCategory SNOW_GOLEM_MELTS = get("minecraft:snow_golem_melts");
    public static final BiomeCategory SPAWNS_COLD_VARIANT_FROGS = get("minecraft:spawns_cold_variant_frogs");
    public static final BiomeCategory SPAWNS_GOLD_RABBITS = get("minecraft:spawns_gold_rabbits");
    public static final BiomeCategory SPAWNS_SNOW_FOXES = get("minecraft:spawns_snow_foxes");
    public static final BiomeCategory SPAWNS_WARM_VARIANT_FROGS = get("minecraft:spawns_warm_variant_frogs");
    public static final BiomeCategory SPAWNS_WHITE_RABBITS = get("minecraft:spawns_white_rabbits");
    public static final BiomeCategory STRONGHOLD_BIASED_TO = get("minecraft:stronghold_biased_to");
    public static final BiomeCategory WATER_ON_MAP_OUTLINES = get("minecraft:water_on_map_outlines");
    public static final BiomeCategory WITHOUT_PATROL_SPAWNS = get("minecraft:without_patrol_spawns");
    public static final BiomeCategory WITHOUT_WANDERING_TRADER_SPAWNS = get("minecraft:without_wandering_trader_spawns");
    public static final BiomeCategory WITHOUT_ZOMBIE_SIEGES = get("minecraft:without_zombie_sieges");

    private BiomeCategories() {
    }

    /**
     * Gets the {@link BiomeCategory} associated with the given id.
     */
    public static BiomeCategory get(String id) {
        BiomeCategory entry = BiomeCategory.REGISTRY.get(id);
        if (entry == null) {
            return new BiomeCategory(id);
        }
        return entry;
    }
}
