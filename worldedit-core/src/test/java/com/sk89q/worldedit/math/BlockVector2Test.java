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

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockVector2Test {

    @Provide
    Arbitrary<BlockVector2> vectors() {
        return VectorTestSupport.blockVector2s();
    }

    @Property
    @Label("is unchanged by adding then subtracting another vector")
    void addThenSubtract(@ForAll("vectors") BlockVector2 a, @ForAll("vectors") BlockVector2 b) {
        assertEquals(a, a.add(b).subtract(b));
        assertEquals(a, a.subtract(b).add(b));
    }

    @Property
    @Label("wraps around on int overflow when adding then subtracting")
    void addSubtractOverflow(@ForAll int x, @ForAll int z, @ForAll int dx, @ForAll int dz) {
        BlockVector2 a = BlockVector2.at(x, z);
        BlockVector2 b = BlockVector2.at(dx, dz);
        assertEquals(a, a.add(b).subtract(b));
    }

    @Property
    @Label("has consistent equals, hashCode and grid ordering")
    void equalsHashCodeCompare(@ForAll("vectors") BlockVector2 a, @ForAll("vectors") BlockVector2 b) {
        BlockVector2 copy = BlockVector2.at(a.x(), a.z());
        assertEquals(a, copy);
        assertEquals(a.hashCode(), copy.hashCode());
        Comparator<BlockVector2> cmp = BlockVector2.COMPARING_GRID_ARRANGEMENT;
        assertEquals(0, cmp.compare(a, copy));
        assertEquals(a.equals(b), cmp.compare(a, b) == 0);
        assertEquals(Integer.signum(cmp.compare(a, b)), -Integer.signum(cmp.compare(b, a)));
    }

    @Property
    @Label("converts to Vector2, Vector3 and BlockVector3 and back")
    void conversions(@ForAll("vectors") BlockVector2 v, @ForAll int y) {
        assertEquals(v, v.toVector2().toBlockPoint());
        assertEquals(v, v.toVector3().toVector2().toBlockPoint());
        assertEquals(v, v.toBlockVector3(y).toBlockVector2());
        assertEquals(y, v.toBlockVector3(y).y());
    }

    @Property
    @Label("has floor, ceil, round and abs consistent with its integer components")
    void integerRounding(@ForAll("vectors") BlockVector2 v) {
        assertEquals(v, v.floor());
        assertEquals(v, v.ceil());
        assertEquals(v, v.round());
        assertEquals(BlockVector2.at(Math.abs(v.x()), Math.abs(v.z())), v.abs());
    }

    @Property
    @Label("has lengthSq equal to dot with itself")
    void lengthConsistency(@ForAll("vectors") BlockVector2 v) {
        // lengthSq is an int, so it wraps for coordinates beyond about +-46340
        assertEquals(v.x() * v.x() + v.z() * v.z(), v.lengthSq());
        assertEquals(v.lengthSq(), v.dot(v));
    }

    @Property
    @Label("has min/max that bound both operands")
    void minMax(@ForAll("vectors") BlockVector2 a, @ForAll("vectors") BlockVector2 b) {
        BlockVector2 min = a.getMinimum(b);
        BlockVector2 max = a.getMaximum(b);
        assertTrue(a.containedWithin(min, max));
        assertTrue(b.containedWithin(min, max));
    }

    @Test
    @DisplayName("sorts in rows of Z then X")
    void gridOrder() {
        Comparator<BlockVector2> cmp = BlockVector2.COMPARING_GRID_ARRANGEMENT;
        assertTrue(cmp.compare(BlockVector2.at(9, 0), BlockVector2.at(0, 1)) < 0);
        assertTrue(cmp.compare(BlockVector2.at(0, 1), BlockVector2.at(1, 1)) < 0);
        assertTrue(cmp.compare(BlockVector2.at(-5, -1), BlockVector2.at(5, -1)) < 0);
    }

    @Test
    @DisplayName("floors doubles when created from them")
    void atDoublesFloors() {
        assertEquals(BlockVector2.at(-1, -1), BlockVector2.at(-0.5, -0.5));
        assertEquals(BlockVector2.at(0, 1), BlockVector2.at(0.5, 1.5));
        assertEquals(BlockVector2.at(-2, 3), BlockVector2.at(-1.0000001, 3.9999));
    }

    @Test
    @DisplayName("returns the shared constants for zero and one")
    void constantsAreCanonical() {
        assertSame(BlockVector2.ZERO, BlockVector2.at(0, 0));
        assertSame(BlockVector2.ONE, BlockVector2.at(1, 1));
    }

}
