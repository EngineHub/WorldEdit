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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Polygonal2DRegionTest {

    @ParameterizedTest
    @MethodSource("areaTestData")
    void testArea(int[][] coordinates, long expectedVolume) {
        List<BlockVector2> points = toPoints(coordinates);
        Polygonal2DRegion region = new Polygonal2DRegion(null, points, 0, 0);

        assertEquals(expectedVolume, region.getVolume());
    }

    static List<Arguments> areaTestData() {
        return List.of(
                Arguments.of(new int[][]{{0, 0}, {0, 1}, {1, 1}, {1, 0}}, 4), // square
                Arguments.of(new int[][]{{0, 0}, {2, 2}, {2, 0}}, 6), // triangle
                Arguments.of(new int[][]{{6, 3}, {6, 1}, {0, 0}}, 10), // polygon with separated parts
                Arguments.of(new int[][]{{-1, 1}, {4, 1}, {4, -3}, {-1, -3}}, 30), // x < 0
                Arguments.of(new int[][]{
                        {0, 9}, {6, 9}, {6, 0}, {1, 2}, {4, 4}, {3, 7}, {0, 5}
                }, 47), // concave
                Arguments.of(new int[][]{
                        {0, 4}, {2, 6}, {4, 6}, {6, 4}, {6, 2}, {4, 0}, {2, 0}, {0, 2}
                }, 37), // octagon
                Arguments.of(new int[][]{
                        {0, 0}, {2, 2}, {2, 4}, {0, 6}, {6, 6}, {4, 4}, {4, 2}, {6, 0}
                }, 33), // hourglass
                Arguments.of(new int[][]{
                        {0, 5}, {11, 5}, {11, 0}, {9, 0}, {9, 4}, {7, 4}, {7, 1}, {6, 1},
                        {6, 2}, {4, 2}, {4, 1}, {3, 1}, {3, 0}, {1, 0}, {1, 3}, {0, 3}
                }, 60), // checks if new direction is well assigned
                Arguments.of(new int[][]{
                        {0, 5}, {2, 3}, {5, 3}, {7, 1}, {0, 1}
                }, 24), // horizontal and downwards
                Arguments.of(new int[][]{
                        {0, 0}, {2, 2}, {4, 2}, {6, 4}, {6, 0}
                }, 21), // horizontal and upwards
                Arguments.of(new int[][]{
                        {0, 5}, {3, 5}, {2, 3}, {5, 3}, {7, 5}, {7, 1}, {0, 1}
                }, 34), // horizontal with upwards and downwards
                Arguments.of(new int[][]{
                        {0, 5}, {3, 5}, {2, 3}, {4, 3}, {5, 3}, {7, 5}, {7, 1}, {0, 1}
                }, 34), // horizontal, upwards, downwards, redundant point
                Arguments.of(new int[][]{
                        {1, 3}, {3, 3}, {4, 5}, {6, 8}, {7, 6}, {9, 6}, {12, 8}, {11, 6}, {11, 4},
                        {10, 2}, {8, 0}, {6, 1}, {9, 4}, {6, 3}, {4, 1}, {3, 0}, {1, 1}
                }, 55) // complex polygon
        );
    }

    private static List<BlockVector2> toPoints(int[]... coordinates) {
        List<BlockVector2> points = new ArrayList<>();
        for (int[] coordinate : coordinates) {
            points.add(BlockVector2.at(coordinate[0], coordinate[1]));
        }

        return points;
    }

}