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
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.sk89q.worldedit.util.test.RegionTestSupport.at;
import static com.sk89q.worldedit.util.test.RegionTestSupport.hull;
import static com.sk89q.worldedit.util.test.RegionTestSupport.iterate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regions with no volume, no height or too few points to be a solid.
 */
class RegionDegenerateShapesTest {

    static Stream<Arguments> undefinedHulls() {
        return Stream.of(
            Arguments.of("single vertex", new BlockVector3[] {at(1, 1, 1)}),
            Arguments.of("two vertices", new BlockVector3[] {at(0, 0, 0), at(4, 0, 0)}),
            Arguments.of("three collinear vertices", new BlockVector3[] {at(0, 0, 0), at(2, 0, 0), at(4, 0, 0)}),
            Arguments.of("many collinear vertices", new BlockVector3[] {at(0, 0, 0), at(1, 1, 1), at(2, 2, 2), at(3, 3, 3)}),
            Arguments.of("repeated vertex", new BlockVector3[] {at(2, 2, 2), at(2, 2, 2), at(2, 2, 2)})
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("undefinedHulls")
    void hullWithoutAreaIsNotDefinedAndContainsNothing(String name, BlockVector3[] vertices) {
        ConvexPolyhedralRegion region = hull(vertices);
        assertFalse(region.isDefined(), name);
        for (int x = -1; x <= 5; x++) {
            for (int y = -1; y <= 5; y++) {
                for (int z = -1; z <= 5; z++) {
                    assertFalse(region.contains(at(x, y, z)), name + " contains " + at(x, y, z));
                }
            }
        }
        assertTrue(iterate(region).isEmpty(), name);
    }

    @Test
    void emptyHullContainsNothing() {
        ConvexPolyhedralRegion region = hull();
        assertFalse(region.isDefined());
        assertFalse(region.contains(at(0, 0, 0)));
        assertTrue(region.getVertices().isEmpty());
    }

    @Test
    @Disabled("An empty ConvexPolyhedralRegion returns null from getMinimumPoint()/getMaximumPoint(), so"
        + " getVolume(), getWidth(), iterator() etc. throw NullPointerException instead of describing an empty region")
    void emptyHullHasZeroVolume() {
        ConvexPolyhedralRegion region = hull();
        assertEquals(0, region.getVolume());
        assertTrue(iterate(region).isEmpty());
    }

    @Test
    void hullWithCoplanarVerticesIsAFlatSlab() {
        ConvexPolyhedralRegion region = hull(at(0, 5, 0), at(4, 5, 0), at(0, 5, 4), at(4, 5, 4));
        assertTrue(region.isDefined());
        assertEquals(25, iterate(region).size());
        assertEquals(25, region.getVolume());
        assertEquals(1, region.getHeight());
        for (BlockVector3 point : region) {
            assertEquals(5, point.y());
        }
    }

    @Test
    void hullOfSingleTriangleAtNegativeY() {
        ConvexPolyhedralRegion region = hull(at(0, -30, 0), at(4, -30, 0), at(0, -30, 4));
        assertTrue(region.isDefined());
        assertEquals(-30, region.getMinimumPoint().y());
        assertEquals(-30, region.getMaximumPoint().y());
        assertTrue(region.contains(at(0, -30, 0)));
        assertTrue(region.contains(at(2, -30, 2)));
        assertFalse(region.contains(at(0, -29, 0)));
        assertFalse(region.contains(at(0, -31, 0)));
    }

    @Test
    @Disabled("A ConvexPolyhedralRegion built from three vertices is treated as its whole bounding rectangle:"
        + " (0,0,0), (4,0,0), (0,4,0) contains (4,4,0), which is outside the triangle (25 blocks instead of 15)")
    void hullOfSingleTriangleOnlyContainsTheTriangle() {
        ConvexPolyhedralRegion region = hull(at(0, 0, 0), at(4, 0, 0), at(0, 4, 0));
        assertFalse(region.contains(at(4, 4, 0)));
        assertEquals(15, iterate(region).size());
    }

    @Test
    void cylinderWithZeroHeightIsEmpty() {
        // height is maxY - minY + 1, so maxY = minY - 1 leaves no layers
        CylinderRegion region = new CylinderRegion(at(0, 0, 0), Vector2.at(2, 2), 5, 4);
        assertEquals(0, region.getVolume());
        assertTrue(iterate(region).isEmpty());
    }

    @Test
    @Disabled("CylinderRegion with maxY < minY (5 and 2, radius 2) reports a negative height (-2) and volume (-40)"
        + " instead of being empty, and its bounding box is inverted: min.y 5, max.y 2")
    void cylinderOfNegativeHeightIsEmpty() {
        CylinderRegion region = new CylinderRegion(at(0, 0, 0), Vector2.at(2, 2), 5, 2);
        assertEquals(0, region.getVolume());
        assertTrue(iterate(region).isEmpty());
        assertTrue(region.getMinimumPoint().y() <= region.getMaximumPoint().y());
    }

    @Test
    void polygonWithSwappedYIsNormalised() {
        Polygonal2DRegion region = new Polygonal2DRegion(null,
            List.of(BlockVector2.at(0, 0), BlockVector2.at(3, 0), BlockVector2.at(3, 3)), 5, 2);
        assertEquals(2, region.getMinimumPoint().y());
        assertEquals(5, region.getMaximumPoint().y());
        assertEquals(iterate(region).size(), region.getVolume());
    }

    @Test
    void ellipsoidAndCylinderOfRadiusZeroAreOneColumn() {
        assertEquals(List.of(at(3, -8, 3)), iterate(new EllipsoidRegion(null, at(3, -8, 3), Vector3.ZERO)));
        assertEquals(3, iterate(new CylinderRegion(at(0, -2, 0), Vector2.ZERO, -2, 0)).size());
    }

    @Test
    void wallsOfSingleBlockAreThatBlock() {
        Region walls = new CuboidRegion(at(0, 0, 0), at(0, 0, 0)).getWalls();
        assertEquals(List.of(at(0, 0, 0)), iterate(walls).stream().distinct().toList());
    }
}
