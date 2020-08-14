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

package com.sk89q.worldedit.internal.util;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An explicit substring. Provides the range from which it was taken.
 */
public final class Substring {

    /**
     * Take a substring from {@code original}, and {@link #wrap(String, int, int)} it into
     * a Substring.
     */
    public static Substring from(String original, int start) {
        return new Substring(original.substring(start), start, original.length());
    }

    /**
     * Take a substring from {@code original}, and {@link #wrap(String, int, int)} it into
     * a Substring.
     */
    public static Substring from(String original, int start, int end) {
        return new Substring(original.substring(start, end), start, end);
    }

    /**
     * Wrap the given parameters into a Substring instance.
     */
    public static Substring wrap(String substring, int start, int end) {
        checkArgument(0 <= start, "Start must be greater than or equal to zero");
        checkArgument(start <= end, "End must be greater than or equal to start");
        return new Substring(substring, start, end);
    }

    private final String substring;
    private final int start;
    private final int end;

    private Substring(String substring, int start, int end) {
        this.substring = substring;
        this.start = start;
        this.end = end;
    }

    public String getSubstring() {
        return substring;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Substring substring1 = (Substring) o;
        return start == substring1.start
            && end == substring1.end
            && substring.equals(substring1.substring);
    }

    @Override
    public int hashCode() {
        return Objects.hash(substring, start, end);
    }

    @Override
    public String toString() {
        return "Substring{"
            + "substring='" + substring + "'"
            + ",start=" + start
            + ",end=" + end
            + "}";
    }
}
