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

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.enginehub.linbus.tree.LinTagType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Damaged schematics must fail with an error the caller can report, not hang, run out of memory, hit a stray
 * {@link NullPointerException}, or load as something other than what is in the file.
 *
 * <p>The load command only catches {@link IOException}, so the best failure is an {@code IOException} (or a
 * {@link SchematicLoadException}). The readers also raise {@link NoSuchElementException} for a missing tag,
 * {@link IllegalStateException} for a bad version or varint, and {@link IllegalArgumentException}; those are
 * tolerated here because they carry a message, unlike an NPE or an index error.</p>
 */
@DisplayName("Corrupt schematics")
@Timeout(60)
class CorruptSchematicTest extends SchematicTestBase {

    record Target(String file, BuiltInClipboardFormat format) {
        @Override
        public String toString() {
            return file;
        }
    }

    static Stream<Target> sponge() {
        return Stream.of(
            new Target("sponge_v1.schem", BuiltInClipboardFormat.SPONGE_V1_SCHEMATIC),
            new Target("sponge_v2.schem", BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC),
            new Target("sponge_v3.schem", BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC)
        );
    }

    static Stream<Target> all() {
        return Stream.concat(sponge(),
            Stream.of(new Target("mcedit.schematic", BuiltInClipboardFormat.MCEDIT_SCHEMATIC)));
    }

    private static boolean isV3(Target target) {
        return target.format() == BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC;
    }

    /** The compound that holds Width, Palette and so on. */
    private static LinCompoundTag schematic(Target target, LinRootEntry root) {
        return isV3(target)
            ? root.value().getTag("Schematic", LinTagType.compoundTag())
            : root.value();
    }

    private static LinRootEntry rebuild(Target target, LinRootEntry root, UnaryOperator<LinCompoundTag> edit) {
        LinCompoundTag changed = edit.apply(schematic(target, root));
        return isV3(target)
            ? new LinRootEntry(root.name(), LinCompoundTag.builder().put("Schematic", changed).build())
            : new LinRootEntry(root.name(), changed);
    }

    private static byte[] mutated(Target target, UnaryOperator<LinCompoundTag> edit) throws IOException {
        return gzip(rebuild(target, readRoot(target.file()), edit));
    }

    /** Applies the edit to the nested compound that holds the palette and block data (V3 keeps it in "Blocks"). */
    private static UnaryOperator<LinCompoundTag> editBlocks(Target target, UnaryOperator<LinCompoundTag.Builder> edit) {
        return tag -> {
            if (!isV3(target)) {
                return edit.apply(tag.toBuilder()).build();
            }
            LinCompoundTag blocks = tag.getTag("Blocks", LinTagType.compoundTag());
            return tag.toBuilder().put("Blocks", edit.apply(blocks.toBuilder()).build()).build();
        };
    }

    private static String dataKey(Target target) {
        return switch (target.format()) {
            case SPONGE_V3_SCHEMATIC, MCEDIT_SCHEMATIC -> "Data";
            default -> "BlockData";
        };
    }

    private static void assertCleanFailure(Target target, byte[] file) {
        Clipboard clipboard;
        try {
            clipboard = read(target.format(), file);
        } catch (IOException | SchematicLoadException | NoSuchElementException | IllegalStateException
                 | IllegalArgumentException _) {
            return;
        } catch (Throwable t) {
            throw new AssertionError("Damaged " + target + " failed with a non-descriptive " + t, t);
        }
        fail("Loaded a damaged " + target + " without complaint: " + clipboard.getRegion());
    }

