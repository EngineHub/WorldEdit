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

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.pattern.ClipboardPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.function.pattern.RandomStatePattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PatternParserTest extends ParserTestBase {

    private static final BlockVector3 ORIGIN = BlockVector3.ZERO;

    private static BlockType type(String id) {
        return BlockTypes.get(id);
    }

    private static BlockState block(String id) {
        return type(id).getDefaultState();
    }

    private static BlockState stairs(Direction facing) {
        BlockType stairs = type("minecraft:oak_stairs");
        return block("minecraft:oak_stairs").with(stairs.getProperty("facing"), facing);
    }

    private static Pattern parse(String input, ParserContext context) throws InputParseException {
        return WorldEdit.getInstance().getPatternFactory().parseFromInput(input, context);
    }

    private static Pattern parse(String input) throws InputParseException {
        return parse(input, context());
    }

    @Test
    void plainBlocksAreTheirOwnPattern() throws InputParseException {
        BaseBlock block = assertInstanceOf(BaseBlock.class, parse("minecraft:oak_stairs[facing=east]"));
        assertEquals(stairs(Direction.EAST), block.toImmutableState());
    }

    @Test
    void randomPatternParsesWeights() throws InputParseException {
        assertInstanceOf(RandomPattern.class, parse("50%stone,50%glass"));
        assertInstanceOf(RandomPattern.class, parse("stone,glass"));
        assertInstanceOf(RandomPattern.class, parse("2.5%stone,glass"));
        assertInstanceOf(RandomPattern.class, parse("30%oak_stairs[facing=north,half=top],70%chest{Lock:\"a,b\"}"));
    }

    @Test
    void randomPatternOnlyProducesItsMembers() throws InputParseException {
        Pattern pattern = parse("25%stone,25%glass,50%oak_stairs[facing=north]");
        Set<BlockState> seen = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            seen.add(pattern.applyBlock(ORIGIN).toImmutableState());
        }
        assertEquals(Set.of(block("minecraft:stone"), block("minecraft:glass"), stairs(Direction.NORTH)), seen);
    }

    @Test
    void zeroWeightMemberNeverAppears() throws InputParseException {
        Pattern pattern = parse("0%stone,100%glass");
        for (int i = 0; i < 500; i++) {
            assertEquals(block("minecraft:glass"), pattern.applyBlock(ORIGIN).toImmutableState());
        }
    }

    @Test
    void randomPatternMissingTypeIsAnError() {
        assertParseError(InputParseException.class, "worldedit.error.parser.missing-random-type",
            () -> parse("stone,50%"));
        assertParseError(InputParseException.class, "worldedit.error.parser.missing-random-type",
            () -> parse("50%,stone"));
    }

    @Test
    void randomPatternWithUnknownMemberIsAnError() {
        assertParseError(InputParseException.class, "worldedit.error.unknown-block", () -> parse("50%nonexistent,stone"));
    }

    @Test
    void stateApplyingPatternChangesOnlyTheStates() throws WorldEditException {
        Extent extent = newExtent();
        BlockState original = stairs(Direction.SOUTH).with(type("minecraft:oak_stairs").getProperty("half"), "top");
        extent.setBlock(ORIGIN, original);
        ParserContext context = context();
        context.setExtent(extent);

        BlockState result = parse("^[facing=north]", context).applyBlock(ORIGIN).toImmutableState();
        assertEquals(stairs(Direction.NORTH).with(type("minecraft:oak_stairs").getProperty("half"), "top"), result);
    }

    @Test
    void stateApplyingPatternIgnoresBlocksWithoutTheProperty() throws WorldEditException {
        Extent extent = newExtent();
        extent.setBlock(ORIGIN, block("minecraft:stone"));
        ParserContext context = context();
        context.setExtent(extent);

        assertEquals(block("minecraft:stone"), parse("^[facing=north]", context).applyBlock(ORIGIN).toImmutableState());
    }

    @Test
    void typeApplyingPatternChangesTheType() throws WorldEditException {
        Extent extent = newExtent();
        extent.setBlock(ORIGIN, stairs(Direction.WEST));
        ParserContext context = context();
        context.setExtent(extent);

        BlockState result = parse("^oak_fence_gate", context).applyBlock(ORIGIN).toImmutableState();
        assertEquals(type("minecraft:oak_fence_gate"), result.getBlockType());
    }

    @Test
    @Disabled("TypeApplyingPattern does not carry states over when the registry creates Property instances per "
        + "block type (as TestBlockRegistry does): BlockState.with(oldTypeProperty, value) goes through "
        + "DefaultBlockTypeStateList.updateIndexOrInvalid, which compares properties with == and silently returns "
        + "the unchanged state. '^oak_fence_gate' applied over oak_stairs[facing=west] yields facing=north (the "
        + "default) instead of west. Unknown whether real platforms share Property instances across types")
    void typeApplyingPatternKeepsSharedStates() throws WorldEditException {
        Extent extent = newExtent();
        extent.setBlock(ORIGIN, stairs(Direction.WEST));
        ParserContext context = context();
        context.setExtent(extent);

        BlockState result = parse("^oak_fence_gate", context).applyBlock(ORIGIN).toImmutableState();
        assertEquals(Direction.WEST, result.getState(type("minecraft:oak_fence_gate").getProperty("facing")));
    }

    @Test
    void typeAndStateApplyingPattern() throws WorldEditException {
        Extent extent = newExtent();
        extent.setBlock(ORIGIN, stairs(Direction.WEST));
        ParserContext context = context();
        context.setExtent(extent);

        BlockState result = parse("^oak_fence_gate[open=true]", context).applyBlock(ORIGIN).toImmutableState();
        assertEquals(type("minecraft:oak_fence_gate"), result.getBlockType());
        assertEquals(true, result.getState(type("minecraft:oak_fence_gate").getProperty("open")));
    }

    static Stream<Arguments> invalidStatePatterns() {
        return Stream.of(
            Arguments.of("^[facing=north", "worldedit.error.parser.missing-rbracket"),
            Arguments.of("^oak_stairs[facing=north", "worldedit.error.parser.missing-rbracket"),
            Arguments.of("^[]", "worldedit.error.parser.empty-state"),
            Arguments.of("^[,facing=north]", "worldedit.error.parser.empty-state"),
            Arguments.of("^[facing=north,,half=top]", "worldedit.error.parser.empty-state"),
            Arguments.of("^[facing]", "worldedit.error.parser.missing-equals-separator"),
            Arguments.of("^[=north]", "worldedit.error.parser.empty-property"),
            Arguments.of("^[facing=]", "worldedit.error.parser.empty-value"),
            Arguments.of("^[facing=north,facing=south]", "worldedit.error.parser.duplicate-property")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidStatePatterns")
    void invalidStatePatternsGiveTheRightError(String input, String key) {
        assertParseError(InputParseException.class, key, () -> parse(input));
    }

    @Test
    void randomStatePatternWithOpenProperties() throws InputParseException {
        Pattern pattern = parse("*oak_stairs[facing=north]");
        assertInstanceOf(RandomStatePattern.class, pattern);
        Set<Object> halves = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            BlockState state = pattern.applyBlock(ORIGIN).toImmutableState();
            assertEquals(type("minecraft:oak_stairs"), state.getBlockType());
            assertEquals(Direction.NORTH, state.getState(type("minecraft:oak_stairs").getProperty("facing")));
            halves.add(state.getState(type("minecraft:oak_stairs").getProperty("half")));
        }
        assertEquals(2, halves.size(), "the open 'half' property should take both values");
    }

    @Test
    void randomStatePatternWithAllPropertiesGivenIsJustABlock() throws InputParseException {
        Pattern pattern = parse("*oak_stairs[facing=north,half=top,shape=straight,waterlogged=false]");
        assertInstanceOf(BaseBlock.class, pattern);
    }

    @Test
    void randomStatePatternRestoresWildcardPreference() throws InputParseException {
        ParserContext context = context();
        parse("*oak_stairs[facing=north]", context);
        assertFalse(context.isPreferringWildcard());
    }

    @Test
    void clipboardPatternNeedsASession() {
        assertParseError(InputParseException.class, "worldedit.error.missing-session", () -> parse("#clipboard"));
    }

    @Test
    void clipboardPatternWithEmptyClipboard() throws Exception {
        LocalSession session = mock(LocalSession.class);
        when(session.getClipboard()).thenThrow(new EmptyClipboardException());
        ParserContext context = context();
        context.setSession(session);
        assertParseError(InputParseException.class, "worldedit.error.empty-clipboard", () -> parse("#clipboard", context));
    }

    @Test
    void clipboardPatternReadsFromTheClipboardWithOffset() throws Exception {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(new CuboidRegion(ORIGIN, BlockVector3.at(2, 2, 2)));
        clipboard.setBlock(BlockVector3.at(1, 0, 0), block("minecraft:glass"));
        LocalSession session = mock(LocalSession.class);
        when(session.getClipboard()).thenReturn(new ClipboardHolder(clipboard));
        ParserContext context = context();
        context.setSession(session);

        assertInstanceOf(ClipboardPattern.class, parse("#clipboard", context));
        assertInstanceOf(ClipboardPattern.class, parse("#copy", context));
        assertInstanceOf(ClipboardPattern.class, parse("#CLIPBOARD@[1,0,0]", context));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("badClipboardOffsets")
    void clipboardPatternBadOffset(String input, String key) throws Exception {
        LocalSession session = mock(LocalSession.class);
        when(session.getClipboard()).thenReturn(new ClipboardHolder(
            new BlockArrayClipboard(new CuboidRegion(ORIGIN, ORIGIN))));
        ParserContext context = context();
        context.setSession(session);
        assertParseError(InputParseException.class, key, () -> parse(input, context));
    }

    static Stream<Arguments> badClipboardOffsets() {
        return Stream.of(
            Arguments.of("#clipboard@", "worldedit.error.parser.clipboard.missing-offset"),
            Arguments.of("#clipboard@1,2,3", "worldedit.error.parser.clipboard.missing-offset"),
            Arguments.of("#clipboard@[1,2]", "worldedit.error.parser.clipboard.missing-offset"),
            Arguments.of("#clipboard@[1,2,3,4]", "worldedit.error.parser.clipboard.missing-coordinates")
        );
    }
}
