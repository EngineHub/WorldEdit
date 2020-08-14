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

package com.sk89q.worldedit.util.test;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.sk89q.worldedit.math.BlockVector3;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

public class VariedVectorGenerator {

    // For better coverage assurance, increase these values for a local Gradle run.
    // Don't do it for IntelliJ, it'll probably run out of memory.
    private static final int DEFAULT_DIVISIONS_XZ =
        Integer.getInteger("variedvecs.divisions.xz", 2);
    private static final int DEFAULT_DIVISIONS_Y =
        Integer.getInteger("variedvecs.divisions.y", 2);

    public final int divisionsXZ;
    public final int divisionsY;
    public final int maxXZ;
    public final long maxY;
    public final int xzStep;
    public final long yStep;
    public final Set<BlockVector3> alwaysInclude;

    public VariedVectorGenerator() {
        this(false, -1, -1);
    }

    public VariedVectorGenerator(boolean vanilla, int divisionsXZ, int divisionsY) {
        this.divisionsXZ = divisionsXZ == -1 ? DEFAULT_DIVISIONS_XZ : divisionsXZ;
        this.divisionsY = divisionsY == -1 ? DEFAULT_DIVISIONS_Y : divisionsY;
        maxXZ = 30_000_000;
        maxY = vanilla ? 255 : Integer.MAX_VALUE;
        xzStep = (maxXZ * 2) / this.divisionsXZ;
        yStep = (maxY * 2) / this.divisionsY;
        alwaysInclude =
            ImmutableSet.of(BlockVector3.ZERO, BlockVector3.ONE,
                BlockVector3.at(-maxXZ, 0, -maxXZ),
                BlockVector3.at(maxXZ, 0, maxXZ),
                BlockVector3.at(-maxXZ, maxY, -maxXZ),
                BlockVector3.at(maxXZ, maxY, maxXZ));
    }

    public Stream<BlockVector3> makeVectorsStream() {
        return Stream.concat(
            alwaysInclude.stream(),
            Streams.stream(generateVectors()).filter(v -> !alwaysInclude.contains(v))
        );
    }

    private Iterator<BlockVector3> generateVectors() {
        return new AbstractIterator<BlockVector3>() {

            private int x = -maxXZ + 1;
            private int z = -maxXZ + 1;
            private long y = maxY;

            @Override
            protected BlockVector3 computeNext() {
                if (x > maxXZ) {
                    return endOfData();
                }
                BlockVector3 newVector = BlockVector3.at(x, (int) y, z);
                y += yStep;
                if (y > maxY) {
                    y = 0;
                    z += xzStep;
                    if (z > maxXZ) {
                        z = -maxXZ;
                        x += xzStep;
                    }
                }
                return newVector;
            }
        };
    }
}
