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

import javax.annotation.Nullable;

/**
 * Stores a list of categories of Item Types.
 */
public final class ItemCategories {

    public static final ItemCategory ACACIA_LOGS = register("minecraft:acacia_logs");
    public static final ItemCategory ANVIL = register("minecraft:anvil");
    public static final ItemCategory BANNERS = register("minecraft:banners");
    public static final ItemCategory BIRCH_LOGS = register("minecraft:birch_logs");
    public static final ItemCategory BOATS = register("minecraft:boats");
    public static final ItemCategory BUTTONS = register("minecraft:buttons");
    public static final ItemCategory CARPETS = register("minecraft:carpets");
    public static final ItemCategory CORAL = register("minecraft:coral");
    public static final ItemCategory CORAL_PLANTS = register("minecraft:coral_plants");
    public static final ItemCategory DARK_OAK_LOGS = register("minecraft:dark_oak_logs");
    public static final ItemCategory DOORS = register("minecraft:doors");
    public static final ItemCategory FISHES = register("minecraft:fishes");
    public static final ItemCategory JUNGLE_LOGS = register("minecraft:jungle_logs");
    public static final ItemCategory LEAVES = register("minecraft:leaves");
    public static final ItemCategory LOGS = register("minecraft:logs");
    public static final ItemCategory OAK_LOGS = register("minecraft:oak_logs");
    public static final ItemCategory PLANKS = register("minecraft:planks");
    public static final ItemCategory RAILS = register("minecraft:rails");
    public static final ItemCategory SAND = register("minecraft:sand");
    public static final ItemCategory SAPLINGS = register("minecraft:saplings");
    public static final ItemCategory SLABS = register("minecraft:slabs");
    public static final ItemCategory SPRUCE_LOGS = register("minecraft:spruce_logs");
    public static final ItemCategory STAIRS = register("minecraft:stairs");
    public static final ItemCategory STONE_BRICKS = register("minecraft:stone_bricks");
    public static final ItemCategory WOODEN_BUTTONS = register("minecraft:wooden_buttons");
    public static final ItemCategory WOODEN_DOORS = register("minecraft:wooden_doors");
    public static final ItemCategory WOODEN_PRESSURE_PLATES = register("minecraft:wooden_pressure_plates");
    public static final ItemCategory WOODEN_SLABS = register("minecraft:wooden_slabs");
    public static final ItemCategory WOODEN_STAIRS = register("minecraft:wooden_stairs");
    public static final ItemCategory WOOL = register("minecraft:wool");

    private ItemCategories() {
    }

    private static ItemCategory register(final String id) {
        return register(new ItemCategory(id));
    }

    public static ItemCategory register(final ItemCategory tag) {
        return ItemCategory.REGISTRY.register(tag.getId(), tag);
    }

    public static @Nullable ItemCategory get(final String id) {
        return ItemCategory.REGISTRY.get(id);
    }
}
