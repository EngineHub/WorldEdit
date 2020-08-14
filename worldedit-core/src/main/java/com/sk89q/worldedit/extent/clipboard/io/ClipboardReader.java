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

import java.io.Closeable;
import java.io.IOException;
import java.util.OptionalInt;

/**
 * Reads {@code Clipboard}s.
 *
 * @see Clipboard
 */
public interface ClipboardReader extends Closeable {

    /**
     * Read a {@code Clipboard}.
     *
     * @return the read clipboard
     * @throws IOException thrown on I/O error
     */
    Clipboard read() throws IOException;

    /**
     * Get the DataVersion from a file (if possible).
     *
     * @return The data version, or empty
     */
    default OptionalInt getDataVersion() {
        return OptionalInt.empty();
    }
}
