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

package com.sk89q.worldedit.math.transform;

import com.sk89q.worldedit.math.Vector3;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Shared jqwik generators for the transform tests.
 */
final class TransformTestSupport {

    static final double EPSILON = 1e-6;

    /** Points with coordinates in a typical world-sized range. */
    static Arbitrary<Vector3> points() {
        Arbitrary<Double> coord = Arbitraries.doubles().between(-30_000, 30_000);
        return Combinators.combine(coord, coord, coord).as(Vector3::at);
    }

    /** Non-degenerate scale factors, including negative ones (flips). */
    static Arbitrary<Double> scaleFactors() {
        return Arbitraries.doubles().between(0.25, 8).flatMap(d ->
            Arbitraries.of(d, -d));
    }

    static Arbitrary<Vector3> scales() {
        return Combinators.combine(scaleFactors(), scaleFactors(), scaleFactors()).as(Vector3::at);
    }

    static Arbitrary<Vector3> offsets() {
        Arbitrary<Double> coord = Arbitraries.doubles().between(-1_000, 1_000);
        return Combinators.combine(coord, coord, coord).as(Vector3::at);
    }

    /** Invertible transforms built from translations, rotations and non-zero scales. */
    static Arbitrary<AffineTransform> invertibleTransforms() {
        Arbitrary<Double> angle = Arbitraries.doubles().between(-720, 720);
        return Combinators.combine(offsets(), scales(), angle, angle, angle).as((offset, scale, rx, ry, rz) ->
            new AffineTransform()
                .translate(offset)
                .rotateX(rx)
                .rotateY(ry)
                .rotateZ(rz)
                .scale(scale));
    }

    static void assertClose(Vector3 expected, Vector3 actual, double tolerance) {
        double scale = Math.max(1, Math.max(expected.length(), actual.length()));
        assertTrue(
            expected.distance(actual) <= tolerance * scale,
            () -> "expected " + expected + " but was " + actual
        );
    }

    private TransformTestSupport() {
    }

}
