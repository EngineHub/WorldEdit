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

package com.sk89q.util;

import com.google.common.base.CharMatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("StringUtil")
class StringUtilTest {

    @Nested
    @DisplayName("findMatchingCloseBracket")
    class FindMatchingCloseBracket {

        @Test
        @DisplayName("matches the outer close bracket when curly brackets are nested")
        void nestedCurlyBrackets() {
            assertEquals(8, StringUtil.findMatchingCloseBracket("{a:{b:1}}", 0));
        }

        @Test
        @DisplayName("handles square brackets inside curly brackets and vice versa")
        void mixedBracketKinds() {
            assertEquals(6, StringUtil.findMatchingCloseBracket("{a:[1]}", 0));
            assertEquals(12, StringUtil.findMatchingCloseBracket("[stuff,{b:2}]", 0));
        }

        @Test
        @DisplayName("matches the open bracket at the given index, not the first one in the input")
        void startsAtOpenIndex() {
            assertEquals(14, StringUtil.findMatchingCloseBracket("chest[a=b]{c:1}", 10));
            assertEquals(9, StringUtil.findMatchingCloseBracket("chest[a=b]{c:1}", 5));
        }

        @Test
        @DisplayName("ignores close brackets inside quoted strings, including after escaped quotes")
        void quotedStrings() {
            assertEquals(6, StringUtil.findMatchingCloseBracket("{a:\"}\"}", 0));
            assertEquals(6, StringUtil.findMatchingCloseBracket("{a:'}'}", 0));
            assertEquals(8, StringUtil.findMatchingCloseBracket("{a:\"\\\"}\"}", 0));
        }

        @Test
        @DisplayName("returns -1 for an unbalanced or mismatched bracket")
        void unbalanced() {
            assertEquals(-1, StringUtil.findMatchingCloseBracket("{a:1", 0));
            assertEquals(-1, StringUtil.findMatchingCloseBracket("{a:\"1}", 0));
            assertEquals(-1, StringUtil.findMatchingCloseBracket("{a:[1}", 0));
            assertEquals(-1, StringUtil.findMatchingCloseBracket("[a{b]c}", 0));
            assertEquals(-1, StringUtil.findMatchingCloseBracket("[a]]", 2));
        }
    }

    @Nested
    @DisplayName("splitOutsideBrackets")
    class SplitOutsideBrackets {

        private static final CharMatcher COMMA = CharMatcher.is(',');
        private static final CharMatcher PIPE_OR_SEMICOLON = CharMatcher.anyOf("|;");

        @Test
        @DisplayName("splits on a delimiter and keeps empty parts")
        void plain() {
            assertEquals(List.of("a", "b"), StringUtil.splitOutsideBrackets("a,b", COMMA));
            assertEquals(List.of("a", ""), StringUtil.splitOutsideBrackets("a,", COMMA));
            assertEquals(List.of(""), StringUtil.splitOutsideBrackets("", COMMA));
        }

        @Test
        @DisplayName("splits on any character in the delimiter set")
        void multipleDelimiters() {
            assertEquals(List.of("a", "b", "c"), StringUtil.splitOutsideBrackets("a|b;c", PIPE_OR_SEMICOLON));
        }

        @Test
        @DisplayName("ignores delimiters inside brackets, including nested and quoted ones")
        void nested() {
            assertEquals(
                List.of("a{x:1,y:2}", "b"),
                StringUtil.splitOutsideBrackets("a{x:1,y:2},b", COMMA)
            );
            assertEquals(
                List.of("a{x:[1,2],y:{z:3}}", "b[c=1,d=2]"),
                StringUtil.splitOutsideBrackets("a{x:[1,2],y:{z:3}},b[c=1,d=2]", COMMA)
            );
            assertEquals(
                List.of("a{x:\"1,2\"}", "b"),
                StringUtil.splitOutsideBrackets("a{x:\"1,2\"},b", COMMA)
            );
            assertEquals(
                List.of("a{x:'1;2'}", "b"),
                StringUtil.splitOutsideBrackets("a{x:'1;2'};b", PIPE_OR_SEMICOLON)
            );
            assertEquals(
                List.of("a{x:[I;1,2]}", "b"),
                StringUtil.splitOutsideBrackets("a{x:[I;1,2]}|b", PIPE_OR_SEMICOLON)
            );
        }

        @Test
        @DisplayName("stops splitting at an unbalanced open bracket")
        void unbalanced() {
            assertEquals(List.of("a{x:1,b"), StringUtil.splitOutsideBrackets("a{x:1,b", COMMA));
            assertEquals(List.of("a", "b[c,d"), StringUtil.splitOutsideBrackets("a,b[c,d", COMMA));
        }
    }
}
