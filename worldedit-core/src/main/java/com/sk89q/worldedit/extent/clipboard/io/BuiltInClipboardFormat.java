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

import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.extent.clipboard.io.sponge.SpongeSchematicV1Reader;
import com.sk89q.worldedit.extent.clipboard.io.sponge.SpongeSchematicV2Reader;
import com.sk89q.worldedit.extent.clipboard.io.sponge.SpongeSchematicV2Writer;
import com.sk89q.worldedit.extent.clipboard.io.sponge.SpongeSchematicV3Reader;
import com.sk89q.worldedit.extent.clipboard.io.sponge.SpongeSchematicV3Writer;
import org.enginehub.linbus.stream.LinBinaryIO;
import org.enginehub.linbus.stream.LinReadOptions;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.enginehub.linbus.tree.LinTagType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A collection of supported clipboard formats.
 */
public enum BuiltInClipboardFormat implements ClipboardFormat {

    /**
     * The Schematic format used by MCEdit.
     */
    MCEDIT_SCHEMATIC("mcedit", "mce", "schematic") {

        @Override
        public String getPrimaryFileExtension() {
            return "schematic";
        }

        @Override
        public ClipboardReader getReader(InputStream inputStream) throws IOException {
            return new MCEditSchematicReader(LinBinaryIO.read(
                new DataInputStream(new GZIPInputStream(inputStream)),
                LEGACY_OPTIONS
            ));
        }

        @Override
        public ClipboardWriter getWriter(OutputStream outputStream) throws IOException {
            throw new IOException("This format does not support saving");
        }

        @Override
        public boolean isFormat(InputStream inputStream) {
            LinRootEntry rootEntry;
            try {
                DataInputStream stream = new DataInputStream(new GZIPInputStream(inputStream));
                rootEntry = LinBinaryIO.readUsing(stream, LEGACY_OPTIONS, LinRootEntry::readFrom);
            } catch (Exception e) {
                return false;
            }
            if (!rootEntry.name().equals("Schematic")) {
                return false;
            }
            return rootEntry.value().value().containsKey("Materials");
        }
    },
    SPONGE_V1_SCHEMATIC("sponge.1") {

        @Override
        public String getPrimaryFileExtension() {
            return "schem";
        }

        @Override
        public ClipboardReader getReader(InputStream inputStream) throws IOException {
            return new SpongeSchematicV1Reader(LinBinaryIO.read(
                new DataInputStream(new GZIPInputStream(inputStream)), LEGACY_OPTIONS
            ));
        }

        @Override
        public ClipboardWriter getWriter(OutputStream outputStream) throws IOException {
            throw new IOException("This format does not support saving");
        }

        @Override
        public boolean isFormat(InputStream inputStream) {
            return detectOldSpongeSchematic(inputStream, 1);
        }
    },
    SPONGE_V2_SCHEMATIC("sponge.2") {

        @Override
        public String getPrimaryFileExtension() {
            return "schem";
        }

        @Override
        public ClipboardReader getReader(InputStream inputStream) throws IOException {
            return new SpongeSchematicV2Reader(LinBinaryIO.read(
                new DataInputStream(new GZIPInputStream(inputStream)), LEGACY_OPTIONS
            ));
        }

        @Override
        public ClipboardWriter getWriter(OutputStream outputStream) throws IOException {
            return new SpongeSchematicV2Writer(new DataOutputStream(new GZIPOutputStream(outputStream)));
        }

        @Override
        public boolean isFormat(InputStream inputStream) {
            return detectOldSpongeSchematic(inputStream, 2);
        }
    },
    SPONGE_V3_SCHEMATIC("sponge.3", "sponge", "schem") {

        @Override
        public String getPrimaryFileExtension() {
            return "schem";
        }

        @Override
        public ClipboardReader getReader(InputStream inputStream) throws IOException {
            return new SpongeSchematicV3Reader(LinBinaryIO.read(
                new DataInputStream(new GZIPInputStream(inputStream))
            ));
        }

        @Override
        public ClipboardWriter getWriter(OutputStream outputStream) throws IOException {
            return new SpongeSchematicV3Writer(new DataOutputStream(new GZIPOutputStream(outputStream)));
        }

        @Override
        public boolean isFormat(InputStream inputStream) {
            LinCompoundTag root;
            try {
                DataInputStream stream = new DataInputStream(new GZIPInputStream(inputStream));
                root = LinBinaryIO.readUsing(stream, LinRootEntry::readFrom).value();
            } catch (Exception e) {
                return false;
            }
            LinCompoundTag schematicTag = root.findTag("Schematic", LinTagType.compoundTag());
            if (schematicTag == null) {
                return false;
            }
            LinIntTag versionTag = schematicTag.findTag("Version", LinTagType.intTag());
            if (versionTag == null) {
                return false;
            }
            return versionTag.valueAsInt() == 3;
        }
    },
    ;

    private static boolean detectOldSpongeSchematic(InputStream inputStream, int version) {
        LinRootEntry rootEntry;
        try {
            DataInputStream stream = new DataInputStream(new GZIPInputStream(inputStream));
            rootEntry = LinBinaryIO.readUsing(stream, LEGACY_OPTIONS, LinRootEntry::readFrom);
        } catch (Exception e) {
            return false;
        }
        if (!rootEntry.name().equals("Schematic")) {
            return false;
        }
        LinCompoundTag schematicTag = rootEntry.value();

        LinIntTag versionTag = schematicTag.findTag("Version", LinTagType.intTag());
        if (versionTag == null) {
            return false;
        }
        return versionTag.valueAsInt() == version;
    }

    /**
     * For backwards compatibility, this points to the Sponge Schematic Specification (Version 2)
     * format. This should not be used going forwards.
     *
     * @deprecated Use {@link #SPONGE_V2_SCHEMATIC} or {@link #SPONGE_V3_SCHEMATIC}
     */
    @Deprecated
    public static final BuiltInClipboardFormat SPONGE_SCHEMATIC = SPONGE_V2_SCHEMATIC;

    private static final LinReadOptions LEGACY_OPTIONS = LinReadOptions.builder().allowNormalUtf8Encoding(true).build();

    private final ImmutableSet<String> aliases;

    BuiltInClipboardFormat(String... aliases) {
        this.aliases = ImmutableSet.copyOf(aliases);
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public Set<String> getAliases() {
        return this.aliases;
    }

    @Override
    public Set<String> getFileExtensions() {
        return ImmutableSet.of(getPrimaryFileExtension());
    }

}
