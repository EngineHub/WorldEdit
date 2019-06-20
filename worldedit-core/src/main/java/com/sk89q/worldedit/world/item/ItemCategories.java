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

package com.sk89q.worldedit.world.item;

/**
 * Stores a list of categories of Item Types.
 */
public final class ItemCategories {

    public static final ItemCategory ACACIA_LOGS = get("minecraft:acacia_logs");
    public static final ItemCategory ANVIL = get("minecraft:anvil");
    public static final ItemCategory ARROWS = get("minecraft:arrows");
    public static final ItemCategory BANNERS = get("minecraft:banners");
    public static final ItemCategory BEDS = get("minecraft:beds");
    public static final ItemCategory BIRCH_LOGS = get("minecraft:birch_logs");
    public static final ItemCategory BOATS = get("minecraft:boats");
    public static final ItemCategory BUTTONS = get("minecraft:buttons");
    public static final ItemCategory CARPETS = get("minecraft:carpets");
    public static final ItemCategory COALS = get("minecraft:coals");
    public static final ItemCategory DARK_OAK_LOGS = get("minecraft:dark_oak_logs");
    public static final ItemCategory DOORS = get("minecraft:doors");
    public static final ItemCategory FENCES = get("minecraft:fences");
    public static final ItemCategory FISHES = get("minecraft:fishes");
    public static final ItemCategory JUNGLE_LOGS = get("minecraft:jungle_logs");
    public static final ItemCategory LEAVES = get("minecraft:leaves");
    public static final ItemCategory LOGS = get("minecraft:logs");
    public static final ItemCategory MUSIC_DISCS = get("minecraft:music_discs");
    public static final ItemCategory OAK_LOGS = get("minecraft:oak_logs");
    public static final ItemCategory PLANKS = get("minecraft:planks");
    public static final ItemCategory RAILS = get("minecraft:rails");
    public static final ItemCategory SAND = get("minecraft:sand");
    public static final ItemCategory SAPLINGS = get("minecraft:saplings");
    public static final ItemCategory SIGNS = get("minecraft:signs");
    public static final ItemCategory SLABS = get("minecraft:slabs");
    public static final ItemCategory SMALL_FLOWERS = get("minecraft:small_flowers");
    public static final ItemCategory SPRUCE_LOGS = get("minecraft:spruce_logs");
    public static final ItemCategory STAIRS = get("minecraft:stairs");
    public static final ItemCategory STONE_BRICKS = get("minecraft:stone_bricks");
    public static final ItemCategory TRAPDOORS = get("minecraft:trapdoors");
    public static final ItemCategory WALLS = get("minecraft:walls");
    public static final ItemCategory WOODEN_BUTTONS = get("minecraft:wooden_buttons");
    public static final ItemCategory WOODEN_DOORS = get("minecraft:wooden_doors");
    public static final ItemCategory WOODEN_FENCES = get("minecraft:wooden_fences");
    public static final ItemCategory WOODEN_PRESSURE_PLATES = get("minecraft:wooden_pressure_plates");
    public static final ItemCategory WOODEN_SLABS = get("minecraft:wooden_slabs");
    public static final ItemCategory WOODEN_STAIRS = get("minecraft:wooden_stairs");
    public static final ItemCategory WOODEN_TRAPDOORS = get("minecraft:wooden_trapdoors");
    public static final ItemCategory WOOL = get("minecraft:wool");

    private ItemCategories() {
    }

    private static ItemCategory get(final String id) {
        ItemCategory itemCategory = ItemCategory.REGISTRY.get(id);
        if (itemCategory == null) {
            return new ItemCategory(id);
        }
        return itemCategory;
    }
}
