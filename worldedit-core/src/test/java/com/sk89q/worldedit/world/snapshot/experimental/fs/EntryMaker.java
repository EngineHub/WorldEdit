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

import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.world.storage.LegacyChunkStore;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import static com.sk89q.worldedit.world.snapshot.experimental.fs.FileSystemSnapshotDatabaseTest.CHUNK_DATA;
import static com.sk89q.worldedit.world.snapshot.experimental.fs.FileSystemSnapshotDatabaseTest.CHUNK_POS;
import static com.sk89q.worldedit.world.snapshot.experimental.fs.FileSystemSnapshotDatabaseTest.FORMATTER;
import static com.sk89q.worldedit.world.snapshot.experimental.fs.FileSystemSnapshotDatabaseTest.REGION_DATA;

interface EntryMaker<T> {
    EntryMaker<ZonedDateTime> TIMESTAMPED_DIR = (directory, time) -> {
        Path timestampedDir = directory.resolve(time.format(FORMATTER));
        Files.createDirectories(timestampedDir);
        return timestampedDir;
    };
    EntryMaker<ZonedDateTime> TIMESTAMPED_ARCHIVE = (directory, time) -> {
        Path zipFile = directory.resolve(time.format(FORMATTER) + ".zip");
        try (FileSystem zipFs = FileSystems.newFileSystem(
            URI.create("jar:" + zipFile.toUri() + "!/"),
            ImmutableMap.of("create", "true")
        )) {
            TIMESTAMPED_DIR.createEntry(zipFs.getPath("/"), time);
        }
        return zipFile;
    };
    EntryMaker<String> WORLD_DIR = (directory, worldName) -> {
        Path worldDir = directory.resolve(worldName);
        Files.createDirectories(worldDir);
        Files.createFile(worldDir.resolve("level.dat"));
        Path regionFolder = worldDir.resolve("region");
        Files.createDirectory(regionFolder);
        Files.write(regionFolder.resolve("r.0.0.mca"), REGION_DATA);
        Files.write(regionFolder.resolve("r.1.1.mcr"), REGION_DATA);
        return worldDir;
    };

    class DimInfo {
        final String worldName;
        final int dim;

        DimInfo(String worldName, int dim) {
            this.worldName = worldName;
            this.dim = dim;
        }
    }

    EntryMaker<DimInfo> WORLD_DIM_DIR = (directory, dimInfo) -> {
        Path worldDir = directory.resolve(dimInfo.worldName);
        Files.createDirectories(worldDir);
        Files.createFile(worldDir.resolve("level.dat"));
        Path dimFolder = worldDir.resolve("DIM" + dimInfo.dim).resolve("region");
        Files.createDirectories(dimFolder);
        Files.write(dimFolder.resolve("r.0.0.mca"), REGION_DATA);
        Files.write(dimFolder.resolve("r.1.1.mcr"), REGION_DATA);
        return worldDir;
    };
    EntryMaker<String> WORLD_NO_REGION_DIR = (directory, worldName) -> {
        Path worldDir = directory.resolve(worldName);
        Files.createDirectories(worldDir);
        Files.createFile(worldDir.resolve("level.dat"));
        Files.write(worldDir.resolve("r.0.0.mca"), REGION_DATA);
        Files.write(worldDir.resolve("r.1.1.mcr"), REGION_DATA);
        return worldDir;
    };
    EntryMaker<String> WORLD_LEGACY_DIR = (directory, worldName) -> {
        Path worldDir = directory.resolve(worldName);
        Files.createDirectories(worldDir);
        Files.createFile(worldDir.resolve("level.dat"));
        Path chunkFile = worldDir.resolve(LegacyChunkStore.getFilename(
            CHUNK_POS.toBlockVector2(), "/"
        ));
        Files.createDirectories(chunkFile.getParent());
        Files.write(chunkFile, CHUNK_DATA);
        chunkFile = worldDir.resolve(LegacyChunkStore.getFilename(
            CHUNK_POS.add(32, 0, 32).toBlockVector2(), "/"
        ));
        Files.createDirectories(chunkFile.getParent());
        Files.write(chunkFile, CHUNK_DATA);
        return worldDir;
    };
    EntryMaker<String> WORLD_ARCHIVE = (directory, worldName) -> {
        Path tempDir = Files.createTempDirectory("worldedit-fs-snap-db" + worldName);
        Path temp = tempDir.resolve(worldName + ".zip");
        try {
            Files.deleteIfExists(temp);
            try (FileSystem zipFs = FileSystems.newFileSystem(
                URI.create("jar:" + temp.toUri() + "!/"),
                ImmutableMap.of("create", "true")
            )) {
                WORLD_DIR.createEntry(zipFs.getPath("/"), worldName);
            }
            Path zipFile = directory.resolve(worldName + ".zip");
            Files.copy(temp, zipFile);
            return zipFile;
        } finally {
            Files.deleteIfExists(temp);
            Files.deleteIfExists(tempDir);
        }
    };

    Path createEntry(Path directory, T name) throws IOException;

}
