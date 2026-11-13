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

package com.sk89q.worldedit.extent.clipboard.io;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.test.TestBlockRegistry;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinTagType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Sponge schematic round trips")
@Timeout(60)
class SpongeSchematicRoundTripTest extends SchematicTestBase {

    // The formats with a writer. Version 1 can only be read.
    private static final BuiltInClipboardFormat[] WRITABLE = {
        BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC,
        BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC,
    };

    static Stream<BuiltInClipboardFormat> writableFormats() {
        return Stream.of(WRITABLE);
    }

    record Shape(String name, BlockVector3 min, BlockVector3 max, BlockVector3 origin) {
        @Override
        public String toString() {
            return name;
        }
    }

    static List<Shape> shapes() {
        return List.of(
            new Shape("single block at zero", BlockVector3.ZERO, BlockVector3.ZERO, BlockVector3.ZERO),
            new Shape("single block, negative", BlockVector3.at(-7, -64, -3), BlockVector3.at(-7, -64, -3),
                BlockVector3.at(-7, -64, -3)),
            new Shape("positive box", BlockVector3.at(10, 0, 20), BlockVector3.at(12, 3, 24), BlockVector3.at(11, 1, 22)),
            new Shape("negative box", BlockVector3.at(-12, -64, -30), BlockVector3.at(-9, -60, -28),
                BlockVector3.at(-12, -64, -28)),
            new Shape("box straddling zero", BlockVector3.at(-2, -2, -2), BlockVector3.at(3, 2, 1), BlockVector3.ZERO),
            new Shape("origin outside the region", BlockVector3.at(0, 0, 0), BlockVector3.at(3, 3, 3),
                BlockVector3.at(-100, 300, 5000)),
            new Shape("flat plane", BlockVector3.at(-3, 70, -3), BlockVector3.at(4, 70, 4), BlockVector3.at(0, 70, 0)),
            new Shape("tall column", BlockVector3.at(5, -64, 5), BlockVector3.at(5, 319, 5), BlockVector3.at(5, 0, 5)),
            new Shape("long line", BlockVector3.at(0, 0, -50), BlockVector3.at(0, 0, 50), BlockVector3.at(0, 0, 0))
        );
    }

    static Stream<Arguments> formatsAndShapes() {
        return writableFormats().flatMap(format -> shapes().stream()
            .map(shape -> Arguments.of(format, shape)));
    }

    private static List<BlockState> allStates() {
        List<BlockState> states = new ArrayList<>();
        for (String id : TestBlockRegistry.blockIds()) {
            states.addAll(type(id).getAllStates());
        }
        return states;
    }

    // Fills every position with a random state from the first paletteSize states
    private static BlockArrayClipboard filled(Shape shape, int paletteSize, long seed) throws WorldEditException {
        BlockArrayClipboard clipboard = newClipboard(shape.min(), shape.max(), shape.origin());
        List<BlockState> palette = allStates().subList(0, paletteSize);
        Random random = new Random(seed);
        for (BlockVector3 pos : clipboard.getRegion()) {
            clipboard.setBlock(pos, palette.get(random.nextInt(palette.size())));
        }
        return clipboard;
    }

