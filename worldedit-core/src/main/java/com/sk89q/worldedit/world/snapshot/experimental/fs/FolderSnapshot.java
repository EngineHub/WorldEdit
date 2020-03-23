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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.function.IORunnable;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotInfo;
import com.sk89q.worldedit.world.storage.ChunkStoreHelper;
import com.sk89q.worldedit.world.storage.LegacyChunkStore;
import com.sk89q.worldedit.world.storage.McRegionChunkStore;
import com.sk89q.worldedit.world.storage.McRegionReader;
import com.sk89q.worldedit.world.storage.MissingChunkException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static com.google.common.base.Preconditions.checkState;

/**
 * Snapshot based on a world folder. Extracts chunks from the region folder.
 *
 * <p>
 * Note that the Path can belong to another filesystem. This allows easy integration with
 * zips due to Java's built-in zipfs support.
 * </p>
 */
public class FolderSnapshot implements Snapshot {

    /**
     * Object used by {@code getRegionFolder(Path)} to indicate that the path does not exist.
     */
    private static final Object NOT_FOUND_TOKEN = new Object();

    private static Object getRegionFolder(Path folder) throws IOException {
        Path regionDir = folder.resolve("region");
        if (Files.exists(regionDir)) {
            checkState(Files.isDirectory(regionDir), "Region folder is actually a file");
            return regionDir;
        }
        // Might be in a DIM* folder
        try (Stream<Path> paths = Files.list(folder)) {
            Optional<Path> path = paths
                .filter(Files::isDirectory)
                .filter(p -> p.getFileName().toString().startsWith("DIM"))
                .map(p -> p.resolve("region"))
                .filter(Files::isDirectory)
                .findFirst();
            if (path.isPresent()) {
                return path.get();
            }
        }
        // Might be its own region folder, check if the appropriate files exist
        try (Stream<Path> paths = Files.list(folder)) {
            if (paths
                .filter(Files::isRegularFile)
                .anyMatch(p -> {
                    String fileName = p.getFileName().toString();
                    return fileName.startsWith("r") &&
                        (fileName.endsWith(".mca") || fileName.endsWith(".mcr"));
                })) {
                return folder;
            }
        }
        return NOT_FOUND_TOKEN;
    }

    private final SnapshotInfo info;
    private final Path folder;
    private final AtomicReference<Object> regionFolder = new AtomicReference<>();
    private final @Nullable IORunnable closeCallback;

    public FolderSnapshot(SnapshotInfo info, Path folder, @Nullable IORunnable closeCallback) {
        this.info = info;
        // This is required to force TrueVfs to properly resolve parents.
        // Kinda odd, but whatever works.
        this.folder = folder.toAbsolutePath();
        this.closeCallback = closeCallback;
    }

    public Path getFolder() {
        return folder;
    }

    @Override
    public SnapshotInfo getInfo() {
        return info;
    }

    private Optional<Path> getRegionFolder() throws IOException {
        Object regFolder = regionFolder.get();
        if (regFolder == null) {
            Object update = getRegionFolder(folder);
            if (!regionFolder.compareAndSet(null, update)) {
                // failed race, get existing value
                regFolder = regionFolder.get();
            } else {
                regFolder = update;
            }
        }
        return regFolder == NOT_FOUND_TOKEN ? Optional.empty() : Optional.of((Path) regFolder);
    }

    @Override
    public CompoundTag getChunkTag(BlockVector3 position) throws DataException, IOException {
        BlockVector2 pos = position.toBlockVector2();
        Optional<Path> regFolder = getRegionFolder();
        if (!regFolder.isPresent()) {
            Path chunkFile = getFolder().resolve(LegacyChunkStore.getFilename(pos, "/"));
            if (!Files.exists(chunkFile)) {
                throw new MissingChunkException();
            }
            return ChunkStoreHelper.readCompoundTag(() ->
                new GZIPInputStream(Files.newInputStream(chunkFile))
            );
        }
        Path regionFile = regFolder.get().resolve(McRegionChunkStore.getFilename(pos));
        if (!Files.exists(regionFile)) {
            // Try mcr as well
            regionFile = regionFile.resolveSibling(
                regionFile.getFileName().toString().replace(".mca", ".mcr")
            );
            if (!Files.exists(regionFile)) {
                throw new MissingChunkException();
            }
        }
        try (InputStream stream = Files.newInputStream(regionFile)) {
            McRegionReader regionReader = new McRegionReader(stream);
            return ChunkStoreHelper.readCompoundTag(() -> regionReader.getChunkInputStream(pos));
        }
    }

    @Override
    public void close() throws IOException {
        if (closeCallback != null) {
            closeCallback.run();
        }
    }
}
