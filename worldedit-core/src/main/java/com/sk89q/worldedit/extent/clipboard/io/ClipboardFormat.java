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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * A collection of supported clipboard formats.
 */
public interface ClipboardFormat {

    /**
     * Returns the name of this format.
     *
     * @return The name of the format
     */
    String getName();

    /**
     * Get a set of aliases.
     *
     * @return a set of aliases
     */
    Set<String> getAliases();

    /**
     * Create a reader.
     *
     * @param inputStream the input stream
     * @return a reader
     * @throws IOException thrown on I/O error
     */
    ClipboardReader getReader(InputStream inputStream) throws IOException;

    /**
     * Create a writer.
     *
     * @param outputStream the output stream
     * @return a writer
     * @throws IOException thrown on I/O error
     */
    ClipboardWriter getWriter(OutputStream outputStream) throws IOException;

    /**
     * Return whether the given file is of this format.
     *
     * @param file the file
     * @return true if the given file is of this format
     */
    boolean isFormat(File file);

    /**
     * Get the file extension this format primarily uses.
     *
     * @return The primary file extension
     */
    String getPrimaryFileExtension();

    /**
     * Get the file extensions this format is commonly known to use. This should
     * include {@link #getPrimaryFileExtension()}.
     *
     * @return The file extensions this format might be known by
     */
    Set<String> getFileExtensions();
}
