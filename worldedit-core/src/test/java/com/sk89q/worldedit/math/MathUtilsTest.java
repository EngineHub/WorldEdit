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

import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathUtilsTest {

    @ParameterizedTest(name = "dSin({0}) = {1}, dCos({0}) = {2}")
    @DisplayName("is exact for multiples of 90 degrees")
    @CsvSource({
        "0, 0, 1", "90, 1, 0", "180, 0, -1", "270, -1, 0", "360, 0, 1",
        "-90, -1, 0", "-180, 0, -1", "-270, 1, 0", "-360, 0, 1", "450, 1, 0", "720, 0, 1"
    })
    void exactQuarterTurns(double degrees, double sin, double cos) {
        assertEquals(sin, MathUtils.dSin(degrees));
        assertEquals(cos, MathUtils.dCos(degrees));
    }

    @Property
    @Label("dSin and dCos are exact for any multiple of 90 degrees")
    void exactForAllQuarterTurns(@ForAll @IntRange(min = -10_000, max = 10_000) int turns) {
        double degrees = turns * 90.0;
        int quarter = Math.floorMod(turns, 4);
        assertEquals(new double[] {0, 1, 0, -1}[quarter], MathUtils.dSin(degrees));
        assertEquals(new double[] {1, 0, -1, 0}[quarter], MathUtils.dCos(degrees));
    }

    @Property
    @Label("dSin and dCos agree with Math for arbitrary angles")
    void agreesWithMath(@ForAll @IntRange(min = -100_000, max = 100_000) int tenths) {
        double degrees = tenths / 10.0;
        assertEquals(Math.sin(Math.toRadians(degrees)), MathUtils.dSin(degrees), 1e-9);
        assertEquals(Math.cos(Math.toRadians(degrees)), MathUtils.dCos(degrees), 1e-9);
    }

    @Test
    @DisplayName("dSin and dCos propagate NaN and infinities as NaN")
    void nonFiniteAngles() {
        assertTrue(Double.isNaN(MathUtils.dSin(Double.NaN)));
        assertTrue(Double.isNaN(MathUtils.dCos(Double.NaN)));
        assertTrue(Double.isNaN(MathUtils.dSin(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(MathUtils.dCos(Double.NEGATIVE_INFINITY)));
    }

    @Property
    @Label("divisorMod always lands in [0, n) and matches floorMod")
    void divisorModRange(
        @ForAll @IntRange(min = -1_000_000, max = 1_000_000) int a,
        @ForAll @IntRange(min = 1, max = 1000) int n
    ) {
        int mod = MathUtils.divisorMod(a, n);
        assertTrue(mod >= 0 && mod < n, "mod " + mod + " out of range for " + a + " % " + n);
        assertEquals(Math.floorMod(a, n), mod);
    }

    @ParameterizedTest(name = "roundHalfUp({0}) = {1}")
    @DisplayName("roundHalfUp rounds ties away from zero (unlike Vector3.round, which rounds ties up)")
    @CsvSource({
        "0.5, 1", "1.5, 2", "2.5, 3", "-0.5, -1", "-1.5, -2", "-2.5, -3",
        "0.49, 0", "-0.49, 0", "0.51, 1", "-0.51, -1", "3, 3", "-3, -3"
    })
    void roundHalfUp(double value, double expected) {
        // delta overload: small negative inputs yield -0.0, which is numerically equal to 0
        assertEquals(expected, MathUtils.roundHalfUp(value), 0.0);
    }

    @Property
    @Label("roundHalfUp is odd-symmetric and within 0.5 of the input")
    void roundHalfUpSymmetric(@ForAll @IntRange(min = -1_000_000, max = 1_000_000) int halves) {
        double value = halves / 2.0;
        double rounded = MathUtils.roundHalfUp(value);
        assertEquals(-rounded, MathUtils.roundHalfUp(-value));
        assertTrue(Math.abs(rounded - value) <= 0.5);
        assertEquals(rounded, Math.rint(rounded));
    }

    @Test
    @DisplayName("roundHalfUp of NaN is NaN")
    void roundHalfUpNaN() {
        assertTrue(Double.isNaN(MathUtils.roundHalfUp(Double.NaN)));
    }

}
