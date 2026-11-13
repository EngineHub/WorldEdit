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

package com.sk89q.worldedit.fabric.gametest;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.TreeType;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

/**
 * Generators run vanilla worldgen against a proxy of the {@link EditSession}, so they have to see (and write
 * to) the session's buffered blocks rather than the real world underneath it.
 */
public class GenerationGameTest {
    private static final int SIZE = 8;
    // Scanned and cleared above the box too, in case a tree reaches over the top
    private static final int HEIGHT = 16;
    private static final int ATTEMPTS = 40;
    private static final BlockPos GROUND = new BlockPos(4, 0, 4);
    private static final BlockPos ABOVE_GROUND = GROUND.above();
    private static final BlockPos HIGH_UP = new BlockPos(4, 5, 4);

    @GameTest
    public void oakGrowsOnGroundThatIsOnlyBuffered(GameTestHelper helper) throws WorldEditException {
        checkTreeOnBufferedGround(helper, "minecraft:oak_checked");
    }

    @GameTest
    public void birchGrowsOnGroundThatIsOnlyBuffered(GameTestHelper helper) throws WorldEditException {
        checkTreeOnBufferedGround(helper, "minecraft:birch_checked");
    }

    @GameTest
    public void treeDoesNotGrowWithoutGround(GameTestHelper helper) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        TreeType type = requireTreeType(helper, "minecraft:oak_checked");

        boolean placed;
        try (EditSession session = bufferedSession(world)) {
            placed = world.generateTree(type, session, FabricAdapter.get().adapt(helper.absolutePos(ABOVE_GROUND)));
        }
        int trees = count(helper, state -> state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES));
        clearBox(helper);
        helper.assertFalse(placed, "a tree should not be generated in mid-air");
        helper.assertValueEqual(trees, 0, "log and leaf blocks placed with no ground");
        helper.succeed();
    }

    @GameTest
    @SuppressWarnings("deprecation")
    public void legacyTreeEnumGrowsOnBufferedGround(GameTestHelper helper) throws WorldEditException {
        checkTreeOnBufferedGround(helper, "the legacy oak tree",
            (session, pos) -> TreeGenerator.TreeType.TREE.generate(session, pos));
    }

    /**
     * A forest rock settles on the first dirt or stone below where it starts. The dirt here is buffered in the
     * session, so the rock only ends up in the box if the feature can see it.
     *
     * <p>Known bug, so this is not a required test (its failure is reported but does not fail the run): the
     * rock is written straight into the world while the session is still open, rather than into the session.
     * {@code CoreMcServerLevelDelegateProxy} only intercepts {@code setBlock} and friends, but forest rocks
     * ({@code BlockBlobFeature}) place their blocks with {@code WorldGenLevel#setBlockAndUpdate}, which the
     * proxy forwards to the real level. Those blocks skip the session's buffering, block limit and undo
     * history.
     */
    @GameTest(required = false)
    public void featureSeesBufferedBlocks(GameTestHelper helper) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        ConfiguredFeatureType feature = requireFeature(helper, "minecraft:forest_rock");

        boolean placed;
        boolean buffered;
        try (EditSession session = bufferedSession(world)) {
            session.setBlock(FabricAdapter.get().adapt(helper.absolutePos(GROUND)), BlockTypes.DIRT.getDefaultState());
            placed = world.generateFeature(feature, session, FabricAdapter.get().adapt(helper.absolutePos(HIGH_UP)));
            buffered = count(helper, state -> !state.isAir()) == 0;
        }
        int rock = count(helper, state -> state.is(Blocks.MOSSY_COBBLESTONE));
        BlockState groundAfter = helper.getBlockState(GROUND);
        clearBox(helper);
        helper.assertTrue(buffered, "nothing should reach the world before the session is closed");
        helper.assertTrue(placed, "the forest rock should have been generated");
        helper.assertTrue(rock > 0, "the rock should have come to rest on the buffered dirt");
        helper.assertTrue(groundAfter.is(Blocks.DIRT), "the buffered dirt should be written when the session closes");
        helper.succeed();
    }

    private void checkTreeOnBufferedGround(GameTestHelper helper, String id) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        TreeType type = requireTreeType(helper, id);
        checkTreeOnBufferedGround(helper, id, (session, pos) -> world.generateTree(type, session, pos));
    }

    /**
     * Without the dirt that only exists in the session's buffer, the tree has nowhere to grow, so growing at
     * all shows the generator reads the buffer.
     *
     * <p>Tree shapes are random and the test box has limited headroom, so tall shapes are rejected. Try several
     * times rather than depend on the random number generator.
     */
    private void checkTreeOnBufferedGround(GameTestHelper helper, String name, TreeAttempt attempt) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        BlockVector3 ground = FabricAdapter.get().adapt(helper.absolutePos(GROUND));

        boolean placed = false;
        boolean buffered = true;
        for (int i = 0; i < ATTEMPTS && !placed; i++) {
            try (EditSession session = bufferedSession(world)) {
                session.setBlock(ground, BlockTypes.DIRT.getDefaultState());
                placed = attempt.generate(session, ground.add(0, 1, 0));
                // Nothing may reach the world before the session is closed
                buffered &= helper.getBlockState(GROUND).isAir() && helper.getBlockState(ABOVE_GROUND).isAir();
            }
            if (!placed) {
                clearBox(helper);
            }
        }
        int logs = count(helper, state -> state.is(BlockTags.LOGS));
        int leaves = count(helper, state -> state.is(BlockTags.LEAVES));
        BlockState groundAfter = helper.getBlockState(GROUND);
        clearBox(helper);

        helper.assertTrue(buffered, "the test needs the session to buffer its blocks");
        helper.assertTrue(placed, name + " should have been generated on the buffered dirt in " + ATTEMPTS + " attempts");
        helper.assertTrue(logs > 0, name + " should have logs");
        helper.assertTrue(leaves > 0, name + " should have leaves");
        helper.assertTrue(groundAfter.is(Blocks.DIRT), "the buffered dirt should be written when the session closes");
        helper.succeed();
    }

    private interface TreeAttempt {
        boolean generate(EditSession session, BlockVector3 pos) throws WorldEditException;
    }

    private static EditSession bufferedSession(World world) {
        EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(world).build();
        session.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
        return session;
    }

    private static TreeType requireTreeType(GameTestHelper helper, String id) {
        TreeType type = TreeType.REGISTRY.get(id);
        if (type == null) {
            throw helper.assertionException("tree type " + id + " is not registered");
        }
        return type;
    }

    private static ConfiguredFeatureType requireFeature(GameTestHelper helper, String id) {
        ConfiguredFeatureType type = ConfiguredFeatureType.REGISTRY.get(id);
        if (type == null) {
            throw helper.assertionException("feature " + id + " is not registered");
        }
        return type;
    }

    private static int count(GameTestHelper helper, Predicate<BlockState> predicate) {
        int count = 0;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < SIZE; z++) {
                    if (predicate.test(helper.getBlockState(new BlockPos(x, y, z)))) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static void clearBox(GameTestHelper helper) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < SIZE; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
    }
}
