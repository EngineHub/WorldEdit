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


package com.sk89q.worldedit.extent.clipboard;

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.test.TestRegistries;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import static com.sk89q.worldedit.util.test.ExtentTestSupport.block;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockArrayClipboardTest extends BaseWorldEditTest {

    @BeforeAll
    static void registerBlocks() {
        TestRegistries.registerBlocks();
    }

    @AfterAll
    static void unregisterBlocks() {
        TestRegistries.unregisterBlocks();
    }

    static IntStream seeds() {
        return IntStream.range(0, 30);
    }

    @ParameterizedTest(name = "seed {0}")
    @MethodSource("seeds")
    void everyPositionStoresItsOwnBlock(int seed) throws WorldEditException {
        Random random = new Random(seed);
        // Regions with negative coordinates and a non-cubic shape catch stride mix-ups
        BlockVector3 min = BlockVector3.at(random.nextInt(200) - 100, random.nextInt(100) - 64, random.nextInt(200) - 100);
        BlockVector3 max = min.add(random.nextInt(6), random.nextInt(9), random.nextInt(6));
        CuboidRegion region = new CuboidRegion(min, max);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        List<BlockState> palette = BlockTypes.get("minecraft:oak_stairs").getAllStates();

        Map<BlockVector3, BlockState> expected = new HashMap<>();
        for (BlockVector3 position : region) {
            BlockState state = palette.get(random.nextInt(palette.size()));
            assertTrue(clipboard.setBlock(position, state));
            expected.put(position, state);
        }

        assertEquals(region.getVolume(), expected.size());
        expected.forEach((position, state) -> {
            assertEquals(state, clipboard.getBlock(position), () -> "block at " + position);
            assertEquals(state.toBaseBlock(), clipboard.getFullBlock(position), () -> "full block at " + position);
        });
    }

    @Test
    void unsetBlocksAreAir() {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(
            new CuboidRegion(BlockVector3.at(-2, -2, -2), BlockVector3.at(2, 2, 2)));

        assertEquals(block("minecraft:air"), clipboard.getBlock(BlockVector3.at(-2, 0, 2)));
        assertEquals(block("minecraft:air").toBaseBlock(), clipboard.getFullBlock(BlockVector3.at(0, 0, 0)));
    }

    @Test
    void settingOutsideTheRegionFailsAndStoresNothing() throws WorldEditException {
        BlockVector3 min = BlockVector3.at(0, -4, 0);
        BlockVector3 max = BlockVector3.at(3, 3, 3);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(new CuboidRegion(min, max));
        List<BlockVector3> outside = List.of(
            min.add(-1, 0, 0), min.add(0, -1, 0), min.add(0, 0, -1),
            max.add(1, 0, 0), max.add(0, 1, 0), max.add(0, 0, 1),
            BlockVector3.at(Integer.MAX_VALUE, 0, 0), BlockVector3.at(0, Integer.MIN_VALUE, 0)
        );

        for (BlockVector3 position : outside) {
            assertFalse(clipboard.setBlock(position, block("minecraft:stone")), position.toString());
            assertEquals(block("minecraft:air"), clipboard.getBlock(position), position.toString());
        }
        // Nothing may have wrapped around into the inside either
        for (BlockVector3 position : new CuboidRegion(min, max)) {
            assertEquals(block("minecraft:air"), clipboard.getBlock(position), position.toString());
        }
    }

    @Test
    void originDefaultsToTheMinimumPointAndCanBeMoved() {
        BlockVector3 min = BlockVector3.at(-3, -64, 8);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(new CuboidRegion(min.add(4, 5, 6), min));

        assertEquals(min, clipboard.getOrigin());
        assertEquals(min, clipboard.getMinimumPoint());
        assertEquals(min.add(4, 5, 6), clipboard.getMaximumPoint());
        assertEquals(BlockVector3.at(5, 6, 7), clipboard.getDimensions());

        clipboard.setOrigin(BlockVector3.at(100, 100, 100));
        assertEquals(BlockVector3.at(100, 100, 100), clipboard.getOrigin());
        assertEquals(min, clipboard.getMinimumPoint(), "the origin does not move the region");
    }

    @Test
    void clipboardIsIndependentOfTheRegionItWasCreatedFrom() throws WorldEditException {
        CuboidRegion region = new CuboidRegion(BlockVector3.ZERO, BlockVector3.at(2, 2, 2));
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        region.expand(BlockVector3.at(10, 0, 0));
        clipboard.getRegion().shift(BlockVector3.at(50, 0, 0));

        assertEquals(BlockVector3.at(2, 2, 2), clipboard.getMaximumPoint());
        assertNotSame(clipboard.getRegion(), clipboard.getRegion());
        assertTrue(clipboard.setBlock(BlockVector3.at(2, 2, 2), block("minecraft:stone")));
        assertFalse(clipboard.setBlock(BlockVector3.at(5, 0, 0), block("minecraft:stone")));
    }

    @Test
    void biomesDefaultToOceanAndOnlyStoreInsideTheRegion() {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(
            new CuboidRegion(BlockVector3.at(-2, -2, -2), BlockVector3.at(2, 2, 2)));
        assertFalse(clipboard.hasBiomes());
        assertEquals(BiomeTypes.OCEAN, clipboard.getBiome(BlockVector3.ZERO));

        assertTrue(clipboard.setBiome(BlockVector3.at(-2, -2, 2), BiomeTypes.PLAINS));
        assertFalse(clipboard.setBiome(BlockVector3.at(3, 0, 0), BiomeTypes.PLAINS));
        assertTrue(clipboard.hasBiomes());
        assertEquals(BiomeTypes.PLAINS, clipboard.getBiome(BlockVector3.at(-2, -2, 2)));
        assertEquals(BiomeTypes.OCEAN, clipboard.getBiome(BlockVector3.at(-2, -2, -2)));
        assertEquals(BiomeTypes.OCEAN, clipboard.getBiome(BlockVector3.at(3, 0, 0)));
    }

    @Test
    void entitiesAreStoredFilteredByRegionAndRemovable() {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(
            new CuboidRegion(BlockVector3.at(-5, -5, -5), BlockVector3.at(5, 5, 5)));
        BaseEntity state = new BaseEntity(new EntityType("minecraft:item"));
        Entity inside = clipboard.createEntity(new Location(clipboard, Vector3.at(0.5, -2.5, 0.5)), state);
        clipboard.createEntity(new Location(clipboard, Vector3.at(4.5, 4.5, 4.5)), state);

        assertEquals(2, clipboard.getEntities().size());
        assertEquals(1, clipboard.getEntities(new CuboidRegion(BlockVector3.at(-1, -3, -1), BlockVector3.at(1, -2, 1))).size());
        assertTrue(inside.remove());
        assertFalse(inside.remove(), "already removed");
        assertEquals(1, clipboard.getEntities().size());
    }
}
