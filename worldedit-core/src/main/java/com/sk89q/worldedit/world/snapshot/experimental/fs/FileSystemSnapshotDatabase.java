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

package com.sk89q.worldedit.world.snapshot.experimental.fs;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.function.IORunnable;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.io.file.ArchiveNioSupports;
import com.sk89q.worldedit.util.io.file.MorePaths;
import com.sk89q.worldedit.util.time.FileNameDateTimeParser;
import com.sk89q.worldedit.util.time.ModificationDateTimeParser;
import com.sk89q.worldedit.util.time.SnapshotDateTimeParser;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotDatabase;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotInfo;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Implements a snapshot database based on a filesystem.
 */
public class FileSystemSnapshotDatabase implements SnapshotDatabase {

    private static final List<SnapshotDateTimeParser> DATE_TIME_PARSERS =
        new ImmutableList.Builder<SnapshotDateTimeParser>()
            .add(FileNameDateTimeParser.getInstance())
            .addAll(ServiceLoader.load(SnapshotDateTimeParser.class))
            .add(ModificationDateTimeParser.getInstance())
            .build();

    public static ZonedDateTime tryParseDate(Path path) {
        return DATE_TIME_PARSERS.stream()
            .map(parser -> parser.detectDateTime(path))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Could not detect date of " + path));
    }

    public static FileSystemSnapshotDatabase maybeCreate(Path root) throws IOException {
        Files.createDirectories(root);
        return new FileSystemSnapshotDatabase(root);
    }

    private final Path root;

    public FileSystemSnapshotDatabase(Path root) {
        checkArgument(Files.isDirectory(root), "Database root is not a directory");
        this.root = root.toAbsolutePath();
    }

    private SnapshotInfo createSnapshotInfo(Path fullPath, Path realPath) {
        return SnapshotInfo.create(MorePaths.toRelativeUri(fullPath), tryParseDate(realPath));
    }

    private Snapshot createSnapshot(Path fullPath, Path realPath, @Nullable IORunnable closeCallback) {
        return new FolderSnapshot(
            createSnapshotInfo(fullPath, realPath), realPath, closeCallback
        );
    }

    public Path getRoot() {
        return root;
    }

    @Override
    public Optional<Snapshot> getSnapshot(URI name) throws IOException {
        if (!name.getScheme().equals("file")) {
            return Optional.empty();
        }
        Path rawResolved = root.resolve(name.getPath());
        if (!Files.exists(rawResolved)) {
            return Optional.empty();
        }
        // Catch trickery with paths:
        Path realPath = rawResolved.toRealPath();
        if (!realPath.startsWith(root)) {
            return Optional.empty();
        }
        Optional<Snapshot> result = tryRegularFileSnapshot(root.relativize(realPath), realPath);
        if (result.isPresent()) {
            return result;
        }
        if (!Files.isDirectory(realPath)) {
            return Optional.empty();
        }
        return Optional.of(createSnapshot(root.relativize(realPath), realPath, null));
    }

    private Optional<Snapshot> tryRegularFileSnapshot(Path fullPath, Path realPath) throws IOException {
        Closer closer = Closer.create();
        Path relative = root.relativize(realPath);
        Iterator<Path> iterator = null;
        try {
            while (true) {
                if (iterator == null) {
                    iterator = MorePaths.iterParents(relative).iterator();
                }
                if (!iterator.hasNext()) {
                    return Optional.empty();
                }
                Path next = iterator.next();
                if (!Files.isRegularFile(next)) {
                    // This will never be it.
                    continue;
                }
                Optional<FileSystem> fs = ArchiveNioSupports.tryOpenAsDir(next);
                if (fs.isPresent()) {
                    closer.register(fs.get());
                    // Switch path to path inside the archive
                    relative = fs.get().getPath(next.relativize(relative).toString());
                    iterator = null;
                    // Check if it exists, if so open snapshot
                    if (Files.exists(relative)) {
                        return Optional.of(createSnapshot(fullPath, relative, closer::close));
                    }
                    // Otherwise, we may have more archives to open.
                    // Keep searching!
                }
            }
        } catch (Throwable t) {
            throw closer.rethrowAndClose(t);
        }
    }

    @Override
    public Stream<Snapshot> getSnapshots(String worldName) throws IOException {
        /*
         There are a few possible snapshot formats we accept:
           - a world directory, identified by <worldName>/level.dat
           - a world archive, identified by <worldName>.ext
             * does not need to have level.dat inside
           - a timestamped directory, identified by <stamp>, that can have
             - the two world formats described above, inside the directory
           - a timestamped archive, identified by <stamp>.ext, that can have
             - the same as timestamped directory, but inside the archive.
           - a directory with the world name, but no level.dat
             - inside must be timestamped directory/archive, with the world inside that

           All archives may have a root directory with the same name as the archive,
           minus the extensions. Due to extension detection methods, this won't work properly
           with some files, e.g. world.qux.zip/world.qux is invalid, but world.qux.zip/world isn't.
         */
        return Stream.of(
            listWorldEntries(Paths.get(""), root, worldName),
            listTimestampedEntries(Paths.get(""), root, worldName)
        ).flatMap(Function.identity());
    }

    private Stream<Snapshot> listWorldEntries(Path fullPath, Path root, String worldName) throws IOException {
        WorldEdit.logger.info("World check in: {}", root);
        return Files.list(root)
            .flatMap(candidate -> {
                WorldEdit.logger.info("World trying: {}", candidate);
                // Try world directory
                if (candidate.getFileName().toString().equalsIgnoreCase(worldName)) {
                    // Direct
                    if (Files.exists(candidate.resolve("level.dat"))) {
                        WorldEdit.logger.info("Direct!");
                        return Stream.of(createSnapshot(
                            fullPath.resolve(candidate.getFileName()), candidate, null
                        ));
                    }
                    // Container for time-stamped entries
                    try {
                        return listTimestampedEntries(
                            fullPath.resolve(candidate.getFileName()), candidate, worldName
                        );
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                // Try world archive
                if (Files.isRegularFile(candidate) && candidate.startsWith(worldName + ".")) {
                    WorldEdit.logger.info("Archive!");
                    try {
                        return tryRegularFileSnapshot(
                            fullPath.resolve(candidate.getFileName()), candidate
                        ).map(Stream::of).orElse(null);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                WorldEdit.logger.info("Nothing!");
                return null;
            });
    }

    private Stream<Snapshot> listTimestampedEntries(Path fullPath, Path root, String worldName) throws IOException {
        WorldEdit.logger.info("Timestamp check in: {}", root);
        return Files.list(root)
            .filter(candidate -> {
                ZonedDateTime date = FileNameDateTimeParser.getInstance().detectDateTime(candidate);
                return date != null;
            })
            .flatMap(candidate -> {
                WorldEdit.logger.info("Timestamp trying: {}", candidate);
                // Try timestamped directory
                if (Files.isDirectory(candidate)) {
                    WorldEdit.logger.info("Timestamped directory");
                    try {
                        return listWorldEntries(
                            fullPath.resolve(candidate.getFileName()), candidate, worldName
                        );
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                // Otherwise archive, get it as a directory & unpack it
                try {
                    Optional<FileSystem> fs = ArchiveNioSupports.tryOpenAsDir(candidate);
                    if (!fs.isPresent()) {
                        WorldEdit.logger.info("Nothing!");
                        return null;
                    }
                    WorldEdit.logger.info("Timestamped archive!");
                    return listWorldEntries(
                        fullPath.resolve(candidate.getFileName()),
                        fs.get().getPath("/"),
                        worldName
                    );
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
    }

}
