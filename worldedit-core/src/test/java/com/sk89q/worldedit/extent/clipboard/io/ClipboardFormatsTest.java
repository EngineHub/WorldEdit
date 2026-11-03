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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Clipboard format registry and detection")
@Timeout(60)
class ClipboardFormatsTest extends SchematicTestBase {

    record Golden(String file, BuiltInClipboardFormat format) {
        @Override
        public String toString() {
            return file;
        }
    }

    static Stream<Golden> goldens() {
        return Stream.of(
            new Golden("sponge_v1.schem", BuiltInClipboardFormat.SPONGE_V1_SCHEMATIC),
            new Golden("sponge_v2.schem", BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC),
            new Golden("sponge_v3.schem", BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC),
            new Golden("mcedit.schematic", BuiltInClipboardFormat.MCEDIT_SCHEMATIC)
        );
    }

    private static Path path(String name) throws URISyntaxException {
        return Paths.get(ClipboardFormatsTest.class.getResource("/schematics/" + name).toURI());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("goldens")
    void eachFileIsDetectedAsExactlyItsFormat(Golden golden) throws IOException {
        for (BuiltInClipboardFormat candidate : BuiltInClipboardFormat.values()) {
            try (InputStream in = open(golden.file())) {
                assertEquals(candidate == golden.format(), candidate.isFormat(in),
                    () -> candidate + " on " + golden.file());
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("goldens")
    void findByPathAndStreamAgree(Golden golden) throws Exception {
        assertSame(golden.format(), ClipboardFormats.findByPath(path(golden.file())));
        assertSame(golden.format(), ClipboardFormats.findByInputStream(() -> open(golden.file())));
    }

    @Test
    void unrelatedFilesAreNotDetected(@TempDir Path dir) throws IOException {
        Path text = Files.writeString(dir.resolve("notes.schem"), "hello");
        Path empty = Files.write(dir.resolve("empty.schem"), new byte[0]);

        assertNull(ClipboardFormats.findByPath(text));
        assertNull(ClipboardFormats.findByPath(empty));
        assertNull(ClipboardFormats.findByPath(dir.resolve("missing.schem")));
        assertNull(ClipboardFormats.findByInputStream(() -> new ByteArrayInputStream(new byte[] {1, 2, 3})));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(BuiltInClipboardFormat.class)
    void everyAliasResolvesCaselessly(BuiltInClipboardFormat format) {
        for (String alias : format.getAliases()) {
            ClipboardFormat found = ClipboardFormats.findByAlias(alias.toUpperCase(Locale.ROOT));
            assertNotNull(found, alias);
            // "schem" is shared by the Sponge formats, and only the first to register keeps it
            if (!alias.equals("schem")) {
                assertSame(format, found, alias);
            }
        }
    }

    @Test
    void aliasesAreTrimmedAndUnknownOnesAreNull() {
        assertSame(BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC, ClipboardFormats.findByAlias(" sponge.3 "));
        assertSame(BuiltInClipboardFormat.MCEDIT_SCHEMATIC, ClipboardFormats.findByAlias("mcedit"));
        assertNull(ClipboardFormats.findByAlias("sponge.4"));
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedSpongeAliasPointsAtVersion2() {
        assertSame(BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC, BuiltInClipboardFormat.SPONGE_SCHEMATIC);
    }

    @Test
    void everyFormatIsRegisteredWithItsExtension() {
        assertTrue(ClipboardFormats.getAll().containsAll(Set.of(BuiltInClipboardFormat.values())));
        for (BuiltInClipboardFormat format : BuiltInClipboardFormat.values()) {
            assertTrue(format.getFileExtensions().contains(format.getPrimaryFileExtension()), format.toString());
            assertTrue(ClipboardFormats.getFileExtensionMap().get(format.getPrimaryFileExtension()).contains(format),
                format.toString());
        }
        assertTrue(new HashSet<>(Arrays.asList(ClipboardFormats.getFileExtensionArray()))
            .containsAll(Set.of("schem", "schematic")));
    }

    @Test
    void schemExtensionIsSharedByTheSpongeFormatsAndSchematicBelongsToMcEdit() {
        assertEquals(Set.of(BuiltInClipboardFormat.SPONGE_V1_SCHEMATIC, BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC,
            BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC),
            new HashSet<>(ClipboardFormats.getFileExtensionMap().get("schem")));
        assertEquals(Set.of(BuiltInClipboardFormat.MCEDIT_SCHEMATIC),
            new HashSet<>(ClipboardFormats.getFileExtensionMap().get("schematic")));
    }

    @Test
    void namesAreUniqueAndMatchTheEnum() {
        Set<String> names = new HashSet<>();
        for (BuiltInClipboardFormat format : BuiltInClipboardFormat.values()) {
            assertEquals(format.name(), format.getName());
            assertTrue(names.add(format.getName()));
            assertFalse(format.getAliases().isEmpty(), format.toString());
        }
    }
}
