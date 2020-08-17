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

package com.sk89q.worldedit.world.block;

/**
 * Stores a list of common {@link BlockCategory BlockCategories}.
 *
 * @see BlockCategory
 */
@SuppressWarnings("unused")
public final class BlockCategories {
    public static final BlockCategory ACACIA_LOGS = get("minecraft:acacia_logs");
    public static final BlockCategory ANVIL = get("minecraft:anvil");
    public static final BlockCategory BAMBOO_PLANTABLE_ON = get("minecraft:bamboo_plantable_on");
    public static final BlockCategory BANNERS = get("minecraft:banners");
    public static final BlockCategory BASE_STONE_NETHER = get("minecraft:base_stone_nether");
    public static final BlockCategory BASE_STONE_OVERWORLD = get("minecraft:base_stone_overworld");
    public static final BlockCategory BEACON_BASE_BLOCKS = get("minecraft:beacon_base_blocks");
    public static final BlockCategory BEDS = get("minecraft:beds");
    public static final BlockCategory BEE_GROWABLES = get("minecraft:bee_growables");
    public static final BlockCategory BEEHIVES = get("minecraft:beehives");
    public static final BlockCategory BIRCH_LOGS = get("minecraft:birch_logs");
    public static final BlockCategory BUTTONS = get("minecraft:buttons");
    public static final BlockCategory CAMPFIRES = get("minecraft:campfires");
    public static final BlockCategory CARPETS = get("minecraft:carpets");
    public static final BlockCategory CLIMBABLE = get("minecraft:climbable");
    public static final BlockCategory CORAL_BLOCKS = get("minecraft:coral_blocks");
    public static final BlockCategory CORAL_PLANTS = get("minecraft:coral_plants");
    public static final BlockCategory CORALS = get("minecraft:corals");
    public static final BlockCategory CRIMSON_STEMS = get("minecraft:crimson_stems");
    public static final BlockCategory CROPS = get("minecraft:crops");
    public static final BlockCategory DARK_OAK_LOGS = get("minecraft:dark_oak_logs");
    @Deprecated public static final BlockCategory DIRT_LIKE = get("minecraft:dirt_like");
    public static final BlockCategory DOORS = get("minecraft:doors");
    public static final BlockCategory DRAGON_IMMUNE = get("minecraft:dragon_immune");
    public static final BlockCategory ENDERMAN_HOLDABLE = get("minecraft:enderman_holdable");
    public static final BlockCategory FENCE_GATES = get("minecraft:fence_gates");
    public static final BlockCategory FENCES = get("minecraft:fences");
    public static final BlockCategory FIRE = get("minecraft:fire");
    public static final BlockCategory FLOWER_POTS = get("minecraft:flower_pots");
    public static final BlockCategory FLOWERS = get("minecraft:flowers");
    public static final BlockCategory GOLD_ORES = get("minecraft:gold_ores");
    public static final BlockCategory GUARDED_BY_PIGLINS = get("minecraft:guarded_by_piglins");
    public static final BlockCategory HOGLIN_REPELLENTS = get("minecraft:hoglin_repellents");
    public static final BlockCategory ICE = get("minecraft:ice");
    public static final BlockCategory IMPERMEABLE = get("minecraft:impermeable");
    public static final BlockCategory INFINIBURN_END = get("minecraft:infiniburn_end");
    public static final BlockCategory INFINIBURN_NETHER = get("minecraft:infiniburn_nether");
    public static final BlockCategory INFINIBURN_OVERWORLD = get("minecraft:infiniburn_overworld");
    public static final BlockCategory JUNGLE_LOGS = get("minecraft:jungle_logs");
    public static final BlockCategory LEAVES = get("minecraft:leaves");
    public static final BlockCategory LOGS = get("minecraft:logs");
    public static final BlockCategory LOGS_THAT_BURN = get("minecraft:logs_that_burn");
    public static final BlockCategory MUSHROOM_GROW_BLOCK = get("minecraft:mushroom_grow_block");
    public static final BlockCategory NON_FLAMMABLE_WOOD = get("minecraft:non_flammable_wood");
    public static final BlockCategory NYLIUM = get("minecraft:nylium");
    public static final BlockCategory OAK_LOGS = get("minecraft:oak_logs");
    public static final BlockCategory PIGLIN_REPELLENTS = get("minecraft:piglin_repellents");
    public static final BlockCategory PLANKS = get("minecraft:planks");
    public static final BlockCategory PORTALS = get("minecraft:portals");
    public static final BlockCategory PRESSURE_PLATES = get("minecraft:pressure_plates");
    public static final BlockCategory PREVENT_MOB_SPAWNING_INSIDE = get("minecraft:prevent_mob_spawning_inside");
    public static final BlockCategory RAILS = get("minecraft:rails");
    public static final BlockCategory SAND = get("minecraft:sand");
    public static final BlockCategory SAPLINGS = get("minecraft:saplings");
    public static final BlockCategory SHULKER_BOXES = get("minecraft:shulker_boxes");
    public static final BlockCategory SIGNS = get("minecraft:signs");
    public static final BlockCategory SLABS = get("minecraft:slabs");
    public static final BlockCategory SMALL_FLOWERS = get("minecraft:small_flowers");
    public static final BlockCategory SOUL_FIRE_BASE_BLOCKS = get("minecraft:soul_fire_base_blocks");
    public static final BlockCategory SOUL_SPEED_BLOCKS = get("minecraft:soul_speed_blocks");
    public static final BlockCategory SPRUCE_LOGS = get("minecraft:spruce_logs");
    public static final BlockCategory STAIRS = get("minecraft:stairs");
    public static final BlockCategory STANDING_SIGNS = get("minecraft:standing_signs");
    public static final BlockCategory STONE_BRICKS = get("minecraft:stone_bricks");
    public static final BlockCategory STONE_PRESSURE_PLATES = get("minecraft:stone_pressure_plates");
    public static final BlockCategory STRIDER_WARM_BLOCKS = get("minecraft:strider_warm_blocks");
    public static final BlockCategory TALL_FLOWERS = get("minecraft:tall_flowers");
    public static final BlockCategory TRAPDOORS = get("minecraft:trapdoors");
    public static final BlockCategory UNDERWATER_BONEMEALS = get("minecraft:underwater_bonemeals");
    public static final BlockCategory UNSTABLE_BOTTOM_CENTER = get("minecraft:unstable_bottom_center");
    public static final BlockCategory VALID_SPAWN = get("minecraft:valid_spawn");
    public static final BlockCategory WALL_CORALS = get("minecraft:wall_corals");
    public static final BlockCategory WALL_POST_OVERRIDE = get("minecraft:wall_post_override");
    public static final BlockCategory WALL_SIGNS = get("minecraft:wall_signs");
    public static final BlockCategory WALLS = get("minecraft:walls");
    public static final BlockCategory WARPED_STEMS = get("minecraft:warped_stems");
    public static final BlockCategory WART_BLOCKS = get("minecraft:wart_blocks");
    public static final BlockCategory WITHER_IMMUNE = get("minecraft:wither_immune");
    public static final BlockCategory WITHER_SUMMON_BASE_BLOCKS = get("minecraft:wither_summon_base_blocks");
    public static final BlockCategory WOODEN_BUTTONS = get("minecraft:wooden_buttons");
    public static final BlockCategory WOODEN_DOORS = get("minecraft:wooden_doors");
    public static final BlockCategory WOODEN_FENCES = get("minecraft:wooden_fences");
    public static final BlockCategory WOODEN_PRESSURE_PLATES = get("minecraft:wooden_pressure_plates");
    public static final BlockCategory WOODEN_SLABS = get("minecraft:wooden_slabs");
    public static final BlockCategory WOODEN_STAIRS = get("minecraft:wooden_stairs");
    public static final BlockCategory WOODEN_TRAPDOORS = get("minecraft:wooden_trapdoors");
    public static final BlockCategory WOOL = get("minecraft:wool");

    private BlockCategories() {
    }

    /**
     * Gets the {@link BlockCategory} associated with the given id.
     */
    public static BlockCategory get(String id) {
        BlockCategory entry = BlockCategory.REGISTRY.get(id);
        if (entry == null) {
            return new BlockCategory(id);
        }
        return entry;
    }
}
