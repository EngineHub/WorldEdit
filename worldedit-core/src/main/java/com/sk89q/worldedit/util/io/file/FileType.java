package com.sk89q.worldedit.util.io.file;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.util.collection.MoreSets;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * Represents a logical file type, made of multiple extensions.
 *
 * <p>
 * This is not perfect, as it does not associate using magic numbers, but it should be good
 * enough for our purposes.
 * </p>
 */
@AutoValue
public abstract class FileType {

    /**
     * This API should not be used unless necessary for compatibility with legacy code.
     *
     * <p>
     * It will be removed in WorldEdit 8.
     * </p>
     *
     * @param extensions the legacy extensions array
     * @return a set of the extracted file types, may be empty
     */
    @Deprecated
    public static Set<FileType> adaptLegacyExtensions(String... extensions) {
        if (extensions == null || extensions.length == 0) {
            return Collections.emptySet();
        }
        return Collections.singleton(FileType.of(
            String.join(", ", extensions), extensions[0], extensions
        ));
    }

    public static FileType of(String description, String primaryExt, String... otherExt) {
        return of(description, primaryExt, Arrays.asList(otherExt));
    }

    public static FileType of(String description, String primaryExt, Iterable<String> otherExt) {
        return new AutoValue_FileType(description, primaryExt, ImmutableSet.copyOf(
            MoreSets.ensureFirst(primaryExt, otherExt)
        ));
    }

    FileType() {
    }

    public abstract String getDescription();

    public abstract String getPrimaryExtension();

    /**
     * Get the extensions associated with this file type.
     *
     * <p>
     * It is guaranteed that the {@linkplain #getPrimaryExtension() primary extension} is
     * first in the set's iteration order.
     * </p>
     *
     * @return the extensions associated with this file type
     */
    public abstract ImmutableSet<String> getExtensions();

}
