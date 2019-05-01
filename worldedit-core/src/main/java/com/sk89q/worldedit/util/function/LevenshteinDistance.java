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

package com.sk89q.worldedit.util.function;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;

import java.util.Locale;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Provides a Levenshtein distance between a given string and each string
 * that this function is applied to.
 */
public class LevenshteinDistance implements Function<String, Integer> {

    public final static Pattern STANDARD_CHARS = Pattern.compile("[ _\\-]");

    private final String baseString;
    private final boolean caseSensitive;
    private final Pattern replacePattern;

    /**
     * Create a new instance.
     *
     * @param baseString the string to compare to
     * @param caseSensitive true to make case sensitive comparisons
     */
    public LevenshteinDistance(String baseString, boolean caseSensitive) {
        this(baseString, caseSensitive, null);
    }

    /**
     * Create a new instance.
     *
     * @param baseString the string to compare to
     * @param caseSensitive true to make case sensitive comparisons
     * @param replacePattern pattern to match characters to be removed in both the input and test strings (may be null)
     */
    public LevenshteinDistance(String baseString, boolean caseSensitive, @Nullable Pattern replacePattern) {
        checkNotNull(baseString);
        this.caseSensitive = caseSensitive;
        this.replacePattern = replacePattern;
        baseString = caseSensitive ? baseString : baseString.toLowerCase(Locale.ROOT);
        baseString = replacePattern != null ? replacePattern.matcher(baseString).replaceAll("") : baseString;
        this.baseString = baseString;
    }

    @Nullable
    @Override
    public Integer apply(String input) {
        if (input == null) {
            return null;
        }

        if (replacePattern != null) {
            input = replacePattern.matcher(input).replaceAll("");
        }

        if (caseSensitive) {
            return distance(baseString, input);
        } else {
            return distance(baseString, input.toLowerCase(Locale.ROOT));
        }
    }

    /**
     * <p>Find the Levenshtein distance between two Strings.</p>
     *
     * <p>This is the number of changes needed to change one String into
     * another, where each change is a single character modification (deletion,
     * insertion or substitution).</p>
     *
     * <p>The previous implementation of the Levenshtein distance algorithm
     * was from <a href="http://www.merriampark.com/ld.htm">http://www.merriampark.com/ld.htm</a></p>
     *
     * <p>Chas Emerick has written an implementation in Java, which avoids an OutOfMemoryError
     * which can occur when my Java implementation is used with very large strings.<br>
     * This implementation of the Levenshtein distance algorithm
     * is from <a href="http://www.merriampark.com/ldjava.htm">http://www.merriampark.com/ldjava.htm</a></p>
     *
     * <pre>
     * distance(null, *)             = IllegalArgumentException
     * distance(*, null)             = IllegalArgumentException
     * distance("","")               = 0
     * distance("","a")              = 1
     * distance("aaapppp", "")       = 7
     * distance("frog", "fog")       = 1
     * distance("fly", "ant")        = 3
     * distance("elephant", "hippo") = 7
     * distance("hippo", "elephant") = 7
     * distance("hippo", "zzzzzzzz") = 8
     * distance("hello", "hallo")    = 1
     * </pre>
     *
     * @param s  the first String, must not be null
     * @param t  the second String, must not be null
     * @return result distance
     * @throws IllegalArgumentException if either String input {@code null}
     */
    public static int distance(String s, String t) {
        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        /*
         * The difference between this impl. and the previous is that, rather
         * than creating and retaining a matrix of size s.length()+1 by
         * t.length()+1, we maintain two single-dimensional arrays of length
         * s.length()+1. The first, d, is the 'current working' distance array
         * that maintains the newest distance cost counts as we iterate through
         * the characters of String s. Each time we increment the index of
         * String t we are comparing, d is copied to p, the second int[]. Doing
         * so allows us to retain the previous cost counts as required by the
         * algorithm (taking the minimum of the cost count to the left, up one,
         * and diagonally up and to the left of the current cost count being
         * calculated). (Note that the arrays aren't really copied anymore, just
         * switched...this is clearly much better than cloning an array or doing
         * a System.arraycopy() each time through the outer loop.)
         *
         * Effectively, the difference between the two implementations is this
         * one does not cause an out of memory condition when calculating the LD
         * over two very large strings.
         */

        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        int[] p = new int[n + 1]; // 'previous' cost array, horizontally
        int[] d = new int[n + 1]; // cost array, horizontally
        int[] _d; // placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char tj; // jth character of t

        int cost; // cost

        for (i = 0; i <= n; ++i) {
            p[i] = i;
        }

        for (j = 1; j <= m; ++j) {
            tj = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; ++i) {
                cost = s.charAt(i - 1) == tj ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left
                // and up +cost
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1]
                        + cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return p[n];
    }

}
