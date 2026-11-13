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

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;

/**
 * Checks that the side effects asked for through a {@link SideEffectSet} are the ones that happen in the
 * world, by looking at what vanilla does in response.
 */
public class SideEffectsGameTest {
    private static final BlockPos TEST_POS = new BlockPos(3, 3, 3);
    private static final BlockPos NEXT_TO_TEST_POS = TEST_POS.east();

    /**
     * A redstone lamp only lights if it is told that a neighbour changed.
     */
    @GameTest
    public void neighbourUpdatesPowerAdjacentLamp(GameTestHelper helper) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        BlockVector3 lamp = FabricAdapter.get().adapt(helper.absolutePos(TEST_POS));
        BlockVector3 power = FabricAdapter.get().adapt(helper.absolutePos(NEXT_TO_TEST_POS));

        record Case(String name, SideEffectSet sideEffects, boolean expectLit) {
        }

        for (Case testCase : new Case[] {
            new Case("defaults", SideEffectSet.defaults(), true),
            new Case("neighbours only", SideEffectSet.none().with(SideEffect.NEIGHBORS, SideEffect.State.ON), true),
            new Case("defaults without neighbours", SideEffectSet.defaults().with(SideEffect.NEIGHBORS, SideEffect.State.OFF), false),
            new Case("none", SideEffectSet.none(), false),
        }) {
            world.setBlock(power, BlockTypes.AIR.getDefaultState(), SideEffectSet.none());
            world.setBlock(lamp, BlockTypes.REDSTONE_LAMP.getDefaultState(), SideEffectSet.none());
            helper.assertFalse(isLit(helper), "lamp should start unlit (" + testCase.name + ")");

            world.setBlock(power, BlockTypes.REDSTONE_BLOCK.getDefaultState(), testCase.sideEffects);

            helper.assertValueEqual(isLit(helper), testCase.expectLit, "lamp lit after placing power with " + testCase.name);
        }

        helper.succeed();
    }

    /**
     * Sand placed with block updates schedules itself to fall; without them it doesn't.
     */
    @GameTest
    public void updatesScheduleBlockTicks(GameTestHelper helper) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        BlockPos nativePos = helper.absolutePos(TEST_POS);
        BlockVector3 pos = FabricAdapter.get().adapt(nativePos);

        // This case goes first: a scheduled tick stays in the world after its block is removed
        world.setBlock(pos, BlockTypes.SAND.getDefaultState(), SideEffectSet.none());
        helper.assertFalse(
            helper.getLevel().getBlockTicks().hasScheduledTick(nativePos, Blocks.SAND),
            "sand placed without UPDATE should not have scheduled a tick"
        );
        world.setBlock(pos, BlockTypes.AIR.getDefaultState(), SideEffectSet.none());

        world.setBlock(pos, BlockTypes.SAND.getDefaultState(), SideEffectSet.none().with(SideEffect.UPDATE, SideEffect.State.ON));
        helper.assertTrue(
            helper.getLevel().getBlockTicks().hasScheduledTick(nativePos, Blocks.SAND),
            "sand placed with UPDATE should have scheduled a tick"
        );
        helper.succeed();
    }

    /**
     * Light follows an emitter being placed and removed with the default side effects. This is a guard
     * rather than a comparison with {@link SideEffect#LIGHTING} off: vanilla queues its own light check when
     * a block's emission changes, so the option makes no observable difference to such blocks.
     */
    @GameTest
    public void lightFollowsEmitterWithDefaultSideEffects(GameTestHelper helper) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        BlockPos nativePos = helper.absolutePos(TEST_POS);
        BlockPos above = nativePos.above();
        BlockVector3 pos = FabricAdapter.get().adapt(nativePos);

        // Other tests' emitters in neighbouring structures can reach here, so compare with what was there
        int baseline = helper.getLevel().getBrightness(LightLayer.BLOCK, above);

        world.setBlock(pos, BlockTypes.GLOWSTONE.getDefaultState(), SideEffectSet.defaults());
        // The light engine works off the main thread, so wait for it rather than for a fixed time
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertValueEqual(
                helper.getLevel().getBrightness(LightLayer.BLOCK, above), 14, "light above a placed glowstone"
            ))
            .thenExecute(() -> {
                try {
                    world.setBlock(pos, BlockTypes.AIR.getDefaultState(), SideEffectSet.defaults());
                } catch (WorldEditException e) {
                    helper.fail(e.toString());
                }
            })
            .thenWaitUntil(() -> helper.assertValueEqual(
                helper.getLevel().getBrightness(LightLayer.BLOCK, above), baseline, "light above a removed glowstone"
            ))
            .thenSucceed();
    }

    private boolean isLit(GameTestHelper helper) {
        return helper.getLevel().getBlockState(helper.absolutePos(TEST_POS)).getValue(RedstoneLampBlock.LIT);
    }
}
