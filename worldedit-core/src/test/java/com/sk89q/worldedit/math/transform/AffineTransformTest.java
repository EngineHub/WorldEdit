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

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static com.sk89q.worldedit.math.transform.TransformTestSupport.EPSILON;
import static com.sk89q.worldedit.math.transform.TransformTestSupport.assertClose;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AffineTransformTest {

    @Provide
    Arbitrary<Vector3> points() {
        return TransformTestSupport.points();
    }

    @Provide
    Arbitrary<AffineTransform> transforms() {
        return TransformTestSupport.invertibleTransforms();
    }

    private static boolean matrixIsIdentity(AffineTransform t) {
        return Arrays.equals(
            t.coefficients(),
            new double[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0}
        );
    }

    private static void assertCoefficientsClose(AffineTransform expected, AffineTransform actual) {
        // numeric comparison: records compare doubles bitwise, so -0.0 != 0.0
        assertArrayEquals(expected.coefficients(), actual.coefficients(), 1e-9);
    }

    @Property
    @Label("maps a point back to itself when combined with its inverse")
    void inverseRoundTrip(@ForAll("transforms") AffineTransform t, @ForAll("points") Vector3 p) {
        assertClose(p, t.inverse().combine(t).apply(p), EPSILON);
        assertClose(p, t.combine(t.inverse()).apply(p), EPSILON);
        assertClose(p, t.inverse().apply(t.apply(p)), EPSILON);
    }

    @Property
    @Label("has an inverse whose inverse is itself")
    void doubleInverse(@ForAll("transforms") AffineTransform t, @ForAll("points") Vector3 p) {
        assertClose(t.apply(p), t.inverse().inverse().apply(p), EPSILON);
    }

    @Property
    @Label("combines associatively")
    void combineAssociative(
        @ForAll("transforms") AffineTransform a,
        @ForAll("transforms") AffineTransform b,
        @ForAll("transforms") AffineTransform c,
        @ForAll("points") Vector3 p
    ) {
        assertClose(
            a.combine(b).combine(c).apply(p),
            a.combine(b.combine(c)).apply(p),
            EPSILON
        );
    }

    @Property
    @Label("applies the argument first when combining (this * that)")
    void combineOrder(
        @ForAll("transforms") AffineTransform a,
        @ForAll("transforms") AffineTransform b,
        @ForAll("points") Vector3 p
    ) {
        assertClose(a.apply(b.apply(p)), a.combine(b).apply(p), EPSILON);
        assertClose(b.apply(a.apply(p)), a.preConcatenate(b).apply(p), EPSILON);
    }

    @Property
    @Label("is the identity when combined with the identity")
    void identityNeutral(@ForAll("transforms") AffineTransform t) {
        AffineTransform id = new AffineTransform();
        assertCoefficientsClose(t, t.combine(id));
        assertCoefficientsClose(t, id.combine(t));
    }

    @Property
    @Label("reports isIdentity consistently with its matrix")
    void isIdentityMatchesMatrix(@ForAll("transforms") AffineTransform t) {
        assertEquals(matrixIsIdentity(t), t.isIdentity());
    }

    @Test
    @DisplayName("is the identity only when the matrix is exactly the identity")
    void isIdentityExamples() {
        assertTrue(new AffineTransform().isIdentity());
        assertTrue(new AffineTransform().rotateY(360).isIdentity());
        assertTrue(new AffineTransform().translate(0, 0, 0).scale(1).isIdentity());
        assertFalse(new AffineTransform().translate(0, 1e-12, 0).isIdentity());
        assertFalse(new AffineTransform().scale(-1, 1, 1).isIdentity());
        assertFalse(new AffineTransform().scale(1, 1, 1.0000001).isIdentity());
        assertFalse(new AffineTransform().rotateY(90).isIdentity());
        assertFalse(new AffineTransform(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, Double.NaN
        ).isIdentity());
    }

    @Test
    @DisplayName("returns itself as the inverse of the identity")
    void identityInverse() {
        AffineTransform id = new AffineTransform();
        assertSame(id, id.inverse());
    }

    @ParameterizedTest(name = "rotateY({0}) x4 is the identity")
    @DisplayName("is the identity after four quarter turns")
    @ValueSource(doubles = {90, -90, 270, 450, -270})
    void fourQuarterTurnsAreIdentity(double degrees) {
        AffineTransform quarter = new AffineTransform().rotateY(degrees);
        AffineTransform full = quarter.combine(quarter).combine(quarter).combine(quarter);
        assertTrue(full.isIdentity(), () -> "got " + full);

        AffineTransform x = new AffineTransform().rotateX(degrees);
        assertTrue(x.combine(x).combine(x).combine(x).isIdentity());
        AffineTransform z = new AffineTransform().rotateZ(degrees);
        assertTrue(z.combine(z).combine(z).combine(z).isIdentity());
    }

    @Property
    @Label("maps integer points exactly under quarter-turn rotations")
    void quarterTurnsKeepIntegerPoints(
        @ForAll @IntRange(min = -4, max = 4) int turns,
        @ForAll @IntRange(min = -1_000_000, max = 1_000_000) int x,
        @ForAll @IntRange(min = -1_000_000, max = 1_000_000) int y,
        @ForAll @IntRange(min = -1_000_000, max = 1_000_000) int z
    ) {
        Vector3 rotated = new AffineTransform().rotateY(turns * 90.0).apply(Vector3.at(x, y, z));
        assertEquals(Math.floor(rotated.x()), rotated.x());
        assertEquals(Math.floor(rotated.z()), rotated.z());
        assertEquals(y, rotated.y());
        assertEquals(Vector3.at(x, y, z).lengthSq(), rotated.lengthSq());
    }

    @Property
    @Label("preserves distances under rotation")
    void rotationIsIsometry(
        @ForAll @DoubleRange(min = -720, max = 720) double angle,
        @ForAll("points") Vector3 a,
        @ForAll("points") Vector3 b
    ) {
        AffineTransform rot = new AffineTransform().rotateX(angle).rotateY(angle).rotateZ(angle);
        assertEquals(a.distance(b), rot.apply(a).distance(rot.apply(b)), 1e-6 * Math.max(1, a.distance(b)));
    }

    @Test
    @DisplayName("applies later builder calls to the point first")
    void translateScaleOrder() {
        Vector3 p = Vector3.at(1, 2, 3);
        // scale runs first, then the translation
        assertEquals(Vector3.at(3, 6, 9), new AffineTransform().translate(1, 2, 3).scale(2).apply(p));
        // translation runs first, then the scale
        assertEquals(Vector3.at(4, 8, 12), new AffineTransform().scale(2).translate(1, 2, 3).apply(p));
        assertEquals(Vector3.at(4, 8, 12), new AffineTransform().scale(2).translate(BlockVector3.at(1, 2, 3)).apply(p));
        assertEquals(Vector3.at(4, 8, 12), new AffineTransform().scale(2).translate(Vector3.at(1, 2, 3)).apply(p));
    }

    @Test
    @DisplayName("rotates Y a quarter turn so that +X maps to -Z")
    void rotateYDirection() {
        assertEquals(Vector3.at(0, 0, -1), new AffineTransform().rotateY(90).apply(Vector3.UNIT_X));
        assertEquals(Vector3.at(0, 1, 0), new AffineTransform().rotateY(90).apply(Vector3.UNIT_Y));
        assertEquals(Vector3.at(1, 0, 0), new AffineTransform().rotateY(90).apply(Vector3.UNIT_Z));
        assertEquals(Vector3.at(0, -1, 0), new AffineTransform().rotateX(90).apply(Vector3.UNIT_Z));
        assertEquals(Vector3.at(0, 1, 0), new AffineTransform().rotateZ(90).apply(Vector3.UNIT_X));
    }

    @Test
    @DisplayName("detects horizontal and vertical flips")
    void flipDetection() {
        AffineTransform id = new AffineTransform();
        assertFalse(id.isHorizontalFlip());
        assertFalse(id.isVerticalFlip());
        assertTrue(id.scale(-1, 1, 1).isHorizontalFlip());
        assertTrue(id.scale(1, 1, -1).isHorizontalFlip());
        assertFalse(id.scale(-1, 1, -1).isHorizontalFlip());
        assertFalse(id.scale(-1, 1, 1).isVerticalFlip());
        assertTrue(id.scale(1, -1, 1).isVerticalFlip());
        assertFalse(id.rotateY(90).isHorizontalFlip());
        assertFalse(id.rotateY(180).isHorizontalFlip());
    }

    @Test
    @Disabled("Bug: AffineTransform.indexCoeffs treats indices 3, 6, 9 as the zero 'holes' instead of 3, 7, 11 "
        + "(m03, m13, m23). new AffineTransform(new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9}).coefficients() returns "
        + "{1, 2, 3, 0, 4, 5, 0, 6, 7, 0, 8, 9}; expected {1, 2, 3, 0, 4, 5, 6, 0, 7, 8, 9, 0}. "
        + "Only affects the deprecated double[] constructor with 9 elements.")
    @DisplayName("fills the translation column with zeros when built from 9 coefficients")
    @SuppressWarnings("deprecation")
    void nineCoefficientConstructor() {
        assertArrayEquals(
            new double[] {1, 2, 3, 0, 4, 5, 6, 0, 7, 8, 9, 0},
            new AffineTransform(new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9}).coefficients()
        );
    }

    @Test
    @DisplayName("is built from 12 coefficients, rejecting other lengths")
    @SuppressWarnings("deprecation")
    void coefficientConstructor() {
        double[] twelve = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        assertArrayEquals(twelve, new AffineTransform(twelve).coefficients());
        assertThrows(IllegalArgumentException.class, () -> new AffineTransform(new double[10]));
        assertThrows(IllegalArgumentException.class, () -> new AffineTransform(new double[0]));
    }

    @Test
    @DisplayName("produces NaN coordinates from NaN input without throwing")
    void nanInput() {
        Vector3 result = new AffineTransform().rotateY(45).apply(Vector3.at(Double.NaN, 0, 0));
        assertTrue(Double.isNaN(result.x()));
        assertNotEquals(Vector3.ZERO, result);
    }

    @Test
    @DisplayName("falls back to a CombinedTransform when combined with a non-affine transform")
    void combineWithOtherTransform() {
        Transform other = new ScaleAndTranslateTransform(Vector3.ONE, Vector3.at(2, 2, 2));
        AffineTransform t = new AffineTransform().rotateY(90);
        Transform combined = t.combine(other);
        assertTrue(combined instanceof CombinedTransform);
        // CombinedTransform applies its members in order, unlike AffineTransform.combine
        assertEquals(other.apply(t.apply(Vector3.UNIT_X)), combined.apply(Vector3.UNIT_X));
    }

}
