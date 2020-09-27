package com.sk89q.worldedit.util.io.file;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.util.collection.SetWithDefault;

import java.util.Arrays;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

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
    public static SetWithDefault<FileType> adaptLegacyExtensions(@Nullable String defaultExt,
                                                                 @Nullable String... extensions) {
        SetWithDefault<String> extensionsSet = SetWithDefault.of(
            defaultExt,
            extensions == null ? ImmutableSet.of() : Arrays.asList(extensions)
        );
        if (extensionsSet.defaultValue() == null) {
            return SetWithDefault.empty();
        }
        return SetWithDefault.of(FileType.of(
            String.join(", ", extensionsSet.values()), extensionsSet
        ));
    }

    public static FileType of(String description, String primaryExt, String... otherExt) {
        return of(description, SetWithDefault.of(primaryExt, Arrays.asList(otherExt)));
    }

    public static FileType of(String description, SetWithDefault<String> extensions) {
        checkArgument(extensions.defaultValue() != null, "Cannot provide an empty extension set");
        return new AutoValue_FileType(description, extensions);
    }

    FileType() {
    }

    public abstract String getDescription();

    /**
     * Get the extensions for this file type. Guaranteed to be non-empty.
     *
     * @return the extensions
     */
    public abstract SetWithDefault<String> getExtensions();

}
