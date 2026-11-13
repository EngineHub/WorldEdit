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

package com.sk89q.worldedit.math.interpolation;

import com.sk89q.worldedit.math.Vector3;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterpolationTest {

    private static final double EPSILON = 1e-9;

    private static List<Node> randomNodes(Random random, int count, boolean varyKochanekBartels) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // negative Y is deliberate
            Node node = new Node(Vector3.at(
                random.nextDouble() * 40 - 20, random.nextDouble() * 40 - 30, random.nextDouble() * 40 - 20));
            if (varyKochanekBartels) {
                node.setTension(random.nextDouble() * 2 - 1);
                node.setBias(random.nextDouble() * 2 - 1);
                node.setContinuity(random.nextDouble() * 2 - 1);
            }
            nodes.add(node);
        }
        return nodes;
    }

    private static void assertVectorEquals(Vector3 expected, Vector3 actual, double delta, String message) {
        assertNotNull(actual, message);
        assertEquals(expected.x(), actual.x(), delta, message + " (x)");
        assertEquals(expected.y(), actual.y(), delta, message + " (y)");
        assertEquals(expected.z(), actual.z(), delta, message + " (z)");
    }

    private static void assertFinite(Vector3 vector, String message) {
        assertNotNull(vector, message);
        assertTrue(Double.isFinite(vector.x()) && Double.isFinite(vector.y()) && Double.isFinite(vector.z()),
            message + ": " + vector);
    }

    private static Vector3 expectedLerp(List<Node> nodes, double t) {
        double scaled = t * (nodes.size() - 1);
        int index = Math.min((int) Math.floor(scaled), nodes.size() - 2);
        double fraction = scaled - index;
        Vector3 a = nodes.get(index).getPosition();
        Vector3 b = nodes.get(index + 1).getPosition();
        return Vector3.at(
            a.x() + (b.x() - a.x()) * fraction,
            a.y() + (b.y() - a.y()) * fraction,
            a.z() + (b.z() - a.z()) * fraction
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 4, 5, 6, 7, 8})
    void kochanekBartelsPassesThroughEveryNode(long seed) {
        Random random = new Random(seed);
        int count = 2 + random.nextInt(8);
        List<Node> nodes = randomNodes(random, count, seed % 2 == 0);
        KochanekBartelsInterpolation interpolation = new KochanekBartelsInterpolation();
        interpolation.setNodes(nodes);

        for (int i = 0; i < count; i++) {
            double t = (double) i / (count - 1);
            assertVectorEquals(nodes.get(i).getPosition(), interpolation.getPosition(t), 1e-7, "node " + i);
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 4})
    void kochanekBartelsIsContinuousAndFinite(long seed) {
        Random random = new Random(seed);
        List<Node> nodes = randomNodes(random, 6, true);
        KochanekBartelsInterpolation interpolation = new KochanekBartelsInterpolation();
        interpolation.setNodes(nodes);

        Vector3 previous = interpolation.getPosition(0);
        for (int i = 1; i <= 2000; i++) {
            double t = i / 2000.0;
            Vector3 current = interpolation.getPosition(t);
            assertFinite(current, "position at " + t);
            assertFinite(interpolation.get1stDerivative(t), "derivative at " + t);
            // no jumps: 2000 samples across a curve inside a 40-unit box
            assertTrue(previous.distance(current) < 5, "discontinuity at " + t);
            previous = current;
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 4, 5, 6})
    void linearMatchesLerp(long seed) {
        Random random = new Random(seed);
        List<Node> nodes = randomNodes(random, 2 + random.nextInt(8), false);
        LinearInterpolation interpolation = new LinearInterpolation();
        interpolation.setNodes(nodes);

        for (int i = 0; i < 200; i++) {
            double t = random.nextDouble();
            assertVectorEquals(expectedLerp(nodes, t), interpolation.getPosition(t), EPSILON, "t=" + t);
        }
        for (int i = 0; i < nodes.size() - 1; i++) {
            double t = (double) i / (nodes.size() - 1);
            assertVectorEquals(nodes.get(i).getPosition(), interpolation.getPosition(t), 1e-7, "node " + i);
        }
    }

    @Test
    @Disabled("LinearInterpolation.getPosition(1.0) throws IndexOutOfBoundsException (Index 4 out of bounds for length 4 "
        + "with 4 nodes): position 1.0 maps to index size-1, and the segment lookup then reads nodes.get(index + 1). "
        + "Expected the last node's position. Same for get1stDerivative(1.0).")
    void linearEndsAtTheLastNode() {
        List<Node> nodes = randomNodes(new Random(1), 4, false);
        LinearInterpolation interpolation = new LinearInterpolation();
        interpolation.setNodes(nodes);

        assertVectorEquals(nodes.get(3).getPosition(), interpolation.getPosition(1.0), EPSILON, "end");
    }

    @Test
    void linearDerivativeIsTheSegmentVector() {
        List<Node> nodes = randomNodes(new Random(9), 5, false);
        LinearInterpolation interpolation = new LinearInterpolation();
        interpolation.setNodes(nodes);

        for (int i = 0; i < 4; i++) {
            double t = (i + 0.5) / 4;
            assertVectorEquals(nodes.get(i + 1).getPosition().subtract(nodes.get(i).getPosition()),
                interpolation.get1stDerivative(t), EPSILON, "segment " + i);
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3})
    void linearArcLengthIsTheSumOfSegmentLengths(long seed) {
        Random random = new Random(seed);
        List<Node> nodes = randomNodes(random, 5, false);
        LinearInterpolation interpolation = new LinearInterpolation();
        interpolation.setNodes(nodes);

        double total = 0;
        for (int i = 0; i < 4; i++) {
            total += nodes.get(i).getPosition().distance(nodes.get(i + 1).getPosition());
        }
        // stay just inside the last segment; the end point itself is covered separately
        assertEquals(total, interpolation.arcLength(0, 1.0 - 1e-12), 1e-6);

        double a = random.nextDouble() * 0.5;
        double b = 0.5 + random.nextDouble() * 0.4;
        double c = 0.9 + random.nextDouble() * 0.09;
        assertEquals(interpolation.arcLength(a, c), interpolation.arcLength(a, b) + interpolation.arcLength(b, c), 1e-9);
    }

    @Test
    @Disabled("LinearInterpolation.arcLength(0, 1) throws IndexOutOfBoundsException (Index 4 out of bounds for length 4 "
        + "with 4 nodes): the zero-width trailing piece arcLengthRecursive(size-1, 0, 0) reads nodes.get(size). "
        + "Expected the summed length of all segments.")
    void linearArcLengthToTheEndOfTheCurve() {
        List<Node> nodes = randomNodes(new Random(3), 4, false);
        LinearInterpolation interpolation = new LinearInterpolation();
        interpolation.setNodes(nodes);

        double total = 0;
        for (int i = 0; i < 3; i++) {
            total += nodes.get(i).getPosition().distance(nodes.get(i + 1).getPosition());
        }
        assertEquals(total, interpolation.arcLength(0, 1), 1e-9);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 4, 5, 6})
    void arcLengthIsSymmetricNonNegativeAndMonotone(long seed) {
        Random random = new Random(seed);
        List<Node> nodes = randomNodes(random, 2 + random.nextInt(6), seed % 2 == 0);
        KochanekBartelsInterpolation kb = new KochanekBartelsInterpolation();
        kb.setNodes(nodes);

        double previous = 0;
        for (int i = 0; i <= 100; i++) {
            double t = i / 100.0;
            double length = kb.arcLength(0, t);
            assertTrue(length >= previous - 1e-9, "arc length decreased at " + t);
            assertEquals(length, kb.arcLength(t, 0), EPSILON, "symmetry at " + t);
            previous = length;
        }
        assertEquals(0, kb.arcLength(0.3, 0.3), EPSILON);
    }

    @Test
    @Disabled("KochanekBartelsInterpolation.arcLength underestimates by 1/8: arcLengthRecursive(int, double, double) "
        + "integrates with 8 trapezoid intervals but its loop (i < nPoints - 1) skips the last interior sample, so a "
        + "constant-speed segment yields about 7/8 of its length. Three collinear nodes at x=0,10,20 give 17.64 for "
        + "arcLength(0, 1); expected 20.")
    void kochanekBartelsArcLengthOfAStraightLineIsItsLength() {
        List<Node> nodes = List.of(
            new Node(Vector3.at(0, -5, 0)), new Node(Vector3.at(10, -5, 0)), new Node(Vector3.at(20, -5, 0)));
        KochanekBartelsInterpolation kb = new KochanekBartelsInterpolation();
        kb.setNodes(nodes);

        assertEquals(20, kb.arcLength(0, 1), 1e-6);
        assertEquals(10, kb.arcLength(0, 0.5), 1e-6);
        assertEquals(5, kb.arcLength(0.25, 0.5), 1e-6);
    }

    @Test
    void segmentsFollowTheNodes() {
        List<Node> nodes = randomNodes(new Random(4), 5, false);
        List<Interpolation> all = List.of(new LinearInterpolation(), new KochanekBartelsInterpolation());
        for (Interpolation interpolation : all) {
            interpolation.setNodes(nodes);
            assertEquals(0, interpolation.getSegment(0));
            assertEquals(1, interpolation.getSegment(0.3));
            assertEquals(2, interpolation.getSegment(0.6));
            assertEquals(3, interpolation.getSegment(0.9));
            assertEquals(Integer.MAX_VALUE, interpolation.getSegment(1.5));
        }
    }

    @Test
    void positionsPastTheEndAreNull() {
        List<Node> nodes = randomNodes(new Random(5), 3, false);
        for (Interpolation interpolation : List.of(new LinearInterpolation(), new KochanekBartelsInterpolation(),
            new ReparametrisingInterpolation(new KochanekBartelsInterpolation()))) {
            interpolation.setNodes(nodes);
            assertNull(interpolation.getPosition(1.0001));
            assertNull(interpolation.get1stDerivative(2));
        }
    }

    private static List<Supplier<Interpolation>> interpolations() {
        return List.of(
            LinearInterpolation::new,
            KochanekBartelsInterpolation::new,
            () -> new ReparametrisingInterpolation(new KochanekBartelsInterpolation())
        );
    }

    @Test
    void singleNodeDoesNotProduceNaN() {
        List<Node> nodes = List.of(new Node(Vector3.at(3, -7, 12)));
        // LinearInterpolation is covered by linearWithASingleNodeReturnsThatNode,
        // and the reparametrised derivative by reparametrisedDerivativeOfADegenerateCurveIsFinite
        for (Interpolation interpolation : List.of(
            new KochanekBartelsInterpolation(), new ReparametrisingInterpolation(new KochanekBartelsInterpolation()))) {
            String name = interpolation.getClass().getSimpleName();
            interpolation.setNodes(nodes);
            for (double t : new double[]{0, 0.5, 1}) {
                assertVectorEquals(Vector3.at(3, -7, 12), interpolation.getPosition(t), EPSILON, name + " position " + t);
                if (!(interpolation instanceof ReparametrisingInterpolation)) {
                    assertFinite(interpolation.get1stDerivative(t), name + " derivative " + t);
                }
            }
            assertEquals(0, interpolation.arcLength(0, 1), EPSILON, name);
        }
    }

    @Test
    @Disabled("LinearInterpolation.getPosition(0) throws IndexOutOfBoundsException (Index 1 out of bounds for length 1) "
        + "with a single node, because the segment lookup always reads nodes.get(index + 1). Expected that node's position.")
    void linearWithASingleNodeReturnsThatNode() {
        LinearInterpolation interpolation = new LinearInterpolation();
        interpolation.setNodes(List.of(new Node(Vector3.at(3, -7, 12))));

        assertVectorEquals(Vector3.at(3, -7, 12), interpolation.getPosition(0), EPSILON, "position");
    }

    @Test
    void duplicateNodesDoNotProduceNaN() {
        List<List<Node>> cases = List.of(
            List.of(new Node(Vector3.ONE), new Node(Vector3.ONE)),
            List.of(new Node(Vector3.ONE), new Node(Vector3.ONE), new Node(Vector3.ONE), new Node(Vector3.ONE)),
            List.of(new Node(Vector3.ZERO), new Node(Vector3.ZERO), new Node(Vector3.at(5, -5, 5))),
            List.of(new Node(Vector3.at(5, -5, 5)), new Node(Vector3.ZERO), new Node(Vector3.ZERO), new Node(Vector3.at(1, 1, 1)))
        );
        for (List<Node> nodes : cases) {
            for (Supplier<Interpolation> factory : interpolations()) {
                Interpolation interpolation = factory.get();
                String name = interpolation.getClass().getSimpleName() + " with " + nodes.size() + " nodes";
                interpolation.setNodes(nodes);
                for (int i = 0; i < 100; i++) {
                    double t = i / 100.0;
                    assertFinite(interpolation.getPosition(t), name + " position " + t);
                    if (!(interpolation instanceof ReparametrisingInterpolation)) {
                        assertFinite(interpolation.get1stDerivative(t), name + " derivative " + t);
                    }
                    assertTrue(Double.isFinite(interpolation.arcLength(0, t)), name + " arc length " + t);
                }
            }
        }
    }

    @Test
    @Disabled("ReparametrisingInterpolation.get1stDerivative returns (NaN, NaN, NaN) when the base derivative is the "
        + "zero vector, e.g. a single node or duplicate nodes (such as two nodes at the same position): it normalises "
        + "the zero vector. Expected a finite vector.")
    void reparametrisedDerivativeOfADegenerateCurveIsFinite() {
        for (List<Node> nodes : List.of(
            List.of(new Node(Vector3.ONE)),
            List.of(new Node(Vector3.ONE), new Node(Vector3.ONE)))) {
            ReparametrisingInterpolation interpolation = new ReparametrisingInterpolation(new KochanekBartelsInterpolation());
            interpolation.setNodes(nodes);
            assertFinite(interpolation.get1stDerivative(0), nodes.size() + " node(s)");
        }
    }

    @Test
    void reparametrisedCurveSpacesSamplesEvenly() {
        // node spacing is very uneven, which the raw spline would reflect in the sample spacing
        List<Node> nodes = new ArrayList<>();
        for (double x : new double[]{0, 4, 12, 16, 24}) {
            nodes.add(new Node(Vector3.at(x, -3, 0)));
        }
        Interpolation raw = new KochanekBartelsInterpolation();
        raw.setNodes(nodes);
        ReparametrisingInterpolation even = new ReparametrisingInterpolation(new KochanekBartelsInterpolation());
        even.setNodes(nodes);

        double rawMin = Double.MAX_VALUE;
        double rawMax = 0;
        double evenMin = Double.MAX_VALUE;
        double evenMax = 0;
        for (int i = 0; i < 40; i++) {
            double a = i / 40.0;
            double b = (i + 1) / 40.0;
            double rawStep = raw.getPosition(a).distance(raw.getPosition(b));
            double evenStep = even.getPosition(a).distance(even.getPosition(b));
            rawMin = Math.min(rawMin, rawStep);
            rawMax = Math.max(rawMax, rawStep);
            evenMin = Math.min(evenMin, evenStep);
            evenMax = Math.max(evenMax, evenStep);
        }
        assertTrue(rawMax / rawMin > 2, "the fixture should be unevenly spaced to begin with");
        assertTrue(evenMax / evenMin < 1.6 && evenMax / evenMin < rawMax / rawMin, "steps range from " + evenMin + " to " + evenMax);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 4})
    void reparametrisedCurveKeepsItsEndpointsAndHasMonotoneArcLength(long seed) {
        Random random = new Random(seed);
        List<Node> nodes = randomNodes(random, 3 + random.nextInt(4), false);
        ReparametrisingInterpolation interpolation = new ReparametrisingInterpolation(new KochanekBartelsInterpolation());
        interpolation.setNodes(nodes);

        assertVectorEquals(nodes.get(0).getPosition(), interpolation.getPosition(0), EPSILON, "start");
        assertVectorEquals(nodes.get(nodes.size() - 1).getPosition(), interpolation.getPosition(1), 1e-7, "end");

        double total = interpolation.getBaseInterpolation().arcLength(0, 1);
        int previousSegment = 0;
        double previousLength = 0;
        for (int i = 1; i <= 20; i++) {
            double t = i / 20.0;
            double length = interpolation.arcLength(0, t);
            assertTrue(length >= previousLength - 1e-9, "arc length decreased at " + t);
            assertEquals(t * total, length, total * 0.03 + 0.05, "arc length at " + t);
            int segment = interpolation.getSegment(t);
            assertTrue(segment >= previousSegment, "segment went backwards at " + t);
            previousLength = length;
            previousSegment = segment;
        }
    }
}