    // ---- whole-file damage -------------------------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("all")
    void emptyFile(Target target) {
        assertThrows(IOException.class, () -> read(target.format(), new byte[0]));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("all")
    void notGzipAtAll(Target target) {
        byte[] text = "this is not a schematic".getBytes(StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> read(target.format(), text));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("all")
    void gzippedGarbage(Target target) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
            gz.write(new byte[] {10, 0, 0, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9});
        }
        byte[] data = out.toByteArray();
        assertCleanFailure(target, data);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("all")
    void everyTruncationFails(Target target) throws IOException {
        byte[] whole;
        try (InputStream in = open(target.file())) {
            whole = in.readAllBytes();
        }
        // Dropping any number of bytes from the NBT must never produce a clipboard. The last 9 bytes are only the
        // end of the deflate stream and the gzip trailer (CRC and size). The NBT is complete without them and the
        // parser is finished before it would read them, so they are not required
        for (int length = 0; length < whole.length - 9; length++) {
            byte[] cut = Arrays.copyOf(whole, length);
            assertCleanFailure(target, cut);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("all")
    void truncatedNbtInsideValidGzip(Target target) throws IOException {
        byte[] nbt;
        try (GZIPInputStream in = new GZIPInputStream(open(target.file()))) {
            nbt = in.readAllBytes();
        }
        // The gzip stream is intact (so no checksum error), but the NBT inside ends early
        for (int length = 0; length < nbt.length; length += 7) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
                gz.write(nbt, 0, length);
            }
            assertCleanFailure(target, out.toByteArray());
        }
    }

    // ---- missing or malformed tags -----------------------------------------------------------------------------

    static Stream<Arguments> missingTags() {
        return sponge().flatMap(t -> {
            Stream<String> top = Stream.of("Version", "Width", "Height", "Length");
            Stream<String> versioned = t.format() == BuiltInClipboardFormat.SPONGE_V1_SCHEMATIC
                ? Stream.empty() : Stream.of("DataVersion");
            return Stream.concat(top, versioned).map(tag -> Arguments.of(t, tag));
        });
    }

    @ParameterizedTest(name = "{0} without {1}")
    @MethodSource("missingTags")
    void missingTopLevelTag(Target target, String tag) throws IOException {
        byte[] file = mutated(target, t -> t.toBuilder().remove(tag).build());
        assertCleanFailure(target, file);
    }

    static Stream<Target> spongeNames() {
        return sponge();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spongeNames")
    void missingPalette(Target target) throws IOException {
        byte[] file = mutated(target, editBlocks(target, b -> {
            b.remove("Palette");
            return b;
        }));
        assertCleanFailure(target, file);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spongeNames")
    void missingBlockData(Target target) throws IOException {
        byte[] file = mutated(target, editBlocks(target, b -> {
            b.remove(dataKey(target));
            return b;
        }));
        assertCleanFailure(target, file);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spongeNames")
    void paletteThatIsNotACompound(Target target) throws IOException {
        byte[] file = mutated(target, editBlocks(target, b -> {
            b.putString("Palette", "nope");
            return b;
        }));
        assertCleanFailure(target, file);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spongeNames")
    void paletteEntryThatIsNotAnInt(Target target) throws IOException {
        byte[] file = mutated(target, editBlocks(target, b -> {
            b.put("Palette", LinCompoundTag.builder().putString("minecraft:stone", "0").build());
            return b;
        }));
        assertCleanFailure(target, file);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spongeNames")
    @Disabled("Bug: ReaderUtil.initializeClipboardFromBlocks throws a NullPointerException when the block data uses a "
        + "palette index that the palette does not define. Input: the golden sponge_v1/v2/v3 file with the "
        + "'minecraft:oak_planks' (index 4) palette entry removed, so the last block's index has no state. "
        + "palette.get(4) is null and state.toBaseBlock(..) at ReaderUtil.java:177 NPEs. Expected an IOException "
        + "(for example 'Block data refers to missing palette index 4').")
    void paletteMissingTheIndexTheDataUses(Target target) throws IOException {
        // Drops "minecraft:oak_planks" (index 4), which the last block refers to
        byte[] file = mutated(target, editBlocks(target, b -> {
            b.put("Palette", LinCompoundTag.builder()
                .putInt("minecraft:air", 0)
                .putInt("minecraft:stone", 1)
                .putInt("minecraft:oak_stairs[facing=west,half=top]", 2)
                .putInt("minecraft:chest[facing=north]", 3)
                .build());
            if (target.format() != BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC) {
                b.putInt("PaletteMax", 4);
            }
            return b;
        }));
        assertCleanFailure(target, file);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spongeNames")
    void blockDataEndingInTheMiddleOfAVarInt(Target target) throws IOException {
        byte[] file = mutated(target, editBlocks(target, b -> {
            b.putByteArray(dataKey(target), new byte[] {0, 1, (byte) 0x80});
            return b;
        }));
        assertCleanFailure(target, file);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spongeNames")
    void varIntThatNeverEnds(Target target) throws IOException {
        byte[] endless = new byte[12];
        Arrays.fill(endless, (byte) 0xFF);
        byte[] file = mutated(target, editBlocks(target, b -> {
            b.putByteArray(dataKey(target), endless);
            return b;
        }));
        assertCleanFailure(target, file);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spongeNames")
    @Disabled("Bug (data loss risk): block data holding more entries than Width*Height*Length is accepted silently. "
        + "Input: the golden sponge_v1/v2/v3 file with 40 palette indices in the block data for a 12-block region. "
        + "ReaderUtil.initializeClipboardFromBlocks tries clipboard.setBlock for every index, ignores the ones that "
        + "fall outside the region, and returns a clipboard. Expected an IOException saying the data does not match "
        + "the dimensions.")
    void moreBlocksThanTheRegionHolds(Target target) throws IOException {
        byte[] file = mutated(target, editBlocks(target, b -> {
            b.putByteArray(dataKey(target), varInts(IntStream.range(0, 40).map(_ -> 1).toArray()));
            return b;
        }));
        assertCleanFailure(target, file);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spongeNames")
    @Disabled("Bug (data loss risk): block data holding fewer entries than Width*Height*Length is accepted silently. "
        + "Input: the golden sponge_v1/v2/v3 file with only 3 palette indices in the block data for a 12-block "
        + "region. ReaderUtil.initializeClipboardFromBlocks only sets the blocks it has indices for and returns a "
        + "clipboard whose remaining blocks are empty. Expected an IOException saying the data does not match the "
        + "dimensions.")
    void fewerBlocksThanTheRegionHolds(Target target) throws IOException {
        byte[] file = mutated(target, editBlocks(target, b -> {
            b.putByteArray(dataKey(target), varInts(1, 1, 1));
            return b;
        }));
        assertCleanFailure(target, file);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spongeNames")
    void offsetWithTwoComponents(Target target) throws IOException {
        byte[] file = mutated(target, t -> t.toBuilder().putIntArray("Offset", new int[] {1, 2}).build());
        assertThrows(IOException.class, () -> read(target.format(), file));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spongeNames")
    void widthOfWrongType(Target target) throws IOException {
        byte[] file = mutated(target, t -> t.toBuilder().putString("Width", "3").build());
        assertCleanFailure(target, file);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spongeNames")
    void blockEntityWithoutPosition(Target target) throws IOException {
        String list = target.format() == BuiltInClipboardFormat.SPONGE_V1_SCHEMATIC ? "TileEntities" : "BlockEntities";
        byte[] file = mutated(target, editBlocks(target, b -> {
            b.put(list, LinListTag.builder(LinTagType.compoundTag())
                .add(LinCompoundTag.builder().putString("Id", "minecraft:chest").build())
                .build());
            return b;
        }));
        assertCleanFailure(target, file);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spongeNames")
    void blockEntityOutsideTheRegion(Target target) throws IOException {
        String list = target.format() == BuiltInClipboardFormat.SPONGE_V1_SCHEMATIC ? "TileEntities" : "BlockEntities";
        byte[] file = mutated(target, editBlocks(target, b -> {
            b.put(list, LinListTag.builder(LinTagType.compoundTag())
                .add(LinCompoundTag.builder().putString("Id", "minecraft:chest")
                    .putIntArray("Pos", new int[] {500, 500, 500}).build())
                .build());
            return b;
        }));
        // An orphan block entity has no block to attach to; the file is still readable and the rest must be intact
        Clipboard clipboard = read(target.format(), file);
        assertEquals("minecraft:stone", clipboard.getFullBlock(clipboard.getMinimumPoint()).toImmutableState().getAsString());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spongeNames")
    void unknownBlockInThePaletteBecomesAir(Target target) throws IOException {
        byte[] file = mutated(target, editBlocks(target, b -> {
            b.put("Palette", LinCompoundTag.builder()
                .putInt("minecraft:air", 0)
                .putInt("minecraft:not_a_block", 1)
                .putInt("minecraft:oak_stairs[facing=sideways]", 2)
                .putInt("minecraft:chest[facing=north]", 3)
                .putInt("minecraft:oak_planks", 4)
                .build());
            return b;
        }));

        Clipboard clipboard = read(target.format(), file);

        assertEquals("minecraft:air", clipboard.getFullBlock(clipboard.getMinimumPoint()).toImmutableState().getAsString());
        assertEquals("minecraft:air", clipboard.getFullBlock(clipboard.getMinimumPoint().add(1, 0, 0))
            .toImmutableState().getAsString());
        assertEquals("minecraft:oak_planks", clipboard.getFullBlock(clipboard.getMaximumPoint()).toImmutableState()
            .getAsString());
    }

    @Test
    void unknownBiomeInThePaletteDoesNotBreakLoading() throws IOException {
        Target target = new Target("sponge_v3.schem", BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC);
        byte[] file = mutated(target, t -> t.toBuilder().put("Biomes", LinCompoundTag.builder()
            .put("Palette", LinCompoundTag.builder()
                .putInt("minecraft:plains", 0).putInt("minecraft:from_the_future", 1).build())
            .putByteArray("Data", varInts(0, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 1))
            .build()).build());

        Clipboard clipboard = read(target.format(), file);

        assertEquals("minecraft:plains", clipboard.getBiome(clipboard.getMinimumPoint()).id());
    }

    @Test
    void biomePaletteSizeMismatchIsRejectedInVersion2() throws IOException {
        Target target = new Target("sponge_v2.schem", BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC);
        byte[] file = mutated(target, t -> t.toBuilder().putInt("BiomePaletteMax", 7).build());
        assertThrows(IOException.class, () -> read(target.format(), file));
    }

    @Test
    void palettMaxMismatchIsRejected() throws IOException {
        for (String name : new String[] {"sponge_v1.schem", "sponge_v2.schem"}) {
            Target target = new Target(name, name.contains("v1")
                ? BuiltInClipboardFormat.SPONGE_V1_SCHEMATIC : BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC);
            byte[] file = mutated(target, t -> t.toBuilder().putInt("PaletteMax", 99).build());
            assertThrows(IOException.class, () -> read(target.format(), file), name);
        }
    }

    // ---- wrong version / wrong reader ------------------------------------------------------------------------

    static Stream<Arguments> mismatchedReaders() {
        return sponge().flatMap(file -> sponge()
            .filter(reader -> reader.format() != file.format())
            .filter(reader -> !(reader.format() == BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC
                && file.format() == BuiltInClipboardFormat.SPONGE_V1_SCHEMATIC))
            .map(reader -> Arguments.of(file, reader)));
    }

    @ParameterizedTest(name = "{0} read as {1}")
    @MethodSource("mismatchedReaders")
    void readerRefusesAFileOfAnotherVersion(Target file, Target reader) throws IOException {
        byte[] data;
        try (InputStream in = open(file.file())) {
            data = in.readAllBytes();
        }
        assertCleanFailure(reader, data);
    }

    @Test
    void unsupportedVersionNumber() throws IOException {
        Target target = new Target("sponge_v2.schem", BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC);
        byte[] file = mutated(target, t -> t.toBuilder().putInt("Version", 99).build());
        assertCleanFailure(target, file);
    }

    @Test
    @Disabled("Bug: ClipboardReader.getDataVersion() throws an IllegalStateException for a file whose Version is not "
        + "the reader's. Input: sponge_v2.schem with Version changed to 99, read by the V2 reader. "
        + "ReaderUtil.checkSchematicVersion uses checkState, and getDataVersion only catches IOException, so the "
        + "exception escapes instead of returning OptionalInt.empty() as for other unreadable files.")
    void unsupportedVersionNumberHasNoDataVersion() throws IOException {
        Target target = new Target("sponge_v2.schem", BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC);
        byte[] file = mutated(target, t -> t.toBuilder().putInt("Version", 99).build());
        try (ClipboardReader reader = target.format().getReader(new ByteArrayInputStream(file))) {
            assertEquals(OptionalInt.empty(), reader.getDataVersion());
        }
    }

    // ---- MCEdit ------------------------------------------------------------------------------------------------

    private static final Target MCEDIT = new Target("mcedit.schematic", BuiltInClipboardFormat.MCEDIT_SCHEMATIC);

    @ParameterizedTest(name = "without {0}")
    @ValueSource(strings = {"Blocks", "Data", "Width", "Height", "Length", "Materials"})
    void mceditMissingTag(String tag) throws IOException {
        byte[] file = mutated(MCEDIT, t -> t.toBuilder().remove(tag).build());
        assertCleanFailure(MCEDIT, file);
    }

    @Test
    void mceditWithClassicMaterialsIsRejected() throws IOException {
        byte[] file = mutated(MCEDIT, t -> t.toBuilder().putString("Materials", "Classic").build());
        assertThrows(IOException.class, () -> read(MCEDIT.format(), file));
    }

    @Test
    void mceditWithWrongRootName() throws IOException {
        LinRootEntry root = readRoot(MCEDIT.file());
        byte[] file = gzip(new LinRootEntry("Other", root.value()));
        assertThrows(IOException.class, () -> read(MCEDIT.format(), file));
    }

    @Test
    void mceditWithUnknownLegacyBlocksSkipsThem() throws IOException {
        byte[] file = mutated(MCEDIT, t -> t.toBuilder()
            .putByteArray("Blocks", new byte[] {(byte) 255, (byte) 254, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0})
            .putByteArray("Data", new byte[12])
            .build());

        Clipboard clipboard = read(MCEDIT.format(), file);

        BlockVector3 min = clipboard.getMinimumPoint();
        assertEquals("minecraft:stone", clipboard.getFullBlock(min.add(2, 0, 0)).toImmutableState().getAsString());
    }

    @Test
    @Disabled("Bug: MCEditSchematicReader throws an ArrayIndexOutOfBoundsException when the Blocks/Data arrays are "
        + "shorter than Width*Height*Length. Input: mcedit.schematic (3x2x2) with 2-byte Blocks and Data arrays; "
        + "read() indexes blocks[index] for all 12 positions (MCEditSchematicReader.java:240). Expected an "
        + "IOException about the mismatched array size.")
    void mceditWithShorterBlockArrayThanDimensions() throws IOException {
        byte[] file = mutated(MCEDIT, t -> t.toBuilder()
            .putByteArray("Blocks", new byte[] {1, 1})
            .putByteArray("Data", new byte[] {0, 0})
            .remove("TileEntities")
            .build());
        assertCleanFailure(MCEDIT, file);
    }

    @Test
    @Disabled("Bug: MCEditSchematicReader throws an ArrayIndexOutOfBoundsException for a tile entity whose x/y/z lies "
        + "outside the schematic. Input: mcedit.schematic with a Chest tile entity at (90, 90, 90); the index "
        + "y*width*length+z*width+x = 900 is used on a 12-element array (MCEditSchematicReader.java:205). "
        + "Expected the tile entity to be skipped, or an IOException.")
    void mceditTileEntityOutsideTheBlockArray() throws IOException {
        byte[] file = mutated(MCEDIT, t -> t.toBuilder().put("TileEntities",
            LinListTag.builder(LinTagType.compoundTag())
                .add(LinCompoundTag.builder().putString("id", "Chest").putInt("x", 90).putInt("y", 90)
                    .putInt("z", 90).build())
                .build()).build());
        assertCleanFailure(MCEDIT, file);
    }
}
