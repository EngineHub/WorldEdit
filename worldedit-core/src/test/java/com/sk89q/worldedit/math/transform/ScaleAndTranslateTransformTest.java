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
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.sk89q.worldedit.math.transform.TransformTestSupport.EPSILON;
import static com.sk89q.worldedit.math.transform.TransformTestSupport.assertClose;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScaleAndTranslateTransformTest {

    @Provide
    Arbitrary<Vector3> points() {
        return TransformTestSupport.points();
    }

    @Provide
    Arbitrary<Vector3> scales() {
        return TransformTestSupport.scales();
    }

    @Provide
    Arbitrary<Vector3> offsets() {
        return TransformTestSupport.offsets();
    }

    @Property
    @Label("equals the equivalent affine transform")
    void equivalentToAffine(
        @ForAll("offsets") Vector3 offset,
        @ForAll("scales") Vector3 scale,
        @ForAll("points") Vector3 p
    ) {
        Transform simple = new ScaleAndTranslateTransform(offset, scale);
        AffineTransform affine = new AffineTransform().translate(offset).scale(scale);
        assertClose(affine.apply(p), simple.apply(p), EPSILON);
    }

    @Property
    @Label("has an inverse that undoes it")
    void inverseRoundTrip(
        @ForAll("offsets") Vector3 offset,
        @ForAll("scales") Vector3 scale,
        @ForAll("points") Vector3 p
    ) {
        Transform simple = new ScaleAndTranslateTransform(offset, scale);
        assertClose(p, simple.inverse().apply(simple.apply(p)), EPSILON);
        assertClose(p, simple.apply(simple.inverse().apply(p)), EPSILON);
        assertClose(p, simple.combine(simple.inverse()).apply(p), EPSILON);
        assertClose(p, simple.inverse().combine(simple).apply(p), EPSILON);
        assertSame(simple, simple.inverse().inverse());
    }

    @Property
    @Label("applies the receiver first when combined")
    void combineOrder(
        @ForAll("offsets") Vector3 offset,
        @ForAll("scales") Vector3 scale,
        @ForAll("offsets") Vector3 offset2,
        @ForAll("scales") Vector3 scale2,
        @ForAll("points") Vector3 p
    ) {
        Transform a = new ScaleAndTranslateTransform(offset, scale);
        Transform b = new ScaleAndTranslateTransform(offset2, scale2);
        assertClose(b.apply(a.apply(p)), a.combine(b).apply(p), EPSILON);
    }

    @Test
    @DisplayName("is the identity only for zero offset and unit scale")
    void isIdentity() {
        assertTrue(new ScaleAndTranslateTransform(Vector3.ZERO, Vector3.ONE).isIdentity());
        assertTrue(new ScaleAndTranslateTransform(Vector3.ZERO, Vector3.ONE).inverse().isIdentity());
        assertFalse(new ScaleAndTranslateTransform(Vector3.UNIT_X, Vector3.ONE).isIdentity());
        assertFalse(new ScaleAndTranslateTransform(Vector3.ZERO, Vector3.at(1, -1, 1)).isIdentity());
        assertFalse(new ScaleAndTranslateTransform(Vector3.ZERO, Vector3.at(1, 1, 1.5)).inverse().isIdentity());
    }

    @Test
    @DisplayName("scales before translating")
    void scaleThenTranslate() {
        Transform t = new ScaleAndTranslateTransform(Vector3.at(10, 20, 30), Vector3.at(2, 3, 4));
        assertEquals(Vector3.at(12, 23, 34), t.apply(Vector3.ONE));
        assertEquals(Vector3.at(10, 20, 30), t.apply(Vector3.ZERO));
    }

}
