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
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Sets every state of a sample of directional/multi-state block families through the
 * platform world, and checks that the real world ends up with the same state.
 */
public class BlockRoundTripGameTest {
    private static final BlockPos TEST_POS = new BlockPos(3, 3, 3);

    /**
     * Properties that vanilla recomputes from the surroundings when updates run, so they
     * only round-trip exactly when {@link SideEffect#UPDATE} is off.
     */
    private static final Set<String> SELF_UPDATING_PROPERTIES = Set.of("powered", "enabled");

    @GameTest
    public void stairs(GameTestHelper helper) throws WorldEditException {
        roundTrip(helper, "oak_stairs", "stone_stairs", "cut_copper_stairs");
    }

    @GameTest
    public void rails(GameTestHelper helper) throws WorldEditException {
        roundTrip(helper, "rail", "powered_rail", "detector_rail", "activator_rail");
    }

    @GameTest
    public void doors(GameTestHelper helper) throws WorldEditException {
        roundTrip(helper, "oak_door", "iron_door", "oak_trapdoor", "oak_fence_gate");
    }

    @GameTest
    public void beds(GameTestHelper helper) throws WorldEditException {
        roundTrip(helper, "red_bed", "white_bed");
    }

    @GameTest
    public void chests(GameTestHelper helper) throws WorldEditException {
        roundTrip(helper, "chest", "trapped_chest", "ender_chest");
    }

    @GameTest
    public void otherDirectionalBlocks(GameTestHelper helper) throws WorldEditException {
        roundTrip(
            helper,
            "crafter", "furnace", "hopper", "observer", "piston", "sticky_piston", "lever", "stone_button",
            "repeater", "comparator", "wall_torch", "ladder", "barrel", "end_rod", "anvil", "loom",
            "lectern", "bell", "grindstone", "oak_wall_sign", "oak_sign", "white_banner", "oak_log"
        );
    }

    private void roundTrip(GameTestHelper helper, String... names) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        BlockPos nativePos = helper.absolutePos(TEST_POS);
        BlockVector3 pos = FabricAdapter.get().adapt(nativePos);

        // Vanilla legitimately breaks blocks that have nothing to stand or lean on, e.g. rails on a slope
        helper.setBlock(TEST_POS.below(), Blocks.STONE);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            helper.setBlock(TEST_POS.relative(side), Blocks.STONE);
        }

        // Only the first failure per block type is kept, so one broken family can't bury the rest
        Map<String, String> firstFailures = new LinkedHashMap<>();
        Map<String, Integer> failureCounts = new HashMap<>();
        for (String name : names) {
            BlockType type = BlockTypes.get("minecraft:" + name);
            if (type == null) {
                firstFailures.put(name, "Unknown block type");
                failureCounts.merge(name, 1, Integer::sum);
                continue;
            }
            for (BlockState state : type.getAllStates()) {
                for (SideEffectSet sideEffects : List.of(SideEffectSet.none(), SideEffectSet.defaults())) {
                    // Reset so a failed set can't be masked by the previous iteration's state
                    world.setBlock(pos, BlockTypes.AIR.getDefaultState(), SideEffectSet.none());
                    world.setBlock(pos, state, sideEffects);

                    String problem = compare(helper, world, state, pos, nativePos, sideEffects.shouldApply(SideEffect.UPDATE));
                    if (problem != null) {
                        firstFailures.putIfAbsent(name, state + " with " + sideEffects.getSideEffectsToApply() + ": " + problem);
                        failureCounts.merge(name, 1, Integer::sum);
                    }
                }
            }
        }
        helper.assertTrue(
            firstFailures.isEmpty(),
            "block states that did not round-trip (count per block type: " + failureCounts + "), first of each: "
                + firstFailures
        );
        helper.succeed();
    }

    /**
     * Compare the real world's block with the WorldEdit state, going through the
     * native property names/values rather than the adapter's own conversion.
     */
    private String compare(
        GameTestHelper helper, World world, BlockState expected, BlockVector3 pos, BlockPos nativePos, boolean updated
    ) {
        net.minecraft.world.level.block.state.BlockState actual = helper.getLevel().getBlockState(nativePos);

        String expectedId = expected.getBlockType().id();
        String actualId = BuiltInRegistries.BLOCK.getKey(actual.getBlock()).toString();
        if (!expectedId.equals(actualId)) {
            return "real world has " + actual;
        }

        Map<String, String> expectedProperties = weProperties(expected, updated);
        Map<String, String> actualProperties = nativeProperties(actual, updated);
        if (!expectedProperties.equals(actualProperties)) {
            return "real world has " + actualProperties + " but expected " + expectedProperties;
        }

        // What the platform world reads back has to be what is really there, whatever updates did to it
        BlockState readBack = world.getBlock(pos);
        if (!weProperties(readBack, false).equals(nativeProperties(actual, false))
            || !readBack.getBlockType().id().equals(actualId)) {
            return "platform world read back " + readBack + " but real world has " + actual;
        }
        if (!updated && !expected.equals(readBack)) {
            return "platform world read back " + readBack;
        }
        return null;
    }

    private static Map<String, String> weProperties(BlockState state, boolean skipSelfUpdating) {
        Map<String, String> properties = new TreeMap<>();
        for (Map.Entry<Property<?>, Object> entry : state.getStates().entrySet()) {
            if (!skipSelfUpdating || !SELF_UPDATING_PROPERTIES.contains(entry.getKey().getName())) {
                properties.put(entry.getKey().getName(), String.valueOf(entry.getValue()).toLowerCase(Locale.ROOT));
            }
        }
        return properties;
    }

    private static Map<String, String> nativeProperties(
        net.minecraft.world.level.block.state.BlockState state, boolean skipSelfUpdating
    ) {
        Map<String, String> properties = new TreeMap<>();
        for (net.minecraft.world.level.block.state.properties.Property<?> property : state.getProperties()) {
            if (!skipSelfUpdating || !SELF_UPDATING_PROPERTIES.contains(property.getName())) {
                properties.put(property.getName(), nativeValueName(state, property));
            }
        }
        return properties;
    }

    private static <T extends Comparable<T>> String nativeValueName(
        net.minecraft.world.level.block.state.BlockState state,
        net.minecraft.world.level.block.state.properties.Property<T> property
    ) {
        return property.getName(state.getValue(property));
    }
}
