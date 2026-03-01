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
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ZipArchiveNioSupport implements ArchiveNioSupport {

    private static final ZipArchiveNioSupport INSTANCE = new ZipArchiveNioSupport();

    private static final Map<Path, RefCountedFs> OPEN_ZIPS = new ConcurrentHashMap<>();

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
        Path key = archive.toAbsolutePath().normalize();
        RefCountedFs ref;
        try {
            ref = OPEN_ZIPS.compute(key, (__, existing) -> {
                if (existing != null && existing.fileSystem.isOpen()) {
                    existing.refCount.incrementAndGet();
                    return existing;
                }
                try {
                    FileSystem fs = openZipFileSystem(archive);
                    return new RefCountedFs(fs, new AtomicInteger(1));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        Path root = ArchiveNioSupports.skipRootSameName(
            ref.fileSystem.getPath("/"), archive.getFileName().toString()
                .replaceFirst("\\.zip$", "")
        );
        return Optional.of(new ArchiveDir() {
            @Override
            public Path getPath() {
                return root;
            }

            @Override
            public void close() throws IOException {
                closeRef(key, ref);
            }
        });
    }

    private static FileSystem openZipFileSystem(Path archive) throws IOException {
        try {
            return FileSystems.newFileSystem(archive, ZipArchiveNioSupport.class.getClassLoader());
        } catch (FileSystemAlreadyExistsException ex) {
            try {
                return FileSystems.getFileSystem(URI.create("jar:" + archive.toUri()));
            } catch (FileSystemNotFoundException notFound) {
                throw ex;
            }
        }
    }

    private static void closeRef(Path key, RefCountedFs ref) throws IOException {
        if (ref.refCount.decrementAndGet() == 0) {
            OPEN_ZIPS.remove(key, ref);
            ref.fileSystem.close();
        }
    }

    private record RefCountedFs(FileSystem fileSystem, AtomicInteger refCount) {
    }
}
