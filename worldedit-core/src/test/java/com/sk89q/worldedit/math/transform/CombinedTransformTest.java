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

import java.util.List;

import static com.sk89q.worldedit.math.transform.TransformTestSupport.EPSILON;
import static com.sk89q.worldedit.math.transform.TransformTestSupport.assertClose;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombinedTransformTest {

    @Provide
    Arbitrary<Vector3> points() {
        return TransformTestSupport.points();
    }

    @Provide
    Arbitrary<AffineTransform> transforms() {
        return TransformTestSupport.invertibleTransforms();
    }

    @Property
    @Label("applies its members in order")
    void appliesInOrder(
        @ForAll("transforms") AffineTransform a,
        @ForAll("transforms") AffineTransform b,
        @ForAll("transforms") AffineTransform c,
        @ForAll("points") Vector3 p
    ) {
        Transform combined = new CombinedTransform(a, b, c);
        assertClose(c.apply(b.apply(a.apply(p))), combined.apply(p), EPSILON);
        assertClose(combined.apply(p), new CombinedTransform(List.of(a, b, c)).apply(p), EPSILON);
    }

    @Property
    @Label("is undone by its inverse")
    void inverseRoundTrip(
        @ForAll("transforms") AffineTransform a,
        @ForAll("transforms") AffineTransform b,
        @ForAll("points") Vector3 p
    ) {
        Transform combined = new CombinedTransform(a, b, new ScaleAndTranslateTransform(Vector3.ONE, Vector3.at(2, -3, 4)));
        assertClose(p, combined.inverse().apply(combined.apply(p)), EPSILON);
    }

    @Property
    @Label("flattens when combined with another combined transform")
    void combineFlattens(
        @ForAll("transforms") AffineTransform a,
        @ForAll("transforms") AffineTransform b,
        @ForAll("transforms") AffineTransform c,
        @ForAll("points") Vector3 p
    ) {
        Transform left = new CombinedTransform(a, b);
        Transform right = new CombinedTransform(c);
        assertClose(c.apply(b.apply(a.apply(p))), left.combine(right).apply(p), EPSILON);
        assertClose(c.apply(b.apply(a.apply(p))), left.combine(c).apply(p), EPSILON);
    }

    @Test
    @DisplayName("is the identity when empty or when all members are")
    void isIdentity() {
        assertTrue(new CombinedTransform().isIdentity());
        assertTrue(new CombinedTransform(new Identity(), new AffineTransform()).isIdentity());
        assertFalse(new CombinedTransform(new Identity(), new AffineTransform().translate(0, 1, 0)).isIdentity());
        assertEquals(Vector3.at(1, 2, 3), new CombinedTransform().apply(Vector3.at(1, 2, 3)));
    }

    @Test
    @DisplayName("copies the member array so later mutation has no effect")
    void defensiveCopy() {
        Transform[] members = {new AffineTransform().translate(1, 0, 0)};
        Transform combined = new CombinedTransform(members);
        members[0] = new Identity();
        assertEquals(Vector3.UNIT_X, combined.apply(Vector3.ZERO));
    }

    @Test
    @DisplayName("is combined predictably with the identity transform")
    void identityTransform() {
        Identity id = new Identity();
        AffineTransform t = new AffineTransform().rotateY(90);
        assertSame(t, id.combine(t));
        assertSame(id, id.combine(new Identity()));
        assertSame(id, id.inverse());
        assertEquals(Vector3.at(5, 6, 7), id.apply(Vector3.at(5, 6, 7)));
    }

}
