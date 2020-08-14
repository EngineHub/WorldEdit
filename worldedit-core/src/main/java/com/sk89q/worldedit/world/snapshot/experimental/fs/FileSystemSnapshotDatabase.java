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

package com.sk89q.worldedit.world.snapshot.experimental.fs;

import com.google.common.collect.ImmutableList;
import com.google.common.net.UrlEscapers;
import com.sk89q.worldedit.util.function.IOFunction;
import com.sk89q.worldedit.util.function.IORunnable;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.io.file.ArchiveDir;
import com.sk89q.worldedit.util.io.file.ArchiveNioSupport;
import com.sk89q.worldedit.util.io.file.MorePaths;
import com.sk89q.worldedit.util.io.file.SafeFiles;
import com.sk89q.worldedit.util.time.FileNameDateTimeParser;
import com.sk89q.worldedit.util.time.ModificationDateTimeParser;
import com.sk89q.worldedit.util.time.SnapshotDateTimeParser;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotDatabase;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotInfo;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Implements a snapshot database based on a filesystem.
 */
public class FileSystemSnapshotDatabase implements SnapshotDatabase {

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
        try {
            this.root = root.toRealPath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to resolve snapshot database path", e);
        }
        this.archiveNioSupport = archiveNioSupport;
    }

    /*
     * When this code says "idPath" it is the path that uniquely identifies that snapshot.
     * A snapshot can be looked up by its idPath.
     *
     * When the code says "ioPath" it is the path that holds the world data, and can actually
     * be read from proper. The "idPath" may not even exist, it is purely for the path components
     * and not for IO.
     */

    private SnapshotInfo createSnapshotInfo(Path idPath, Path ioPath) {
        // Try ID for parsing out of file name, IO for parsing mod time.
        ZonedDateTime date = tryParseDateInternal(idPath).orElseGet(() -> tryParseDate(ioPath));
        return SnapshotInfo.create(createUri(idPath.toString()), date);
    }

    private Snapshot createSnapshot(Path idPath, Path ioPath, @Nullable Closer closeCallback) {
        return new FolderSnapshot(
            createSnapshotInfo(idPath, ioPath), ioPath, closeCallback
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
        return getSnapshot(name.getSchemeSpecificPart());
    }

    private Optional<Snapshot> getSnapshot(String id) throws IOException {
        Path rawResolved = root.resolve(id);
        // Catch trickery with paths:
        Path ioPath = rawResolved.normalize();
        if (!ioPath.startsWith(root)) {
            return Optional.empty();
        }
        Path idPath = root.relativize(ioPath);
        Optional<Snapshot> result = tryRegularFileSnapshot(idPath);
        if (result.isPresent()) {
            return result;
        }
        if (!Files.isDirectory(ioPath)) {
            return Optional.empty();
        }
        return Optional.of(createSnapshot(idPath, ioPath, null));
    }

    private Optional<Snapshot> tryRegularFileSnapshot(Path idPath) throws IOException {
        Closer closer = Closer.create();
        Path root = this.root;
        Path relative = idPath;
        Iterator<Path> iterator = null;
        try {
            while (true) {
                if (iterator == null) {
                    iterator = MorePaths.iterPaths(relative).iterator();
                }
                if (!iterator.hasNext()) {
                    closer.close();
                    return Optional.empty();
                }
                Path relativeNext = iterator.next();
                Path next = root.resolve(relativeNext);
                if (!Files.isRegularFile(next)) {
                    // This will never be it.
                    continue;
                }
                Optional<ArchiveDir> newRootOpt = archiveNioSupport.tryOpenAsDir(next);
                if (newRootOpt.isPresent()) {
                    ArchiveDir archiveDir = newRootOpt.get();
                    root = archiveDir.getPath();
                    closer.register(archiveDir);
                    // Switch path to path inside the archive
                    relative = root.resolve(relativeNext.relativize(relative).toString());
                    iterator = null;
                    // Check if it exists, if so open snapshot
                    if (Files.exists(relative)) {
                        return Optional.of(createSnapshot(idPath, relative, closer));
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
           - a directory with the world name, but no level.dat
             - inside must be a timestamped directory/archive, which then has one of the two world
               formats inside of it!
           - a world archive, identified by <worldName>.ext
             * does not need to have level.dat inside
           - a timestamped directory, identified by <stamp>, that can have
             - the two world formats described above, inside the directory
           - a timestamped archive, identified by <stamp>.ext, that can have
             - the same as timestamped directory, but inside the archive.

           All archives may have a root directory with the same name as the archive,
           minus the extensions. Due to extension detection methods, this won't work properly
           with some files, e.g. world.qux.zip/world.qux is invalid, but world.qux.zip/world isn't.
         */
        return SafeFiles.noLeakFileList(root)
            .flatMap(IOFunction.unchecked(entry -> {
                String worldEntry = getWorldEntry(worldName, entry);
                if (worldEntry != null) {
                    return Stream.of(worldEntry);
                }
                String fileName = SafeFiles.canonicalFileName(entry);
                if (fileName.equals(worldName)
                    && Files.isDirectory(entry)
                    && !Files.exists(entry.resolve("level.dat"))) {
                    // world dir with timestamp entries
                    return listTimestampedEntries(worldName, entry)
                        .map(id -> worldName + "/" + id);
                }
                return getTimestampedEntries(worldName, entry);
            }))
            .map(IOFunction.unchecked(id ->
                getSnapshot(id)
                    .orElseThrow(() ->
                        new AssertionError("Could not find discovered snapshot: " + id)
                    )
            ));
    }

    private Stream<String> listTimestampedEntries(String worldName, Path directory) throws IOException {
        return SafeFiles.noLeakFileList(directory)
            .flatMap(IOFunction.unchecked(entry -> getTimestampedEntries(worldName, entry)));
    }

    private Stream<String> getTimestampedEntries(String worldName, Path entry) throws IOException {
        ZonedDateTime dateTime = FileNameDateTimeParser.getInstance().detectDateTime(entry);
        if (dateTime == null) {
            // nothing available at this path
            return Stream.of();
        }
        String fileName = SafeFiles.canonicalFileName(entry);
        if (Files.isDirectory(entry)) {
            // timestamped directory, find worlds inside
            return listWorldEntries(worldName, entry)
                .map(id -> fileName + "/" + id);
        }
        if (!Files.isRegularFile(entry)) {
            // not an archive either?
            return Stream.of();
        }
        Optional<ArchiveDir> asArchive = archiveNioSupport.tryOpenAsDir(entry);
        if (asArchive.isPresent()) {
            // timestamped archive
            ArchiveDir dir = asArchive.get();
            return listWorldEntries(worldName, dir.getPath())
                .map(id -> fileName + "/" + id)
                .onClose(IORunnable.unchecked(dir::close));
        }
        return Stream.of();
    }

    private Stream<String> listWorldEntries(String worldName, Path directory) throws IOException {
        return SafeFiles.noLeakFileList(directory)
            .map(IOFunction.unchecked(entry -> getWorldEntry(worldName, entry)))
            .filter(Objects::nonNull);
    }

    private String getWorldEntry(String worldName, Path entry) throws IOException {
        String fileName = SafeFiles.canonicalFileName(entry);
        if (fileName.equals(worldName) && Files.exists(entry.resolve("level.dat"))) {
            // world directory
            return worldName;
        }
        if (fileName.startsWith(worldName + ".") && Files.isRegularFile(entry)) {
            Optional<ArchiveDir> asArchive = archiveNioSupport.tryOpenAsDir(entry);
            if (asArchive.isPresent()) {
                // world archive
                asArchive.get().close();
                return fileName;
            }
        }
        return null;
    }

}
