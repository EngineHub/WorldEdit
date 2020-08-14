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

package com.sk89q.worldedit.internal.util;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Verifies that {@link RegionOptimizedVectorSorter} sorts properly.
 */
public class RegionOptimizedVectorSorterTest {
    /**
     * Find factors, smallest to biggest.
     *
     * @param num the number to find factors of
     * @return the factors from smallest to biggest
     */
    private static IntSortedSet findFactors(int num) {
        IntSortedSet factors = new IntRBTreeSet();

        // Skip two if the number is odd
        int incrementer = num % 2 == 0 ? 1 : 2;

        for (int i = 1; i <= Math.sqrt(num); i += incrementer) {

            // If there is no remainder, then the number is a factor.
            if (num % i == 0) {
                factors.add(i);
                factors.add(num / i);
            }
        }

        return factors;
    }

    @ParameterizedTest(
        name = "size={0}"
    )
    @ValueSource(ints = {
        0, 1, 10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000
    })
    void checkSorted(int size) {
        Random rng = new Random(size);
        List<BlockVector3> toSort;
        if (size == 0) {
            toSort = new ArrayList<>();
        } else {
            IntSortedSet factors = findFactors(size);
            // take the middle factors
            int x = factors.toIntArray()[factors.size() / 2];
            int z = size / x;
            int minX = x / 2;
            int maxX = minX + x % 2;
            int minZ = z / 2;
            int maxZ = minZ + z % 2;
            toSort = Lists.newArrayList(new CuboidRegion(
                BlockVector3.at(-minX, 0, -minZ), BlockVector3.at(maxX - 1, 0, maxZ - 1)
            ));
        }
        assertEquals(size, toSort.size());
        Collections.shuffle(toSort, rng);
        RegionOptimizedVectorSorter.sort(toSort);
        for (int i = 0; i < toSort.size() - 1; i++) {
            BlockVector3 curr = toSort.get(i);
            BlockVector3 next = toSort.get(i + 1);
            int currChunkX = curr.getX() >> 4;
            int nextChunkX = next.getX() >> 4;
            int currChunkZ = curr.getZ() >> 4;
            int nextChunkZ = next.getZ() >> 4;
            int currRegionX = currChunkX >> 5;
            int nextRegionX = nextChunkX >> 5;
            int currRegionZ = currChunkZ >> 5;
            int nextRegionZ = nextChunkZ >> 5;
            String spaceship = "(" + curr + " <=> " + next + ")";
            if (currRegionX > nextRegionX) {
                fail(spaceship + " "
                    + currRegionX + " region x should be less than or equal to " + nextRegionX);
            } else if (currRegionX == nextRegionX) {
                if (currRegionZ > nextRegionZ) {
                    fail(spaceship + " "
                        + currRegionZ + " region z should be less than or equal to " + nextRegionZ);
                } else if (currRegionZ == nextRegionZ) {
                    if (currChunkX > nextChunkX) {
                        fail(spaceship + " "
                            + currChunkX + " chunk x should be less than or equal to " + nextChunkX);
                    } else if (currChunkX == nextChunkX) {
                        if (currChunkZ > nextChunkZ) {
                            fail(spaceship + " "
                                + currChunkZ + " chunk z should be less than or equal to " + nextChunkZ);
                        } else if (currChunkZ == nextChunkZ) {
                            if (curr.getY() < next.getY()) {
                                fail(spaceship + " "
                                    + curr + " y should be greater than or equal to " + next);
                            }
                        }
                    }
                }
            }
        }
    }
}
