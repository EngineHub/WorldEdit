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

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public class ArchiveNioSupports {

    private static final List<ArchiveNioSupport> SUPPORTS;

    static {
        ImmutableList.Builder<ArchiveNioSupport> builder = ImmutableList.builder();
        try {
            builder.add(TrueVfsArchiveNioSupport.getInstance());
        } catch (NoClassDefFoundError ignore) {
            // No TrueVFS available. That's OK.
        }
        SUPPORTS = builder.add(ZipArchiveNioSupport.getInstance())
            .addAll(ServiceLoader.load(ArchiveNioSupport.class))
            .build();
    }

    public static Optional<Path> tryOpenAsDir(Path archive) throws IOException {
        for (ArchiveNioSupport support : SUPPORTS) {
            Optional<Path> fs = support.tryOpenAsDir(archive);
            if (fs.isPresent()) {
                return fs;
            }
        }
        return Optional.empty();
    }

    private static final ArchiveNioSupport COMBINED = ArchiveNioSupports::tryOpenAsDir;

    /**
     * Get an {@link ArchiveNioSupport} that combines all known instances.
     * @return a combined {@link ArchiveNioSupport} instance
     */
    public static ArchiveNioSupport combined() {
        return COMBINED;
    }

    /**
     * If root contains a folder with the same name as {@code name}, and no regular files,
     * returns the path to that folder. Otherwise, return the root path.
     *
     * <p>
     * This method is used to provide equal outputs for archives that do and do not contain
     * their name as part of their root folder.
     * </p>
     *
     * @param root the root path
     * @param name the name that might exist inside root
     * @return the corrected path
     */
    public static Path skipRootSameName(Path root, String name) throws IOException {
        Path innerDir = root.resolve(name);
        if (Files.isDirectory(innerDir)) {
            try (Stream<Path> files = Files.list(root)) {
                // The reason we check this, is that macOS creates a __MACOSX directory inside
                // its zip files. We want to allow this to pass if that exists, or a similar
                // mechanism, but fail if there are regular files, since that indicates that
                // it may not be the right thing to do.
                if (files.allMatch(Files::isDirectory)) {
                    return innerDir;
                }
            }
        }
        return root;
    }

    private ArchiveNioSupports() {
    }
}