    private static LinCompoundTag chestNbt() {
        return LinCompoundTag.builder()
            .putString("id", "minecraft:chest")
            .putString("CustomName", "{\"text\":\"Loot\"}")
            .put("Items", LinListTag.builder(LinTagType.compoundTag())
                .add(LinCompoundTag.builder()
                    .putByte("Slot", (byte) 3)
                    .putString("id", "minecraft:stone")
                    .putInt("count", 7)
                    .build())
                .build())
            .build();
    }

    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("formatsAndShapes")
    void blocksSurviveForEveryShape(BuiltInClipboardFormat format, Shape shape) throws Exception {
        BlockArrayClipboard original = filled(shape, 40, shape.name().hashCode());

        Clipboard copy = read(format, write(format, original));

        assertClipboardsEqual(original, copy, true);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("writableFormats")
    void clipboardOfOnlyAirSurvives(BuiltInClipboardFormat format) throws Exception {
        BlockArrayClipboard original = newClipboard(BlockVector3.at(-1, -1, -1), BlockVector3.at(1, 1, 1),
            BlockVector3.ZERO);
        for (BlockVector3 pos : original.getRegion()) {
            original.setBlock(pos, type("minecraft:air").getDefaultState());
        }

        assertClipboardsEqual(original, read(format, write(format, original)), true);
    }

    // A palette is written as a map of block string to varint index; these sizes straddle the 1- and 2-byte edge.
    static IntStream paletteSizes() {
        return IntStream.of(1, 2, 126, 127, 128, 129, 255, 256, 1000, 2000);
    }

    @ParameterizedTest(name = "{0}, {1} distinct states")
    @MethodSource("formatPaletteSizes")
    void largePalettesSurvive(BuiltInClipboardFormat format, int paletteSize) throws Exception {
        int available = allStates().size();
        // One block per palette entry, in a line, so that every index is used exactly once
        BlockArrayClipboard original = newClipboard(BlockVector3.at(-5, 0, -5),
            BlockVector3.at(-5 + paletteSize - 1, 0, -5), BlockVector3.ZERO);
        List<BlockState> palette = allStates().subList(0, Math.min(paletteSize, available));
        assertEquals(paletteSize, palette.size(), "fixture has enough states");
        int i = 0;
        for (BlockVector3 pos : original.getRegion()) {
            original.setBlock(pos, palette.get(i++));
        }

        byte[] data = write(format, original);
        Clipboard copy = read(format, data);

        assertClipboardsEqual(original, copy, true);
    }

    static Stream<Arguments> formatPaletteSizes() {
        return writableFormats().flatMap(format -> paletteSizes().mapToObj(size ->
            Arguments.of(format, size)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("writableFormats")
    void blockEntitiesSurvive(BuiltInClipboardFormat format) throws Exception {
        BlockArrayClipboard original = newClipboard(BlockVector3.at(-3, -2, 4), BlockVector3.at(0, 1, 6),
            BlockVector3.at(-3, -2, 4));
        original.setBlock(BlockVector3.at(-3, -2, 4), type("minecraft:chest").getDefaultState()
            .toBaseBlock(chestNbt()));
        original.setBlock(BlockVector3.at(0, 1, 6), type("minecraft:chest").getDefaultState()
            .toBaseBlock(chestNbt().toBuilder().putString("CustomName", "second").build()));
        original.setBlock(BlockVector3.at(-1, 0, 5), type("minecraft:stone").getDefaultState());

        Clipboard copy = read(format, write(format, original));

        assertClipboardsEqual(original, copy, true);
        BaseBlock chest = copy.getFullBlock(BlockVector3.at(0, 1, 6));
        assertEquals("minecraft:chest", chest.getNbtId());
        assertEquals("second", chest.getNbt().getTag("CustomName", LinTagType.stringTag()).value());
        // The position tags are bookkeeping of the file, but the copy must say where each block entity lives
        assertEquals(0, chest.getNbt().getTag("x", LinTagType.intTag()).valueAsInt());
        assertEquals(1, chest.getNbt().getTag("y", LinTagType.intTag()).valueAsInt());
        assertEquals(6, chest.getNbt().getTag("z", LinTagType.intTag()).valueAsInt());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("writableFormats")
    void entitiesSurvive(BuiltInClipboardFormat format) throws Exception {
        BlockArrayClipboard original = newClipboard(BlockVector3.at(-10, -3, 100), BlockVector3.at(-7, 2, 103),
            BlockVector3.at(-10, -3, 100));
        addEntity(original, "minecraft:armor_stand", -9.5, -2.0, 101.25, 90f, -15f,
            LinCompoundTag.builder().putString("CustomName", "{\"text\":\"Stand\"}").putByte("Small", (byte) 1).build());
        addEntity(original, "minecraft:pig", -7.75, 1.5, 100.5, -180f, 45f, null);
        addEntity(original, "minecraft:item_frame", -10.0, -3.0, 100.0, 0f, 0f,
            LinCompoundTag.builder().putByte("Facing", (byte) 2).build());

        Clipboard copy = read(format, write(format, original));

        assertEquals(3, copy.getEntities().size());
        assertEntitiesEqual(original, copy);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("writableFormats")
    void columnBiomesSurvive(BuiltInClipboardFormat format) throws Exception {
        BlockArrayClipboard original = newClipboard(BlockVector3.at(-3, -4, -3), BlockVector3.at(0, 2, 1),
            BlockVector3.ZERO);
        List<String> biomes = BIOME_IDS;
        for (BlockVector3 pos : original.getRegion()) {
            // Sponge version 2 stores one biome per column, so make the biome independent of y
            original.setBiome(pos, biome(biomes.get(Math.floorMod(pos.x() * 3 + pos.z(), biomes.size()))));
        }

        Clipboard copy = read(format, write(format, original));

        assertClipboardsEqual(original, copy, true);
    }

    @Test
    void version3KeepsBiomesPerBlock() throws Exception {
        BlockArrayClipboard original = newClipboard(BlockVector3.at(-3, -4, -3), BlockVector3.at(0, 2, 1),
            BlockVector3.ZERO);
        for (BlockVector3 pos : original.getRegion()) {
            original.setBiome(pos, biome(BIOME_IDS.get(Math.floorMod(pos.x() + pos.y() * 2 + pos.z() * 3,
                BIOME_IDS.size()))));
        }

        Clipboard copy = read(BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC,
            write(BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC, original));

        assertClipboardsEqual(original, copy, false);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("writableFormats")
    void clipboardWithoutBiomesStaysWithoutBiomes(BuiltInClipboardFormat format) throws Exception {
        BlockArrayClipboard original = filled(shapes().get(2), 5, 1);

        Clipboard copy = read(format, write(format, original));

        assertTrue(!copy.hasBiomes(), "no biomes were written, none should appear");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("writableFormats")
    void readerReportsTheDataVersionTheWriterWrote(BuiltInClipboardFormat format) throws Exception {
        byte[] data = write(format, filled(shapes().get(2), 3, 2));

        try (ClipboardReader reader = format.getReader(new ByteArrayInputStream(data))) {
            assertEquals(OptionalInt.of(LIVE_DATA_VERSION), reader.getDataVersion());
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("writableFormats")
    void writtenFilesAreDetectedAsTheirOwnFormat(BuiltInClipboardFormat format) throws Exception {
        byte[] data = write(format, filled(shapes().get(2), 3, 3));

        for (BuiltInClipboardFormat candidate : BuiltInClipboardFormat.values()) {
            assertEquals(candidate == format,
                candidate.isFormat(new ByteArrayInputStream(data)),
                () -> candidate + " detecting a " + format + " file");
        }
        assertEquals(format, ClipboardFormats.findByInputStream(() -> new ByteArrayInputStream(data)));
    }

    @ParameterizedTest(name = "seed {0}")
    @MethodSource("seeds")
    void randomClipboardsSurviveBothFormats(int seed) throws Exception {
        Random random = new Random(seed);
        BlockVector3 min = BlockVector3.at(random.nextInt(400) - 200, random.nextInt(200) - 64, random.nextInt(400) - 200);
        BlockVector3 max = min.add(random.nextInt(7), random.nextInt(7), random.nextInt(7));
        BlockVector3 origin = BlockVector3.at(random.nextInt(50) - 25, random.nextInt(50) - 25, random.nextInt(50) - 25);
        Shape shape = new Shape("random", min, max, origin);
        BlockArrayClipboard original = filled(shape, 1 + random.nextInt(300), seed);
        if (random.nextBoolean()) {
            original.setBlock(max, type("minecraft:chest").getDefaultState().toBaseBlock(chestNbt()));
        }
        if (random.nextBoolean()) {
            addEntity(original, "minecraft:pig", min.x() + random.nextInt(4) + 0.5, min.y(), max.z() - 0.25,
                random.nextInt(360) - 180, random.nextInt(180) - 90, null);
        }

        for (BuiltInClipboardFormat format : WRITABLE) {
            assertClipboardsEqual(original, read(format, write(format, original)), true);
        }
    }

    static IntStream seeds() {
        return IntStream.range(0, 40);
    }

    @Test
    void version1HasNoWriter() {
        IOException e = assertThrows(IOException.class,
            () -> BuiltInClipboardFormat.SPONGE_V1_SCHEMATIC.getWriter(new ByteArrayOutputStream()));
        assertTrue(e.getMessage().contains("does not support saving"), e.getMessage());
    }

    @Test
    void mceditHasNoWriter() {
        assertThrows(IOException.class,
            () -> BuiltInClipboardFormat.MCEDIT_SCHEMATIC.getWriter(new ByteArrayOutputStream()));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = BuiltInClipboardFormat.class, names = {"SPONGE_V2_SCHEMATIC", "SPONGE_V3_SCHEMATIC"})
    void regionOneBlockTooWideForTheFormatIsRejected(BuiltInClipboardFormat format) {
        // Width is an unsigned short, so 65536 does not fit. A one-block-thick slab keeps the clipboard small
        BlockArrayClipboard clipboard = newClipboard(BlockVector3.ZERO, BlockVector3.at(65535, 0, 0),
            BlockVector3.ZERO);
        assertThrows(IllegalArgumentException.class, () -> write(format, clipboard));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = BuiltInClipboardFormat.class, names = {"SPONGE_V2_SCHEMATIC", "SPONGE_V3_SCHEMATIC"})
    void widestRepresentableRegionSurvives(BuiltInClipboardFormat format) throws Exception {
        // Width 65535 is the largest unsigned short; it would come back negative if read as a signed short
        BlockArrayClipboard original = newClipboard(BlockVector3.at(-30000, 0, 0), BlockVector3.at(35534, 0, 0),
            BlockVector3.ZERO);
        original.setBlock(BlockVector3.at(-30000, 0, 0), type("minecraft:stone").getDefaultState());
        original.setBlock(BlockVector3.at(35534, 0, 0), type("minecraft:oak_planks").getDefaultState());

        Clipboard copy = read(format, write(format, original));

        assertEquals(65535, copy.getDimensions().x());
        assertEquals(original.getMinimumPoint(), copy.getMinimumPoint());
        assertEquals("minecraft:stone", copy.getFullBlock(BlockVector3.at(-30000, 0, 0)).toImmutableState().getAsString());
        assertEquals("minecraft:oak_planks", copy.getFullBlock(BlockVector3.at(35534, 0, 0)).toImmutableState()
            .getAsString());
        assertEquals("minecraft:air", copy.getFullBlock(BlockVector3.at(0, 0, 0)).toImmutableState().getAsString());
    }

    @Test
    void fixtureHasEnoughStatesForTheLargestPaletteTests() {
        assertTrue(allStates().size() >= 2000, "states in the test registry: " + allStates().size());
    }
}
