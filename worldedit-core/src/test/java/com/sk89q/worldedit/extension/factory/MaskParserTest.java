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
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.NoiseFilter;
import com.sk89q.worldedit.function.mask.OffsetsMask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskParserTest extends ParserTestBase {

    private static final BlockVector3 ORIGIN = BlockVector3.ZERO;
    private static final BlockVector3 ABOVE = BlockVector3.at(0, 1, 0);
    private static final BlockVector3 BELOW = BlockVector3.at(0, -1, 0);

    private static BlockState block(String id) {
        return BlockTypes.get(id).getDefaultState();
    }

    private static Mask parse(String input, Extent extent) throws InputParseException {
        ParserContext context = context();
        context.setExtent(extent);
        return WorldEdit.getInstance().getMaskFactory().parseFromInput(input, context);
    }

    private static Extent extentWith(Object... positionsAndBlocks) throws WorldEditException {
        Extent extent = newExtent();
        for (int i = 0; i < positionsAndBlocks.length; i += 2) {
            extent.setBlock((BlockVector3) positionsAndBlocks[i], (BlockState) positionsAndBlocks[i + 1]);
        }
        return extent;
    }

    @Test
    void singleBlockMatchesOnlyThatType() throws WorldEditException {
        Extent extent = extentWith(ORIGIN, block("minecraft:stone"), ABOVE, block("minecraft:glass"));
        Mask mask = parse("stone", extent);
        assertInstanceOf(BlockMask.class, mask);
        assertTrue(mask.test(ORIGIN));
        assertFalse(mask.test(ABOVE));
        assertFalse(mask.test(BELOW));
    }

    @Test
    void blockListMatchesAnyOfThem() throws WorldEditException {
        Extent extent = extentWith(ORIGIN, block("minecraft:stone"), ABOVE, block("minecraft:glass"),
            BELOW, block("minecraft:oak_planks"));
        Mask mask = parse("stone,glass", extent);
        assertTrue(mask.test(ORIGIN));
        assertTrue(mask.test(ABOVE));
        assertFalse(mask.test(BELOW));
    }

    @Test
    void namespacedAndUnnamespacedAreTheSame() throws WorldEditException {
        Extent extent = extentWith(ORIGIN, block("minecraft:stone"));
        assertTrue(parse("minecraft:stone", extent).test(ORIGIN));
        assertTrue(parse("MINECRAFT:STONE", extent).test(ORIGIN));
    }

    @Test
    void blockMaskWithoutStatesIsAWildcard() throws WorldEditException {
        BlockState stairsNorth = block("minecraft:oak_stairs").with(
            BlockTypes.get("minecraft:oak_stairs").getProperty("facing"), Direction.NORTH);
        BlockState stairsSouth = block("minecraft:oak_stairs").with(
            BlockTypes.get("minecraft:oak_stairs").getProperty("facing"), Direction.SOUTH);
        Extent extent = extentWith(ORIGIN, stairsNorth, ABOVE, stairsSouth);

        Mask any = parse("oak_stairs", extent);
        assertTrue(any.test(ORIGIN));
        assertTrue(any.test(ABOVE));

        Mask northOnly = parse("oak_stairs[facing=north]", extent);
        assertTrue(northOnly.test(ORIGIN));
        assertFalse(northOnly.test(ABOVE));
    }

    @Test
    void negation() throws WorldEditException {
        Extent extent = extentWith(ORIGIN, block("minecraft:stone"));
        Mask mask = parse("!stone", extent);
        assertFalse(mask.test(ORIGIN));
        assertTrue(mask.test(ABOVE));

        Mask doubleNegated = parse("!!stone", extent);
        assertTrue(doubleNegated.test(ORIGIN));
        assertFalse(doubleNegated.test(ABOVE));
    }

    @Test
    void negationOfNothingIsAnError() {
        assertParseError(InputParseException.class, "worldedit.error.parser.negate-nothing",
            () -> parse("!", newExtent()));
    }

    @Test
    void existingAndAirAreComplements() throws WorldEditException {
        Extent extent = extentWith(ORIGIN, block("minecraft:stone"));
        Mask existing = parse("#existing", extent);
        Mask air = parse("#air", extent);
        for (BlockVector3 pos : new BlockVector3[] {ORIGIN, ABOVE, BELOW}) {
            assertEquals(!existing.test(pos), air.test(pos), pos::toString);
        }
        assertTrue(existing.test(ORIGIN));
        assertTrue(air.test(ABOVE));
    }

    @Test
    void spaceSeparatedMasksAreIntersected() throws WorldEditException {
        Extent extent = extentWith(ORIGIN, block("minecraft:stone"), ABOVE, block("minecraft:glass"));
        Mask mask = parse("stone,glass !glass", extent);
        assertInstanceOf(MaskIntersection.class, mask);
        assertTrue(mask.test(ORIGIN));
        assertFalse(mask.test(ABOVE));
    }

    @Test
    void repeatedSpacesAreIgnored() throws WorldEditException {
        Extent extent = extentWith(ORIGIN, block("minecraft:stone"));
        assertTrue(parse("stone   #existing", extent).test(ORIGIN));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"", " ", "   "})
    void blankInputIsNoMatch(String input) {
        assertParseError(NoMatchException.class, "worldedit.error.no-match", () -> parse(input, newExtent()));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"nonexistent", "stone,nonexistent", "stone[bogus=1]x"})
    void unknownBlocksAreNoMatch(String input) {
        assertParseError(NoMatchException.class, "worldedit.error.no-match", () -> parse(input, newExtent()));
    }

    @Test
    void syntaxErrorsInBlocksAreReportedNotSwallowed() {
        assertParseError(InputParseException.class, "worldedit.error.parser.missing-rbracket",
            () -> parse("oak_stairs[facing=north", newExtent()));
    }

    /**
     * {@code >} tests the block below, {@code <} the block above, and an empty sub-mask means "any block".
     */
    @Test
    void verticalOffsetMasks() throws WorldEditException {
        Extent extent = extentWith(ORIGIN, block("minecraft:stone"), BlockVector3.at(5, 0, 5), block("minecraft:glass"));

        Mask onTopOfBlock = parse(">", extent);
        assertTrue(onTopOfBlock.test(ABOVE));
        assertFalse(onTopOfBlock.test(ORIGIN));

        Mask belowBlock = parse("<", extent);
        assertTrue(belowBlock.test(BELOW));
        assertFalse(belowBlock.test(ORIGIN));

        Mask onTopOfStone = parse(">stone", extent);
        assertTrue(onTopOfStone.test(ABOVE));
        assertFalse(onTopOfStone.test(BlockVector3.at(5, 1, 5)));

        Mask belowStone = parse("<stone", extent);
        assertTrue(belowStone.test(BELOW));
        assertFalse(belowStone.test(BlockVector3.at(5, -1, 5)));
    }

    @Test
    void adjacentMask() throws WorldEditException {
        Extent extent = extentWith(ORIGIN, block("minecraft:stone"));
        Mask adjacent = parse("~stone", extent);
        assertInstanceOf(OffsetsMask.class, adjacent);
        for (BlockVector3 neighbour : new BlockVector3[] {
            BlockVector3.at(1, 0, 0), BlockVector3.at(-1, 0, 0), BlockVector3.at(0, 1, 0),
            BlockVector3.at(0, -1, 0), BlockVector3.at(0, 0, 1), BlockVector3.at(0, 0, -1)}) {
            assertTrue(adjacent.test(neighbour), neighbour::toString);
        }
        assertFalse(adjacent.test(BlockVector3.at(1, 1, 0)), "diagonals are not adjacent");
        assertFalse(adjacent.test(BlockVector3.at(2, 0, 0)));
    }

    @Test
    void offsetMaskNests() throws WorldEditException {
        // two blocks below must be stone
        Extent extent = extentWith(ORIGIN, block("minecraft:stone"));
        Mask mask = parse(">>stone", extent);
        assertTrue(mask.test(BlockVector3.at(0, 2, 0)));
        assertFalse(mask.test(ABOVE));
    }

    static Stream<Arguments> noiseDensities() {
        return Stream.of(Arguments.of("%0", 0.0), Arguments.of("%50", 0.5), Arguments.of("%100", 1.0),
            Arguments.of("%7", 0.07));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("noiseDensities")
    void noiseMaskDensity(String input, double density) throws InputParseException {
        NoiseFilter mask = assertInstanceOf(NoiseFilter.class, parse(input, newExtent()));
        assertEquals(density, mask.getDensity(), 1e-9);
    }

    @Test
    void noiseMaskExtremes() throws InputParseException {
        Mask none = parse("%0", newExtent());
        Mask all = parse("%100", newExtent());
        for (int i = 0; i < 200; i++) {
            BlockVector3 pos = BlockVector3.at(i, -i, i * 7);
            assertTrue(all.test(pos));
            assertFalse(none.test(pos), pos::toString);
        }
    }

    @Test
    void noiseMaskRoughlyMatchesItsDensity() throws InputParseException {
        Mask mask = parse("%25", newExtent());
        int hits = 0;
        int total = 20_000;
        for (int i = 0; i < total; i++) {
            if (mask.test(BlockVector3.at(i % 100, (i / 100) % 100, i / 10_000))) {
                hits++;
            }
        }
        assertEquals(0.25, hits / (double) total, 0.03);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"%", "%abc", "%1.5"})
    @Disabled("NoiseMaskParser.parseFromInput calls Integer.parseInt unguarded: '%', '%abc' and '%1.5' escape as "
        + "NumberFormatException instead of an InputParseException, so users get an internal error rather than "
        + "a parse message")
    void malformedNoiseMasksAreInputErrors(String input) {
        assertThrows(InputParseException.class, () -> parse(input, newExtent()));
    }

    @Test
    void blockStateMask() throws WorldEditException {
        BlockType stairs = BlockTypes.get("minecraft:oak_stairs");
        BlockState north = block("minecraft:oak_stairs").with(stairs.getProperty("facing"),
            Direction.NORTH);
        BlockState south = block("minecraft:oak_stairs").with(stairs.getProperty("facing"),
            Direction.SOUTH);
        Extent extent = extentWith(ORIGIN, north, ABOVE, south, BELOW, block("minecraft:stone"));

        Mask loose = parse("^[facing=north]", extent);
        assertTrue(loose.test(ORIGIN));
        assertFalse(loose.test(ABOVE));
        // The loose form only rejects blocks that have the property with another value
        assertTrue(loose.test(BELOW));

        Mask strict = parse("^=[facing=north]", extent);
        assertTrue(strict.test(ORIGIN));
        assertFalse(strict.test(ABOVE));
        assertFalse(strict.test(BELOW));
    }
}
