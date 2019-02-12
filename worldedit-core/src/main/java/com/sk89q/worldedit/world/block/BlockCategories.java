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

import javax.annotation.Nullable;

/**
 * Stores a list of categories of Block Types.
 */
public final class BlockCategories {

    public static final BlockCategory ACACIA_LOGS = register("minecraft:acacia_logs");
    public static final BlockCategory ANVIL = register("minecraft:anvil");
    public static final BlockCategory BANNERS = register("minecraft:banners");
    public static final BlockCategory BIRCH_LOGS = register("minecraft:birch_logs");
    public static final BlockCategory BUTTONS = register("minecraft:buttons");
    public static final BlockCategory CARPETS = register("minecraft:carpets");
    public static final BlockCategory CORALS = register("minecraft:corals");
    public static final BlockCategory CORAL_BLOCKS = register("minecraft:coral_blocks");
    public static final BlockCategory DARK_OAK_LOGS = register("minecraft:dark_oak_logs");
    public static final BlockCategory DOORS = register("minecraft:doors");
    public static final BlockCategory ENDERMAN_HOLDABLE = register("minecraft:enderman_holdable");
    public static final BlockCategory FLOWER_POTS = register("minecraft:flower_pots");
    public static final BlockCategory ICE = register("minecraft:ice");
    public static final BlockCategory JUNGLE_LOGS = register("minecraft:jungle_logs");
    public static final BlockCategory LEAVES = register("minecraft:leaves");
    public static final BlockCategory LOGS = register("minecraft:logs");
    public static final BlockCategory OAK_LOGS = register("minecraft:oak_logs");
    public static final BlockCategory PLANKS = register("minecraft:planks");
    public static final BlockCategory RAILS = register("minecraft:rails");
    public static final BlockCategory SAND = register("minecraft:sand");
    public static final BlockCategory SAPLINGS = register("minecraft:saplings");
    public static final BlockCategory SLABS = register("minecraft:slabs");
    public static final BlockCategory SPRUCE_LOGS = register("minecraft:spruce_logs");
    public static final BlockCategory STAIRS = register("minecraft:stairs");
    public static final BlockCategory STONE_BRICKS = register("minecraft:stone_bricks");
    public static final BlockCategory VALID_SPAWN = register("minecraft:valid_spawn");
    public static final BlockCategory WOODEN_BUTTONS = register("minecraft:wooden_buttons");
    public static final BlockCategory WOODEN_DOORS = register("minecraft:wooden_doors");
    public static final BlockCategory WOODEN_PRESSURE_PLATES = register("minecraft:wooden_pressure_plates");
    public static final BlockCategory WOODEN_SLABS = register("minecraft:wooden_slabs");
    public static final BlockCategory WOODEN_STAIRS = register("minecraft:wooden_stairs");
    public static final BlockCategory WOOL = register("minecraft:wool");

    private BlockCategories() {
    }

    private static BlockCategory register(final String id) {
        return register(new BlockCategory(id));
    }

    public static BlockCategory register(final BlockCategory tag) {
        return BlockCategory.REGISTRY.register(tag.getId(), tag);
    }

    public static @Nullable BlockCategory get(final String id) {
        return BlockCategory.REGISTRY.get(id);
    }
}
