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
import com.google.common.net.UrlEscapers;
import com.sk89q.worldedit.util.function.IORunnable;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.io.file.ArchiveNioSupport;
import com.sk89q.worldedit.util.io.file.MorePaths;
import com.sk89q.worldedit.util.time.FileNameDateTimeParser;
import com.sk89q.worldedit.util.time.ModificationDateTimeParser;
import com.sk89q.worldedit.util.time.SnapshotDateTimeParser;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotDatabase;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileSystems;
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

    private static final Logger logger = LoggerFactory.getLogger(FileSystemSnapshotDatabase.class);

    private static final String SCHEME = "snapfs";

    private static final List<SnapshotDateTimeParser> DATE_TIME_PARSERS =
        new ImmutableList.Builder<SnapshotDateTimeParser>()
            .add(FileNameDateTimeParser.getInstance())
            .addAll(ServiceLoader.load(SnapshotDateTimeParser.class))
            .add(ModificationDateTimeParser.getInstance())
            .build();

    public static ZonedDateTime tryParseDate(Path path) {
        return tryParseDateInternal(path)
            .orElseThrow(() -> new IllegalStateException("Could not detect date of " + path));
    }

    private static Optional<ZonedDateTime> tryParseDateInternal(Path path) {
        return DATE_TIME_PARSERS.stream()
            .map(parser -> parser.detectDateTime(path))
            .filter(Objects::nonNull)
            .findFirst();
    }

    public static URI createUri(String name) {
        return URI.create(SCHEME + ":" + UrlEscapers.urlFragmentEscaper().escape(name));
    }

    public static FileSystemSnapshotDatabase maybeCreate(
        Path root,
        ArchiveNioSupport archiveNioSupport
    ) throws IOException {
        Files.createDirectories(root);
        return new FileSystemSnapshotDatabase(root, archiveNioSupport);
    }

    private final Path root;
    private final ArchiveNioSupport archiveNioSupport;

    public FileSystemSnapshotDatabase(Path root, ArchiveNioSupport archiveNioSupport) {
        checkArgument(Files.isDirectory(root), "Database root is not a directory");
        this.root = root.toAbsolutePath();
        this.archiveNioSupport = archiveNioSupport;
    }

    private SnapshotInfo createSnapshotInfo(Path fullPath, Path realPath) {
        // Try full for parsing out of file name, real for parsing mod time.
        ZonedDateTime date = tryParseDateInternal(fullPath).orElseGet(() -> tryParseDate(realPath));
        return SnapshotInfo.create(createUri(fullPath.toString()), date);
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
    public String getScheme() {
        return SCHEME;
    }

    @Override
    public Optional<Snapshot> getSnapshot(URI name) throws IOException {
        if (!name.getScheme().equals(SCHEME)) {
            return Optional.empty();
        }
        // drop the / in the path to make it absolute
        Path rawResolved = root.resolve(name.getSchemeSpecificPart());
        // Catch trickery with paths:
        Path realPath = rawResolved.normalize();
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
        Path root = this.root;
        Path relative = root.relativize(realPath);
        Iterator<Path> iterator = null;
        try {
            while (true) {
                if (iterator == null) {
                    iterator = MorePaths.iterPaths(relative).iterator();
                }
                if (!iterator.hasNext()) {
                    return Optional.empty();
                }
                Path relativeNext = iterator.next();
                Path next = root.resolve(relativeNext);
                if (!Files.isRegularFile(next)) {
                    // This will never be it.
                    continue;
                }
                Optional<Path> newRootOpt = archiveNioSupport.tryOpenAsDir(next);
                if (newRootOpt.isPresent()) {
                    root = newRootOpt.get();
                    if (root.getFileSystem() != FileSystems.getDefault()) {
                        closer.register(root.getFileSystem());
                    }
                    // Switch path to path inside the archive
                    relative = root.resolve(relativeNext.relativize(relative).toString());
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
        logger.debug("World check in: {}", root);
        return Files.list(root)
            .flatMap(candidate -> {
                logger.debug("World trying: {}", candidate);
                // Try world directory
                String fileName = candidate.getFileName().toString();
                if (isSameDirectoryName(fileName, worldName)) {
                    // Direct
                    if (Files.exists(candidate.resolve("level.dat"))) {
                        logger.debug("Direct!");
                        return Stream.of(createSnapshot(
                            fullPath.resolve(fileName), candidate, null
                        ));
                    }
                    // Container for time-stamped entries
                    try {
                        return listTimestampedEntries(
                            fullPath.resolve(fileName), candidate, worldName
                        );
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                // Try world archive
                if (Files.isRegularFile(candidate)
                    && fileName.startsWith(worldName + ".")) {
                    logger.debug("Archive!");
                    try {
                        return tryRegularFileSnapshot(
                            fullPath.resolve(fileName), candidate
                        ).map(Stream::of).orElse(null);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                logger.debug("Nothing!");
                return null;
            });
    }

    private boolean isSameDirectoryName(String fileName, String worldName) {
        if (fileName.lastIndexOf('/') == fileName.length() - 1) {
            fileName = fileName.substring(0, fileName.length() - 1);
        }
        return fileName.equalsIgnoreCase(worldName);
    }

    private Stream<Snapshot> listTimestampedEntries(Path fullPath, Path root, String worldName) throws IOException {
        logger.debug("Timestamp check in: {}", root);
        return Files.list(root)
            .filter(candidate -> {
                ZonedDateTime date = FileNameDateTimeParser.getInstance().detectDateTime(candidate);
                return date != null;
            })
            .flatMap(candidate -> {
                logger.debug("Timestamp trying: {}", candidate);
                // Try timestamped directory
                if (Files.isDirectory(candidate)) {
                    logger.debug("Timestamped directory");
                    try {
                        return listWorldEntries(
                            fullPath.resolve(candidate.getFileName().toString()), candidate, worldName
                        );
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                // Otherwise archive, get it as a directory & unpack it
                try {
                    Optional<Path> newRoot = archiveNioSupport.tryOpenAsDir(candidate);
                    if (!newRoot.isPresent()) {
                        logger.debug("Nothing!");
                        return null;
                    }
                    logger.debug("Timestamped archive!");
                    return listWorldEntries(
                        fullPath.resolve(candidate.getFileName().toString()),
                        newRoot.get(),
                        worldName
                    );
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
    }

}
