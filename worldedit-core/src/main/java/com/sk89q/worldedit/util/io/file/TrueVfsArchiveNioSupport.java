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

package com.sk89q.worldedit.util.io.file;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import net.java.truevfs.access.TArchiveDetector;
import net.java.truevfs.access.TFileSystem;
import net.java.truevfs.access.TPath;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public final class TrueVfsArchiveNioSupport implements ArchiveNioSupport {

    private static final TrueVfsArchiveNioSupport INSTANCE = new TrueVfsArchiveNioSupport();

    public static TrueVfsArchiveNioSupport getInstance() {
        return INSTANCE;
    }

    private static final Set<String> ALLOWED_EXTENSIONS = ImmutableSet.copyOf(
        Splitter.on('|').split(TArchiveDetector.ALL.getExtensions())
    );

    private TrueVfsArchiveNioSupport() {
    }

    @Override
    public Optional<ArchiveDir> tryOpenAsDir(Path archive) throws IOException {
        String fileName = archive.getFileName().toString();
        int dot = fileName.indexOf('.');
        if (dot < 0 || dot >= fileName.length() || !ALLOWED_EXTENSIONS
            .contains(fileName.substring(dot + 1))) {
            return Optional.empty();
        }
        TFileSystem fileSystem = new TPath(archive).getFileSystem();
        TPath root = fileSystem.getPath("/");
        Path realRoot = ArchiveNioSupports.skipRootSameName(
            root, fileName.substring(0, dot)
        );
        return Optional.of(new ArchiveDir() {
            @Override
            public Path getPath() {
                return realRoot;
            }

            @Override
            public void close() throws IOException {
                fileSystem.close();
            }
        });
    }
}
