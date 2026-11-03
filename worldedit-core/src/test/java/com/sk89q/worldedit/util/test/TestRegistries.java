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

import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BlockRegistry;
import com.sk89q.worldedit.world.registry.BundledRegistries;

/**
 * Registries for tests, whose blocks have the real properties described in {@code test-blocks.json}.
 *
 * <p>Anything that touches {@link BlockType#REGISTRY} or the platform must hold the
 * {@link ResourceLockKeys#WORLDEDIT_PLATFORM} lock.</p>
 */
public class TestRegistries extends BundledRegistries {

    private final BlockRegistry blockRegistry = new TestBlockRegistry();

    public TestRegistries() {
    }

    @Override
    public BlockRegistry getBlockRegistry() {
        return blockRegistry;
    }

    /**
     * Registers a block type for every block in the test data that is not registered yet.
     *
     * <p>This is idempotent so that several test classes can share it, and it never removes anything.
     * Use {@link #unregisterBlocks()} to clean up.</p>
     */
    public static void registerBlocks() {
        for (String id : TestBlockRegistry.blockIds()) {
            if (BlockType.REGISTRY.get(id) == null) {
                BlockType.REGISTRY.register(id, new BlockType(id));
            }
        }
    }

    /**
     * Clears {@link BlockType#REGISTRY}, so that the next test class starts from an empty registry.
     */
    public static void unregisterBlocks() {
        BlockType.REGISTRY.clear();
    }
}
