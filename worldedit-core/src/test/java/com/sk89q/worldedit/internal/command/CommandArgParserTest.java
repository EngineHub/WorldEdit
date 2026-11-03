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
import com.sk89q.worldedit.internal.util.Substring;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandArgParserTest {

    private static List<Substring> argParse(String s) {
        return CommandArgParser.forArgString(s).parseArgs().collect(Collectors.toList());
    }

    @Test
    void testArgumentParsing() {
        assertEquals(ImmutableList.of(
            Substring.wrap("", 0, 0)
        ), argParse(""));
        assertEquals(ImmutableList.of(
            Substring.wrap("ab", 0, 2)
        ), argParse("ab"));
        assertEquals(ImmutableList.of(
            Substring.wrap("", 0, 0),
            Substring.wrap("", 1, 1)
        ), argParse(" "));
        assertEquals(ImmutableList.of(
            Substring.wrap("a", 0, 1),
            Substring.wrap("", 2, 2)
        ), argParse("a "));
        assertEquals(ImmutableList.of(
            Substring.wrap("a", 0, 1),
            Substring.wrap("b", 2, 3)
        ), argParse("a b"));
    }

    @Test
    void testQuotedArguments() {
        assertEquals(ImmutableList.of(
            Substring.wrap("a b", 1, 4)
        ), argParse("\"a b\""));
        assertEquals(ImmutableList.of(
            Substring.wrap("x", 0, 1),
            Substring.wrap("a b c", 3, 8),
            Substring.wrap("y", 10, 11)
        ), argParse("x \"a b c\" y"));
        // an unterminated quote swallows the rest of the input, and is still emitted
        assertEquals(ImmutableList.of(
            Substring.wrap("a b", 1, 4)
        ), argParse("\"a b"));
        assertEquals(ImmutableList.of(
            Substring.wrap("", 1, 1)
        ), argParse("\"\""));
    }

    @Test
    void testQuotesInTheMiddleOfAnArgumentAreLiteral() {
        assertEquals(ImmutableList.of(
            Substring.wrap("a\"b", 0, 3),
            Substring.wrap("c\"", 4, 6)
        ), argParse("a\"b c\""));
    }
}
