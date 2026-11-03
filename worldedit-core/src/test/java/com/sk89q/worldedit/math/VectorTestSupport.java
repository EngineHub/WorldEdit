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

package com.sk89q.worldedit.math;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

/**
 * Shared jqwik generators for the vector tests.
 */
final class VectorTestSupport {

    /** Doubles that are exactly representable, so add/subtract round-trip precisely. */
    static Arbitrary<Double> exactDoubles() {
        return Arbitraries.integers().between(-1_000_000, 1_000_000).map(i -> i / 4.0);
    }

    static Arbitrary<Vector3> vector3s() {
        return Combinators.combine(exactDoubles(), exactDoubles(), exactDoubles()).as(Vector3::at);
    }

    static Arbitrary<Vector3> nonZeroVector3s() {
        return vector3s().filter(v -> v.lengthSq() > 0);
    }

    static Arbitrary<Vector2> vector2s() {
        return Combinators.combine(exactDoubles(), exactDoubles()).as(Vector2::at);
    }

    static Arbitrary<Vector2> nonZeroVector2s() {
        return vector2s().filter(v -> v.lengthSq() > 0);
    }

    static Arbitrary<BlockVector2> blockVector2s() {
        return Combinators.combine(
            Arbitraries.integers().between(-1_000_000, 1_000_000),
            Arbitraries.integers().between(-1_000_000, 1_000_000)
        ).as(BlockVector2::at);
    }

    private VectorTestSupport() {
    }

}
