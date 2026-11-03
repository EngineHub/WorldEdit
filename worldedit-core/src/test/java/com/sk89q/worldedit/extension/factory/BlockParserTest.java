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

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.test.TestBlockRegistry;
import com.sk89q.worldedit.util.test.TestWorld;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.FuzzyBlockState;
import org.enginehub.linbus.tree.LinTagType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link com.sk89q.worldedit.extension.factory.parser.DefaultBlockParser}, including the
 * bracket handling that was fixed in #3017.
 */
class BlockParserTest extends ParserTestBase {

    private static BaseBlock parse(String input) throws InputParseException {
        return WorldEdit.getInstance().getBlockFactory().parseFromInput(input, context());
    }

    private static BlockState state(String id, String... propertyValues) {
        BlockType type = BlockTypes.get(id);
        BlockState state = type.getDefaultState();
        for (String propertyValue : propertyValues) {
            String[] parts = propertyValue.split("=", 2);
            state = withValue(state, type.getPropertyMap().get(parts[0]), parts[1]);
        }
        return state;
    }

    private static <V> BlockState withValue(BlockState state, Property<V> property, String value) {
        return state.with(property, property.getValueFor(value));
    }

    static Stream<Arguments> validBlocks() {
        return Stream.of(
            Arguments.of("stone", state("minecraft:stone")),
            Arguments.of("minecraft:stone", state("minecraft:stone")),
            Arguments.of("STONE", state("minecraft:stone")),
            Arguments.of("MineCraft:Stone", state("minecraft:stone")),
            Arguments.of("  stone  ", state("minecraft:stone")),
            Arguments.of("stone|", state("minecraft:stone")),
            Arguments.of("stone;", state("minecraft:stone")),
            Arguments.of("oak_stairs", state("minecraft:oak_stairs")),
            Arguments.of("oak_stairs[facing=north]", state("minecraft:oak_stairs", "facing=north")),
            Arguments.of("minecraft:oak_stairs[facing=west,half=top]",
                state("minecraft:oak_stairs", "facing=west", "half=top")),
            Arguments.of("oak_stairs[half=top,facing=west]",
                state("minecraft:oak_stairs", "facing=west", "half=top")),
            Arguments.of("repeater[delay=3,locked=true]", state("minecraft:repeater", "delay=3", "locked=true")),
            Arguments.of("oak_sign[rotation=15]", state("minecraft:oak_sign", "rotation=15")),
            Arguments.of("oak_log[axis=z]", state("minecraft:oak_log", "axis=z"))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validBlocks")
    void parsesValidBlocks(String input, BlockState expected) throws InputParseException {
        BaseBlock block = parse(input);
        assertEquals(expected, block.toImmutableState());
        assertNull(block.getNbt());
    }

    @Test
    @Disabled("Possible bug in DefaultBlockParser.parseLogic: 'oak_stairs[]' throws InputParseException "
        + "(worldedit.error.parser.bad-state-format) because \"\".split(\",\") yields one empty property; "
        + "vanilla accepts an empty state list, so the default state was expected")
    void emptyStateListGivesDefaultState() throws InputParseException {
        assertEquals(state("minecraft:oak_stairs"), parse("oak_stairs[]").toImmutableState());
    }

    @Test
    void nonWildcardParsingFillsInDefaultsForOmittedProperties() throws InputParseException {
        BaseBlock block = parse("oak_stairs[facing=north]");
        assertEquals(BlockTypes.get("minecraft:oak_stairs").getPropertyMap().size(), block.getStates().size());
        assertFalse(block.toImmutableState() instanceof FuzzyBlockState);
    }

    @Test
    void wildcardParsingLeavesOmittedPropertiesOpen() throws InputParseException {
        ParserContext context = context();
        context.setPreferringWildcard(true);
        BaseBlock block = WorldEdit.getInstance().getBlockFactory().parseFromInput("oak_stairs[facing=north]", context);

        assertInstanceOf(FuzzyBlockState.class, block.toImmutableState());
        assertEquals(1, block.getStates().size());

        BlockState north = state("minecraft:oak_stairs", "facing=north", "half=top");
        BlockState south = state("minecraft:oak_stairs", "facing=south", "half=top");
        assertTrue(block.toImmutableState().equalsFuzzy(north));
        assertFalse(block.toImmutableState().equalsFuzzy(south));
    }

    @Test
    void wildcardWithoutStatesMatchesEveryState() throws InputParseException {
        ParserContext context = context();
        context.setPreferringWildcard(true);
        BaseBlock block = WorldEdit.getInstance().getBlockFactory().parseFromInput("oak_stairs", context);
        for (BlockState candidate : BlockTypes.get("minecraft:oak_stairs").getAllStates()) {
            assertTrue(block.toImmutableState().equalsFuzzy(candidate), candidate::toString);
        }
    }

    /**
     * Every state of every test block must parse back from its string form.
     */
    @Test
    void parseOfGetAsStringRoundTripsEveryState() throws InputParseException {
        int checked = 0;
        for (String id : TestBlockRegistry.blockIds()) {
            for (BlockState state : BlockTypes.get(id).getAllStates()) {
                String string = state.getAsString();
                assertEquals(state, parse(string).toImmutableState(), string);
                checked++;
            }
        }
        assertTrue(checked > 100, "Expected to check many states, but checked " + checked);
    }

    @Test
    void parseOfGetAsStringRoundTripsNbt() throws InputParseException {
        for (String input : new String[] {
            "chest{Lock:\"key\"}",
            "chest[facing=south]{Lock:\"key\"}",
            "chest[facing=south,type=left]{Items:[{Slot:0b,id:\"minecraft:stone\",count:3}]}",
        }) {
            BaseBlock first = parse(input);
            assertTrue(first.getNbt() != null, input);
            BaseBlock second = parse(first.getAsString());
            assertEquals(first, second, input);
        }
    }

    static Stream<Arguments> nbtBlocks() {
        return Stream.of(
            // Regression for #3017: brackets and braces inside NBT strings must not be seen as structure
            Arguments.of("chest{CustomName:\"a]b\"}", "CustomName", "a]b"),
            Arguments.of("chest{CustomName:\"a[b\"}", "CustomName", "a[b"),
            Arguments.of("chest{CustomName:\"a}b\"}", "CustomName", "a}b"),
            Arguments.of("chest{CustomName:\"a{b\"}", "CustomName", "a{b"),
            Arguments.of("chest[facing=north]{CustomName:\"[x]\"}", "CustomName", "[x]"),
            Arguments.of("chest{CustomName:'a]b'}", "CustomName", "a]b"),
            Arguments.of("chest{CustomName:\"a|b\"}", "CustomName", "a|b"),
            Arguments.of("chest{CustomName:\"a;b\"}", "CustomName", "a;b")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nbtBlocks")
    void nbtWithStructuralCharactersInStrings(String input, String key, String expected) throws InputParseException {
        BaseBlock block = parse(input);
        assertEquals(BlockTypes.get("minecraft:chest"), block.getBlockType());
        assertEquals(expected, block.getNbt().getTag(key, LinTagType.stringTag()).value());
    }

    @Test
    void stateAndNbtTogether() throws InputParseException {
        BaseBlock block = parse("chest[facing=east]{Lock:\"x\"}");
        assertEquals(state("minecraft:chest", "facing=east"), block.toImmutableState());
        assertTrue(block.getNbt() != null);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"stone|extra", "stone;extra"})
    void extraDataIsAcceptedWithoutNbt(String input) throws InputParseException {
        assertEquals(state("minecraft:stone"), parse(input).toImmutableState());
    }

    @Test
    void extraDataIsRejectedTogetherWithNbt() {
        assertParseError(InputParseException.class, "worldedit.error.parser.nbt-with-extra-data",
            () -> parse("chest{Lock:\"x\"}|extra"));
    }

    @Test
    void delimitersInsideBracketsAreNotExtraDataDelimiters() throws InputParseException {
        assertEquals(state("minecraft:oak_stairs", "facing=north"), parse("oak_stairs[facing=north]|").toImmutableState());
    }

    static Stream<Arguments> invalidBlocks() {
        return Stream.of(
            Arguments.of("nonexistent", NoMatchException.class, "worldedit.error.unknown-block"),
            Arguments.of("minecraft:nonexistent", NoMatchException.class, "worldedit.error.unknown-block"),
            Arguments.of("othermod:stone", NoMatchException.class, "worldedit.error.unknown-block"),
            Arguments.of("oak_stairs[facing=up]", NoMatchException.class, "worldedit.error.parser.unknown-value"),
            Arguments.of("oak_stairs[delay=1]", NoMatchException.class, "worldedit.error.parser.unknown-property"),
            Arguments.of("stone[facing=north]", NoMatchException.class, "worldedit.error.parser.unknown-property"),
            Arguments.of("oak_stairs[facing]", InputParseException.class, "worldedit.error.parser.bad-state-format"),
            Arguments.of("oak_stairs[facing=]", InputParseException.class, "worldedit.error.parser.bad-state-format"),
            Arguments.of("oak_stairs[=north]", NoMatchException.class, "worldedit.error.parser.unknown-property"),
            Arguments.of("oak_stairs[facing=north=south]", InputParseException.class,
                "worldedit.error.parser.bad-state-format"),
            Arguments.of("oak_stairs[facing=north,facing=south]", InputParseException.class,
                "worldedit.error.parser.duplicate-property"),
            Arguments.of("oak_stairs[facing=north", InputParseException.class, "worldedit.error.parser.missing-rbracket"),
            Arguments.of("oak_stairs[facing=north,half=top", InputParseException.class,
                "worldedit.error.parser.missing-rbracket"),
            Arguments.of("oak_stairs[", InputParseException.class, "worldedit.error.parser.hanging-lbracket"),
            Arguments.of("chest{", InputParseException.class, "worldedit.error.parser.hanging-lbrace"),
            Arguments.of("chest{Lock:\"x\"", InputParseException.class, "worldedit.error.parser.missing-rbrace"),
            Arguments.of("chest{Lock:}", NoMatchException.class, "worldedit.error.parser.invalid-nbt"),
            Arguments.of("chest{not nbt}", NoMatchException.class, "worldedit.error.parser.invalid-nbt"),
            Arguments.of("", InputParseException.class, "worldedit.error.parser.missing-type"),
            Arguments.of("   ", InputParseException.class, "worldedit.error.parser.missing-type"),
            Arguments.of("[facing=north]", InputParseException.class, "worldedit.error.parser.missing-type"),
            Arguments.of("{Lock:\"x\"}", InputParseException.class, "worldedit.error.parser.missing-type"),
            // The stricter parsing from #3017
            Arguments.of("oak_stairs[facing=north]x", NoMatchException.class, "worldedit.error.parser.trailing-input"),
            Arguments.of("chest{Lock:\"x\"}x", NoMatchException.class, "worldedit.error.parser.trailing-input"),
            Arguments.of("oak_stairs[facing=north][half=top]", NoMatchException.class,
                "worldedit.error.parser.duplicate-state"),
            Arguments.of("chest{Lock:\"x\"}{Lock:\"y\"}", NoMatchException.class,
                "worldedit.error.parser.duplicate-nbt"),
            Arguments.of("chest{Lock:\"x\"}[facing=north]", NoMatchException.class,
                "worldedit.error.parser.state-after-nbt")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidBlocks")
    void invalidBlocksGiveTheRightError(String input, Class<? extends InputParseException> type, String key) {
        assertParseError(type, key, () -> WorldEdit.getInstance().getBlockFactory()
            .parseFromInput(input, contextWithActor()));
    }

    @Test
    void unknownPropertiesAreIgnoredWithoutAnActor() throws InputParseException {
        // Without an actor nobody could be told about the mistake, so the whole state list is dropped
        assertEquals(state("minecraft:stone"), parse("stone[facing=north]").toImmutableState());
    }

    @Test
    void parsesBlockList() throws InputParseException {
        Set<BaseBlock> blocks = WorldEdit.getInstance().getBlockFactory().parseFromListInput(
            "stone,oak_stairs[facing=north,half=top],chest{CustomName:\"a,b\"}", context());
        assertEquals(3, blocks.size());
        assertTrue(blocks.stream().anyMatch(b -> b.getBlockType() == BlockTypes.get("minecraft:stone")));
        assertTrue(blocks.stream().anyMatch(b -> b.getBlockType() == BlockTypes.get("minecraft:chest")
            && b.getNbt() != null));
    }

    @Test
    void handUsesTheBlockInHand() throws Exception {
        Player player = mock(Player.class);
        BaseBlock held = state("minecraft:oak_stairs", "facing=east").toBaseBlock();
        when(player.getBlockInHand(HandSide.MAIN_HAND)).thenReturn(held);
        ParserContext context = context();
        context.setActor(player);

        assertEquals(held, WorldEdit.getInstance().getBlockFactory().parseFromInput("hand", context));
        assertEquals(held, WorldEdit.getInstance().getBlockFactory().parseFromInput("HAND", context));
    }

    @Test
    void offhandUsesTheOffHandBlock() throws Exception {
        Player player = mock(Player.class);
        BaseBlock held = state("minecraft:glass").toBaseBlock();
        when(player.getBlockInHand(HandSide.OFF_HAND)).thenReturn(held);
        ParserContext context = context();
        context.setActor(player);

        assertEquals(held, WorldEdit.getInstance().getBlockFactory().parseFromInput("offhand", context));
    }

    @Test
    void handRequiresAPlayer() {
        ParserContext context = context();
        context.setActor(mock(Actor.class));
        assertParseError(InputParseException.class, "worldedit.error.parser.player-only",
            () -> WorldEdit.getInstance().getBlockFactory().parseFromInput("hand", context));
        assertParseError(InputParseException.class, "worldedit.error.parser.player-only",
            () -> WorldEdit.getInstance().getBlockFactory().parseFromInput("offhand", context));
    }

    @Test
    void pos1UsesTheBlockAtThePrimaryPosition() throws Exception {
        TestWorld world = new TestWorld();
        BlockVector3 pos = BlockVector3.at(3, -10, 7);
        world.setBlock(pos, state("minecraft:oak_stairs", "facing=south"));
        LocalSession session = new LocalSession();
        session.getRegionSelector(world).selectPrimary(pos, null);

        ParserContext context = context();
        context.setWorld(world);
        context.setSession(session);

        BaseBlock block = WorldEdit.getInstance().getBlockFactory().parseFromInput("pos1", context);
        assertEquals(state("minecraft:oak_stairs", "facing=south"), block.toImmutableState());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({"stone, stone", "oak_stairs[facing=north], oak_stairs"})
    void typeIsParsedIndependentlyOfProperties(String input, String typeId) throws InputParseException {
        assertEquals(BlockTypes.get("minecraft:" + typeId), parse(input).getBlockType());
    }
}
