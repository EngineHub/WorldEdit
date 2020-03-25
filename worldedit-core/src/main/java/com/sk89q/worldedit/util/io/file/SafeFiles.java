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

package com.sk89q.worldedit.util.io.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SafeFiles {

    /**
     * A version of {@link Files#list(Path)} that won't leak resources.
     *
     * <p>
     * Instead, it immediately consumes the entire listing into a {@link List} and
     * calls {@link List#stream()}.
     * </p>
     *
     * @param dir the directory to list
     * @return an I/O-resource-free stream of the files in the directory
     * @throws IOException if an I/O error occurs
     */
    public static Stream<Path> noLeakFileList(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.collect(Collectors.toList()).stream();
        }
    }

    /**
     * {@link Path#getFileName()} includes a slash sometimes for some reason.
     * This will get rid of it.
     *
     * @param path the path to get the file name for
     * @return the file name of the given path
     */
    public static String canonicalFileName(Path path) {
        return dropSlash(path.getFileName().toString());
    }

    private static String dropSlash(String name) {
        if (name.isEmpty() || name.codePointBefore(name.length()) != '/') {
            return name;
        }
        return name.substring(0, name.length() - 1);
    }

    private SafeFiles() {
    }
}
