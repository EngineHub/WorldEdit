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

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.sk89q.worldedit.util.test.RegionTestSupport.at;
import static com.sk89q.worldedit.util.test.RegionTestSupport.collect;
import static com.sk89q.worldedit.util.test.RegionTestSupport.hull;
import static com.sk89q.worldedit.util.test.RegionTestSupport.iterate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Invariants that every {@link Region} implementation is expected to uphold,
 * run against a table of hand-built shapes.
 */
class RegionContractTest extends BaseWorldEditTest {

    /**
     * How closely the iterated block count has to match {@link Region#getVolume()}.
     */
    private enum VolumeMode {
        /** The volume is the exact number of blocks. */
        EXACT,
        /** The volume is an analytic estimate (pi * r^2 * h and friends). */
        ESTIMATE,
        /** The volume is whatever the implementation reports; only sanity-checked. */
        UNCHECKED
    }

    private record Case(
        String name,
        Supplier<Region> factory,
        VolumeMode volumeMode,
        boolean allowsDuplicates,
        boolean resizable,
        boolean shiftable,
        Map<String, String> knownIssues
    ) {
        Case(String name, Supplier<Region> factory) {
            this(name, factory, VolumeMode.EXACT, false, true, true, Map.of());
        }

        Case estimate() {
            return new Case(name, factory, VolumeMode.ESTIMATE, allowsDuplicates, resizable, shiftable, knownIssues);
        }

        Case overlapping() {
            return new Case(name, factory, VolumeMode.UNCHECKED, true, false, false, knownIssues);
        }

        Case unchecked() {
            return new Case(name, factory, VolumeMode.UNCHECKED, allowsDuplicates, resizable, shiftable, knownIssues);
        }

        /** Cannot be expanded, contracted or shifted. */
        Case fixed() {
            return new Case(name, factory, volumeMode, allowsDuplicates, false, false, knownIssues);
        }

        /** Cannot be expanded or contracted, but can be shifted. */
        Case shiftOnly() {
            return new Case(name, factory, volumeMode, allowsDuplicates, false, true, knownIssues);
        }

        /** Skips the named test for this case because of a bug that is reproduced in a disabled test below. */
        Case knownIssue(String test, String reason) {
            Map<String, String> issues = new HashMap<>(knownIssues);
            issues.put(test, reason);
            return new Case(name, factory, volumeMode, allowsDuplicates, resizable, shiftable, issues);
        }

        void assumeNoKnownIssue(String test) {
            assumeFalse(knownIssues.containsKey(test), () -> "known issue: " + knownIssues.get(test));
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static List<BlockVector2> square(int min, int max) {
        return List.of(
            BlockVector2.at(min, min), BlockVector2.at(min, max),
            BlockVector2.at(max, max), BlockVector2.at(max, min)
        );
    }

    static Stream<Case> regions() {
        return Stream.of(
            // Cuboid
            new Case("cuboid", () -> new CuboidRegion(at(0, 0, 0), at(4, 3, 2))),
            new Case("cuboid single block", () -> new CuboidRegion(at(7, 7, 7), at(7, 7, 7))),
            new Case("cuboid swapped corners", () -> new CuboidRegion(at(5, 5, 5), at(0, 0, 0))),
            new Case("cuboid negative Y", () -> new CuboidRegion(at(-2, -10, -2), at(2, -5, 3))),
            new Case("cuboid spanning Y=0", () -> new CuboidRegion(at(-1, -2, -1), at(1, 2, 1))),
            new Case("cuboid across negative chunks", () -> new CuboidRegion(at(-20, -2, -17), at(17, 1, 16))),
            new Case("cuboid flat", () -> new CuboidRegion(at(0, 5, 0), at(9, 5, 9))),
            new Case("cuboid line", () -> new CuboidRegion(at(-3, 0, 4), at(12, 0, 4))),

            // Cylinder
            new Case("cylinder", () -> new CylinderRegion(at(0, 64, 0), Vector2.at(3, 3), 60, 64)).estimate(),
            new Case("cylinder elliptical", () -> new CylinderRegion(at(2, 10, -3), Vector2.at(5, 2), 8, 12)).estimate(),
            new Case("cylinder negative Y", () -> new CylinderRegion(at(-5, -20, -5), Vector2.at(4, 2), -24, -18)).estimate(),
            new Case("cylinder single column", () -> new CylinderRegion(at(0, 0, 0), Vector2.ZERO, 0, 5)).estimate()
                .knownIssue("expandThenContractRestoresRegion", "contract() clamps the radius to a minimum of 1.5"),
            new Case("cylinder single layer", () -> new CylinderRegion(at(16, -64, -16), Vector2.at(6, 6), -64, -64)).estimate(),

            // Ellipsoid
            new Case("ellipsoid", () -> new EllipsoidRegion(null, at(0, 0, 0), Vector3.at(3, 2, 4))).estimate()
                .knownIssue("expandThenContractRestoresRegion", "contract() direction is flipped"),
            new Case("ellipsoid negative Y", () -> new EllipsoidRegion(null, at(2, -30, -3), Vector3.at(2, 2, 2))).estimate()
                .knownIssue("expandThenContractRestoresRegion", "contract() direction is flipped"),
            new Case("ellipsoid across chunks", () -> new EllipsoidRegion(null, at(16, -1, -16), Vector3.at(5, 5, 5))).estimate()
                .knownIssue("expandThenContractRestoresRegion", "contract() direction is flipped"),
            new Case("ellipsoid single block", () -> new EllipsoidRegion(null, at(3, 3, 3), Vector3.ZERO)).estimate()
                .knownIssue("expandThenContractRestoresRegion", "contract() clamps the radius to a minimum of 1.5"),

            // Convex polyhedron
            new Case("convex tetrahedron", () -> hull(at(0, 0, 0), at(6, 0, 0), at(0, 6, 0), at(0, 0, 6))).unchecked().shiftOnly()
                .knownIssue("shiftPreservesShape", "shifting resets the stale contains() cache, see convexShiftKeepsSameBlocks")
                .knownIssue("shiftThereAndBackIsIdentity", "shifting resets the stale contains() cache, see convexShiftKeepsSameBlocks"),
            new Case("convex cube", () -> hull(
                at(0, 0, 0), at(3, 0, 0), at(0, 3, 0), at(0, 0, 3),
                at(3, 3, 0), at(3, 0, 3), at(0, 3, 3), at(3, 3, 3))).shiftOnly(),
            new Case("convex octahedron", () -> hull(
                at(-3, 0, 0), at(3, 0, 0), at(0, -3, 0), at(0, 3, 0), at(0, 0, -3), at(0, 0, 3))).unchecked().shiftOnly()
                .knownIssue("chunksCoverWholeRegion", "AbstractRegion.getChunks() only scans the lowest Y layer"),
            new Case("convex negative Y", () -> hull(
                at(-5, -20, -5), at(5, -20, -5), at(0, -20, 5), at(0, -10, 0))).unchecked().shiftOnly()
                .knownIssue("shiftPreservesShape", "shifting resets the stale contains() cache, see convexShiftKeepsSameBlocks")
                .knownIssue("shiftThereAndBackIsIdentity", "shifting resets the stale contains() cache, see convexShiftKeepsSameBlocks"),
            new Case("convex pyramid on a planar base", () -> hull(
                at(0, 0, 0), at(4, 0, 0), at(0, 4, 0), at(4, 4, 0), at(2, 2, 3))).unchecked().shiftOnly()
                .knownIssue("everyContainedPointIsIterated", "contains() depends on a stale cache, see convexContainsIsNotAffectedByStaleCache")
                .knownIssue("shiftPreservesShape", "contains() depends on a stale cache, see convexContainsIsNotAffectedByStaleCache")
                .knownIssue("shiftThereAndBackIsIdentity", "contains() depends on a stale cache, see convexContainsIsNotAffectedByStaleCache")
                .knownIssue("cloneIsIndependent", "contains() depends on a stale cache, see convexContainsIsNotAffectedByStaleCache"),
            new Case("convex with interior points", () -> hull(
                at(0, 0, 0), at(4, 0, 0), at(0, 4, 0), at(0, 0, 4), at(4, 4, 0), at(4, 0, 4), at(0, 4, 4), at(4, 4, 4),
                at(2, 2, 2), at(1, 1, 1))).shiftOnly(),

            // Polygon
            new Case("polygon square", () -> new Polygonal2DRegion(null, square(0, 3), 0, 4)),
            new Case("polygon negative Y", () -> new Polygonal2DRegion(null,
                List.of(BlockVector2.at(-4, -4), BlockVector2.at(4, -4), BlockVector2.at(0, 5)), -8, -2)),
            new Case("polygon concave", () -> new Polygonal2DRegion(null, List.of(
                BlockVector2.at(0, 0), BlockVector2.at(6, 0), BlockVector2.at(6, 6), BlockVector2.at(3, 2),
                BlockVector2.at(0, 6)), 1, 3)),

            // Intersection (really a union)
            new Case("intersection of disjoint cuboids", () -> new RegionIntersection(
                new CuboidRegion(at(0, 0, 0), at(2, 2, 2)),
                new CuboidRegion(at(10, 0, 0), at(12, 2, 2)))).unchecked().fixed(),
            new Case("intersection of overlapping cuboids", () -> new RegionIntersection(
                new CuboidRegion(at(0, 0, 0), at(4, 4, 4)),
                new CuboidRegion(at(3, 3, 3), at(7, 7, 7)))).overlapping(),
            new Case("intersection of mixed regions", () -> new RegionIntersection(
                new CuboidRegion(at(-10, -5, -10), at(-8, -3, -8)),
                new EllipsoidRegion(null, at(0, 0, 0), Vector3.at(2, 2, 2)),
                new CylinderRegion(at(10, 0, 10), Vector2.at(2, 2), 0, 3))).overlapping()
                .knownIssue("chunksCoverWholeRegion", "AbstractRegion.getChunks() only scans the lowest Y layer"),
            new Case("cuboid walls", () -> new CuboidRegion(at(-3, -2, -3), at(3, 2, 3)).getWalls()).overlapping(),
            new Case("cuboid faces", () -> new CuboidRegion(at(-3, -2, -3), at(3, 2, 3)).getFaces()).overlapping(),

            // Transform
            new Case("transform translation", () -> new TransformRegion(
                new CuboidRegion(at(0, 0, 0), at(3, 3, 3)),
                new AffineTransform().translate(5, -7, 2))).fixed(),
            new Case("transform translation of cylinder", () -> new TransformRegion(
                new CylinderRegion(at(0, 0, 0), Vector2.at(3, 3), 0, 3),
                new AffineTransform().translate(-9, -70, 4))).estimate().fixed()
        );
    }

    @ParameterizedTest
    @MethodSource("regions")
    void iteratorCountMatchesVolume(Case c) {
        c.assumeNoKnownIssue("iteratorCountMatchesVolume");
        Region region = c.factory().get();
        List<BlockVector3> points = iterate(region);
        long volume = region.getVolume();

        switch (c.volumeMode()) {
            case EXACT -> assertEquals(volume, points.size(), "iterated block count vs getVolume()");
            case ESTIMATE -> {
                long tolerance = Math.max(2, volume / 3);
                assertTrue(Math.abs(volume - points.size()) <= tolerance,
                    "iterated " + points.size() + " blocks but getVolume() is " + volume);
            }
            // the volume may be that of the bounding box, which is never smaller than the block count
            default -> assertTrue(volume >= points.size(), "getVolume() " + volume + " is below the block count " + points.size());
        }
        assertFalse(points.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("regions")
    void iteratorHasNoDuplicates(Case c) {
        c.assumeNoKnownIssue("iteratorHasNoDuplicates");
        assumeFalse(c.allowsDuplicates(), "region may return a block more than once");
        Region region = c.factory().get();
        List<BlockVector3> points = iterate(region);
        assertEquals(new HashSet<>(points).size(), points.size(), "iterator returned a block more than once");
    }

    @ParameterizedTest
    @MethodSource("regions")
    void everyIteratedPointIsContained(Case c) {
        c.assumeNoKnownIssue("everyIteratedPointIsContained");
        Region region = c.factory().get();
        for (BlockVector3 point : region) {
            assertTrue(region.contains(point), point + " was iterated but is not contained");
        }
    }

    @ParameterizedTest
    @MethodSource("regions")
    void everyContainedPointIsIterated(Case c) {
        c.assumeNoKnownIssue("everyContainedPointIsIterated");
        Region region = c.factory().get();
        Set<BlockVector3> points = collect(region);
        BlockVector3 min = region.getMinimumPoint().subtract(2, 2, 2);
        BlockVector3 max = region.getMaximumPoint().add(2, 2, 2);
        for (int x = min.x(); x <= max.x(); x++) {
            for (int y = min.y(); y <= max.y(); y++) {
                for (int z = min.z(); z <= max.z(); z++) {
                    BlockVector3 point = at(x, y, z);
                    assertEquals(points.contains(point), region.contains(point),
                        point + ": contains() disagrees with the iterator");
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("regions")
    void boundingBoxEnclosesEveryPoint(Case c) {
        c.assumeNoKnownIssue("boundingBoxEnclosesEveryPoint");
        Region region = c.factory().get();
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        assertTrue(min.x() <= max.x() && min.y() <= max.y() && min.z() <= max.z(),
            "minimum " + min + " is not <= maximum " + max);

        for (BlockVector3 point : region) {
            assertTrue(point.containedWithin(min, max), point + " is outside of " + min + " - " + max);
        }

        CuboidRegion box = region.getBoundingBox();
        assertEquals(min, box.getMinimumPoint());
        assertEquals(max, box.getMaximumPoint());
    }

    @ParameterizedTest
    @MethodSource("regions")
    void dimensionsMatchBoundingBox(Case c) {
        c.assumeNoKnownIssue("dimensionsMatchBoundingBox");
        Region region = c.factory().get();
        BlockVector3 size = region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1);
        assertEquals(size.x(), region.getWidth(), "width");
        assertEquals(size.y(), region.getHeight(), "height");
        assertEquals(size.z(), region.getLength(), "length");
    }

    @ParameterizedTest
    @MethodSource("regions")
    void centerIsInsideBoundingBox(Case c) {
        c.assumeNoKnownIssue("centerIsInsideBoundingBox");
        Region region = c.factory().get();
        Vector3 center = region.getCenter();
        Vector3 min = region.getMinimumPoint().toVector3();
        Vector3 max = region.getMaximumPoint().toVector3().add(1, 1, 1);
        assertTrue(center.x() >= min.x() && center.x() <= max.x()
                && center.y() >= min.y() && center.y() <= max.y()
                && center.z() >= min.z() && center.z() <= max.z(),
            "center " + center + " is outside of " + min + " - " + max);
    }

    @ParameterizedTest
    @MethodSource("regions")
    void doesNotContainPointsOutsideBoundingBox(Case c) {
        c.assumeNoKnownIssue("doesNotContainPointsOutsideBoundingBox");
        Region region = c.factory().get();
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        assertFalse(region.contains(min.subtract(1, 0, 0)));
        assertFalse(region.contains(min.subtract(0, 1, 0)));
        assertFalse(region.contains(min.subtract(0, 0, 1)));
        assertFalse(region.contains(max.add(1, 0, 0)));
        assertFalse(region.contains(max.add(0, 1, 0)));
        assertFalse(region.contains(max.add(0, 0, 1)));
    }

    @ParameterizedTest
    @MethodSource("regions")
    void chunksCoverWholeRegion(Case c) {
        c.assumeNoKnownIssue("chunksCoverWholeRegion");
        Region region = c.factory().get();
        Set<BlockVector2> chunks = region.getChunks();
        for (BlockVector3 point : region) {
            BlockVector2 chunk = BlockVector2.at(point.x() >> 4, point.z() >> 4);
            assertTrue(chunks.contains(chunk), point + " is in chunk " + chunk + " which is missing from " + chunks);
        }
    }

    @ParameterizedTest
    @MethodSource("regions")
    void chunkCubesCoverWholeRegion(Case c) {
        c.assumeNoKnownIssue("chunkCubesCoverWholeRegion");
        Region region = c.factory().get();
        Set<BlockVector3> cubes = region.getChunkCubes();
        for (BlockVector3 point : region) {
            BlockVector3 cube = at(point.x() >> 4, point.y() >> 4, point.z() >> 4);
            assertTrue(cubes.contains(cube), point + " is in chunk cube " + cube + " which is missing from " + cubes);
        }
    }

    @ParameterizedTest
    @MethodSource("regions")
    void chunksAreNotOverreported(Case c) {
        c.assumeNoKnownIssue("chunksAreNotOverreported");
        Region region = c.factory().get();
        Set<BlockVector2> expected = new HashSet<>();
        for (BlockVector3 point : region) {
            expected.add(BlockVector2.at(point.x() >> 4, point.z() >> 4));
        }
        Set<BlockVector2> chunks = region.getChunks();
        assertTrue(expected.containsAll(chunks), "getChunks() reports chunks the region does not touch: " + chunks);
    }

    @ParameterizedTest
    @MethodSource("regions")
    void shiftPreservesShape(Case c) {
        c.assumeNoKnownIssue("shiftPreservesShape");
        Region region = c.factory().get();
        assumeTrue(c.shiftable(), "region cannot be shifted");
        Set<BlockVector3> before = collect(region);
        long volume = region.getVolume();
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 shift = at(17, -33, -5);

        try {
            region.shift(shift);
        } catch (RegionOperationException e) {
            throw new AssertionError(e);
        }

        assertEquals(volume, region.getVolume(), "volume after shift");
        assertEquals(min.add(shift), region.getMinimumPoint());
        Set<BlockVector3> expected = new HashSet<>();
        for (BlockVector3 point : before) {
            expected.add(point.add(shift));
        }
        assertEquals(expected, collect(region), "shifted blocks");
    }

    @ParameterizedTest
    @MethodSource("regions")
    void shiftThereAndBackIsIdentity(Case c) throws RegionOperationException {
        c.assumeNoKnownIssue("shiftThereAndBackIsIdentity");
        Region region = c.factory().get();
        assumeTrue(c.shiftable(), "region cannot be shifted");
        Set<BlockVector3> before = collect(region);
        region.shift(at(100, 20, -40));
        region.shift(at(-100, -20, 40));
        assertEquals(before, collect(region));
    }

    @ParameterizedTest
    @MethodSource("regions")
    void expandThenContractRestoresRegion(Case c) throws RegionOperationException {
        c.assumeNoKnownIssue("expandThenContractRestoresRegion");
        Region region = c.factory().get();
        assumeTrue(c.resizable(), "region cannot be resized");
        // Y is the only axis a polygon can be resized along. Cylinders are only checked vertically
        // because their horizontal contract() direction is flipped, see cylinderContractShrinksTowardsDirection.
        boolean verticalOnly = region instanceof Polygonal2DRegion || region instanceof CylinderRegion;
        BlockVector3[] changes = verticalOnly
            ? new BlockVector3[] {at(0, 2, 0), at(0, -2, 0)}
            : new BlockVector3[] {at(2, 0, 0), at(-2, 0, 0), at(0, 2, 0), at(0, -2, 0), at(0, 0, 2), at(0, 0, -2)};

        for (BlockVector3 change : changes) {
            Region copy = region.clone();
            Set<BlockVector3> before = collect(copy);
            copy.expand(change);
            assertTrue(copy.getVolume() > region.getVolume(), "expanding by " + change + " did not grow the region");
            if (c.volumeMode() == VolumeMode.EXACT) {
                // round shapes change their curvature when resized, so this only holds for boxes and prisms
                assertTrue(collect(copy).containsAll(before), "expanding by " + change + " lost blocks");
            }
            // contract() takes the vector pointing away from the side that shrinks
            copy.contract(change.multiply(-1));
            assertEquals(before, collect(copy), "expand then contract by " + change);
        }
    }

    @ParameterizedTest
    @MethodSource("regions")
    void cloneHasTheSameBlocks(Case c) {
        c.assumeNoKnownIssue("cloneHasTheSameBlocks");
        Region region = c.factory().get();
        Region clone = region.clone();
        assertNotSame(region, clone);
        assertEquals(collect(region), collect(clone));
    }

    @ParameterizedTest
    @MethodSource("regions")
    void cloneIsIndependent(Case c) throws RegionOperationException {
        c.assumeNoKnownIssue("cloneIsIndependent");
        Region region = c.factory().get();
        Set<BlockVector3> before = collect(region);
        Region clone = region.clone();
        assumeTrue(c.shiftable(), "region cannot be shifted");
        clone.shift(at(50, 0, 0));
        assertEquals(before, collect(region), "mutating the clone changed the original");
    }

    @ParameterizedTest
    @MethodSource("regions")
    void flatRegionAgreesWithThreeDimensionalIteration(Case c) {
        c.assumeNoKnownIssue("flatRegionAgreesWithThreeDimensionalIteration");
        Region region = c.factory().get();
        assumeTrue(region instanceof FlatRegion, "not a flat region");
        FlatRegion flat = (FlatRegion) region;
        Set<BlockVector2> columns = new HashSet<>();
        for (BlockVector3 point : region) {
            columns.add(point.toBlockVector2());
        }
        Set<BlockVector2> flatColumns = new HashSet<>();
        int count = 0;
        for (BlockVector2 column : flat.asFlatRegion()) {
            flatColumns.add(column);
            count++;
        }
        assertEquals(columns, flatColumns);
        assertEquals(flatColumns.size(), count, "asFlatRegion() returned a column more than once");
        assertEquals(region.getMinimumPoint().y(), flat.getMinimumY());
        assertEquals(region.getMaximumPoint().y(), flat.getMaximumY());
    }

    @ParameterizedTest
    @MethodSource("regions")
    void polygonizeStaysWithinBoundingBox(Case c) {
        c.assumeNoKnownIssue("polygonizeStaysWithinBoundingBox");
        Region region = c.factory().get();
        List<BlockVector2> polygon = region.polygonize(-1);
        assertTrue(polygon.size() >= 3);
        for (BlockVector2 point : polygon) {
            assertTrue(point.x() >= region.getMinimumPoint().x() - 1 && point.x() <= region.getMaximumPoint().x() + 1
                    && point.z() >= region.getMinimumPoint().z() - 1 && point.z() <= region.getMaximumPoint().z() + 1,
                "polygon point " + point + " is outside the bounding box");
        }
    }

    @Test
    void roundShapeVolumesMatchTheAnalyticFormula() {
        // pi * 3.5^2 * 5, 4/3 * pi * 3.5^3 and pi * 4.5 * 2.5 * 7, rounded down: the radius is measured from block centres
        assertEquals(192, new CylinderRegion(at(0, 0, 0), Vector2.at(3, 3), 0, 4).getVolume());
        assertEquals(179, new EllipsoidRegion(null, at(0, 0, 0), Vector3.at(3, 3, 3)).getVolume());
        assertEquals(247, new CylinderRegion(at(0, 0, 0), Vector2.at(4, 2), -3, 3).getVolume());
    }

    @Test
    void nonAdjustableRegionsRejectResizing() {
        for (Case c : regions().filter(c -> !c.resizable()).toList()) {
            Region region = c.factory().get();
            assertThrows(RegionOperationException.class, () -> region.expand(at(2, 2, 2)), c.name());
            assertThrows(RegionOperationException.class, () -> region.contract(at(2, 2, 2)), c.name());
            if (!c.shiftable()) {
                assertThrows(RegionOperationException.class, () -> region.shift(at(2, 2, 2)), c.name());
            }
        }
    }

    // Reproductions of bugs found by the contract above. They are disabled until fixed; the matching cases
    // in regions() are skipped via Case#knownIssue.

    @Test
    @Disabled("AbstractRegion.getChunks() only scans the lowest Y layer: for an octahedron with vertices at"
        + " (+-3,0,0), (0,+-3,0), (0,0,+-3) it returns only chunk (0,0) but block (0,0,-3) is in chunk (0,-1)."
        + " Affects ConvexPolyhedralRegion and RegionIntersection, which inherit it")
    void abstractRegionChunksCoverAllYLayers() {
        Region region = hull(at(-3, 0, 0), at(3, 0, 0), at(0, -3, 0), at(0, 3, 0), at(0, 0, -3), at(0, 0, 3));
        assertTrue(region.contains(at(0, 0, -3)));
        assertTrue(region.getChunks().contains(BlockVector2.at(0, -1)), region.getChunks().toString());
    }

    @Test
    @Disabled("ConvexPolyhedralRegion.shift() changes which boundary blocks are contained: the hull with vertices"
        + " (0,0,0), (6,0,0), (0,6,0), (0,0,6) shifted by (17,-33,-5) iterates different blocks than the original"
        + " translated by the same amount (10 blocks missing, 1 extra). shift() resets the contains() cache, so"
        + " this is most likely the stale cache described in convexContainsIsNotAffectedByStaleCache")
    void convexShiftKeepsSameBlocks() {
        ConvexPolyhedralRegion region = hull(at(0, 0, 0), at(6, 0, 0), at(0, 6, 0), at(0, 0, 6));
        BlockVector3 shift = at(17, -33, -5);
        Set<BlockVector3> expected = new HashSet<>();
        for (BlockVector3 point : region) {
            expected.add(point.add(shift));
        }
        region.shift(shift);
        assertEquals(expected, collect(region));
    }

    @Test
    @Disabled("ConvexPolyhedralRegion.addVertex() leaves lastTriangle (the cache used by contains()) pointing at a"
        + " triangle it has just removed from the mesh, so the hull (0,0,0), (6,0,0), (0,6,0), (0,0,6) does not"
        + " contain its own last vertex (0,0,6) until another query replaces the cache. With the cache cleared it"
        + " does. Consequently contains() depends on the order of earlier queries: for the hull (0,0,0), (4,0,0),"
        + " (0,4,0), (4,4,0), (2,2,3) the base block (3,2,0) is outside on the fresh hull but inside after"
        + " contains((0,0,1)), and iterating (z outermost) finds 26 blocks while scanning x-first finds 36")
    void convexContainsIsNotAffectedByStaleCache() {
        ConvexPolyhedralRegion tetrahedron = hull(at(0, 0, 0), at(6, 0, 0), at(0, 6, 0), at(0, 0, 6));
        assertTrue(tetrahedron.contains(at(0, 0, 6)), "last added vertex");

        ConvexPolyhedralRegion fresh = hull(at(0, 0, 0), at(4, 0, 0), at(0, 4, 0), at(4, 4, 0), at(2, 2, 3));
        ConvexPolyhedralRegion warmed = hull(at(0, 0, 0), at(4, 0, 0), at(0, 4, 0), at(4, 4, 0), at(2, 2, 3));
        warmed.contains(at(0, 0, 1));
        assertEquals(warmed.contains(at(3, 2, 0)), fresh.contains(at(3, 2, 0)));
    }

    @Test
    @Disabled("CylinderRegion.getMaximumPoint() floors center + radius, so with a fractional radius the bounding box"
        + " is one block too small: radius (2.5, 2.5) at the origin contains (3,0,0) but getMaximumPoint().x() is 2")
    void cylinderBoundingBoxIncludesFractionalRadius() {
        CylinderRegion region = new CylinderRegion(at(0, 0, 0), Vector2.at(2.5, 2.5), 0, 0);
        assertTrue(region.contains(at(3, 0, 0)));
        assertTrue(at(3, 0, 0).containedWithin(region.getMinimumPoint(), region.getMaximumPoint()));
    }

    @Test
    @Disabled("EllipsoidRegion.getMaximumPoint() floors center + radius, so with a fractional radius the bounding box"
        + " is one block too small: radius (2.5, 2.5, 2.5) at the origin contains (3,0,0) but getMaximumPoint().x() is 2")
    void ellipsoidBoundingBoxIncludesFractionalRadius() {
        EllipsoidRegion region = new EllipsoidRegion(null, at(0, 0, 0), Vector3.at(2.5, 2.5, 2.5));
        assertTrue(region.contains(at(3, 0, 0)));
        assertTrue(at(3, 0, 0).containedWithin(region.getMinimumPoint(), region.getMaximumPoint()));
    }

    @Test
    @Disabled("CylinderRegion.contract() clamps the radius to at least 1.5 on both axes, so contracting a column with"
        + " radius 0 along X grows it (volume 4 -> 42 for height 6, and Z grows too)")
    void cylinderContractNeverGrows() throws RegionOperationException {
        CylinderRegion region = new CylinderRegion(at(0, 0, 0), Vector2.ZERO, 0, 5);
        long before = region.getVolume();
        region.contract(at(2, 0, 0));
        assertTrue(region.getVolume() <= before, before + " -> " + region.getVolume());
    }

    @Test
    @Disabled("EllipsoidRegion.contract() clamps the radius to at least 1.5 on all axes, so contracting a single"
        + " block along X grows it to a sphere (volume 0 -> 14)")
    void ellipsoidContractNeverGrows() throws RegionOperationException {
        EllipsoidRegion region = new EllipsoidRegion(null, at(0, 0, 0), Vector3.ZERO);
        long before = region.getVolume();
        region.contract(at(2, 0, 0));
        assertTrue(region.getVolume() <= before, before + " -> " + region.getVolume());
    }

    @Test
    @Disabled("CylinderRegion.contract() shrinks the opposite side horizontally compared to CuboidRegion (and"
        + " to its own vertical axis): contract((2,0,0)) lowers the maximum X instead of raising the minimum X")
    void cylinderContractShrinksTowardsDirection() throws RegionOperationException {
        CylinderRegion region = new CylinderRegion(at(0, 0, 0), Vector2.at(5, 5), 0, 0);
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        region.contract(at(2, 0, 0));
        assertEquals(min.x() + 2, region.getMinimumPoint().x());
        assertEquals(max.x(), region.getMaximumPoint().x());
    }

    @Test
    @Disabled("EllipsoidRegion.contract() shrinks the opposite side compared to CuboidRegion:"
        + " contract((2,0,0)) lowers the maximum X instead of raising the minimum X")
    void ellipsoidContractShrinksTowardsDirection() throws RegionOperationException {
        EllipsoidRegion region = new EllipsoidRegion(null, at(0, 0, 0), Vector3.at(5, 5, 5));
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        region.contract(at(2, 0, 0));
        assertEquals(min.x() + 2, region.getMinimumPoint().x());
        assertEquals(max.x(), region.getMaximumPoint().x());
    }

    @Test
    @Disabled("TransformRegion.getMinimumPoint()/getMaximumPoint() transform the two corners of the source box instead"
        + " of computing the bounds of the transformed region: rotating cuboid (1,-4,2)-(5,0,3) by 90 degrees"
        + " around Y gives minimum (2,-4,-1) and maximum (3,0,-5), i.e. maximum.z < minimum.z")
    void transformRegionBoundsAreOrderedAfterRotation() {
        Region region = new TransformRegion(
            new CuboidRegion(at(1, -4, 2), at(5, 0, 3)), new AffineTransform().rotateY(90));
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        assertTrue(min.x() <= max.x() && min.y() <= max.y() && min.z() <= max.z(), min + " - " + max);
        for (BlockVector3 point : region) {
            assertTrue(point.containedWithin(min, max), point + " outside " + min + " - " + max);
        }
    }

    @Test
    @Disabled("RegionIntersection does not override getVolume(), so it reports the volume of its bounding box:"
        + " two disjoint 3x3x3 cuboids (0,0,0)-(2,2,2) and (10,0,0)-(12,2,2) iterate 54 blocks but report 117")
    void regionIntersectionVolumeIsBlockCount() {
        Region region = new RegionIntersection(
            new CuboidRegion(at(0, 0, 0), at(2, 2, 2)),
            new CuboidRegion(at(10, 0, 0), at(12, 2, 2)));
        assertEquals(iterate(region).size(), region.getVolume());
    }
}
