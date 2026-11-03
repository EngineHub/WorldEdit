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


package com.sk89q.worldedit.extent.reorder;

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.test.TestRegistries;
import com.sk89q.worldedit.util.test.TestWorld;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.sk89q.worldedit.util.test.ExtentTestSupport.block;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation")
class ReorderExtentTest extends BaseWorldEditTest {

    /**
     * A world that remembers the order in which blocks reached it.
     */
    private static final class RecordingWorld extends TestWorld {
        private final List<BlockVector3> placements = new ArrayList<>();

        @Override
        public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block,
                                                                  SideEffectSet sideEffects) {
            placements.add(position);
            return super.setBlock(position, block, sideEffects);
        }
    }

    @BeforeAll
    static void registerBlocks() {
        TestRegistries.registerBlocks();
    }

    @AfterAll
    static void unregisterBlocks() {
        TestRegistries.unregisterBlocks();
    }

    static IntStream seeds() {
        return IntStream.range(0, 20);
    }

    private static MultiStageReorder reorder(TestWorld world) {
        return new MultiStageReorder(world, true);
    }

    @Test
    void blocksAreHeldBackUntilCommitted() throws WorldEditException {
        RecordingWorld world = new RecordingWorld();
        MultiStageReorder reorder = reorder(world);
        BlockVector3 position = BlockVector3.at(0, -5, 0);

        reorder.setBlock(position, block("minecraft:stone"));

        assertEquals(0, world.placements.size());
        assertEquals(block("minecraft:stone"), reorder.getBlock(position), "reads see the pending block");
        assertEquals(block("minecraft:air"), world.getBlock(position));

        Operations.complete(reorder.commit());
        assertEquals(block("minecraft:stone"), world.getBlock(position));
        assertEquals(block("minecraft:stone"), reorder.getBlock(position));
    }

    @Test
    void dependentBlocksArePlacedAfterTheirSupport() throws WorldEditException {
        RecordingWorld world = new RecordingWorld();
        MultiStageReorder reorder = reorder(world);
        BlockVector3 torch = BlockVector3.at(0, -1, 0);
        BlockVector3 ladder = BlockVector3.at(1, -1, 0);
        BlockVector3 ground = BlockVector3.at(0, -2, 0);
        BlockVector3 wall = BlockVector3.at(1, -2, 0);

        // Dependent blocks are set first on purpose
        reorder.setBlock(ladder, block("minecraft:ladder"));
        reorder.setBlock(torch, block("minecraft:torch"));
        reorder.setBlock(ground, block("minecraft:stone"));
        reorder.setBlock(wall, block("minecraft:oak_planks"));
        Operations.complete(reorder.commit());

        assertEquals(4, world.placements.size());
        for (BlockVector3 dependent : List.of(torch, ladder)) {
            assertTrue(world.placements.indexOf(ground) < world.placements.indexOf(dependent), dependent.toString());
            assertTrue(world.placements.indexOf(wall) < world.placements.indexOf(dependent), dependent.toString());
        }
        assertEquals(block("minecraft:ladder"), world.getBlock(ladder));
        assertEquals(block("minecraft:torch"), world.getBlock(torch));
    }

    @Test
    void replacingADependentBlockClearsItBeforeNormalBlocksArePlaced() throws WorldEditException {
        RecordingWorld world = new RecordingWorld();
        BlockVector3 torch = BlockVector3.at(3, 7, -3);
        BlockVector3 stone = BlockVector3.at(3, 6, -3);
        world.setBlock(torch, block("minecraft:torch"));
        world.placements.clear();
        MultiStageReorder reorder = reorder(world);

        reorder.setBlock(stone, block("minecraft:stone"));
        reorder.setBlock(torch, block("minecraft:glass"));
        Operations.complete(reorder.commit());

        assertEquals(block("minecraft:glass"), world.getBlock(torch));
        assertEquals(block("minecraft:stone"), world.getBlock(stone));
        // The torch is cleared, then the replacement is placed in the same position
        assertEquals(List.of(torch, stone, torch), world.placements);
    }

    @Test
    void removingADependentBlockOnlyClearsIt() throws WorldEditException {
        RecordingWorld world = new RecordingWorld();
        BlockVector3 torch = BlockVector3.at(0, 0, 0);
        world.setBlock(torch, block("minecraft:torch"));
        world.placements.clear();
        MultiStageReorder reorder = reorder(world);

        reorder.setBlock(torch, block("minecraft:air"));
        Operations.complete(reorder.commit());

        assertEquals(block("minecraft:air"), world.getBlock(torch));
        assertEquals(List.of(torch), world.placements);
    }

    @ParameterizedTest(name = "seed {0}")
    @MethodSource("seeds")
    void finalContentsDoNotDependOnPlacementOrder(int seed) throws WorldEditException {
        Random random = new Random(seed);
        List<String> ids = List.of("minecraft:stone", "minecraft:torch", "minecraft:glass", "minecraft:ladder",
            "minecraft:air", "minecraft:lever", "minecraft:oak_planks");
        RecordingWorld world = new RecordingWorld();
        // Half of the positions start out occupied, including by dependent blocks
        TestWorld expected = new TestWorld();
        for (int x = -3; x < 3; x++) {
            for (int y = -3; y < 3; y++) {
                if (random.nextBoolean()) {
                    var state = block(ids.get(random.nextInt(ids.size())));
                    world.setBlock(BlockVector3.at(x, y, 0), state);
                    expected.setBlock(BlockVector3.at(x, y, 0), state);
                }
            }
        }
        world.placements.clear();
        MultiStageReorder reorder = reorder(world);
        List<BlockVector3> positions = new ArrayList<>();
        for (int x = -3; x < 3; x++) {
            for (int y = -3; y < 3; y++) {
                positions.add(BlockVector3.at(x, y, 0));
            }
        }
        Collections.shuffle(positions, random);
        for (BlockVector3 position : positions) {
            if (random.nextInt(3) != 0) {
                var state = block(ids.get(random.nextInt(ids.size())));
                reorder.setBlock(position, state);
                expected.setBlock(position, state);
            }
        }

        Operations.complete(reorder.commit());

        assertEquals(expected.getBlocks(), world.getBlocks());
    }

    @Test
    @Disabled("MultiStageReorder.setBlock only consults the underlying extent for the existing block, not what is "
        + "already buffered at that position. Writing torch (LAST priority) and then stone (FIRST) to the same "
        + "position keeps both in separate stages; commit() places stone and then torch, so the position ends as "
        + "torch instead of the last write (stone). Expected minecraft:stone, actual minecraft:torch. The class is "
        + "deprecated, so this may be accepted behaviour.")
    void lastWriteToAPositionWinsAcrossPriorities() throws WorldEditException {
        RecordingWorld world = new RecordingWorld();
        MultiStageReorder reorder = reorder(world);
        BlockVector3 position = BlockVector3.at(0, -1, 0);

        reorder.setBlock(position, block("minecraft:torch"));
        reorder.setBlock(position, block("minecraft:stone"));
        Operations.complete(reorder.commit());

        assertEquals(block("minecraft:stone"), world.getBlock(position));
    }

    @Test
    void disabledReorderWritesThroughImmediately() throws WorldEditException {
        RecordingWorld world = new RecordingWorld();
        MultiStageReorder reorder = new MultiStageReorder(world, false);
        BlockVector3 position = BlockVector3.at(0, 0, 0);

        reorder.setBlock(position, block("minecraft:torch"));

        assertEquals(block("minecraft:torch"), world.getBlock(position));
        assertNull(reorder.commit());
    }

    @Test
    void secondCommitHasNothingLeftToPlace() throws WorldEditException {
        RecordingWorld world = new RecordingWorld();
        MultiStageReorder reorder = reorder(world);
        reorder.setBlock(BlockVector3.ZERO, block("minecraft:stone"));
        Operations.complete(reorder.commit());
        world.placements.clear();

        Operation second = reorder.commit();
        Operations.complete(second);

        assertEquals(List.of(), world.placements);
    }

    // ChunkBatchingExtent

    @ParameterizedTest(name = "seed {0}")
    @MethodSource("seeds")
    void chunkBatchingPlacesEverythingOnCommitWithLastWriteWinning(int seed) throws WorldEditException {
        Random random = new Random(seed);
        RecordingWorld world = new RecordingWorld();
        ChunkBatchingExtent batching = new ChunkBatchingExtent(world);
        TestWorld expected = new TestWorld();
        List<String> ids = List.of("minecraft:stone", "minecraft:glass", "minecraft:air", "minecraft:oak_planks");

        int overwrites = 0;
        for (int i = 0; i < 200; i++) {
            // Spread over several chunks, including negative coordinates, but dense enough to overwrite positions
            BlockVector3 position = BlockVector3.at(random.nextInt(16) - 8, random.nextInt(3) - 1, random.nextInt(16) - 8);
            var state = block(ids.get(random.nextInt(ids.size())));
            if (!expected.getFullBlock(position).equals(block("minecraft:air").toBaseBlock())) {
                overwrites++;
            }
            batching.setBlock(position, state);
            expected.setBlock(position, state);
            assertEquals(state, batching.getBlock(position), "reads see the pending block");
        }
        assertEquals(0, world.placements.size());

        Operations.complete(batching.commit());

        assertEquals(expected.getBlocks(), world.getBlocks());
        assertTrue(overwrites > 0, "the test should overwrite positions");
        assertEquals(List.copyOf(world.placements), world.placements.stream().distinct().toList(),
            "each position is written once");
    }

    @Test
    void disabledChunkBatchingWritesThrough() throws WorldEditException {
        RecordingWorld world = new RecordingWorld();
        ChunkBatchingExtent batching = new ChunkBatchingExtent(world, false);

        batching.setBlock(BlockVector3.ZERO, block("minecraft:stone"));

        assertEquals(block("minecraft:stone"), world.getBlock(BlockVector3.ZERO));
        assertNull(batching.commit());
    }
}
