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

import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Vector2Test {

    @Provide
    Arbitrary<Vector2> vectors() {
        return VectorTestSupport.vector2s();
    }

    @Provide
    Arbitrary<Vector2> nonZeroVectors() {
        return VectorTestSupport.nonZeroVector2s();
    }

    @Property
    @Label("is unchanged by adding then subtracting another vector")
    void addThenSubtract(@ForAll("vectors") Vector2 a, @ForAll("vectors") Vector2 b) {
        assertEquals(a, a.add(b).subtract(b));
        assertEquals(a, a.subtract(b).add(b));
    }

    @Property
    @Label("normalizes to length 1")
    void normalizeHasUnitLength(@ForAll("nonZeroVectors") Vector2 v) {
        assertEquals(1.0, v.normalize().length(), 1e-12);
    }

    @Property
    @Label("converts to a block point at the floor of each component")
    void toBlockPointIsFloor(@ForAll("vectors") Vector2 v) {
        BlockVector2 block = v.toBlockPoint();
        assertEquals((int) Math.floor(v.x()), block.x());
        assertEquals((int) Math.floor(v.z()), block.z());
        assertEquals(v.floor(), block.toVector2());
    }

    @Property
    @Label("has consistent equals and hashCode")
    void equalsHashCode(@ForAll("vectors") Vector2 a) {
        Vector2 copy = Vector2.at(a.x(), a.z());
        assertEquals(a, copy);
        assertEquals(a.hashCode(), copy.hashCode());
    }

    @Property
    @Label("survives a round-trip through Vector3")
    void vector3RoundTrip(@ForAll("vectors") Vector2 v, @ForAll("vectors") Vector2 y) {
        assertEquals(v, v.toVector3().toVector2());
        assertEquals(v, v.toVector3(y.x()).toVector2());
        assertEquals(y.x(), v.toVector3(y.x()).y());
    }

    @Property
    @Label("keeps its length when rotated, and returns to itself after 360 degrees")
    void transform2DRotation(@ForAll("vectors") Vector2 v) {
        Vector2 rotated = v.transform2D(37, 0, 0, 0, 0);
        assertEquals(v.length(), rotated.length(), 1e-6 * Math.max(1, v.length()));
        Vector2 full = v.transform2D(360, 0, 0, 0, 0);
        assertEquals(v.x(), full.x(), 1e-6 * Math.max(1, v.length()));
        assertEquals(v.z(), full.z(), 1e-6 * Math.max(1, v.length()));
    }

    @ParameterizedTest(name = "({0}, {1}) -> block ({2}, {3})")
    @DisplayName("converts to a block point flooring negatives and x.5")
    @CsvSource({"0.5, 0.5, 0, 0", "-0.5, -0.5, -1, -1", "1.5, -1.5, 1, -2", "-0.0, 0.0, 0, 0"})
    void toBlockPointExamples(double x, double z, int bx, int bz) {
        assertEquals(BlockVector2.at(bx, bz), Vector2.at(x, z).toBlockPoint());
    }

    @ParameterizedTest(name = "round({0}) = {1}")
    @DisplayName("rounds x.5 up toward positive infinity")
    @CsvSource({"0.5, 1", "1.5, 2", "-0.5, 0", "-1.5, -1", "0.49, 0", "-0.51, -1"})
    void roundHalves(double in, double out) {
        assertEquals(Vector2.at(out, out), Vector2.at(in, in).round());
    }

    @Test
    @DisplayName("rotates a quarter turn about a point")
    void transform2DQuarterTurn() {
        Vector2 r = Vector2.at(3, 1).transform2D(90, 1, 1, 0, 0);
        assertEquals(1, r.x(), 1e-9);
        assertEquals(3, r.z(), 1e-9);
    }

    @Test
    @DisplayName("normalizes the zero vector to NaN components")
    void normalizeZero() {
        Vector2 n = Vector2.ZERO.normalize();
        assertTrue(Double.isNaN(n.x()) && Double.isNaN(n.z()));
    }

}
