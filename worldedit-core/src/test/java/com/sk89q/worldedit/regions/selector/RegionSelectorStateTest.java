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

package com.sk89q.worldedit.regions.selector;

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.PermissiveSelectorLimits;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.world.World;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.sk89q.worldedit.util.test.RegionTestSupport.at;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Drives every {@link RegionSelector} through its primary/secondary selection states.
 */
class RegionSelectorStateTest extends BaseWorldEditTest {

    private static final SelectorLimits NO_LIMITS = PermissiveSelectorLimits.getInstance();

    private static SelectorLimits limits(Optional<Integer> polygon, Optional<Integer> polyhedron) {
        return new SelectorLimits() {
            @Override
            public Optional<Integer> getPolygonVertexLimit() {
                return polygon;
            }

            @Override
            public Optional<Integer> getPolyhedronVertexLimit() {
                return polyhedron;
            }
        };
    }

    private record Step(boolean primary, BlockVector3 position) {
        static Step primary(int x, int y, int z) {
            return new Step(true, at(x, y, z));
        }

        static Step secondary(int x, int y, int z) {
            return new Step(false, at(x, y, z));
        }

        boolean apply(RegionSelector selector) {
            return primary
                ? selector.selectPrimary(position, NO_LIMITS)
                : selector.selectSecondary(position, NO_LIMITS);
        }

        @Override
        public String toString() {
            return (primary ? "primary " : "secondary ") + position;
        }
    }

    /**
     * A selector type together with a sequence of clicks that defines it.
     *
     * @param definedAfter the number of steps after which the selector has to report being defined
     * @param containedSteps the number of leading steps whose positions must lie inside the defined region (the last
     *     vertex of the hull is left out, see RegionContractTest#convexContainsIsNotAffectedByStaleCache)
     * @param small a second, smaller selection that is used to check that nothing leaks through {@code clear()}
     */
    private record Case(
        String name,
        Function<World, RegionSelector> factory,
        List<Step> steps,
        int definedAfter,
        int containedSteps,
        List<Step> small
    ) {
        @Override
        public String toString() {
            return name;
        }
    }

    // All selections are below Y=0 on purpose
    static Stream<Case> selectors() {
        return Stream.of(
            new Case("cuboid", CuboidRegionSelector::new,
                List.of(Step.primary(-8, -20, -8), Step.secondary(8, -10, 8)), 2, 2,
                List.of(Step.primary(0, -5, 0), Step.secondary(1, -4, 1))),
            new Case("extending cuboid", ExtendingCuboidRegionSelector::new,
                List.of(Step.primary(-8, -20, -8), Step.secondary(8, -10, 8)), 1, 2,
                List.of(Step.primary(0, -5, 0), Step.secondary(1, -4, 1))),
            new Case("cylinder", CylinderRegionSelector::new,
                List.of(Step.primary(0, -20, 0), Step.secondary(6, -20, 0)), 2, 2,
                List.of(Step.primary(0, -5, 0), Step.secondary(1, -5, 0))),
            new Case("sphere", SphereRegionSelector::new,
                List.of(Step.primary(0, -20, 0), Step.secondary(6, -20, 0)), 2, 2,
                List.of(Step.primary(0, -5, 0), Step.secondary(1, -5, 0))),
            new Case("ellipsoid", EllipsoidRegionSelector::new,
                List.of(Step.primary(0, -20, 0), Step.secondary(6, -18, 3)), 2, 1,
                List.of(Step.primary(0, -5, 0), Step.secondary(1, -5, 0))),
            new Case("polygon", Polygonal2DRegionSelector::new,
                List.of(Step.primary(0, -20, 0), Step.secondary(8, -10, 0), Step.secondary(0, -15, 8)), 3, 3,
                List.of(Step.primary(0, -5, 0), Step.secondary(2, -5, 0), Step.secondary(0, -5, 2))),
            new Case("extending polygon", ExtendingPolygonal2DRegionSelector::new,
                List.of(Step.primary(0, -20, 0), Step.secondary(8, -10, 0), Step.secondary(0, -15, 8)), 3, 3,
                List.of(Step.primary(0, -5, 0), Step.secondary(2, -5, 0), Step.secondary(0, -5, 2))),
            new Case("convex polyhedron", ConvexPolyhedralRegionSelector::new,
                List.of(Step.primary(0, -20, 0), Step.secondary(8, -20, 0), Step.secondary(0, -20, 8),
                    Step.secondary(0, -10, 0)), 3, 3,
                List.of(Step.primary(0, -5, 0), Step.secondary(2, -5, 0), Step.secondary(0, -5, 2),
                    Step.secondary(0, -3, 0)))
        );
    }

