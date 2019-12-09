/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.internal.expression;

import com.sk89q.worldedit.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for various real-world expressions.
 */
class RealExpressionTest extends BaseExpressionTest {

    private static final class TestCase {

        final Vector3 loc;
        final double result;
        final Consumer<Expression> postChecks;

        private TestCase(Vector3 loc, double result, Consumer<Expression> postChecks) {
            this.loc = loc;
            this.result = result;
            this.postChecks = postChecks;
        }

        TestCase withData(int expectedData) {
            return new TestCase(loc, result, expr -> {
                postChecks.accept(expr);
                double data = readSlot(expr, "data");
                assertEquals(expectedData, (int) data,
                    "Test case " + this + " failed (data)");
            });
        }

        @Override
        public String toString() {
            return loc + " -> " + result;
        }
    }

    private static TestCase testCase(Vector3 loc, double result) {
        return testCase(loc, result, e -> {
        });
    }

    private static TestCase testCase(Vector3 loc, double result, Consumer<Expression> postChecks) {
        return new TestCase(loc, result, postChecks);
    }

    private void checkExpression(String expr, TestCase... cases) {
        Expression compiled = compile(expr, "x", "y", "z");
        for (TestCase aCase : cases) {
            Vector3 loc = aCase.loc;
            assertEquals(aCase.result, compiled.evaluate(loc.getX(), loc.getY(), loc.getZ()), 0,
                "Test case " + aCase + " failed (result)");
            aCase.postChecks.accept(compiled);
        }
    }

    @Test
    void torus() {
        checkExpression("(0.75-sqrt(x^2+y^2))^2+z^2 < 0.25^2",
            testCase(Vector3.at(0, 0, 0), 0),
            testCase(Vector3.at(0.5, 0.5, 0.5), 0),
            testCase(Vector3.at(1, 0, 0), 0),
            testCase(Vector3.at(0.5, 0.5, 0), 1),
            testCase(Vector3.at(0.75, 0.5, 0), 1),
            testCase(Vector3.at(0.75, 0, 0), 1));
    }

    @Test
    void gnarledOakTree() {
        checkExpression("(0.5+sin(atan2(x,z)*8)*0.2)*(sqrt(x*x+z*z)/0.5)^(-2)-1.2 < y",
            testCase(Vector3.at(-1, -1, -1), 1),
            testCase(Vector3.at(-1, 0, 1), 1),
            testCase(Vector3.at(1, 1, 1), 1),
            testCase(Vector3.at(0, 0, -1), 1),
            testCase(Vector3.at(0, 0, 0), 0),
            testCase(Vector3.at(0, 1, 0), 0),
            testCase(Vector3.at(0, 0, 0.32274), 0),
            testCase(Vector3.at(0, 0, 0.32275), 1));
    }

    @Test
    void rainbowTorus() {
        checkExpression("data=(32+15/2/pi*atan2(x,y))%16; (0.75-sqrt(x^2+y^2))^2+z^2 < 0.25^2",
            testCase(Vector3.at(0, 0, 0), 0),
            testCase(Vector3.at(0.5, 0.5, 0.5), 0),
            testCase(Vector3.at(1, 0, 0), 0),
            testCase(Vector3.at(0.5, 0.5, 0), 1).withData(1),
            testCase(Vector3.at(0.75, 0.5, 0), 1).withData(2),
            testCase(Vector3.at(0.75, 0, 0), 1).withData(3));
    }

    @Test
    void rainbowEgg() {
        TestCase[] testCases = new TestCase[15];
        for (int i = 0; i < testCases.length; i++) {
            testCases[i] = testCase(Vector3.at(0, i / 16.0 - 0.5, 0), 1)
                .withData((i + 9) % 16);
        }
        testCases = Stream.concat(Stream.of(testCases), Stream.of(
            testCase(Vector3.at(0, 1, 0), 0)
        )).toArray(TestCase[]::new);
        checkExpression("data=(32+y*16+1)%16; y^2/9+x^2/6*(1/(1-0.4*y))+z^2/6*(1/(1-0.4*y))<0.08",
            testCases);
    }

    @Test
    void heart() {
        checkExpression("(z/2)^2+x^2+(5*y/4-sqrt(abs(x)))^2<0.6",
            testCase(Vector3.at(0, 0, -1), 1),
            testCase(Vector3.at(0, 1, -1), 0),
            testCase(Vector3.at(-0.5, 1, 0), 1));
    }

    @Test
    void sineWave() {
        checkExpression("sin(x*5)/2<y",
            testCase(Vector3.at(1, -0.47947, 0), 0),
            testCase(Vector3.at(1, -0.47946, 0), 1),
            testCase(Vector3.at(2, -0.27202, 0), 0),
            testCase(Vector3.at(2, -0.27201, 0), 1),
            testCase(Vector3.at(3, 0.32513, 0), 0),
            testCase(Vector3.at(3, 0.32515, 0), 1));
    }

    @Test
    void radialCosine() {
        checkExpression("cos(sqrt(x^2+z^2)*5)/2<y",
            testCase(Vector3.at(0, 0.5, 0), 0),
            testCase(Vector3.at(0, 0.51, 0), 1),
            testCase(Vector3.at(Math.PI / 5, -0.5, 0), 0),
            testCase(Vector3.at(Math.PI / 5, -0.49, 0), 1),
            testCase(Vector3.at(Math.PI / 10, 0, 0), 0),
            testCase(Vector3.at(Math.PI / 10, 0.1, 0), 1));
    }

    @Test
    void circularHyperboloid() {
        checkExpression("-(z^2/12)+(y^2/4)-(x^2/12)>-0.03",
            testCase(Vector3.at(0, 0, 0), 1),
            testCase(Vector3.at(0, 1, 0), 1),
            testCase(Vector3.at(0, 1, 1), 1),
            testCase(Vector3.at(1, 1, 1), 1),
            testCase(Vector3.at(0, 0, 1), 0),
            testCase(Vector3.at(1, 0, 1), 0));
    }
}
