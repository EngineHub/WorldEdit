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

package com.sk89q.util;

public final class ArrayUtil {

    private ArrayUtil() {
    }

    public static String[] removePortionOfArray(String[] array, int from, int to, String replace) {
        String[] newArray = new String[from + array.length - to - (replace == null ? 1 : 0)];
        System.arraycopy(array, 0, newArray, 0, from);
        if (replace != null) newArray[from] = replace;
        System.arraycopy(array, to + 1, newArray, from + (replace == null ? 0 : 1),
                    array.length - to - 1);
        return newArray;
    }

    public static char[] removePortionOfArray(char[] array, int from, int to, Character replace) {
        char[] newArray = new char[from + array.length - to - (replace == null ? 1 : 0)];
        System.arraycopy(array, 0, newArray, 0, from);
        if (replace != null) newArray[from] = replace;
        System.arraycopy(array, to + 1, newArray, from + (replace == null ? 0 : 1),
                    array.length - to - 1);
        return newArray;
    }
}
