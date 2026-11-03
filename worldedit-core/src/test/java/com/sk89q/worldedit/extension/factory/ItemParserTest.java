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

package com.sk89q.worldedit.extension.factory;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import org.enginehub.linbus.tree.LinTagType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemParserTest extends ParserTestBase {

    private static BaseItem parse(String input) throws InputParseException {
        return WorldEdit.getInstance().getItemFactory().parseFromInput(input, context());
    }

    static Stream<Arguments> validItems() {
        return Stream.of(
            Arguments.of("stone", "minecraft:stone"),
            Arguments.of("minecraft:stone", "minecraft:stone"),
            Arguments.of("STONE", "minecraft:stone"),
            Arguments.of("diamond_sword", "minecraft:diamond_sword")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validItems")
    void parsesItemTypes(String input, String expectedId) throws InputParseException {
        BaseItem item = parse(input);
        assertEquals(expectedId, item.getType().id());
        assertNull(item.getNbt());
    }

    @Test
    void parsesNbt() throws InputParseException {
        BaseItem item = parse("diamond_sword{Damage:5}");
        assertEquals("minecraft:diamond_sword", item.getType().id());
        assertNotNull(item.getNbt());
        assertEquals(5, item.getNbt().getTag("Damage", LinTagType.intTag()).valueAsInt());
    }

    @Test
    void bracesInsideNbtStringsAreKept() throws InputParseException {
        BaseItem item = parse("diamond_sword{Name:\"a}b\"}");
        assertEquals("a}b", item.getNbt().getTag("Name", LinTagType.stringTag()).value());
    }

    static Stream<Arguments> invalidItems() {
        return Stream.of(
            Arguments.of("nonexistent", NoMatchException.class, "worldedit.error.unknown-item"),
            Arguments.of("", NoMatchException.class, "worldedit.error.unknown-item"),
            Arguments.of("{Damage:5}", NoMatchException.class, "worldedit.error.unknown-item"),
            Arguments.of("diamond_sword{", InputParseException.class, "worldedit.error.parser.hanging-lbrace"),
            Arguments.of("diamond_sword{Damage:5", InputParseException.class, "worldedit.error.parser.missing-rbrace"),
            Arguments.of("diamond_sword{Damage:}", NoMatchException.class, "worldedit.error.invalid-nbt"),
            Arguments.of("diamond_sword{not nbt}", NoMatchException.class, "worldedit.error.invalid-nbt")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidItems")
    void invalidItemsGiveTheRightError(String input, Class<? extends InputParseException> type, String key) {
        assertParseError(type, key, () -> parse(input));
    }
}
