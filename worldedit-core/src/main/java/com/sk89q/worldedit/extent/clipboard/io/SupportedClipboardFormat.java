/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.extent.clipboard.io;

import com.google.common.collect.ImmutableSet;
import com.sk89q.jnbt.NBTConstants;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A collection of supported clipboard formats.
 */
public enum SupportedClipboardFormat implements ClipboardFormat {

    /**
     * The Schematic format used by many software.
     */
    SCHEMATIC("mcedit", "mce", "schematic") {

        @Override
        public String getPrimaryFileExtension() {
            return "schematic";
        }

        @Override
        public ClipboardReader getReader(InputStream inputStream) throws IOException {
            NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(inputStream));
            return new SchematicReader(nbtStream);
        }

        @Override
        public ClipboardWriter getWriter(OutputStream outputStream) throws IOException {
            NBTOutputStream nbtStream = new NBTOutputStream(new GZIPOutputStream(outputStream));
            return new SchematicWriter(nbtStream);
        }

        @Override
        public boolean isFormat(File file) {
            DataInputStream str = null;
            try {
                str = new DataInputStream(new GZIPInputStream(new FileInputStream(file)));
                if ((str.readByte() & 0xFF) != NBTConstants.TYPE_COMPOUND) {
                    return false;
                }
                byte[] nameBytes = new byte[str.readShort() & 0xFFFF];
                str.readFully(nameBytes);
                String name = new String(nameBytes, NBTConstants.CHARSET);
                return name.equals("Schematic");
            } catch (IOException e) {
                return false;
            } finally {
                if (str != null) {
                    try {
                        str.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    };

    private final ImmutableSet<String> aliases;

    private SupportedClipboardFormat(String... aliases) {
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
