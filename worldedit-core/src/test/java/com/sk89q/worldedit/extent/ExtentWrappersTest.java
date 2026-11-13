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


package com.sk89q.worldedit.extent;

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.buffer.ExtentBuffer;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.test.TestRegistries;
import com.sk89q.worldedit.util.test.TestWorld;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.sk89q.worldedit.util.test.ExtentTestSupport.block;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the small wrapping extents: masking, tracing, buffering, null and delegation.
 */
class ExtentWrappersTest extends BaseWorldEditTest {

    private static final BlockVector3 INSIDE = BlockVector3.at(1, -10, 1);
    private static final BlockVector3 OUTSIDE = BlockVector3.at(50, -10, 50);
    private static final Mask ONLY_INSIDE = new RegionMask(new CuboidRegion(BlockVector3.at(0, -20, 0), BlockVector3.at(10, 0, 10)));

    @BeforeAll
    static void registerBlocks() {
        TestRegistries.registerBlocks();
    }

    @AfterAll
    static void unregisterBlocks() {
        TestRegistries.unregisterBlocks();
    }

    // MaskingExtent

    @Test
    void maskingExtentOnlyPlacesBlocksTheMaskAccepts() throws WorldEditException {
        TestWorld world = new TestWorld();
        MaskingExtent extent = new MaskingExtent(world, ONLY_INSIDE);

        assertTrue(extent.setBlock(INSIDE, block("minecraft:stone")));
        assertFalse(extent.setBlock(OUTSIDE, block("minecraft:stone")));

        assertEquals(block("minecraft:stone"), world.getBlock(INSIDE));
        assertEquals(block("minecraft:air"), world.getBlock(OUTSIDE));
        assertEquals(1, world.getBlocks().size());
    }

    @Test
    void maskingExtentReadsAreNotMasked() throws WorldEditException {
        TestWorld world = new TestWorld();
        world.setBlock(OUTSIDE, block("minecraft:glass"));

        assertEquals(block("minecraft:glass"), new MaskingExtent(world, ONLY_INSIDE).getBlock(OUTSIDE));
    }

    @Test
    void maskingExtentUsesTheCurrentMaskAndMasksBiomes() throws WorldEditException {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(
            new CuboidRegion(BlockVector3.at(0, -20, 0), BlockVector3.at(60, 0, 60)));
        MaskingExtent extent = new MaskingExtent(clipboard, ONLY_INSIDE);

        assertTrue(extent.setBiome(INSIDE, BiomeTypes.PLAINS));
        assertFalse(extent.setBiome(OUTSIDE, BiomeTypes.PLAINS));
        assertEquals(BiomeTypes.OCEAN, clipboard.getBiome(OUTSIDE));

        extent.setMask(_ -> true);
        assertTrue(extent.setBlock(OUTSIDE, block("minecraft:stone")));
        assertEquals(block("minecraft:stone"), clipboard.getBlock(OUTSIDE));
        assertThrows(NullPointerException.class, () -> extent.setMask(null));
    }

    // ExtentBuffer

    @Test
    void extentBufferHoldsWritesWithoutTouchingTheDelegate() throws WorldEditException {
        TestWorld world = new TestWorld();
        world.setBlock(INSIDE, block("minecraft:glass"));
        ExtentBuffer buffer = new ExtentBuffer(world);

        assertTrue(buffer.setBlock(INSIDE, block("minecraft:stone")));
        assertTrue(buffer.setBlock(OUTSIDE, block("minecraft:oak_planks")));

        assertEquals(block("minecraft:stone"), buffer.getBlock(INSIDE));
        assertEquals(block("minecraft:oak_planks"), buffer.getFullBlock(OUTSIDE).toImmutableState());
        assertEquals(block("minecraft:glass"), world.getBlock(INSIDE), "the delegate is not written");
        assertEquals(block("minecraft:air"), world.getBlock(OUTSIDE));
        assertEquals(1, world.getBlocks().size());
    }

    @Test
    void extentBufferFallsThroughForUnbufferedPositionsAndMasksWrites() throws WorldEditException {
        TestWorld world = new TestWorld();
        world.setBlock(OUTSIDE, block("minecraft:glass"));
        ExtentBuffer buffer = new ExtentBuffer(world, ONLY_INSIDE);

        assertFalse(buffer.setBlock(OUTSIDE, block("minecraft:stone")));

        assertEquals(block("minecraft:glass"), buffer.getBlock(OUTSIDE), "a masked-out write was ignored");
        assertEquals(block("minecraft:air"), buffer.getBlock(BlockVector3.at(2, -10, 2)));
    }

