package com.sk89q.worldedit.extent.clipboard.io;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Base class for NBT schematic readers
 */
public abstract class NBTSchematicReader implements ClipboardReader {

    protected static <T extends Tag> T requireTag(Map<String, Tag> items, String key, Class<T> expected) throws IOException {
        if (!items.containsKey(key)) {
            throw new IOException("Schematic file is missing a \"" + key + "\" tag");
        }

        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IOException(key + " tag is not of tag type " + expected.getName());
        }

        return expected.cast(tag);
    }

    @Nullable
    protected static <T extends Tag> T getTag(CompoundTag tag, Class<T> expected, String key) {
        Map<String, Tag> items = tag.getValue();

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
