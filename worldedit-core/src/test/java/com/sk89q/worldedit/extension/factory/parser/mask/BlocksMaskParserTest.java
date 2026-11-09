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

package com.sk89q.worldedit.extension.factory.parser.mask;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.factory.BlockFactory;
import com.sk89q.worldedit.extension.factory.MaskFactory;
import com.sk89q.worldedit.extension.input.ParserContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlocksMaskParserTest {

    private final WorldEdit worldEdit = mock(WorldEdit.class);
    private final BlockFactory blockFactory = mock(BlockFactory.class);
    private final BlocksMaskParser parser = new BlocksMaskParser(worldEdit);
    private final ParserContext context = new ParserContext();

    BlocksMaskParserTest() {
        when(worldEdit.getBlockFactory()).thenReturn(blockFactory);
    }

    static Stream<Arguments> completionInputs() {
        return Stream.of(
                Arguments.of("", "bri", "bricks"),
                Arguments.of("", "", "stone"),
                Arguments.of("stone,", "bri", "bricks"),
                Arguments.of("stone,dirt,", "bri", "bricks"),
                Arguments.of("minecraft:stone,", "minecraft:bri", "minecraft:bricks"),
                Arguments.of("", "oak_stairs[facing=north,", "oak_stairs[facing=north,half="),
                Arguments.of("oak_stairs[facing=north,half=bottom],", "bri", "bricks"),
                Arguments.of("chest{CustomName:'a,b'},", "bri", "bricks"),
                Arguments.of("stone,", "oak_stairs[facing=north,half=b", "oak_stairs[facing=north,half=bottom]"),
                Arguments.of("stone,", "oak_stairs[facing=north,", "oak_stairs[facing=north,half="),
                Arguments.of("stone,", "chest{CustomName:'a,b", "chest{CustomName:'a,b'}"),
                Arguments.of("stone ,dirt,", "bri", "bricks"),
                Arguments.of("stone,", "", "bricks")
        );
    }

    @ParameterizedTest
    @MethodSource("completionInputs")
    void completesLastBlock(String prefix, String token, String completion) {
        when(blockFactory.getSuggestions(token, context)).thenReturn(List.of(completion));
        assertEquals(List.of(prefix + completion), parser.getSuggestions(prefix + token, context).toList());
    }

    @Test
    void preservesNegatedMaskPrefix() {
        MaskFactory maskFactory = new MaskFactory(worldEdit);
        when(worldEdit.getMaskFactory()).thenReturn(maskFactory);
        when(blockFactory.getSuggestions("bri", context)).thenReturn(List.of("bricks"));
        assertTrue(maskFactory.getSuggestions("!stone,bri", context).contains("!stone,bricks"));
    }
}
