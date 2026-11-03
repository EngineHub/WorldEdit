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


package com.sk89q.worldedit.util.test;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Small helpers shared by the extent, copy and history tests.
 */
public final class ExtentTestSupport {

    private ExtentTestSupport() {
    }

    /**
     * Gets the default state of a registered block type.
     */
    public static BlockState block(String id) {
        BlockType type = Objects.requireNonNull(BlockType.REGISTRY.get(id), id);
        return type.getDefaultState();
    }

    /**
     * Reads every block in the region, so that two extents can be compared by content.
     */
    public static Map<BlockVector3, BaseBlock> snapshot(Extent extent, Region region) {
        Map<BlockVector3, BaseBlock> blocks = new LinkedHashMap<>();
        for (BlockVector3 position : region) {
            blocks.put(position, extent.getFullBlock(position));
        }
        return blocks;
    }
}
