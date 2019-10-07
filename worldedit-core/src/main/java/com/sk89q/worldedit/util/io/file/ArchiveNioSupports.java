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
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public class ArchiveNioSupports {

    private static final List<ArchiveNioSupport> SUPPORTS;

    static {
        ImmutableList.Builder<ArchiveNioSupport> builder = ImmutableList.builder();
        try {
            builder.add(TrueZipArchiveNioSupport.getInstance());
        } catch (NoClassDefFoundError ignore) {
            // No TrueVFS available. That's OK.
        }
        SUPPORTS = builder.add(ZipArchiveNioSupport.getInstance())
            .addAll(ServiceLoader.load(ArchiveNioSupport.class))
            .build();
    }

    public static Optional<FileSystem> tryOpenAsDir(Path archive) throws IOException {
        for (ArchiveNioSupport support : SUPPORTS) {
            Optional<FileSystem> fs = support.tryOpenAsDir(archive);
            if (fs.isPresent()) {
                return fs;
            }
        }
        return Optional.empty();
    }

    private ArchiveNioSupports() {
    }
}
