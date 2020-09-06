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

import com.sk89q.jnbt.Tag;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Base class for NBT schematic readers.
 */
public abstract class NBTSchematicReader implements ClipboardReader {

    protected static <T extends Tag> T requireTag(Map<String, Tag> items, String key, Class<T> expected) throws IOException {
        if (!items.containsKey(key)) {
            throw new IOException("Schematic file is missing a \"" + key + "\" tag of type "
                + expected.getName());
        }

        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IOException(key + " tag is not of tag type " + expected.getName() + ", got "
                + tag.getClass().getName() + " instead");
        }

        return expected.cast(tag);
    }

    @Nullable
    protected static <T extends Tag> T getTag(Map<String, Tag> items, String key, Class<T> expected) {
        if (!items.containsKey(key)) {
            return null;
        }

        Tag test = items.get(key);
        if (!expected.isInstance(test)) {
            return null;
        }

        return expected.cast(test);
    }
}
