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

package com.sk89q.worldedit.world.block;

/**
 * Stores a list of categories of Block Types.
 */
public final class BlockCategories {

    public static final BlockCategory ACACIA_LOGS = get("minecraft:acacia_logs");
    public static final BlockCategory ANVIL = get("minecraft:anvil");
    public static final BlockCategory BAMBOO_PLANTABLE_ON = get("minecraft:bamboo_plantable_on");
    public static final BlockCategory BANNERS = get("minecraft:banners");
    public static final BlockCategory BEDS = get("minecraft:beds");
    public static final BlockCategory BIRCH_LOGS = get("minecraft:birch_logs");
    public static final BlockCategory BUTTONS = get("minecraft:buttons");
    public static final BlockCategory CARPETS = get("minecraft:carpets");
    public static final BlockCategory CORAL_BLOCKS = get("minecraft:coral_blocks");
    public static final BlockCategory CORAL_PLANTS = get("minecraft:coral_plants");
    public static final BlockCategory CORALS = get("minecraft:corals");
    public static final BlockCategory DARK_OAK_LOGS = get("minecraft:dark_oak_logs");
    public static final BlockCategory DIRT_LIKE = get("minecraft:dirt_like");
    public static final BlockCategory DOORS = get("minecraft:doors");
    public static final BlockCategory DRAGON_IMMUNE = get("minecraft:dragon_immune");
    public static final BlockCategory ENDERMAN_HOLDABLE = get("minecraft:enderman_holdable");
    public static final BlockCategory FENCES = get("minecraft:fences");
    public static final BlockCategory FLOWER_POTS = get("minecraft:flower_pots");
    public static final BlockCategory ICE = get("minecraft:ice");
    public static final BlockCategory IMPERMEABLE = get("minecraft:impermeable");
    public static final BlockCategory JUNGLE_LOGS = get("minecraft:jungle_logs");
    public static final BlockCategory LEAVES = get("minecraft:leaves");
    public static final BlockCategory LOGS = get("minecraft:logs");
    public static final BlockCategory OAK_LOGS = get("minecraft:oak_logs");
    public static final BlockCategory PLANKS = get("minecraft:planks");
    public static final BlockCategory RAILS = get("minecraft:rails");
    public static final BlockCategory SAND = get("minecraft:sand");
    public static final BlockCategory SAPLINGS = get("minecraft:saplings");
    public static final BlockCategory SIGNS = get("minecraft:signs");
    public static final BlockCategory SLABS = get("minecraft:slabs");
    public static final BlockCategory SMALL_FLOWERS = get("minecraft:small_flowers");
    public static final BlockCategory SPRUCE_LOGS = get("minecraft:spruce_logs");
    public static final BlockCategory STAIRS = get("minecraft:stairs");
    public static final BlockCategory STANDING_SIGNS = get("minecraft:standing_signs");
    public static final BlockCategory STONE_BRICKS = get("minecraft:stone_bricks");
    public static final BlockCategory TRAPDOORS = get("minecraft:trapdoors");
    public static final BlockCategory UNDERWATER_BONEMEALS = get("minecraft:underwater_bonemeals");
    public static final BlockCategory VALID_SPAWN = get("minecraft:valid_spawn");
    public static final BlockCategory WALL_CORALS = get("minecraft:wall_corals");
    public static final BlockCategory WALL_SIGNS = get("minecraft:wall_signs");
    public static final BlockCategory WALLS = get("minecraft:walls");
    public static final BlockCategory WITHER_IMMUNE = get("minecraft:wither_immune");
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

    private static BlockCategory get(final String id) {
        BlockCategory blockCategory = BlockCategory.REGISTRY.get(id);
        if (blockCategory == null) {
            return new BlockCategory(id);
        }
        return blockCategory;
    }
}
