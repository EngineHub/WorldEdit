package com.sk89q.worldedit.extent.clipboard.io;

import com.sk89q.jnbt.Tag;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nullable;

// note, when clearing deprecations these methods don't need to remain -- they're introduced in 7.3.0
public class SchematicNbtUtil {
    public static <T extends Tag> T requireTag(Map<String, Tag> items, String key, Class<T> expected) throws IOException {
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
    public static <T extends Tag> T getTag(Map<String, Tag> items, String key, Class<T> expected) {
        if (!items.containsKey(key)) {
            return null;
        }

        Tag test = items.get(key);
        if (!expected.isInstance(test)) {
            return null;
        }

        return expected.cast(test);
    }

    private SchematicNbtUtil() {
    }
}
