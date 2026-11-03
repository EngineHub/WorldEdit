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

package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.sk89q.worldedit.util.test.RegionTestSupport.at;
import static com.sk89q.worldedit.util.test.RegionTestSupport.iterate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CuboidRegionTest {

    private static World worldWithHeights(int minY, int maxY) {
        World world = mock(World.class);
        when(world.getMinY()).thenReturn(minY);
        when(world.getMaxY()).thenReturn(maxY);
        return world;
    }

    private static boolean onXZBoundary(BlockVector3 p, BlockVector3 min, BlockVector3 max) {
        return p.x() == min.x() || p.x() == max.x() || p.z() == min.z() || p.z() == max.z();
    }

    private static boolean onYBoundary(BlockVector3 p, BlockVector3 min, BlockVector3 max) {
        return p.y() == min.y() || p.y() == max.y();
    }

    static Stream<Arguments> sizes() {
        return Stream.of(
            Arguments.of(at(0, 0, 0), at(0, 0, 0), 1L, 1, 1, 1),
            Arguments.of(at(0, 0, 0), at(4, 3, 2), 60L, 5, 4, 3),
            Arguments.of(at(4, 3, 2), at(0, 0, 0), 60L, 5, 4, 3),
            Arguments.of(at(-3, -64, -3), at(3, 319, 3), 7L * 384 * 7, 7, 384, 7),
            Arguments.of(at(-5, -10, -5), at(-1, -6, -1), 125L, 5, 5, 5),
            // would overflow an int when multiplied: 100000 * 384 * 100000
            Arguments.of(at(0, -64, 0), at(99_999, 319, 99_999), 100_000L * 384 * 100_000, 100_000, 384, 100_000),
            Arguments.of(at(-1_000_000_000, 0, 0), at(1_000_000_000, 0, 0), 2_000_000_001L, 2_000_000_001, 1, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("sizes")
    void dimensionsAndVolume(BlockVector3 pos1, BlockVector3 pos2, long volume, int width, int height, int length) {
        CuboidRegion region = new CuboidRegion(pos1, pos2);
        assertEquals(volume, region.getVolume());
        assertEquals(width, region.getWidth());
        assertEquals(height, region.getHeight());
        assertEquals(length, region.getLength());
    }

    @Test
    @Disabled("AbstractRegion.getVolume() computes (max.x() - min.x() + 1L) with the subtraction done in int, so a"
        + " cuboid spanning the whole int range on X ((MIN,0,0)-(MAX,1,2)) reports volume 0 instead of 2^32 * 6")
    void volumeDoesNotOverflowAtIntegerLimits() {
        CuboidRegion region = new CuboidRegion(at(Integer.MIN_VALUE, 0, 0), at(Integer.MAX_VALUE, 1, 2));
        assertEquals(6L << 32, region.getVolume());
    }

    @Test
    void minimumAndMaximumPointsNormaliseCorners() {
        CuboidRegion region = new CuboidRegion(at(5, -2, 7), at(-3, 9, 1));
        assertEquals(at(-3, -2, 1), region.getMinimumPoint());
        assertEquals(at(5, 9, 7), region.getMaximumPoint());
        assertEquals(-2, region.getMinimumY());
        assertEquals(9, region.getMaximumY());
    }

    @Test
    void worldHeightLimitsClampY() {
        World world = worldWithHeights(-64, 319);
        CuboidRegion region = new CuboidRegion(world, at(0, -100, 0), at(1, 400, 1));
        assertEquals(-64, region.getMinimumPoint().y());
        assertEquals(319, region.getMaximumPoint().y());
        assertEquals(2L * 2 * 384, region.getVolume());
    }

    @Test
    void worldHeightLimitsClampYAfterChanges() {
        World world = worldWithHeights(-64, 319);
        CuboidRegion region = new CuboidRegion(world, at(0, -60, 0), at(1, 10, 1));

        region.expand(at(0, -100, 0), at(0, 1000, 0));
        assertEquals(-64, region.getMinimumPoint().y());
        assertEquals(319, region.getMaximumPoint().y());

        region.shift(at(0, 50, 0));
        assertEquals(-14, region.getMinimumPoint().y());
        assertEquals(319, region.getMaximumPoint().y());
    }

    @Test
    void negativeYIsNotClampedWithoutWorld() {
        CuboidRegion region = new CuboidRegion(at(0, -500, 0), at(1, -400, 1));
        assertEquals(-500, region.getMinimumPoint().y());
        assertEquals(-400, region.getMaximumPoint().y());
        assertEquals(2L * 101 * 2, region.getVolume());
    }

    @ParameterizedTest
    @MethodSource("cornerOrders")
    void expandGrowsOnlyTheRequestedSide(BlockVector3 pos1, BlockVector3 pos2) {
        CuboidRegion region = new CuboidRegion(pos1, pos2);
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        region.expand(at(2, 0, 0));
        assertEquals(min, region.getMinimumPoint());
        assertEquals(max.add(2, 0, 0), region.getMaximumPoint());

        region.expand(at(-3, 0, 0));
        assertEquals(min.subtract(3, 0, 0), region.getMinimumPoint());

        region.expand(at(0, 4, 0), at(0, -5, 0), at(0, 0, 6), at(0, 0, -7));
        assertEquals(min.subtract(3, 5, 7), region.getMinimumPoint());
        assertEquals(max.add(2, 4, 6), region.getMaximumPoint());
    }

    @ParameterizedTest
    @MethodSource("cornerOrders")
    void contractShrinksTowardsTheGivenDirection(BlockVector3 pos1, BlockVector3 pos2) {
        CuboidRegion region = new CuboidRegion(pos1, pos2);
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        // a positive change moves the minimum side towards the maximum, a negative change the reverse
        region.contract(at(2, 0, 0));
        assertEquals(min.add(2, 0, 0), region.getMinimumPoint());
        assertEquals(max, region.getMaximumPoint());

        region.contract(at(-1, 0, 0), at(0, 1, 0), at(0, 0, -1));
        assertEquals(min.add(2, 1, 0), region.getMinimumPoint());
        assertEquals(max.subtract(1, 0, 1), region.getMaximumPoint());
    }

    @ParameterizedTest
    @MethodSource("cornerOrders")
    void expandThenOppositeContractRestores(BlockVector3 pos1, BlockVector3 pos2) {
        CuboidRegion region = new CuboidRegion(pos1, pos2);
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        for (BlockVector3 change : List.of(at(3, 0, 0), at(-3, 0, 0), at(0, 3, 0), at(0, -3, 0), at(0, 0, 3), at(0, 0, -3))) {
            region.expand(change);
            region.contract(change.multiply(-1));
            assertEquals(min, region.getMinimumPoint(), "after " + change);
            assertEquals(max, region.getMaximumPoint(), "after " + change);
        }
    }

    static Stream<Arguments> cornerOrders() {
        return Stream.of(
            Arguments.of(at(0, 0, 0), at(9, 9, 9)),
            Arguments.of(at(9, 9, 9), at(0, 0, 0)),
            Arguments.of(at(0, 9, 0), at(9, 0, 9)),
            Arguments.of(at(-9, -20, -9), at(-1, -10, -1)),
            Arguments.of(at(-4, -2, 3), at(4, 2, -3))
        );
    }

    @Test
    void contractingPastEmptyCrossesTheCornersOver() {
        // the corner that moves ends up beyond the other one, which is still a valid (mirrored) box
        CuboidRegion region = new CuboidRegion(at(0, 0, 0), at(2, 2, 2));
        region.contract(at(10, 0, 0));
        assertEquals(at(2, 0, 0), region.getMinimumPoint());
        assertEquals(at(10, 2, 2), region.getMaximumPoint());
        assertEquals(9L * 3 * 3, region.getVolume());
    }

    @Test
    void facesOfSolidBlockAreEverything() {
        CuboidRegion region = new CuboidRegion(at(0, 0, 0), at(1, 1, 1));
        Set<BlockVector3> faces = new HashSet<>(iterate(region.getFaces()));
        assertEquals(new HashSet<>(iterate(region)), faces);
    }

    @ParameterizedTest
    @MethodSource("wallAndFaceBoxes")
    void facesAreTheOuterShell(BlockVector3 pos1, BlockVector3 pos2) {
        CuboidRegion region = new CuboidRegion(pos1, pos2);
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        Region faces = region.getFaces();

        Set<BlockVector3> expected = new HashSet<>();
        for (BlockVector3 p : region) {
            if (onXZBoundary(p, min, max) || onYBoundary(p, min, max)) {
                expected.add(p);
            }
        }
        assertEquals(expected, new HashSet<>(iterate(faces)));
        for (BlockVector3 p : region) {
            assertEquals(expected.contains(p), faces.contains(p), p.toString());
        }
        assertFalse(faces.contains(min.subtract(1, 0, 0)));
        assertFalse(faces.contains(max.add(0, 1, 0)));
    }

    @ParameterizedTest
    @MethodSource("wallAndFaceBoxes")
    void wallsAreTheSidesWithoutTopAndBottom(BlockVector3 pos1, BlockVector3 pos2) {
        CuboidRegion region = new CuboidRegion(pos1, pos2);
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        Region walls = region.getWalls();

        Set<BlockVector3> expected = new HashSet<>();
        for (BlockVector3 p : region) {
            if (onXZBoundary(p, min, max)) {
                expected.add(p);
            }
        }
        assertEquals(expected, new HashSet<>(iterate(walls)));
        for (BlockVector3 p : region) {
            assertEquals(expected.contains(p), walls.contains(p), p.toString());
        }
    }

    static Stream<Arguments> wallAndFaceBoxes() {
        return Stream.of(
            Arguments.of(at(0, 0, 0), at(4, 3, 5)),
            Arguments.of(at(4, 3, 5), at(0, 0, 0)),
            Arguments.of(at(-3, -20, -4), at(3, -15, 4)),
            Arguments.of(at(0, 0, 0), at(0, 0, 0)),
            Arguments.of(at(0, 0, 0), at(1, 1, 1)),
            Arguments.of(at(0, -5, 0), at(6, -5, 6)),
            Arguments.of(at(-1, -1, -1), at(-1, 3, 5)),
            Arguments.of(at(-17, -2, -17), at(16, 2, 16))
        );
    }

    @Test
    void chunksOfNegativeCoordinates() {
        CuboidRegion region = new CuboidRegion(at(-17, -1, -1), at(-16, 0, 0));
        assertEquals(Set.of(BlockVector2.at(-2, -1), BlockVector2.at(-1, -1), BlockVector2.at(-2, 0), BlockVector2.at(-1, 0)),
            region.getChunks());
        Set<BlockVector3> cubes = new HashSet<>();
        for (int x : new int[] {-2, -1}) {
            for (int y : new int[] {-1, 0}) {
                for (int z : new int[] {-1, 0}) {
                    cubes.add(at(x, y, z));
                }
            }
        }
        assertEquals(cubes, region.getChunkCubes());
    }

    @Test
    void chunkCubesFollowNegativeY() {
        CuboidRegion region = new CuboidRegion(at(0, -64, 0), at(0, 319, 0));
        assertEquals(24, region.getChunkCubes().size());
        assertEquals(1, region.getChunks().size());
    }

    @Test
    void flatRegionColumnsMatchFootprint() {
        CuboidRegion region = new CuboidRegion(at(-2, -10, 3), at(1, -5, 4));
        List<BlockVector2> columns = new ArrayList<>();
        region.asFlatRegion().forEach(columns::add);
        assertEquals(4 * 2, columns.size());
        assertEquals(columns.size(), new HashSet<>(columns).size());
    }

    @Test
    @Disabled("CuboidRegion's iterator uses Integer.MIN_VALUE as its end marker, so a cuboid whose minimum X is"
        + " Integer.MIN_VALUE iterates nothing (expected 2 blocks for (MIN,0,0)-(MIN+1,0,0))")
    void iteratorStartingAtMinimumInteger() {
        CuboidRegion region = new CuboidRegion(at(Integer.MIN_VALUE, 0, 0), at(Integer.MIN_VALUE + 1, 0, 0));
        assertEquals(2, iterate(region).size());
    }

    @Test
    @Disabled("CuboidRegion's iterator overflows nextX past Integer.MAX_VALUE and then mistakes it for its end marker,"
        + " so (MAX-1,0,0)-(MAX,0,1) yields 2 of its 4 blocks (the second row is lost)")
    void iteratorEndingAtMaximumInteger() {
        CuboidRegion region = new CuboidRegion(at(Integer.MAX_VALUE - 1, 0, 0), at(Integer.MAX_VALUE, 0, 1));
        assertEquals(4, iterate(region).size());
    }

    @Provide
    Arbitrary<BlockVector3> positions() {
        return Arbitraries.integers().between(-40, 40).flatMap(x ->
            Arbitraries.integers().between(-70, 70).flatMap(y ->
                Arbitraries.integers().between(-40, 40).map(z -> at(x, y, z))));
    }

    @Property(tries = 100)
    boolean iteratedBlocksAreExactlyTheContainedBlocks(
        @ForAll("positions") BlockVector3 a, @ForAll("positions") BlockVector3 b) {
        CuboidRegion region = new CuboidRegion(a, b);
        List<BlockVector3> points = iterate(region);
        Set<BlockVector3> unique = new HashSet<>(points);
        return points.size() == region.getVolume()
            && unique.size() == points.size()
            && unique.stream().allMatch(region::contains)
            && unique.stream().allMatch(p -> p.containedWithin(region.getMinimumPoint(), region.getMaximumPoint()));
    }

    @Property
    boolean shiftThenShiftBackIsIdentity(
        @ForAll("positions") BlockVector3 a, @ForAll("positions") BlockVector3 b, @ForAll("positions") BlockVector3 by) {
        CuboidRegion region = new CuboidRegion(a, b);
        long volume = region.getVolume();
        region.shift(by);
        boolean sameVolume = region.getVolume() == volume;
        region.shift(by.multiply(-1));
        return sameVolume
            && region.getMinimumPoint().equals(a.getMinimum(b))
            && region.getMaximumPoint().equals(a.getMaximum(b));
    }

    @Property
    boolean chunksCoverEveryCorner(@ForAll("positions") BlockVector3 a, @ForAll("positions") BlockVector3 b) {
        CuboidRegion region = new CuboidRegion(a, b);
        Set<BlockVector2> chunks = region.getChunks();
        Set<BlockVector3> cubes = region.getChunkCubes();
        for (BlockVector3 corner : List.of(a, b, a.getMinimum(b), a.getMaximum(b))) {
            if (!chunks.contains(BlockVector2.at(corner.x() >> 4, corner.z() >> 4))
                || !cubes.contains(at(corner.x() >> 4, corner.y() >> 4, corner.z() >> 4))) {
                return false;
            }
        }
        return true;
    }
}
