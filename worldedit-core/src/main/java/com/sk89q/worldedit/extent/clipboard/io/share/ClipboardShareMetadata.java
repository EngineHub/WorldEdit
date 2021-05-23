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

package com.sk89q.worldedit.extent.clipboard.io.share;

import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;

/**
 * Items of metadata about shared clipboards.
 */
public class ClipboardShareMetadata {
    private final ClipboardFormat format;
    private final String name;
    private final String author;

    public ClipboardShareMetadata(ClipboardFormat format, String name, String author) {
        this.format = format;
        this.name = name;
        this.author = author;
    }

    public ClipboardFormat format() {
        return this.format;
    }

    public String name() {
        return this.name;
    }

    public String author() {
        return this.author;
    }
}