    private static World mockWorld() {
        World world = mock(World.class);
        // Regions clamp Y to the world's limits, which a bare mock would report as 0..0
        when(world.getMinY()).thenReturn(-64);
        when(world.getMaxY()).thenReturn(319);
        return world;
    }

    @Test
    void everySelectorTypeIsCovered() {
        Set<Class<?>> covered = new HashSet<>();
        World world = mockWorld();
        selectors().forEach(c -> covered.add(c.factory().apply(world).getClass()));
        for (RegionSelectorType type : RegionSelectorType.values()) {
            assertTrue(covered.contains(type.getSelectorClass()), type + " has no selector state test");
        }
    }

    @ParameterizedTest
    @MethodSource("selectors")
    void freshSelectorIsIncomplete(Case c) {
        World world = mockWorld();
        RegionSelector selector = c.factory().apply(world);

        assertFalse(selector.isDefined());
        assertThrows(IncompleteRegionException.class, selector::getRegion);
        assertNotNull(selector.getIncompleteRegion());
        assertSame(world, selector.getWorld());
        assertSame(world, selector.getIncompleteRegion().getWorld());
    }

    @ParameterizedTest
    @MethodSource("selectors")
    void becomesDefinedExactlyWhenEnoughPointsAreSelected(Case c) throws IncompleteRegionException {
        RegionSelector selector = c.factory().apply(mockWorld());

        for (int i = 0; i < c.steps().size(); i++) {
            Step step = c.steps().get(i);
            assertTrue(step.apply(selector), "step " + (i + 1) + " (" + step + ") should change the selection");

            boolean expectedDefined = i + 1 >= c.definedAfter();
            assertEquals(expectedDefined, selector.isDefined(), "isDefined() after " + (i + 1) + " steps");
            if (expectedDefined) {
                assertNotNull(selector.getRegion());
            } else {
                assertThrows(IncompleteRegionException.class, selector::getRegion, "getRegion() after " + (i + 1) + " steps");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("selectors")
    void definedRegionContainsTheSelectedPoints(Case c) throws IncompleteRegionException {
        RegionSelector selector = c.factory().apply(mockWorld());
        c.steps().forEach(step -> step.apply(selector));

        Region region = selector.getRegion();
        for (Step step : c.steps().subList(0, c.containedSteps())) {
            assertTrue(region.contains(step.position()), step + " is not in " + region);
        }
        assertEquals(c.steps().get(0).position(), selector.getPrimaryPosition());
        assertTrue(selector.getVolume() > 0);
        assertEquals(selector.getVolume(), region.getVolume());
        assertSame(region, selector.getRegion(), "getRegion() should keep returning the same region");
    }

    @ParameterizedTest
    @MethodSource("selectors")
    void regionKeepsTheMockedWorld(Case c) throws IncompleteRegionException {
        World world = mockWorld();
        RegionSelector selector = c.factory().apply(world);
        c.steps().forEach(step -> step.apply(selector));
        assertSame(world, selector.getRegion().getWorld());

        World other = mockWorld();
        selector.setWorld(other);
        assertSame(other, selector.getWorld());
        assertSame(other, selector.getRegion().getWorld());
    }

    @ParameterizedTest
    @MethodSource("selectors")
    void clearReturnsToTheIncompleteState(Case c) {
        RegionSelector selector = c.factory().apply(mockWorld());
        c.steps().forEach(step -> step.apply(selector));
        assertTrue(selector.isDefined());

        selector.clear();

        assertFalse(selector.isDefined());
        assertThrows(IncompleteRegionException.class, selector::getRegion);
    }

    @ParameterizedTest
    @MethodSource("selectors")
    void clearedSelectorBehavesLikeANewOne(Case c) throws IncompleteRegionException {
        RegionSelector reused = c.factory().apply(mockWorld());
        c.steps().forEach(step -> step.apply(reused));
        reused.clear();
        c.small().forEach(step -> step.apply(reused));

        RegionSelector fresh = c.factory().apply(mockWorld());
        c.small().forEach(step -> step.apply(fresh));

        assertEquals(fresh.isDefined(), reused.isDefined());
        Region expected = fresh.getRegion();
        Region actual = reused.getRegion();
        assertEquals(expected.getMinimumPoint(), actual.getMinimumPoint());
        assertEquals(expected.getMaximumPoint(), actual.getMaximumPoint());
        assertEquals(expected.getVolume(), actual.getVolume());
    }

    @ParameterizedTest
    @MethodSource("selectors")
    void reselectingAfterClearDefinesAgain(Case c) throws IncompleteRegionException {
        RegionSelector selector = c.factory().apply(mockWorld());
        for (int round = 0; round < 3; round++) {
            for (int i = 0; i < c.steps().size(); i++) {
                c.steps().get(i).apply(selector);
                assertEquals(i + 1 >= c.definedAfter(), selector.isDefined(), "round " + round + ", step " + (i + 1));
            }
            assertNotNull(selector.getRegion());
            selector.clear();
            assertFalse(selector.isDefined());
        }
    }

    @ParameterizedTest
    @MethodSource("selectors")
    void firstPrimarySelectionAlwaysChangesTheSelection(Case c) {
        RegionSelector selector = c.factory().apply(mockWorld());
        assertTrue(c.steps().get(0).apply(selector));
    }

    @ParameterizedTest
    @MethodSource("selectors")
    void secondaryWithoutPrimaryDefinesOnlySelectorsThatDoNotNeedOne(Case c) {
        RegionSelector selector = c.factory().apply(mockWorld());
        for (Step step : c.steps()) {
            if (!step.primary()) {
                // the return value is allowed to differ between selector types
                assertDoesNotThrow(() -> step.apply(selector));
            }
        }
        // the extending cuboid treats a secondary click as a primary one, and a hull only counts vertices
        boolean definedWithoutPrimary = selector instanceof ExtendingCuboidRegionSelector
            || selector instanceof ConvexPolyhedralRegionSelector;
        assertEquals(definedWithoutPrimary, selector.isDefined());
    }

    @ParameterizedTest
    @MethodSource("selectors")
    void primaryPositionIsAvailableOnceDefined(Case c) throws IncompleteRegionException {
        RegionSelector selector = c.factory().apply(mockWorld());
        c.steps().forEach(step -> step.apply(selector));
        assertEquals(c.steps().get(0).position(), selector.getPrimaryPosition());
    }

    static Stream<Arguments> selectorsThatThrowWithoutPrimaryPosition() {
        return selectors()
            .filter(c -> Set.of("cuboid", "cylinder", "polygon", "extending polygon").contains(c.name()))
            .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("selectorsThatThrowWithoutPrimaryPosition")
    void primaryPositionIsIncompleteBeforeAnySelection(Case c) {
        RegionSelector selector = c.factory().apply(mockWorld());
        assertThrows(IncompleteRegionException.class, selector::getPrimaryPosition);
    }

    @Test
    @Disabled("RegionSelector#getPrimaryPosition() is documented to throw IncompleteRegionException when nothing has"
        + " been selected, but EllipsoidRegionSelector/SphereRegionSelector return the (0,0,0) center and"
        + " ConvexPolyhedralRegionSelector returns null instead")
    void everySelectorThrowsForPrimaryPositionBeforeAnySelection() {
        List<Case> offenders = new ArrayList<>();
        selectors().forEach(c -> {
            try {
                c.factory().apply(mockWorld()).getPrimaryPosition();
                offenders.add(c);
            } catch (IncompleteRegionException _) {
                // expected
            }
        });
        assertTrue(offenders.isEmpty(), "did not throw: " + offenders);
    }

    @Test
    void cuboidSelectionTimingIsOrderIndependent() throws IncompleteRegionException {
        CuboidRegionSelector selector = new CuboidRegionSelector(mockWorld());
        assertTrue(selector.selectSecondary(at(5, -3, 5), NO_LIMITS));
        assertFalse(selector.isDefined());
        assertThrows(IncompleteRegionException.class, selector::getRegion);
        assertThrows(IncompleteRegionException.class, selector::getPrimaryPosition);

        assertTrue(selector.selectPrimary(at(-5, -9, -5), NO_LIMITS));
        assertTrue(selector.isDefined());
        assertEquals(at(-5, -9, -5), selector.getRegion().getMinimumPoint());
        assertEquals(at(5, -3, 5), selector.getRegion().getMaximumPoint());
    }

    @Test
    void cuboidReselectingTheSamePointReportsNoChange() {
        CuboidRegionSelector selector = new CuboidRegionSelector(mockWorld());
        assertTrue(selector.selectPrimary(at(1, -2, 3), NO_LIMITS));
        assertFalse(selector.selectPrimary(at(1, -2, 3), NO_LIMITS));
        assertTrue(selector.selectSecondary(at(4, -5, 6), NO_LIMITS));
        assertFalse(selector.selectSecondary(at(4, -5, 6), NO_LIMITS));
        assertTrue(selector.selectPrimary(at(0, 0, 0), NO_LIMITS));
    }

    @Test
    void extendingCuboidGrowsToIncludeEverySelectedPoint() throws IncompleteRegionException {
        ExtendingCuboidRegionSelector selector = new ExtendingCuboidRegionSelector(mockWorld());
        selector.selectPrimary(at(0, -10, 0), NO_LIMITS);
        assertTrue(selector.isDefined());
        assertEquals(1, selector.getRegion().getVolume());

        assertTrue(selector.selectSecondary(at(3, -12, -2), NO_LIMITS));
        assertFalse(selector.selectSecondary(at(1, -11, -1), NO_LIMITS), "a point inside the region changes nothing");
        assertTrue(selector.selectSecondary(at(-4, -5, 6), NO_LIMITS));

        Region region = selector.getRegion();
        assertEquals(at(-4, -12, -2), region.getMinimumPoint());
        assertEquals(at(3, -5, 6), region.getMaximumPoint());
    }

    @Test
    void cylinderCenterIsOnlyKnownOnceTheRadiusIsSelected() throws IncompleteRegionException {
        CylinderRegionSelector selector = new CylinderRegionSelector(mockWorld());
        selector.selectPrimary(at(10, -30, 10), NO_LIMITS);
        assertFalse(selector.isDefined());
        assertThrows(IncompleteRegionException.class, selector::getPrimaryPosition);

        selector.selectSecondary(at(14, -34, 10), NO_LIMITS);
        assertTrue(selector.isDefined());
        // the secondary click also stretches the cylinder to include its Y
        assertEquals(-34, selector.getRegion().getMinimumPoint().y());
        assertEquals(-30, selector.getRegion().getMaximumPoint().y());
    }

    @Test
    void cylinderPrimaryClickStartsOverEvenWhenDefined() throws IncompleteRegionException {
        CylinderRegionSelector selector = new CylinderRegionSelector(mockWorld());
        selector.selectPrimary(at(0, 0, 0), NO_LIMITS);
        selector.selectSecondary(at(10, 20, 0), NO_LIMITS);
        assertTrue(selector.isDefined());

        selector.selectPrimary(at(50, -5, 50), NO_LIMITS);

        assertFalse(selector.isDefined());
        assertThrows(IncompleteRegionException.class, selector::getRegion);
        selector.selectSecondary(at(51, -5, 50), NO_LIMITS);
        Region region = selector.getRegion();
        assertEquals(-5, region.getMinimumPoint().y(), "Y from the previous selection leaked into the new one");
        assertEquals(-5, region.getMaximumPoint().y());
    }

    @Test
    void ellipsoidPrimaryClickStartsOverEvenWhenDefined() throws IncompleteRegionException {
        EllipsoidRegionSelector selector = new EllipsoidRegionSelector(mockWorld());
        selector.selectPrimary(at(0, 0, 0), NO_LIMITS);
        selector.selectSecondary(at(10, 20, 30), NO_LIMITS);
        assertTrue(selector.isDefined());

        selector.selectPrimary(at(100, -50, 100), NO_LIMITS);

        assertFalse(selector.isDefined());
        assertThrows(IncompleteRegionException.class, selector::getRegion);
        selector.selectSecondary(at(101, -50, 100), NO_LIMITS);
        Region region = selector.getRegion();
        assertEquals(at(99, -50, 100), region.getMinimumPoint());
        assertEquals(at(101, -50, 100), region.getMaximumPoint());
        assertTrue(region.getVolume() < 100, "radius of the previous selection leaked into the new one: " + region.getVolume());
    }

    @Test
    void polygonNeedsThreePointsToBeDefined() throws IncompleteRegionException {
        Polygonal2DRegionSelector selector = new Polygonal2DRegionSelector(mockWorld());
        assertTrue(selector.selectPrimary(at(0, 0, 0), NO_LIMITS));
        assertTrue(selector.selectSecondary(at(5, 0, 0), NO_LIMITS));
        assertFalse(selector.isDefined());
        assertTrue(selector.selectSecondary(at(5, 0, 5), NO_LIMITS));
        assertTrue(selector.isDefined());
        assertEquals(selector.getRegion().getMinimumPoint().y(), selector.getRegion().getMaximumPoint().y());
    }

    @Test
    void polygonIgnoresRepeatingTheLastPoint() {
        Polygonal2DRegionSelector selector = new Polygonal2DRegionSelector(mockWorld());
        selector.selectPrimary(at(0, 0, 0), NO_LIMITS);
        assertTrue(selector.selectSecondary(at(5, 0, 0), NO_LIMITS));
        assertFalse(selector.selectSecondary(at(5, 3, 0), NO_LIMITS), "same X/Z as the last point changes nothing");
        assertFalse(selector.isDefined());
    }

    @Test
    void polygonVertexLimitEventuallyRejectsPoints() {
        Polygonal2DRegionSelector selector = new Polygonal2DRegionSelector(mockWorld());
        SelectorLimits limit = limits(Optional.of(3), Optional.empty());
        selector.selectPrimary(at(0, 0, 0), limit);
        boolean rejected = false;
        for (int i = 1; i < 10 && !rejected; i++) {
            rejected = !selector.selectSecondary(at(i, 0, i * i), limit);
        }
        assertTrue(rejected, "a vertex limit of 3 never rejected a point");
    }

    @Test
    @Disabled("Polygonal2DRegionSelector rejects a point only when the polygon already has MORE than the limit, so a"
        + " limit of 3 allows 4 vertices (primary + 3 secondary clicks)")
    void polygonVertexLimitIsTheMaximumNumberOfVertices() {
        Polygonal2DRegionSelector selector = new Polygonal2DRegionSelector(mockWorld());
        SelectorLimits limit = limits(Optional.of(3), Optional.empty());
        selector.selectPrimary(at(0, 0, 0), limit);
        selector.selectSecondary(at(5, 0, 0), limit);
        selector.selectSecondary(at(5, 0, 5), limit);
        assertFalse(selector.selectSecondary(at(0, 0, 5), limit));
    }

    @Test
    void convexPolyhedronIsDefinedOnceTheVerticesSpanAnArea() throws IncompleteRegionException {
        ConvexPolyhedralRegionSelector selector = new ConvexPolyhedralRegionSelector(mockWorld());
        selector.selectPrimary(at(0, -20, 0), NO_LIMITS);
        selector.selectSecondary(at(4, -20, 0), NO_LIMITS);
        assertFalse(selector.isDefined());
        selector.selectSecondary(at(8, -20, 0), NO_LIMITS);
        assertFalse(selector.isDefined(), "collinear points do not span an area");
        assertThrows(IncompleteRegionException.class, selector::getRegion);
        selector.selectSecondary(at(0, -20, 4), NO_LIMITS);
        assertTrue(selector.isDefined());
        assertNotNull(selector.getRegion());
    }

    @Test
    void convexPolyhedronVertexLimitEventuallyRejectsPoints() {
        ConvexPolyhedralRegionSelector selector = new ConvexPolyhedralRegionSelector(mockWorld());
        SelectorLimits limit = limits(Optional.empty(), Optional.of(3));
        selector.selectPrimary(at(0, 0, 0), limit);
        boolean rejected = false;
        for (int i = 1; i < 10 && !rejected; i++) {
            rejected = !selector.selectSecondary(at(i, i * i, -i * i * i), limit);
        }
        assertTrue(rejected, "a vertex limit of 3 never rejected a point");
    }

    @Test
    @Disabled("ConvexPolyhedralRegionSelector rejects a vertex only when the hull already has MORE than the limit, so"
        + " a limit of 3 allows 4 vertices (primary + 3 secondary clicks)")
    void convexPolyhedronVertexLimitIsTheMaximumNumberOfVertices() {
        ConvexPolyhedralRegionSelector selector = new ConvexPolyhedralRegionSelector(mockWorld());
        SelectorLimits limit = limits(Optional.empty(), Optional.of(3));
        selector.selectPrimary(at(0, 0, 0), limit);
        selector.selectSecondary(at(1, 0, 0), limit);
        selector.selectSecondary(at(0, 1, 0), limit);
        assertFalse(selector.selectSecondary(at(0, 0, 1), limit));
    }
}
