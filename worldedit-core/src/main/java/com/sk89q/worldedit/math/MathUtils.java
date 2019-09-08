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

package com.sk89q.worldedit.math;

/**
 * Various math utility methods.
 */
public final class MathUtils {

    /**
     * Safe minimum, such that 1 / SAFE_MIN does not overflow.
     *
     * <p>In IEEE 754 arithmetic, this is also the smallest normalized number
     * 2<sup>-1022</sup>. The value of this constant is from Apache Commons
     * Math 2.2.</p>
     */
    public static final double SAFE_MIN = 0x1.0p-1022;

    private MathUtils() {
    }

    /**
     * Modulus, divisor-style.
     *
     * @param a a
     * @param n n
     * @return the modulus
     */
    public static int divisorMod(int a, int n) {
        return (int) (a - n * Math.floor(Math.floor(a) / n));
    }

    /**
     * Returns the cosine of an angle given in degrees. This is better than
     * just {@code Math.cos(Math.toRadians(degrees))} because it provides a
     * more accurate result for angles divisible by 90 degrees.
     *
     * @param degrees the angle
     * @return the cosine of the given angle
     */
    public static double dCos(double degrees) {
        int dInt = (int) degrees;
        if (degrees == dInt && dInt % 90 == 0) {
            dInt %= 360;
            if (dInt < 0) {
                dInt += 360;
            }
            switch (dInt) {
            case 0:
                return 1.0;
            case 90:
                return 0.0;
            case 180:
                return -1.0;
            case 270:
                return 0.0;
            }
        }
        return Math.cos(Math.toRadians(degrees));
    }

    /**
     * Returns the sine of an angle given in degrees. This is better than just
     * {@code Math.sin(Math.toRadians(degrees))} because it provides a more
     * accurate result for angles divisible by 90 degrees.
     *
     * @param degrees the angle
     * @return the sine of the given angle
     */
    public static double dSin(double degrees) {
        int dInt = (int) degrees;
        if (degrees == dInt && dInt % 90 == 0) {
            dInt %= 360;
            if (dInt < 0) {
                dInt += 360;
            }
            switch (dInt) {
            case 0:
                return 0.0;
            case 90:
                return 1.0;
            case 180:
                return 0.0;
            case 270:
                return -1.0;
            }
        }
        return Math.sin(Math.toRadians(degrees));
    }

    /**
     * Returns the rounded double of the given value, rounding to the
     * nearest integer value away from zero on ties.
     *
     * This behavior is the same as {@link java.math.RoundingMode#HALF_UP}.
     *
     * @param value the value
     * @return the rounded value
     */
    public static double roundHalfUp(double value) {
        return Math.signum(value) * Math.round(Math.abs(value));
    }
}
