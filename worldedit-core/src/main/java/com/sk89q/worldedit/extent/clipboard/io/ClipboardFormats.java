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

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.io.file.FileType;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class ClipboardFormats {

    private static final Map<String, ClipboardFormat> aliasMap = new HashMap<>();
    private static final Map<String, ClipboardFormat> fileExtensionMap = new HashMap<>();
    private static final List<ClipboardFormat> registeredFormats = new ArrayList<>();

    public static void registerClipboardFormat(ClipboardFormat format) {
        checkNotNull(format);

        for (String key : format.getAliases()) {
            String lowKey = key.toLowerCase(Locale.ENGLISH);
            ClipboardFormat old = aliasMap.put(lowKey, format);
            if (old != null) {
                aliasMap.put(lowKey, old);
                WorldEdit.logger.warn(format.getClass().getName() + " cannot override existing alias '" + lowKey + "' used by " + old.getClass().getName());
            }
        }
        for (String ext : format.getFileExtensions()) {
            String lowExt = ext.toLowerCase(Locale.ENGLISH);
            ClipboardFormat old = fileExtensionMap.put(lowExt, format);
            if (old != null) {
                aliasMap.put(lowExt, old);
                WorldEdit.logger.warn(format.getClass().getName() + " cannot override existing file extension '" + lowExt + "' used by " + old.getClass().getName());
            }
        }
        registeredFormats.add(format);
    }

    static {
        for (BuiltInClipboardFormat format : BuiltInClipboardFormat.values()) {
            registerClipboardFormat(format);
        }
    }

    /**
     * Find the clipboard format named by the given alias.
     *
     * @param alias
     *            the alias
     * @return the format, otherwise null if none is matched
     */
    @Nullable
    public static ClipboardFormat findByAlias(String alias) {
        checkNotNull(alias);
        return aliasMap.get(alias.toLowerCase(Locale.ENGLISH).trim());
    }

    /**
     * Detect the format of given a file.
     *
     * @param file
     *            the file
     * @return the format, otherwise null if one cannot be detected
     * @deprecated Use {@link #findByPath(Path)} instead
     */
    @Deprecated
    @Nullable
    public static ClipboardFormat findByFile(File file) {
        return findByPath(file.toPath());
    }

    /**
     * Detect the format of given a path.
     *
     * @param path the path
     * @return the format, otherwise null if one cannot be detected
     */
    @Nullable
    public static ClipboardFormat findByPath(Path path) {
        checkNotNull(path);

        for (ClipboardFormat format : registeredFormats) {
            if (format.isFormat(path)) {
                return format;
            }
        }

        return null;
    }

    /**
     * A mapping from extensions to formats.
     *
     * @return a multimap from a file extension to the potential matching formats.
     * @deprecated the file extension is now a 1-to-1 mapping, use {@link #getFileExtensions()}
     */
    @Deprecated
    public static Multimap<String, ClipboardFormat> getFileExtensionMap() {
        return Multimaps.forMap(fileExtensionMap);
    }

    /**
     * A mapping from extensions to formats.
     *
     * @return a map from a file extension to the potential matching format
     */
    public static Map<String, ClipboardFormat> getFileExtensions() {
        return Collections.unmodifiableMap(fileExtensionMap);
    }

    /**
     * Get the file types of registered formats.
     *
     * @return the file types corresponding to registered formats
     */
    public static Set<FileType> getFileTypes() {
        return registeredFormats.stream()
            .map(ClipboardFormat::getFileType)
            .collect(Collectors.toSet());
    }

    public static Collection<ClipboardFormat> getAll() {
        return Collections.unmodifiableCollection(registeredFormats);
    }

    private ClipboardFormats() {
    }

}
