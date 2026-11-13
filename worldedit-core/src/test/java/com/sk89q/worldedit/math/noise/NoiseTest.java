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

package com.sk89q.worldedit.math.noise;

import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoiseTest {

    private static final double[] EXTREMES = {0, -0.0, 1, -1, 0.5, -64, 319, 1e6, -1e6, 30_000_000, -30_000_000};

    private static final List<Function<Integer, JLibNoiseGenerator<?>>> FACTORIES = List.of(
        seed -> {
            PerlinNoise noise = new PerlinNoise();
            noise.setSeed(seed);
            return noise;
        },
        seed -> {
            RidgedMultiFractalNoise noise = new RidgedMultiFractalNoise();
            noise.setSeed(seed);
            return noise;
        },
        seed -> {
            VoronoiNoise noise = new VoronoiNoise();
            noise.setSeed(seed);
            return noise;
        }
    );

    private static void forEachSeededGenerator(Consumer<JLibNoiseGenerator<?>> consumer, int seed) {
        FACTORIES.forEach(factory -> consumer.accept(factory.apply(seed)));
    }

    private static void assertInRange(float value, String message) {
        assertTrue(value >= 0 && value <= 1, message + ": " + value);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, -1, 12345, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void outputStaysInRange(int seed) {
        Random random = new Random(seed);
        forEachSeededGenerator(noise -> {
            String name = noise.getClass().getSimpleName();
            for (int i = 0; i < 2000; i++) {
                Vector3 position = Vector3.at(
                    random.nextGaussian() * 100, random.nextGaussian() * 100, random.nextGaussian() * 100);
                assertInRange(noise.noise(position), name + " 3D at " + position);
                assertInRange(noise.noise(Vector2.at(position.x(), position.z())), name + " 2D at " + position);
            }
            for (double x : EXTREMES) {
                for (double y : EXTREMES) {
                    for (double z : EXTREMES) {
                        assertInRange(noise.noise(Vector3.at(x, y, z)), name + " at " + x + "," + y + "," + z);
                    }
                }
            }
        }, seed);
    }

    @Test
    void outputStaysInRangeForOtherSettings() {
        Random random = new Random(7);
        PerlinNoise perlin = new PerlinNoise();
        perlin.setFrequency(0.01);
        perlin.setOctaveCount(1);
        PerlinNoise busyPerlin = new PerlinNoise();
        busyPerlin.setFrequency(8);
        busyPerlin.setOctaveCount(6);
        busyPerlin.setPersistence(0.9);
        busyPerlin.setLacunarity(3);
        RidgedMultiFractalNoise ridged = new RidgedMultiFractalNoise();
        ridged.setFrequency(5);
        ridged.setOctaveCount(6);
        VoronoiNoise voronoi = new VoronoiNoise();
        voronoi.setFrequency(0.05);
        for (NoiseGenerator noise : new NoiseGenerator[]{perlin, busyPerlin, ridged, voronoi}) {
            for (int i = 0; i < 2000; i++) {
                Vector3 position = Vector3.at(
                    random.nextGaussian() * 500, random.nextGaussian() * 100, random.nextGaussian() * 500);
                assertInRange(noise.noise(position), noise.getClass().getSimpleName() + " at " + position);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 42, -7})
    void sameSeedGivesSameOutput(int seed) {
        Random random = new Random(1);
        Vector3[] positions = new Vector3[500];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = Vector3.at(random.nextGaussian() * 50, random.nextGaussian() * 50, random.nextGaussian() * 50);
        }

        for (Function<Integer, JLibNoiseGenerator<?>> factory : FACTORIES) {
            JLibNoiseGenerator<?> first = factory.apply(seed);
            JLibNoiseGenerator<?> second = factory.apply(seed);
            for (Vector3 position : positions) {
                assertEquals(first.noise(position), second.noise(position),
                    first.getClass().getSimpleName() + " at " + position);
            }
        }
    }

    @Test
    void repeatedQueriesAreStable() {
        forEachSeededGenerator(noise -> {
            Vector3 position = Vector3.at(12.3, -45.6, 78.9);
            float value = noise.noise(position);
            for (int i = 0; i < 10; i++) {
                assertEquals(value, noise.noise(position));
            }
        }, 99);
    }

    @Test
    void differentSeedsGiveDifferentOutput() {
        Random random = new Random(3);
        for (Function<Integer, JLibNoiseGenerator<?>> factory : FACTORIES) {
            JLibNoiseGenerator<?> a = factory.apply(1);
            JLibNoiseGenerator<?> b = factory.apply(2);
            boolean differs = false;
            for (int i = 0; i < 200 && !differs; i++) {
                Vector3 position = Vector3.at(random.nextDouble() * 20, random.nextDouble() * 20, random.nextDouble() * 20);
                differs = a.noise(position) != b.noise(position);
            }
            assertTrue(differs, a.getClass().getSimpleName() + " ignores its seed");
        }
    }

    @Test
    void seedGettersReturnWhatWasSet() {
        forEachSeededGenerator(noise -> assertEquals(-123456, noise.getSeed()), -123456);
    }

    @Test
    void twoDimensionalNoiseIsTheFlatSliceOfThreeDimensionalNoise() {
        Random random = new Random(11);
        forEachSeededGenerator(noise -> {
            for (int i = 0; i < 200; i++) {
                double x = random.nextGaussian() * 30;
                double z = random.nextGaussian() * 30;
                assertEquals(noise.noise(Vector3.at(x, 0, z)), noise.noise(Vector2.at(x, z)));
            }
        }, 5);
    }

    @Test
    void randomNoiseIsReproducibleAndInRange() {
        RandomNoise first = new RandomNoise(new Random(8));
        RandomNoise second = new RandomNoise(new Random(8));
        for (int i = 0; i < 1000; i++) {
            float value = first.noise(Vector3.at(i, -i, 0));
            assertEquals(value, second.noise(Vector2.at(i, i)));
            assertTrue(value >= 0 && value < 1, "value " + value);
        }
    }
}
