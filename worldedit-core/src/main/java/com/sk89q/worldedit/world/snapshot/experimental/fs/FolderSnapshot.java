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

    private static boolean isLegacyFolder(Path folder) {
        Path regionDir = folder.resolve("region");
        if (Files.exists(regionDir)) {
            checkState(Files.isDirectory(regionDir), "Region folder is actually a file");
            return false;
        }
        return true;
    }

    private final SnapshotInfo info;
    private final Path folder;
    private final boolean isLegacy;
    private final @Nullable IORunnable closeCallback;

    public FolderSnapshot(SnapshotInfo info, Path folder, @Nullable IORunnable closeCallback) {
        this.info = info;
        this.folder = folder;
        this.isLegacy = isLegacyFolder(folder);
        this.closeCallback = closeCallback;
    }

    public Path getFolder() {
        return folder;
    }

    @Override
    public SnapshotInfo getInfo() {
        return info;
    }

    @Override
    public CompoundTag getChunkTag(BlockVector3 position) throws DataException, IOException {
        BlockVector2 pos = position.toBlockVector2();
        if (this.isLegacy) {
            Path chunkFile = getFolder().resolve(LegacyChunkStore.getFilename(pos, "/"));
            if (!Files.exists(chunkFile)) {
                throw new MissingChunkException();
            }
            return ChunkStoreHelper.readCompoundTag(() ->
                new GZIPInputStream(Files.newInputStream(chunkFile))
            );
        }
        Path regionFile = getFolder().resolve(McRegionChunkStore.getFilename(pos));
        if (!Files.exists(regionFile)) {
            throw new MissingChunkException();
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
