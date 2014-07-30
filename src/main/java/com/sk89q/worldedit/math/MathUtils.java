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
     * Clamp a value between a minimum and a maximum.
     *
     * @param value the value to clamp
     * @param min the minimum, inclusive
     * @param max the maximum, inclusive
     * @return the clamped value
     */
    public static byte clamp(byte value, byte min, byte max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    /**
     * Clamp a value between a minimum and a maximum.
     *
     * @param value the value to clamp
     * @param min the minimum, inclusive
     * @param max the maximum, inclusive
     * @return the clamped value
     */
    public static short clamp(short value, short min, short max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    /**
     * Clamp a value between a minimum and a maximum.
     *
     * @param value the value to clamp
     * @param min the minimum, inclusive
     * @param max the maximum, inclusive
     * @return the clamped value
     */
    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    /**
     * Clamp a value between a minimum and a maximum.
     *
     * @param value the value to clamp
     * @param min the minimum, inclusive
     * @param max the maximum, inclusive
     * @return the clamped value
     */
    public static long clamp(long value, long min, long max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    /**
     * Clamp a value between a minimum and a maximum.
     *
     * @param value the value to clamp
     * @param min the minimum, inclusive
     * @param max the maximum, inclusive
     * @return the clamped value
     */
    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    /**
     * Clamp a value between a minimum and a maximum.
     *
     * @param value the value to clamp
     * @param min the minimum, inclusive
     * @param max the maximum, inclusive
     * @return the clamped value
     */
    public static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

}
