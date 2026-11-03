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

package com.sk89q.worldedit.function.pattern;

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.test.TestRegistries;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RandomPatternTest extends BaseWorldEditTest {

    private static final int SAMPLES = 200_000;
    // The sample proportion has a standard deviation of at most ~0.0011, so this is about 9 sigma
    private static final double TOLERANCE = 0.01;

    @BeforeAll
    static void registerBlocks() {
        TestRegistries.registerBlocks();
    }

    @AfterAll
    static void unregisterBlocks() {
        TestRegistries.unregisterBlocks();
    }

    private static BaseBlock block(String id) {
        return BlockTypes.get(id).getDefaultState().toBaseBlock();
    }

    private static Map<String, Integer> sample(RandomPattern pattern) {
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < SAMPLES; i++) {
            counts.merge(pattern.applyBlock(BlockVector3.at(i, 0, 0)).getBlockType().id(), 1, Integer::sum);
        }
        return counts;
    }

    static Stream<Arguments> weights() {
        return Stream.of(
            Arguments.of(new double[] {1, 1}),
            Arguments.of(new double[] {1, 2, 7}),
            Arguments.of(new double[] {50, 50}),
            Arguments.of(new double[] {0.5, 0.25, 0.25}),
            Arguments.of(new double[] {99, 1}),
            Arguments.of(new double[] {1, 0, 3})
        );
    }

    @ParameterizedTest
    @MethodSource("weights")
    void distributionFollowsWeights(double[] weights) {
        String[] ids = {"minecraft:stone", "minecraft:glass", "minecraft:oak_planks"};
        RandomPattern pattern = new RandomPattern();
        double total = 0;
        for (int i = 0; i < weights.length; i++) {
            pattern.add(block(ids[i]), weights[i]);
            total += weights[i];
        }

        Map<String, Integer> counts = sample(pattern);
        for (int i = 0; i < weights.length; i++) {
            double observed = counts.getOrDefault(ids[i], 0) / (double) SAMPLES;
            assertEquals(weights[i] / total, observed, TOLERANCE, ids[i]);
        }
    }

    @Test
    void zeroWeightIsNeverChosen() {
        RandomPattern pattern = new RandomPattern();
        pattern.add(block("minecraft:stone"), 0);
        pattern.add(block("minecraft:glass"), 1);
        assertEquals(Map.of("minecraft:glass", SAMPLES), sample(pattern));
    }

    @Test
    void singleMemberIsAlwaysChosen() {
        RandomPattern pattern = new RandomPattern();
        pattern.add(block("minecraft:stone"), 5);
        assertEquals(Map.of("minecraft:stone", SAMPLES), sample(pattern));
    }

    @Test
    void emptyPatternFails() {
        assertThrows(RuntimeException.class, () -> new RandomPattern().applyBlock(BlockVector3.ZERO));
    }
}
