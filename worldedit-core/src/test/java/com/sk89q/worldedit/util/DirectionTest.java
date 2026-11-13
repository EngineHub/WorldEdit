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

package com.sk89q.worldedit.util;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.OptionalInt;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectionTest {

    private static final double EPSILON = 1e-9;

    @ParameterizedTest
    @EnumSource(Direction.class)
    void vectorsAreUnitLength(Direction direction) {
        assertEquals(1.0, direction.toVector().length(), EPSILON);
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void everyDirectionHasExactlyOneCategory(Direction direction) {
        int categories = (direction.isCardinal() ? 1 : 0) + (direction.isOrdinal() ? 1 : 0)
            + (direction.isSecondaryOrdinal() ? 1 : 0) + (direction.isUpright() ? 1 : 0);
        assertEquals(1, categories);
        assertEquals(direction.isUpright(), direction.toVector().y() != 0);
    }

    @Test
    void blockVectorsOfNonSecondaryDirectionsAreExact() {
        assertEquals(BlockVector3.at(0, 0, -1), Direction.NORTH.toBlockVector());
        assertEquals(BlockVector3.at(1, 0, 0), Direction.EAST.toBlockVector());
        assertEquals(BlockVector3.at(0, 0, 1), Direction.SOUTH.toBlockVector());
        assertEquals(BlockVector3.at(-1, 0, 0), Direction.WEST.toBlockVector());
        assertEquals(BlockVector3.at(0, 1, 0), Direction.UP.toBlockVector());
        assertEquals(BlockVector3.at(0, -1, 0), Direction.DOWN.toBlockVector());
        assertEquals(BlockVector3.at(1, 0, -1), Direction.NORTHEAST.toBlockVector());
        assertEquals(BlockVector3.at(-1, 0, -1), Direction.NORTHWEST.toBlockVector());
        assertEquals(BlockVector3.at(1, 0, 1), Direction.SOUTHEAST.toBlockVector());
        assertEquals(BlockVector3.at(-1, 0, 1), Direction.SOUTHWEST.toBlockVector());
    }

    @Test
    void valuesOfCountsMatchTheFlags() {
        assertEquals(0, Direction.valuesOf(0).size());
        assertEquals(4, Direction.valuesOf(Direction.Flag.CARDINAL).size());
        assertEquals(4, Direction.valuesOf(Direction.Flag.ORDINAL).size());
        assertEquals(8, Direction.valuesOf(Direction.Flag.SECONDARY_ORDINAL).size());
        assertEquals(2, Direction.valuesOf(Direction.Flag.UPRIGHT).size());
        assertEquals(6, Direction.valuesOf(Direction.Flag.CARDINAL | Direction.Flag.UPRIGHT).size());
        assertEquals(8, Direction.valuesOf(Direction.Flag.CARDINAL | Direction.Flag.ORDINAL).size());
        assertEquals(Direction.values().length, Direction.valuesOf(Direction.Flag.ALL).size());
    }

    @Test
    void everyDirectionIsItsOwnClosestDirection() {
        for (Direction direction : Direction.values()) {
            assertEquals(direction, Direction.findClosest(direction.toVector(), Direction.Flag.ALL));
            assertEquals(direction, Direction.findClosest(direction.toVector().multiply(37), Direction.Flag.ALL));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15})
    void findClosestMatchesBruteForceSearch(int flags) {
        Random random = new Random(flags);
        List<Direction> allowed = Direction.valuesOf(flags);
        for (int i = 0; i < 500; i++) {
            Vector3 vector = Vector3.at(random.nextGaussian(), random.nextGaussian(), random.nextGaussian());
            Vector3 projected = (flags & Direction.Flag.UPRIGHT) == 0 ? vector.withY(0) : vector;
            Direction expected = null;
            double best = -2;
            for (Direction direction : allowed) {
                double dot = direction.toVector().dot(projected.normalize());
                if (dot > best) {
                    best = dot;
                    expected = direction;
                }
            }

            Direction actual = Direction.findClosest(vector, flags);
            if (allowed.isEmpty()) {
                assertNull(actual);
            } else {
                assertEquals(expected, actual, "flags " + flags + " vector " + vector);
                assertTrue(allowed.contains(actual));
            }
        }
    }

    @Test
    void findClosestWithNoPermittedDirectionsIsNull() {
        assertNull(Direction.findClosest(Vector3.UNIT_X, 0));
    }

    @Test
    void findClosestIgnoresHeightWhenUprightIsNotPermitted() {
        assertEquals(Direction.EAST, Direction.findClosest(Vector3.at(0.1, 50, 0), Direction.Flag.CARDINAL));
        assertEquals(Direction.UP, Direction.findClosest(Vector3.at(0.1, 50, 0), Direction.Flag.ALL));
    }

    @Test
    void findClosestOfAVectorWithNoUsableHorizontalPartIsNull() {
        // normalising the zero vector gives NaN dot products, so nothing can be chosen
        assertNull(Direction.findClosest(Vector3.ZERO, Direction.Flag.CARDINAL));
        assertNull(Direction.findClosest(Vector3.UNIT_Y, Direction.Flag.CARDINAL));
    }

    @Test
    void rotationIndexesRoundTrip() {
        for (int index = 0; index < 16; index++) {
            Direction direction = Direction.fromRotationIndex(index).orElseThrow();
            assertEquals(OptionalInt.of(index), direction.toRotationIndex());
        }
        for (Direction direction : Direction.values()) {
            OptionalInt index = direction.toRotationIndex();
            if (index.isPresent()) {
                assertEquals(direction, Direction.fromRotationIndex(index.getAsInt()).orElseThrow());
            }
        }
    }

    @Test
    void rotationIndexesAreOutOfRangeOutsideZeroToFifteen() {
        for (int index : new int[]{-1, 16, 17, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            assertTrue(Direction.fromRotationIndex(index).isEmpty(), "index " + index);
        }
    }

    @Test
    void verticalDirectionsHaveNoRotationIndex() {
        assertTrue(Direction.UP.toRotationIndex().isEmpty());
        assertTrue(Direction.DOWN.toRotationIndex().isEmpty());
    }

    @Test
    void rotationIndexesStepClockwiseInEvenSteps() {
        // index 0 is south, and each step is 22.5 degrees towards west
        double step = Math.PI / 8;
        for (int index = 0; index < 16; index++) {
            Vector3 vector = Direction.fromRotationIndex(index).orElseThrow().toVector();
            double expectedAngle = index * step;
            assertEquals(Math.sin(expectedAngle), -vector.x(), EPSILON, "x for " + index);
            assertEquals(Math.cos(expectedAngle), vector.z(), EPSILON, "z for " + index);
            assertEquals(0, vector.y(), EPSILON);
        }
    }
}
