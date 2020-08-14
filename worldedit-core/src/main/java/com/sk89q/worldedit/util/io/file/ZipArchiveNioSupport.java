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

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;

public final class ZipArchiveNioSupport implements ArchiveNioSupport {

    private static final ZipArchiveNioSupport INSTANCE = new ZipArchiveNioSupport();

    public static ZipArchiveNioSupport getInstance() {
        return INSTANCE;
    }

    private ZipArchiveNioSupport() {
    }

    @Override
    public Optional<ArchiveDir> tryOpenAsDir(Path archive) throws IOException {
        if (!archive.getFileName().toString().endsWith(".zip")) {
            return Optional.empty();
        }
        FileSystem zipFs = FileSystems.newFileSystem(
            archive, getClass().getClassLoader()
        );
        Path root = ArchiveNioSupports.skipRootSameName(
            zipFs.getPath("/"), archive.getFileName().toString()
                .replaceFirst("\\.zip$", "")
        );
        return Optional.of(new ArchiveDir() {
            @Override
            public Path getPath() {
                return root;
            }

            @Override
            public void close() throws IOException {
                zipFs.close();
            }
        });
    }

}
