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
import com.sk89q.worldedit.util.function.IORunnable;
import com.sk89q.worldedit.util.io.file.ArchiveNioSupports;
import com.sk89q.worldedit.util.time.FileNameDateTimeParser;
import com.sk89q.worldedit.util.time.ModificationDateTimeParser;
import com.sk89q.worldedit.util.time.SnapshotDateTimeParser;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotDatabase;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotInfo;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotName;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
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

    private SnapshotInfo createSnapshotInfo(Path snapshotFile) {
        SnapshotName name = new FileSystemSnapshotName(root.relativize(snapshotFile), null);
        return SnapshotInfo.create(name, tryParseDate(snapshotFile));
    }

    private Snapshot createSnapshot(Path snapshotFile, @Nullable IORunnable closeCallback) {
        return new FolderSnapshot(
            createSnapshotInfo(snapshotFile), snapshotFile, closeCallback
        );
    }

    @Override
    public Optional<Snapshot> getSnapshot(SnapshotName name) throws IOException {
        if (!(name instanceof FileSystemSnapshotName)) {
            return Optional.empty();
        }
        FileSystemSnapshotName fsName = (FileSystemSnapshotName) name;
        Optional<Snapshot> result = tryRegularFileSnapshot(fsName);
        if (result.isPresent()) {
            return result;
        }
        if (!Files.isDirectory(fsName.getFile()) || fsName.getInternalPath() != null) {
            // We never provide names like this.
            return Optional.empty();
        }
        return Optional.of(createSnapshot(fsName.getFile(), null));
    }

    private Optional<Snapshot> tryRegularFileSnapshot(FileSystemSnapshotName name) throws IOException {
        if (Files.isDirectory(name.getFile()) || name.getInternalPath() == null) {
            return Optional.empty();
        }

        return ArchiveNioSupports.tryOpenAsDir(name.getFile())
            .map(fs -> createSnapshot(fs.getPath(name.getInternalPath()), fs::close));
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
            listWorldEntries(root, worldName),
            listTimestampedEntries(root, worldName)
        ).flatMap(Function.identity());
    }

    private Stream<Snapshot> listWorldEntries(Path root, String worldName) throws IOException {
        return Files.list(root)
            .flatMap(candidate -> {
                // Try world directory
                if (candidate.getFileName().toString().equalsIgnoreCase(worldName)) {
                    // Direct
                    if (Files.exists(candidate.resolve("level.dat"))) {
                        return Stream.of(createSnapshot(candidate, null));
                    }
                    // Container for time-stamped entries
                    try {
                        return listTimestampedEntries(candidate, worldName);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                // Try world archive
                if (Files.isRegularFile(candidate) && candidate.startsWith(worldName + ".")) {
                    try {
                        return tryRegularFileSnapshot(
                            new FileSystemSnapshotName(candidate, "/")
                        ).map(Stream::of).orElse(null);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                return null;
            });
    }

    private Stream<Snapshot> listTimestampedEntries(Path root, String worldName) throws IOException {
        return Files.list(root)
            .filter(candidate -> {
                ZonedDateTime date = FileNameDateTimeParser.getInstance().detectDateTime(candidate);
                return date != null;
            })
            .flatMap(candidate -> {
                // Try timestamped directory
                if (Files.isDirectory(candidate)) {
                    try {
                        return listWorldEntries(candidate, worldName);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                // Otherwise archive, get it as a directory & unpack it
                try {
                    Optional<FileSystem> fs = ArchiveNioSupports.tryOpenAsDir(candidate);
                    if (!fs.isPresent()) {
                        return null;
                    }
                    return listWorldEntries(fs.get().getPath("/"), worldName);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
    }

}
