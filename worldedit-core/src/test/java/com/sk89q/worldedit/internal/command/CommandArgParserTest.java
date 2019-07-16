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

package com.sk89q.worldedit.internal.command;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.internal.util.Substring;
import org.junit.jupiter.api.Test;

import static com.sk89q.worldedit.internal.command.CommandArgParser.spaceSplit;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandArgParserTest {
    @Test
    public void testSpaceSplit() {
        assertEquals(ImmutableList.of(
            Substring.wrap("", 0, 0)
        ), spaceSplit(""));
        assertEquals(ImmutableList.of(
            Substring.wrap("ab", 0, 2)
        ), spaceSplit("ab"));
        assertEquals(ImmutableList.of(
            Substring.wrap("", 0, 0),
            Substring.wrap("", 1, 1)
        ), spaceSplit(" "));
        assertEquals(ImmutableList.of(
            Substring.wrap("a", 0, 1),
            Substring.wrap("", 2, 2)
        ), spaceSplit("a "));
        assertEquals(ImmutableList.of(
            Substring.wrap("a", 0, 1),
            Substring.wrap("b", 2, 3)
        ), spaceSplit("a b"));
    }
}
