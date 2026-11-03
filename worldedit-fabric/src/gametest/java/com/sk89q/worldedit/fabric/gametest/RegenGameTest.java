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
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * //regen regenerates whole chunks in a temporary level, then copies just the requested region out of them, so
 * it can be run on a part of a chunk without disturbing the rest.
 */
public class RegenGameTest {
    private static final int SIZE = 8;
    // Include some ground below the test box, so a regen has something other than air to produce
    private static final int DEPTH = 3;

    @GameTest(maxTicks = 600)
    public void regenRestoresRegionToGeneratedTerrain(GameTestHelper helper) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        CuboidRegion region = region(helper, world);
        List<BlockState> baseline = blocks(world, region);

        try {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
                session.setBlocks(region, BlockTypes.STONE.getDefaultState());
            }

            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
                helper.assertTrue(world.regenerate(region, session, RegenOptions.builder().regenBiomes(false).build()), "regenerate should succeed");
            }

            checkMatchesGeneratedTerrain(helper, region, baseline, blocks(world, region), "the world");
        } finally {
            restore(world, region, baseline);
        }
        helper.succeed();
    }

    /**
     * The extent is where the regenerated blocks go, so a different extent must leave the world as it was.
     */
    @GameTest(maxTicks = 600)
    public void regenIntoClipboardLeavesWorldAlone(GameTestHelper helper) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        CuboidRegion region = region(helper, world);
        List<BlockState> baseline = blocks(world, region);

        try {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
                session.setBlocks(region, BlockTypes.STONE.getDefaultState());
            }

            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
            helper.assertTrue(world.regenerate(region, clipboard, RegenOptions.builder().regenBiomes(false).build()), "regenerate should succeed");

            checkMatchesGeneratedTerrain(helper, region, baseline, blocks(clipboard, region), "the clipboard");
            helper.assertTrue(
                blocks(world, region).stream().allMatch(block -> block.getBlockType() == BlockTypes.STONE),
                "the world should still be stone, since it was not the extent regenerated into"
            );
        } finally {
            restore(world, region, baseline);
        }
        helper.succeed();
    }

    @GameTest(maxTicks = 600)
    public void regenBiomesRestoresBiomes(GameTestHelper helper) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        CuboidRegion region = region(helper, world);
        List<BlockState> baselineBlocks = blocks(world, region);
        BlockVector3 probe = region.getMinimumPoint().add(SIZE / 2, DEPTH + 1, SIZE / 2);
        BiomeType baseline = world.getBiome(probe);
        BiomeType other = BiomeTypes.DESERT.equals(baseline) ? BiomeTypes.JUNGLE : BiomeTypes.DESERT;

        try {
            world.setBiome(probe, other);
            helper.assertValueEqual(world.getBiome(probe), other, "biome before regenerating");

            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
                helper.assertTrue(world.regenerate(region, session, RegenOptions.builder().regenBiomes(true).build()), "regenerate should succeed");
            }

            helper.assertValueEqual(world.getBiome(probe), baseline, "biome after regenerating with biomes");
        } finally {
            world.setBiome(probe, baseline);
            restore(world, region, baselineBlocks);
        }
        helper.succeed();
    }

    /**
     * What the world looked like before the test matches what a regen generates, apart from the barrier floor
     * that the test framework puts under the test box, which is not part of the terrain. The test world is not
     * stone at the height of that floor, so stone there means the regen did not happen.
     */
    private static void checkMatchesGeneratedTerrain(GameTestHelper helper, CuboidRegion region, List<BlockState> baseline,
                                                     List<BlockState> regenerated, String where) {
        int i = 0;
        for (BlockVector3 pos : region) {
            BlockState before = baseline.get(i);
            BlockState after = regenerated.get(i);
            i++;
            if (before.getBlockType() == BlockTypes.BARRIER) {
                helper.assertFalse(
                    after.getBlockType() == BlockTypes.STONE || after.getBlockType() == BlockTypes.BARRIER,
                    "block regenerated into " + where + " at " + pos + " should have been generated, not left as " + after
                );
            } else {
                helper.assertValueEqual(after, before, "block regenerated into " + where + " at " + pos);
            }
        }
    }

    /**
     * Seeds can't be overridden with the worldgen of 26.1+, and the platform says so rather than ignoring it.
     */
    @GameTest
    public void regenWithSeedIsRejected(GameTestHelper helper) {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        CuboidRegion region = region(helper, world);
        RegenOptions options = RegenOptions.builder().seed(1234L).build();

        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
            world.regenerate(region, session, options);
        } catch (UnsupportedOperationException _) {
            helper.succeed();
            return;
        }
        helper.fail("regenerating with a seed should have been rejected");
    }

    /**
     * Puts back what was there, including the framework's barrier floor, which a regen replaces.
     */
    private static void restore(World world, CuboidRegion region, List<BlockState> blocks) throws WorldEditException {
        int i = 0;
        for (BlockVector3 pos : region) {
            world.setBlock(pos, blocks.get(i++));
        }
    }

    private static CuboidRegion region(GameTestHelper helper, World world) {
        BlockVector3 origin = FabricAdapter.get().adapt(helper.absolutePos(BlockPos.ZERO));
        return new CuboidRegion(world, origin.add(0, -DEPTH, 0), origin.add(SIZE - 1, SIZE - 1, SIZE - 1));
    }

    private static List<BlockState> blocks(Extent extent, CuboidRegion region) {
        List<BlockState> blocks = new ArrayList<>();
        for (BlockVector3 pos : region) {
            blocks.add(extent.getBlock(pos));
        }
        return blocks;
    }
}
