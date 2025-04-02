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

package com.sk89q.worldedit.internal.command;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandArgParserForCompletionsTest {

    private static List<ExtractedArg> argParse(String s) {
        return CommandArgParser.parse(s, true).toList();
    }

    @Test
    void testArgumentParsing() {
        assertEquals(ImmutableList.of(
            new ExtractedArg("", 0, 0)
        ), argParse(""));
        assertEquals(ImmutableList.of(
            new ExtractedArg("ab", 0, 2)
        ), argParse("ab"));
        assertEquals(ImmutableList.of(
            new ExtractedArg("", 0, 0),
            new ExtractedArg("", 1, 1)
        ), argParse(" "));
        assertEquals(ImmutableList.of(
            new ExtractedArg("a", 0, 1),
            new ExtractedArg("", 2, 2)
        ), argParse("a "));
        assertEquals(ImmutableList.of(
            new ExtractedArg("a", 0, 1),
            new ExtractedArg("b", 2, 3)
        ), argParse("a b"));
    }

    @ParameterizedTest
    @ValueSource(chars = {'"', '\''})
    void testQuotes(char quote) {
        assertEquals(ImmutableList.of(
            new ExtractedArg("", 0, 2)
        ), argParse(quote + "" + quote));
        assertEquals(ImmutableList.of(
            new ExtractedArg("a", 0, 3)
        ), argParse(quote + "a" + quote));
        assertEquals(ImmutableList.of(
            new ExtractedArg(" a ", 0, 5)
        ), argParse(quote + " a " + quote));
        assertEquals(ImmutableList.of(
            new ExtractedArg("a ", 0, 4),
            new ExtractedArg("", 5, 5)
        ), argParse(quote + "a " + quote + " "));
        assertEquals(ImmutableList.of(
            new ExtractedArg("a", 0, 3),
            new ExtractedArg("b", 4, 5)
        ), argParse(quote + "a" + quote + " b"));
        assertEquals(ImmutableList.of(
            new ExtractedArg("a", 0, 1),
            new ExtractedArg("b", 2, 5)
        ), argParse("a " + quote + "b" + quote));
        assertEquals(ImmutableList.of(
            new ExtractedArg("a", 0, 1),
            new ExtractedArg("b", 2, 5),
            new ExtractedArg("c", 6, 7)
        ), argParse("a " + quote + "b" + quote + " c"));
        assertEquals(ImmutableList.of(
            new ExtractedArg("a", 0, 1),
            new ExtractedArg("something" + quote + "quotable" + quote + "here", 2, 25),
            new ExtractedArg("c", 26, 27)
        ), argParse("a something" + quote + "quotable" + quote + "here c"));
    }

    @ParameterizedTest
    @ValueSource(chars = {'"', '\''})
    void testPartialQuotes(char quote) {
        // In 'for completions' mode, we should always return a result.
        assertEquals(ImmutableList.of(
            new ExtractedArg("", 0, 1)
        ), argParse(quote + ""));
        assertEquals(ImmutableList.of(
            new ExtractedArg("a", 0, 2)
        ), argParse(quote + "a"));
        assertEquals(ImmutableList.of(
            new ExtractedArg("a ", 0, 3)
        ), argParse(quote + "a "));
        assertEquals(ImmutableList.of(
            new ExtractedArg("a b", 0, 4)
        ), argParse(quote + "a b"));
        assertEquals(ImmutableList.of(
            new ExtractedArg("a", 0, 1),
            new ExtractedArg("", 2, 3)
        ), argParse("a " + quote));
        // Mid quotes are fine.
        assertEquals(ImmutableList.of(
            new ExtractedArg("a", 0, 1),
            new ExtractedArg("b" + quote + "c", 2, 5)
        ), argParse("a b" + quote + "c"));
        // End quotes are fine.
        assertEquals(ImmutableList.of(
            new ExtractedArg("a", 0, 1),
            new ExtractedArg("b" + quote, 2, 4)
        ), argParse("a b" + quote));
        assertEquals(ImmutableList.of(
            new ExtractedArg("a", 0, 1),
            new ExtractedArg("b" + quote, 2, 4),
            new ExtractedArg("c", 5, 6)
        ), argParse("a b" + quote + " c"));
    }

    @ParameterizedTest
    @ValueSource(chars = {'"', '\''})
    void testEscapingQuotes(char quote) {
        assertEquals(ImmutableList.of(
            new ExtractedArg(quote + "", 0, 2)
        ), argParse("\\" + quote));
        assertEquals(ImmutableList.of(
            new ExtractedArg(quote + "a", 0, 3)
        ), argParse("\\" + quote + "a"));
        assertEquals(ImmutableList.of(
            new ExtractedArg(quote + "a spaced out arg" + quote, 0, 22)
        ), argParse(quote + "\\" + quote + "a spaced out arg" + "\\" + quote + quote));
    }
}
