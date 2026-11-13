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
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.enginehub.linbus.tree.LinTagType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Checks when the readers run the platform's {@link DataFixer}, which version they tell it the data came from,
 * and that its output is what ends up in the clipboard. The real fixer needs Minecraft's data converters, so a
 * recording stand-in is used. What the real converters do to MC 26.3 data is not covered here.
 *
 * <p>The stand-in is installed on the shared platform mock, so the tests of this class must not overlap.</p>
 */
@DisplayName("Schematic data fixing")
@Execution(ExecutionMode.SAME_THREAD)
@Timeout(60)
class SchematicDataFixerTest extends SchematicTestBase {

    record Call(DataFixer.FixType<?> type, Object value, int sourceVersion) {
    }

    /** Renames planks to glass and desert to forest, tags NBT, and remembers what it was asked. */
    private static final class RecordingFixer implements DataFixer {
        final List<Call> calls = new ArrayList<>();
        boolean failBlockEntities;

        @Override
        @SuppressWarnings("unchecked")
        public <T> T fixUp(FixType<T> type, T original, int srcVer) {
            calls.add(new Call(type, original, srcVer));
            if (type == FixTypes.BLOCK_STATE) {
                return (T) ((String) original).replace("minecraft:oak_planks", "minecraft:glass");
            } else if (type == FixTypes.BIOME) {
                return (T) ((String) original).replace("minecraft:desert", "minecraft:forest");
            } else if (type == FixTypes.BLOCK_ENTITY) {
                if (failBlockEntities) {
                    throw new IllegalStateException("converter exploded");
                }
                return (T) ((LinCompoundTag) original).toBuilder().putString("Fixed", "yes").build();
            } else if (type == FixTypes.ENTITY) {
                LinCompoundTag tag = (LinCompoundTag) original;
                String id = tag.getTag("id", LinTagType.stringTag()).value();
                return (T) tag.toBuilder().putString("Fixed", "yes")
                    .putString("id", id.equals("ArmorStand") ? "minecraft:armor_stand" : id).build();
            }
            return original;
        }

        List<Call> callsOf(DataFixer.FixType<?> type) {
            return calls.stream().filter(c -> c.type() == type).toList();
        }
    }

    private RecordingFixer fixer;

    @BeforeAll
    static void loadLegacyMapperWithoutFixer() {
        // The mapper is a singleton and runs the fixer over its own table when first created
        when(MOCKED_PLATFORM.getDataFixer()).thenReturn(null);
        LegacyMapper.getInstance();
    }

    @BeforeEach
    void installFixer() {
        fixer = new RecordingFixer();
        when(MOCKED_PLATFORM.getDataFixer()).thenReturn(fixer);
    }

    @AfterEach
    void removeFixer() {
        when(MOCKED_PLATFORM.getDataFixer()).thenReturn(null);
    }

