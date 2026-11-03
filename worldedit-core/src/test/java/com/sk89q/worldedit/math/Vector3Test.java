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

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Vector3Test {

    @Provide
    Arbitrary<Vector3> vectors() {
        return VectorTestSupport.vector3s();
    }

    @Provide
    Arbitrary<Vector3> nonZeroVectors() {
        return VectorTestSupport.nonZeroVector3s();
    }

    @Property
    @Label("is unchanged by adding then subtracting another vector")
    void addThenSubtract(@ForAll("vectors") Vector3 a, @ForAll("vectors") Vector3 b) {
        assertEquals(a, a.add(b).subtract(b));
        assertEquals(a, a.subtract(b).add(b));
    }

    @Property
    @Label("addition is commutative and has ZERO as identity")
    void additionLaws(@ForAll("vectors") Vector3 a, @ForAll("vectors") Vector3 b) {
        assertEquals(a.add(b), b.add(a));
        assertEquals(a, a.add(Vector3.ZERO));
    }

    @Property
    @Label("normalizes to length 1")
    void normalizeHasUnitLength(@ForAll("nonZeroVectors") Vector3 v) {
        assertEquals(1.0, v.normalize().length(), 1e-12);
    }

    @Property
    @Label("normalizes to the same direction")
    void normalizeKeepsDirection(@ForAll("nonZeroVectors") Vector3 v) {
        Vector3 n = v.normalize();
        assertEquals(v.length(), v.dot(n), 1e-6 * Math.max(1, v.length()));
    }

    @Property
    @Label("has lengthSq equal to length squared and dot with itself")
    void lengthConsistency(@ForAll("vectors") Vector3 v) {
        assertEquals(v.lengthSq(), v.dot(v), 1e-9 * Math.max(1, v.lengthSq()));
        assertEquals(v.lengthSq(), v.length() * v.length(), 1e-6 * Math.max(1, v.lengthSq()));
    }

    @Property
    @Label("has a cross product orthogonal to both operands")
    void crossIsOrthogonal(@ForAll("vectors") Vector3 a, @ForAll("vectors") Vector3 b) {
        Vector3 c = a.cross(b);
        assertEquals(0, c.dot(a), 1e-3 * Math.max(1, c.length() * a.length()));
        assertEquals(0, c.dot(b), 1e-3 * Math.max(1, c.length() * b.length()));
    }

    @Property
    @Label("converts to a block point at the floor of each component")
    void toBlockPointIsFloor(@ForAll("vectors") Vector3 v) {
        BlockVector3 block = v.toBlockPoint();
        assertEquals((int) Math.floor(v.x()), block.x());
        assertEquals((int) Math.floor(v.y()), block.y());
        assertEquals((int) Math.floor(v.z()), block.z());
        assertEquals(v.floor(), block.toVector3());
    }

    @Property
    @Label("has floor <= value <= ceil, and round within 0.5")
    void floorCeilRound(@ForAll("vectors") Vector3 v) {
        assertTrue(v.containedWithin(v.floor(), v.ceil()));
        assertTrue(v.ceil().subtract(v.floor()).containedWithin(Vector3.ZERO, Vector3.ONE));
        Vector3 diff = v.round().subtract(v).abs();
        assertTrue(diff.containedWithin(Vector3.ZERO, Vector3.at(0.5, 0.5, 0.5)));
        assertEquals(v.floor(), v.floor().floor());
    }

    @Property
    @Label("has min/max that bound both operands")
    void minMax(@ForAll("vectors") Vector3 a, @ForAll("vectors") Vector3 b) {
        Vector3 min = a.getMinimum(b);
        Vector3 max = a.getMaximum(b);
        assertTrue(a.containedWithin(min, max));
        assertTrue(b.containedWithin(min, max));
        assertEquals(min, b.getMinimum(a));
    }

    @Property
    @Label("has consistent equals, hashCode and YZX ordering")
    void equalsHashCodeCompare(@ForAll("vectors") Vector3 a, @ForAll("vectors") Vector3 b) {
        Vector3 copy = Vector3.at(a.x(), a.y(), a.z());
        assertEquals(a, copy);
        assertEquals(a.hashCode(), copy.hashCode());
        Comparator<Vector3> cmp = Vector3.sortByCoordsYzx();
        assertEquals(0, cmp.compare(a, copy));
        assertEquals(a.equals(b), cmp.compare(a, b) == 0);
        assertEquals(Integer.signum(cmp.compare(a, b)), -Integer.signum(cmp.compare(b, a)));
    }

    @Test
    @DisplayName("sorts by Y, then Z, then X")
    void yzxOrder() {
        Comparator<Vector3> cmp = Vector3.sortByCoordsYzx();
        assertTrue(cmp.compare(Vector3.at(9, 0, 9), Vector3.at(0, 1, 0)) < 0);
        assertTrue(cmp.compare(Vector3.at(9, 1, 0), Vector3.at(0, 1, 1)) < 0);
        assertTrue(cmp.compare(Vector3.at(0, 1, 1), Vector3.at(1, 1, 1)) < 0);
    }

    @ParameterizedTest(name = "({0}, {1}, {2}) -> block ({3}, {4}, {5})")
    @DisplayName("converts to a block point flooring negatives and x.5")
    @CsvSource({
        "0, 0, 0, 0, 0, 0",
        "0.5, 0.5, 0.5, 0, 0, 0",
        "-0.5, -0.5, -0.5, -1, -1, -1",
        "-0.0, -0.0, -0.0, 0, 0, 0",
        "1.5, -1.5, 2.5, 1, -2, 2",
        "-1, -64, -1.0000001, -1, -64, -2",
        "0.9999999, 255.99, -64.01, 0, 255, -65"
    })
    void toBlockPointExamples(double x, double y, double z, int bx, int by, int bz) {
        assertEquals(BlockVector3.at(bx, by, bz), Vector3.at(x, y, z).toBlockPoint());
    }

    @ParameterizedTest(name = "round({0}) = {1}")
    @DisplayName("rounds x.5 up toward positive infinity")
    @CsvSource({"0.5, 1", "1.5, 2", "-0.5, 0", "-1.5, -1", "-2.5, -2", "0.49, 0", "-0.51, -1"})
    void roundHalves(double in, double out) {
        assertEquals(Vector3.at(out, out, out), Vector3.at(in, in, in).round());
    }

    @Test
    @DisplayName("normalizes the zero vector to NaN components")
    void normalizeZero() {
        Vector3 n = Vector3.ZERO.normalize();
        assertTrue(Double.isNaN(n.x()) && Double.isNaN(n.y()) && Double.isNaN(n.z()));
    }

    @Test
    @DisplayName("treats NaN as equal to itself in equals and hashCode, but not in containedWithin")
    void nanBehaviour() {
        Vector3 nan = Vector3.at(Double.NaN, 0, 0);
        assertEquals(nan, Vector3.at(Double.NaN, 0, 0));
        assertEquals(nan.hashCode(), Vector3.at(Double.NaN, 0, 0).hashCode());
        assertNotEquals(nan, Vector3.ZERO);
        assertFalse(nan.containedWithin(Vector3.at(-1, -1, -1), Vector3.ONE));
    }

    @Test
    @DisplayName("returns the shared constants for zero and one")
    void constantsAreCanonical() {
        assertSame(Vector3.ZERO, Vector3.at(0, 0, 0));
        assertSame(Vector3.ONE, Vector3.at(1, 1, 1));
    }

    @Test
    @DisplayName("clamps Y only")
    void clampY() {
        assertEquals(Vector3.at(5, 10, -3), Vector3.at(5, 99, -3).clampY(-64, 10));
        assertEquals(Vector3.at(5, -64, -3), Vector3.at(5, -99, -3).clampY(-64, 10));
        assertEquals(Vector3.at(5, 3, -3), Vector3.at(5, 3, -3).clampY(-64, 10));
    }

}
