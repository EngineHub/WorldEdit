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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Stores a list of common Block String IDs.
 */
public class BlockTypes {

    private BlockTypes() {
    }

    public static final String AIR = "minecraft:air";
    public static final String GRASS = "minecraft:grass";
    public static final String WATER = "minecraft:water";
    public static final String LAVA = "minecraft:lava";
    public static final String WOOL = "minecraft:wool";
    public static final String STATIONARY_WATER = "minecraft:stationary_water";
    public static final String STATIONARY_LAVA = "minecraft:stationary_lava";
    public static final String WALL_SIGN = "minecraft:wall_sign";
    public static final String SIGN_POST = "minecraft:sign_post";

    private static final Map<String, BlockType> blockMapping = new HashMap<>();

    public static void registerBlock(BlockType blockType) {
        if (blockMapping.containsKey(blockType.getId())) {
            throw new IllegalArgumentException("Existing block with this ID already registered");
        }

        blockMapping.put(blockType.getId(), blockType);
    }

    @Nullable
    public static BlockType getBlockType(String id) {
        return blockMapping.get(id);
    }
}