    // TracingExtent

    @Test
    void tracingExtentRecordsTouchedAndFailedLocations() throws WorldEditException {
        TracingExtent tracing = new TracingExtent(new MaskingExtent(new TestWorld(), ONLY_INSIDE));
        assertFalse(tracing.isActive());

        tracing.setBlock(INSIDE, block("minecraft:stone"));
        tracing.setBlock(OUTSIDE, block("minecraft:stone"));

        assertTrue(tracing.isActive());
        assertEquals(Set.of(INSIDE, OUTSIDE), tracing.getTouchedLocations());
        assertEquals(Set.of(TracingExtent.Action.SET_BLOCK), tracing.getFailedActions().get(OUTSIDE));
        assertTrue(tracing.getFailedActions().get(INSIDE).isEmpty());
    }

    @Test
    void tracingExtentRecordsFailedBiomeAndEntityActions() {
        TracingExtent tracing = new TracingExtent(new NullExtent());
        BlockVector3 position = BlockVector3.at(-3, -70, 4);

        tracing.setBiome(position, BiomeTypes.PLAINS);
        Location location = new Location(tracing, Vector3.at(-2.5, 8.5, 1.5));
        assertNull(tracing.createEntity(location, new BaseEntity(new EntityType("minecraft:item"))));

        assertEquals(Set.of(TracingExtent.Action.SET_BIOME), tracing.getFailedActions().get(position));
        assertEquals(Set.of(TracingExtent.Action.CREATE_ENTITY), tracing.getFailedActions().get(BlockVector3.at(-3, 8, 1)));
        assertEquals(2, tracing.getTouchedLocations().size());
    }

    @Test
    void tracingExtentTouchedLocationsAreReadOnly() {
        TracingExtent tracing = new TracingExtent(new NullExtent());
        assertThrows(UnsupportedOperationException.class, () -> tracing.getTouchedLocations().add(INSIDE));
        assertThrows(UnsupportedOperationException.class,
            () -> tracing.getFailedActions().put(INSIDE, TracingExtent.Action.SET_BLOCK));
    }

    // NullExtent

    @Test
    void nullExtentRefusesEverythingAndReadsAir() throws WorldEditException {
        NullExtent extent = new NullExtent();

        assertFalse(extent.setBlock(INSIDE, block("minecraft:stone")));
        assertFalse(extent.setBiome(INSIDE, BiomeTypes.PLAINS));
        assertNull(extent.createEntity(new Location(extent, Vector3.ZERO), new BaseEntity(new EntityType("minecraft:item"))));
        assertEquals(BlockTypes.AIR, extent.getBlock(INSIDE).getBlockType());
        assertEquals(BlockTypes.AIR, extent.getFullBlock(INSIDE).getBlockType());
        assertEquals(BiomeTypes.THE_VOID, extent.getBiome(INSIDE));
        assertTrue(extent.getEntities().isEmpty());
        assertTrue(extent.getEntities(new CuboidRegion(BlockVector3.ZERO, BlockVector3.at(5, 5, 5))).isEmpty());
        assertNull(extent.commit());
        assertEquals(BlockVector3.ZERO, extent.getMinimumPoint());
        assertEquals(BlockVector3.ZERO, extent.getMaximumPoint());
    }

    // AbstractDelegateExtent

    @Test
    void delegateExtentPassesWritesAndReadsThrough() throws WorldEditException {
        TestWorld world = new TestWorld();
        AbstractDelegateExtent delegate = new AbstractDelegateExtent(world) {
        };

        assertSame(world, delegate.getExtent());
        assertTrue(delegate.setBlock(INSIDE, block("minecraft:stone")));
        assertEquals(block("minecraft:stone"), world.getBlock(INSIDE));
        assertEquals(block("minecraft:stone"), delegate.getBlock(INSIDE));
        assertFalse(delegate.setBlock(INSIDE, block("minecraft:stone")), "unchanged block reports no change");
        assertThrows(NullPointerException.class, () -> new AbstractDelegateExtent(null) {
        });
    }
}
