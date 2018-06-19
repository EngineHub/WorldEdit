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
 * Stores a list of categories of Block Types.
 */
public class BlockCategories {

    private BlockCategories() {
    }

    public static final BlockCategory ACACIA_LOGS = new BlockCategory("minecraft:acacia_logs");
    public static final BlockCategory ANVIL = new BlockCategory("minecraft:anvil");
    public static final BlockCategory BANNERS = new BlockCategory("minecraft:banners");
    public static final BlockCategory BIRCH_LOGS = new BlockCategory("minecraft:birch_logs");
    public static final BlockCategory BUTTONS = new BlockCategory("minecraft:buttons");
    public static final BlockCategory CARPETS = new BlockCategory("minecraft:carpets");
    public static final BlockCategory CORAL = new BlockCategory("minecraft:coral");
    public static final BlockCategory CORAL_PLANTS = new BlockCategory("minecraft:coral_plants");
    public static final BlockCategory DARK_OAK_LOGS = new BlockCategory("minecraft:dark_oak_logs");
    public static final BlockCategory DOORS = new BlockCategory("minecraft:doors");
    public static final BlockCategory ENDERMAN_HOLDABLE = new BlockCategory("minecraft:enderman_holdable");
    public static final BlockCategory FLOWER_POTS = new BlockCategory("minecraft:flower_pots");
    public static final BlockCategory ICE = new BlockCategory("minecraft:ice");
    public static final BlockCategory JUNGLE_LOGS = new BlockCategory("minecraft:jungle_logs");
    public static final BlockCategory LEAVES = new BlockCategory("minecraft:leaves");
    public static final BlockCategory LOGS = new BlockCategory("minecraft:logs");
    public static final BlockCategory OAK_LOGS = new BlockCategory("minecraft:oak_logs");
    public static final BlockCategory PLANKS = new BlockCategory("minecraft:planks");
    public static final BlockCategory RAILS = new BlockCategory("minecraft:rails");
    public static final BlockCategory SAND = new BlockCategory("minecraft:sand");
    public static final BlockCategory SAPLINGS = new BlockCategory("minecraft:saplings");
    public static final BlockCategory SLABS = new BlockCategory("minecraft:slabs");
    public static final BlockCategory SPRUCE_LOGS = new BlockCategory("minecraft:spruce_logs");
    public static final BlockCategory STAIRS = new BlockCategory("minecraft:stairs");
    public static final BlockCategory STONE_BRICKS = new BlockCategory("minecraft:stone_bricks");
    public static final BlockCategory VALID_SPAWN = new BlockCategory("minecraft:valid_spawn");
    public static final BlockCategory WOODEN_BUTTONS = new BlockCategory("minecraft:wooden_buttons");
    public static final BlockCategory WOODEN_DOORS = new BlockCategory("minecraft:wooden_doors");
    public static final BlockCategory WOODEN_PRESSURE_PLATES = new BlockCategory("minecraft:wooden_pressure_plates");
    public static final BlockCategory WOODEN_SLABS = new BlockCategory("minecraft:wooden_slabs");
    public static final BlockCategory WOODEN_STAIRS = new BlockCategory("minecraft:wooden_stairs");
    public static final BlockCategory WOOL = new BlockCategory("minecraft:wool");

    private static final Map<String, BlockCategory> categoryMapping = new HashMap<>();

    static {
        for (Field field : BlockCategories.class.getFields()) {
            if (field.getType() == BlockCategory.class) {
                try {
                    registerCategory((BlockCategory) field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void registerCategory(BlockCategory blockCategory) {
        if (categoryMapping.containsKey(blockCategory.getId()) && !blockCategory.getId().startsWith("minecraft:")) {
            throw new IllegalArgumentException("Existing category with this ID already registered");
        }

        categoryMapping.put(blockCategory.getId(), blockCategory);
    }

    @Nullable
    public static BlockCategory getBlockCategory(String id) {
        // If it has no namespace, assume minecraft.
        if (id != null && !id.contains(":")) {
            id = "minecraft:" + id;
        }
        return categoryMapping.get(id);
    }

    public static Collection<BlockCategory> values() {
        return categoryMapping.values();
    }
}
