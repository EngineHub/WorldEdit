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
import org.enginehub.linbus.tree.LinRootEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The palette index of a block is a varint, so indices of 128 and above take two bytes and 16384 and above
 * three. The writers cannot emit those from the small test registry, so these tests read hand-built files
 * whose palettes use sparse indices.
 */
@DisplayName("Sponge schematic varint palette indices")
@Timeout(60)
class SpongeVarIntTest extends SchematicTestBase {

    private static final String[] STATES = {
        "minecraft:air", "minecraft:stone", "minecraft:oak_planks", "minecraft:glass",
        "minecraft:oak_stairs[facing=west,half=top]", "minecraft:chest[facing=south]", "minecraft:torch",
    };

    private static LinCompoundTag palette(int[] ids) {
        LinCompoundTag.Builder builder = LinCompoundTag.builder();
        for (int i = 0; i < ids.length; i++) {
            builder.putInt(STATES[i], ids[i]);
        }
        return builder.build();
    }

    private static byte[] v3File(int[] ids, int[] data) throws IOException {
        LinCompoundTag schematic = LinCompoundTag.builder()
            .putInt("Version", 3)
            .putInt("DataVersion", LIVE_DATA_VERSION)
            .putShort("Width", (short) data.length)
            .putShort("Height", (short) 1)
            .putShort("Length", (short) 1)
            .put("Blocks", LinCompoundTag.builder()
                .put("Palette", palette(ids))
                .putByteArray("Data", varInts(data))
                .build())
            .build();
        return gzip(new LinRootEntry("", LinCompoundTag.builder().put("Schematic", schematic).build()));
    }

    private static byte[] v2File(int[] ids, int[] data) throws IOException {
        LinCompoundTag schematic = LinCompoundTag.builder()
            .putInt("Version", 2)
            .putInt("DataVersion", LIVE_DATA_VERSION)
            .putShort("Width", (short) data.length)
            .putShort("Height", (short) 1)
            .putShort("Length", (short) 1)
            .putIntArray("Offset", new int[] {0, 0, 0})
            .putInt("PaletteMax", ids.length)
            .put("Palette", palette(ids))
            .putByteArray("BlockData", varInts(data))
            .build();
        return gzip(new LinRootEntry("Schematic", schematic));
    }

    /** Indices on both sides of every varint length boundary, up to the largest that fits in 31 bits. */
    private static final int[] SPARSE_IDS = {0, 127, 128, 16383, 16384, 2097151, Integer.MAX_VALUE};

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"SPONGE_V2_SCHEMATIC", "SPONGE_V3_SCHEMATIC"})
    void sparseIndicesPickTheRightState(String formatName) throws Exception {
        BuiltInClipboardFormat format = BuiltInClipboardFormat.valueOf(formatName);
        // Reverse order, so that a reader that decoded an index wrongly would produce a different block
        int[] data = {Integer.MAX_VALUE, 2097151, 16384, 16383, 128, 127, 0, 16384, 128};
        byte[] file = format == BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC
            ? v3File(SPARSE_IDS, data) : v2File(SPARSE_IDS, data);

        Clipboard clipboard = read(format, file);

        Map<Integer, String> byId = Map.of(
            0, STATES[0], 127, STATES[1], 128, STATES[2], 16383, STATES[3],
            16384, STATES[4], 2097151, STATES[5], Integer.MAX_VALUE, STATES[6]);
        for (int i = 0; i < data.length; i++) {
            String expected = state(byId.get(data[i])).getAsString();
            assertEquals(expected, clipboard.getFullBlock(BlockVector3.at(i, 0, 0)).toImmutableState().getAsString(),
                "index " + data[i] + " at x=" + i);
        }
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"SPONGE_V2_SCHEMATIC", "SPONGE_V3_SCHEMATIC"})
    void denseIndicesAcrossTheOneByteEdgeAreAllRead(String formatName) throws Exception {
        BuiltInClipboardFormat format = BuiltInClipboardFormat.valueOf(formatName);
        // 130 blocks cycling through three states whose indices sit at 126, 127 and 128
        int[] ids = {126, 127, 128, 129, 130, 131, 132};
        int[] data = new int[130];
        for (int i = 0; i < data.length; i++) {
            data[i] = 126 + (i % 3);
        }
        byte[] file = format == BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC ? v3File(ids, data) : v2File(ids, data);

        Clipboard clipboard = read(format, file);

        for (int i = 0; i < data.length; i++) {
            assertEquals(state(STATES[i % 3]).getAsString(),
                clipboard.getFullBlock(BlockVector3.at(i, 0, 0)).toImmutableState().getAsString(), "x=" + i);
        }
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(ints = {0, 127, 128, 255, 256, 16383, 16384, 65535, 2097151, 2097152, 268435455, 268435456,
        Integer.MAX_VALUE})
    void everyBoundaryIndexRoundTripsThroughTheReader(int index) throws Exception {
        byte[] file = v3File(new int[] {index, 1}, new int[] {index, 1, index});

        Clipboard clipboard = read(BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC, file);

        assertEquals("minecraft:air", clipboard.getFullBlock(BlockVector3.at(0, 0, 0)).toImmutableState().getAsString());
        assertEquals("minecraft:stone", clipboard.getFullBlock(BlockVector3.at(1, 0, 0)).toImmutableState().getAsString());
        assertEquals("minecraft:air", clipboard.getFullBlock(BlockVector3.at(2, 0, 0)).toImmutableState().getAsString());
    }
}