    private static byte[] withDataVersion(String resource, boolean nested, int dataVersion) throws IOException {
        LinRootEntry root = readRoot(resource);
        UnaryOperator<LinCompoundTag> edit = t -> t.toBuilder().putInt("DataVersion", dataVersion).build();
        if (nested) {
            LinCompoundTag schematic = root.value().getTag("Schematic", LinTagType.compoundTag());
            return gzip(new LinRootEntry(root.name(),
                LinCompoundTag.builder().put("Schematic", edit.apply(schematic)).build()));
        }
        return gzip(new LinRootEntry(root.name(), edit.apply(root.value())));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"sponge_v2.schem", "sponge_v3.schem"})
    void olderSchematicIsFixedFromItsOwnVersion(String file) throws IOException {
        BuiltInClipboardFormat format = file.contains("v3")
            ? BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC : BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC;

        Clipboard clipboard = readResource(format, file);

        assertTrue(fixer.calls.size() > 0, "the fixer was used");
        for (Call call : fixer.calls) {
            assertEquals(4671, call.sourceVersion(), () -> "fix of " + call.value());
        }
        assertEquals(5, fixer.callsOf(DataFixer.FixTypes.BLOCK_STATE).size(), "once per palette entry");
        assertEquals(2, fixer.callsOf(DataFixer.FixTypes.BIOME).size(), "once per biome palette entry");
        assertEquals(1, fixer.callsOf(DataFixer.FixTypes.BLOCK_ENTITY).size());
        assertEquals(1, fixer.callsOf(DataFixer.FixTypes.ENTITY).size());

        BlockVector3 min = clipboard.getMinimumPoint();
        assertEquals("minecraft:glass", clipboard.getFullBlock(min.add(2, 1, 1)).toImmutableState().getAsString());
        assertEquals("minecraft:forest", clipboard.getBiome(min.add(1, 0, 0)).id());
        assertEquals("yes", clipboard.getFullBlock(min.add(0, 0, 1)).getNbt()
            .getTag("Fixed", LinTagType.stringTag()).value());
        assertEquals("yes", clipboard.getEntities().get(0).getState().getNbt()
            .getTag("Fixed", LinTagType.stringTag()).value());
    }

    @Test
    void version1IsAlwaysFixedFromMinecraft1_13_2() throws IOException {
        Clipboard clipboard = readResource(BuiltInClipboardFormat.SPONGE_V1_SCHEMATIC, "sponge_v1.schem");

        assertTrue(fixer.calls.size() > 0, "the fixer was used");
        for (Call call : fixer.calls) {
            assertEquals(1631, call.sourceVersion());
        }
        assertEquals("minecraft:glass", clipboard.getFullBlock(clipboard.getMaximumPoint()).toImmutableState()
            .getAsString());
    }

    @ParameterizedTest(name = "{0}, DataVersion {1}")
    @ValueSource(strings = {"sponge_v2.schem", "sponge_v3.schem"})
    void fixerIsSkippedWhenTheSchematicIsNotOlder(String file) throws IOException {
        BuiltInClipboardFormat format = file.contains("v3")
            ? BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC : BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC;
        for (int dataVersion : new int[] {LIVE_DATA_VERSION, LIVE_DATA_VERSION + 1, Integer.MAX_VALUE, -1}) {
            fixer.calls.clear();

            Clipboard clipboard = read(format, withDataVersion(file, file.contains("v3"), dataVersion));

            assertEquals(List.of(), fixer.calls, "DataVersion " + dataVersion);
            // Nothing renamed it, so the planks are still planks
            assertEquals("minecraft:oak_planks", clipboard.getFullBlock(clipboard.getMaximumPoint())
                .toImmutableState().getAsString(), "DataVersion " + dataVersion);
        }
    }

    @Test
    void mceditFixesBlockEntitiesFromMinecraft1_13_2AndEntitiesFromTheOldestVersion() throws IOException {
        Clipboard clipboard = readResource(BuiltInClipboardFormat.MCEDIT_SCHEMATIC, "mcedit.schematic");

        assertEquals(List.of(1631), fixer.callsOf(DataFixer.FixTypes.BLOCK_ENTITY).stream()
            .map(Call::sourceVersion).toList());
        assertEquals(List.of(-1), fixer.callsOf(DataFixer.FixTypes.ENTITY).stream()
            .map(Call::sourceVersion).toList());
        BlockVector3 min = clipboard.getMinimumPoint();
        assertEquals("yes", clipboard.getFullBlock(min.add(0, 0, 1)).getNbt()
            .getTag("Fixed", LinTagType.stringTag()).value());
        // The fixer, not the reader, is responsible for turning the legacy entity id into a modern one
        assertEquals("minecraft:armor_stand", clipboard.getEntities().get(0).getState().getType().id());
    }

    @Test
    void mceditSurvivesAFixerThatThrowsOnBlockEntities() throws IOException {
        fixer.failBlockEntities = true;

        Clipboard clipboard = readResource(BuiltInClipboardFormat.MCEDIT_SCHEMATIC, "mcedit.schematic");

        BlockVector3 min = clipboard.getMinimumPoint();
        assertEquals(state("minecraft:chest[facing=north,type=single]").getAsString(),
            clipboard.getFullBlock(min.add(0, 0, 1)).toImmutableState().getAsString());
        assertEquals("Golden", clipboard.getFullBlock(min.add(0, 0, 1)).getNbt()
            .getTag("CustomName", LinTagType.stringTag()).value());
    }
}
