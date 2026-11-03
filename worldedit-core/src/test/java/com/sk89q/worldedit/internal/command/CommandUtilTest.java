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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandUtilTest {

    @Test
    void testFixSuggestionsDoubleQuotedArg() {
        String input = "/schem load \"my sc";
        assertEquals(List.of("schem\""), CommandUtil.fixSuggestions(input, List.of(
            new FullStringSuggestion("\"my schem\"", 12, input.length())
        )));
    }

    @Test
    void testFixSuggestionsSingleQuotedArg() {
        String input = "/schem load 'my sc";
        assertEquals(List.of("schem'"), CommandUtil.fixSuggestions(input, List.of(
            new FullStringSuggestion("'my schem'", 12, input.length())
        )));
    }

    @Test
    void testFixSuggestionsTabInLastWord() {
        String input = "/a b\tc";
        assertEquals(List.of("b\tcd"), CommandUtil.fixSuggestions(input, List.of(
            new FullStringSuggestion("cd", 5, input.length())
        )));
    }
}
