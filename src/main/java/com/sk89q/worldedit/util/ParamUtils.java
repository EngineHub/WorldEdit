// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.util;

/**
 * Java-relevant parameter functions.
 */
public class ParamUtils {
    
    private ParamUtils() {
    }
    
    /**
     * Check that a value is non-null.
     * 
     * @param val value to check
     * @throws IllegalArgumentException on error
     */
    public static void nonNull(Object val) {
        if (val == null) {
            throw new IllegalArgumentException("Null parameter not exected");
        }
    }
    
    /**
     * Check that a value is between or equal to two numbers.
     * 
     * @param val value to check
     * @param lower lower value
     * @param upper upper value
     * @throws IllegalArgumentException on error
     */
    public static void inRange(Integer val, int lower, int upper) {
        if (val == null) {
            throw new IllegalArgumentException("Null parameter not exected");
        }

        if (val < lower || val > upper) {
            throw new IllegalArgumentException(
                    "Parameter expected to be a value between " + lower
                            + " and " + upper + " (received " + val + ")");
        }
    }
    
    /**
     * Check that a value is between or equal to two numbers.
     * 
     * @param val value to check
     * @param lower lower value
     * @param upper upper value
     * @throws IllegalArgumentException on error
     */
    public static void inRange(Long val, long lower, long upper) {
        if (val == null) {
            throw new IllegalArgumentException("Null parameter not exected");
        }

        if (val < lower || val > upper) {
            throw new IllegalArgumentException(
                    "Parameter expected to be a value between " + lower
                            + " and " + upper + " (received " + val + ")");
        }
    }
    
    /**
     * Check that a value is between or equal to two numbers.
     * 
     * @param val value to check
     * @param lower lower value
     * @param upper upper value
     * @throws IllegalArgumentException on error
     */
    public static void inRange(Double val, double lower, double upper) {
        if (val == null) {
            throw new IllegalArgumentException("Null parameter not exected");
        }

        if (val < lower || val > upper) {
            throw new IllegalArgumentException(
                    "Parameter expected to be a value between " + lower
                            + " and " + upper + " (received " + val + ")");
        }
    }
    
    /**
     * Check that a value is between or equal to two numbers.
     * 
     * @param val value to check
     * @param lower lower value
     * @param upper upper value
     * @throws IllegalArgumentException on error
     */
    public static void inRange(Float val, float lower, float upper) {
        if (val == null) {
            throw new IllegalArgumentException("Null parameter not exected");
        }

        if (val < lower || val > upper) {
            throw new IllegalArgumentException(
                    "Parameter expected to be a value between " + lower
                            + " and " + upper + " (received " + val + ")");
        }
    }

}
