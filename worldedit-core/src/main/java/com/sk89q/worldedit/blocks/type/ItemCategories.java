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

package com.sk89q.worldedit.blocks.type;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Stores a list of categories of Item Types.
 */
public class ItemCategories {

    private ItemCategories() {
    }

    public static final ItemCategory ACACIA_LOGS = new ItemCategory("minecraft:acacia_logs");
    public static final ItemCategory ANVIL = new ItemCategory("minecraft:anvil");
    public static final ItemCategory BANNERS = new ItemCategory("minecraft:banners");
    public static final ItemCategory BIRCH_LOGS = new ItemCategory("minecraft:birch_logs");
    public static final ItemCategory BOATS = new ItemCategory("minecraft:boats");
    public static final ItemCategory BUTTONS = new ItemCategory("minecraft:buttons");
    public static final ItemCategory CARPETS = new ItemCategory("minecraft:carpets");
    public static final ItemCategory CORAL = new ItemCategory("minecraft:coral");
    public static final ItemCategory CORAL_PLANTS = new ItemCategory("minecraft:coral_plants");
    public static final ItemCategory DARK_OAK_LOGS = new ItemCategory("minecraft:dark_oak_logs");
    public static final ItemCategory DOORS = new ItemCategory("minecraft:doors");
    public static final ItemCategory FISHES = new ItemCategory("minecraft:fishes");
    public static final ItemCategory JUNGLE_LOGS = new ItemCategory("minecraft:jungle_logs");
    public static final ItemCategory LEAVES = new ItemCategory("minecraft:leaves");
    public static final ItemCategory LOGS = new ItemCategory("minecraft:logs");
    public static final ItemCategory OAK_LOGS = new ItemCategory("minecraft:oak_logs");
    public static final ItemCategory PLANKS = new ItemCategory("minecraft:planks");
    public static final ItemCategory RAILS = new ItemCategory("minecraft:rails");
    public static final ItemCategory SAND = new ItemCategory("minecraft:sand");
    public static final ItemCategory SAPLINGS = new ItemCategory("minecraft:saplings");
    public static final ItemCategory SLABS = new ItemCategory("minecraft:slabs");
    public static final ItemCategory SPRUCE_LOGS = new ItemCategory("minecraft:spruce_logs");
    public static final ItemCategory STAIRS = new ItemCategory("minecraft:stairs");
    public static final ItemCategory STONE_BRICKS = new ItemCategory("minecraft:stone_bricks");
    public static final ItemCategory WOODEN_BUTTONS = new ItemCategory("minecraft:wooden_buttons");
    public static final ItemCategory WOODEN_DOORS = new ItemCategory("minecraft:wooden_doors");
    public static final ItemCategory WOODEN_PRESSURE_PLATES = new ItemCategory("minecraft:wooden_pressure_plates");
    public static final ItemCategory WOODEN_SLABS = new ItemCategory("minecraft:wooden_slabs");
    public static final ItemCategory WOODEN_STAIRS = new ItemCategory("minecraft:wooden_stairs");
    public static final ItemCategory WOOL = new ItemCategory("minecraft:wool");

    private static final Map<String, ItemCategory> categoryMapping = new HashMap<>();

    static {
        for (Field field : ItemCategories.class.getFields()) {
            if (field.getType() == ItemCategory.class) {
                try {
                    registerCategory((ItemCategory) field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void registerCategory(ItemCategory itemCategory) {
        if (categoryMapping.containsKey(itemCategory.getId()) && !itemCategory.getId().startsWith("minecraft:")) {
            throw new IllegalArgumentException("Existing category with this ID already registered");
        }

        categoryMapping.put(itemCategory.getId(), itemCategory);
    }

    @Nullable
    public static ItemCategory getBlockType(String id) {
        // If it has no namespace, assume minecraft.
        if (id != null && !id.contains(":")) {
            id = "minecraft:" + id;
        }
        return categoryMapping.get(id);
    }

    public static Collection<ItemCategory> values() {
        return categoryMapping.values();
    }
}
