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

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.io.file.ArchiveDir;
import com.sk89q.worldedit.util.io.file.ZipArchiveNioSupport;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.NullWorld;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotRestore;
import com.sk89q.worldedit.world.storage.McRegionReader;
import org.enginehub.linbus.stream.LinBinaryIO;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("FolderSnapshot")
class FolderSnapshotTest extends BaseWorldEditTest {

    private static final String WORLD_NAME = "World Alpha";
    private static final String TIMESTAMP = "2023-05-13-12-00-00";
    private static byte[] REGION_DATA;
    private static byte[] CHUNK_DATA;
    private static LinCompoundTag CHUNK_TAG;
    private static BlockVector3 CHUNK_POS;
    private static Path TEMP_DIR;

    @BeforeAll
    static void setUpStatic() throws IOException, DataException {
        try (InputStream in = Resources.getResource("world_region.mca.gzip").openStream();
             GZIPInputStream gzIn = new GZIPInputStream(in)) {
            REGION_DATA = ByteStreams.toByteArray(gzIn);
        }
        McRegionReader reader = new McRegionReader(new ByteArrayInputStream(REGION_DATA));
        try {
            // Find the single chunk
            BlockVector2 chunkPos = IntStream.range(0, 32).mapToObj(
                x -> IntStream.range(0, 32).filter(z -> reader.hasChunk(x, z))
                    .mapToObj(z -> BlockVector2.at(x, z))
            ).flatMap(Function.identity())
                .findAny()
                .orElseThrow(() -> new AssertionError("No chunk in region file."));
            ByteArrayOutputStream cap = new ByteArrayOutputStream();
            try (InputStream in = reader.getChunkInputStream(chunkPos);
                 GZIPOutputStream gzOut = new GZIPOutputStream(cap)) {
                ByteStreams.copy(in, gzOut);
            }
            CHUNK_DATA = cap.toByteArray();
            try (var chunkStream = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(CHUNK_DATA)))) {
                CHUNK_TAG = LinBinaryIO.readUsing(chunkStream, LinRootEntry::readFrom).value();
            }
            CHUNK_POS = chunkPos.toBlockVector3();
            registerChunkBlockTypes(CHUNK_TAG);
        } finally {
            reader.close();
        }
        TEMP_DIR = Files.createTempDirectory("worldedit-folder-snapshot-test").toRealPath();
    }

    @AfterAll
    static void tearDownStatic() throws IOException {
        deleteTree(TEMP_DIR);
    }

    @Test
    @DisplayName("snapshot from a zip remains readable after the listing stream closes")
    void snapshotFromZipSurvivesStreamClose() throws Exception {
        Path root = Files.createTempDirectory(TEMP_DIR, "db");
        try {
            createWorldArchive(root, WORLD_NAME);
            FileSystemSnapshotDatabase db = new FileSystemSnapshotDatabase(root, ZipArchiveNioSupport.getInstance());

            Snapshot snapshot;
            try (Stream<Snapshot> snapshotStream = db.getSnapshotsNewestFirst(WORLD_NAME)) {
                snapshot = snapshotStream.findFirst().orElse(null);
            }
            assertNotNull(snapshot, "Expected a snapshot to be returned");

            try (snapshot) {
                LinCompoundTag chunkTag = snapshot.getChunkTag(CHUNK_POS);
                assertEquals(CHUNK_TAG.toString(), chunkTag.toString());
                LinCompoundTag offsetTag = snapshot.getChunkTag(CHUNK_POS.add(32, 0, 32));
                assertEquals(CHUNK_TAG.toString(), offsetTag.toString());
            }
        } finally {
            deleteTree(root);
        }
    }

    @Test
    @DisplayName("snapshot from a timestamped zip remains readable after listing closes")
    void snapshotFromTimestampedZipSurvivesStreamClose() throws Exception {
        Path root = Files.createTempDirectory(TEMP_DIR, "db");
        try {
            createTimestampedArchiveWithWorld(root, TIMESTAMP, WORLD_NAME);
            FileSystemSnapshotDatabase db = new FileSystemSnapshotDatabase(root, ZipArchiveNioSupport.getInstance());

            Snapshot snapshot;
            try (Stream<Snapshot> snapshotStream = db.getSnapshotsNewestFirst(WORLD_NAME)) {
                snapshot = snapshotStream.findFirst().orElse(null);
            }
            assertNotNull(snapshot, "Expected a snapshot to be returned");

            try (snapshot) {
                LinCompoundTag chunkTag = snapshot.getChunkTag(CHUNK_POS);
                assertEquals(CHUNK_TAG.toString(), chunkTag.toString());
                LinCompoundTag offsetTag = snapshot.getChunkTag(CHUNK_POS.add(32, 0, 32));
                assertEquals(CHUNK_TAG.toString(), offsetTag.toString());
            }
        } finally {
            deleteTree(root);
        }
    }

    @Test
    @DisplayName("snapshot from a timestamped zip under a world dir remains readable after listing closes")
    void snapshotFromTimestampedZipUnderWorldDirSurvivesStreamClose() throws Exception {
        Path root = Files.createTempDirectory(TEMP_DIR, "db");
        try {
            createTimestampedArchiveWithWorldUnderWorldDir(root, WORLD_NAME, TIMESTAMP);
            FileSystemSnapshotDatabase db = new FileSystemSnapshotDatabase(root, ZipArchiveNioSupport.getInstance());

            Snapshot snapshot;
            try (Stream<Snapshot> snapshotStream = db.getSnapshotsNewestFirst(WORLD_NAME)) {
                snapshot = snapshotStream.findFirst().orElse(null);
            }
            assertNotNull(snapshot, "Expected a snapshot to be returned");

            try (snapshot) {
                LinCompoundTag chunkTag = snapshot.getChunkTag(CHUNK_POS);
                assertEquals(CHUNK_TAG.toString(), chunkTag.toString());
                LinCompoundTag offsetTag = snapshot.getChunkTag(CHUNK_POS.add(32, 0, 32));
                assertEquals(CHUNK_TAG.toString(), offsetTag.toString());
            }
        } finally {
            deleteTree(root);
        }
    }

    @Test
    @DisplayName("snapshot remains readable after an external archive handle closes")
    void snapshotSurvivesExternalArchiveClose() throws Exception {
        Path root = Files.createTempDirectory(TEMP_DIR, "db");
        Snapshot snapshot = null;
        try {
            createTimestampedArchiveWithWorldUnderWorldDir(root, WORLD_NAME, TIMESTAMP);
            FileSystemSnapshotDatabase db = new FileSystemSnapshotDatabase(root, ZipArchiveNioSupport.getInstance());
            Path zipPath = root.resolve(WORLD_NAME).resolve(TIMESTAMP + ".zip");
            String id = WORLD_NAME + "/" + TIMESTAMP + ".zip/" + WORLD_NAME;

            try (ArchiveDir archiveDir = ZipArchiveNioSupport.getInstance()
                .tryOpenAsDir(zipPath)
                .orElseThrow(() -> new AssertionError("Expected archive to open"))) {
                archiveDir.getPath();
                snapshot = db.getSnapshot(FileSystemSnapshotDatabase.createUri(id))
                    .orElseThrow(() -> new AssertionError("Expected a snapshot to be returned"));
            }

            LinCompoundTag chunkTag = snapshot.getChunkTag(CHUNK_POS);
            assertEquals(CHUNK_TAG.toString(), chunkTag.toString());
            LinCompoundTag offsetTag = snapshot.getChunkTag(CHUNK_POS.add(32, 0, 32));
            assertEquals(CHUNK_TAG.toString(), offsetTag.toString());
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
            deleteTree(root);
        }
    }

    @Test
    @DisplayName("snapshot restore from a timestamped zip under a world dir stays readable")
    void snapshotRestoreFromTimestampedZipUnderWorldDir() throws Exception {
        Path root = Files.createTempDirectory(TEMP_DIR, "db");
        try {
            createTimestampedArchiveWithWorldUnderWorldDir(root, WORLD_NAME, TIMESTAMP);
            FileSystemSnapshotDatabase db = new FileSystemSnapshotDatabase(root, ZipArchiveNioSupport.getInstance());

            Snapshot snapshot;
            try (Stream<Snapshot> snapshotStream = db.getSnapshotsNewestFirst(WORLD_NAME)) {
                snapshot = snapshotStream.findFirst().orElse(null);
            }
            assertNotNull(snapshot, "Expected a snapshot to be returned");

            BlockVector3 min = BlockVector3.at(CHUNK_POS.x() << 4, 0, CHUNK_POS.z() << 4);
            BlockVector3 max = min;
            try (snapshot; EditSession editSession = WorldEdit.getInstance()
                .newEditSessionBuilder()
                .world(NullWorld.getInstance())
                .build()) {
                SnapshotRestore restore = new SnapshotRestore(snapshot, editSession, new CuboidRegion(min, max));
                restore.restore();
                assertEquals(1, restore.getChunksAffected());
                assertEquals(0, restore.getMissingChunks().size());
                assertEquals(0, restore.getErrorChunks().size(),
                    "Restore error: " + restore.getLastErrorMessage());
            }
        } finally {
            deleteTree(root);
        }
    }

    private static Path createWorldArchive(Path root, String worldName) throws IOException {
        Path zipFile = root.resolve(worldName + ".zip");
        Files.deleteIfExists(zipFile);
        try (FileSystem zipFs = FileSystems.newFileSystem(
            URI.create("jar:" + zipFile.toUri() + "!/"),
            ImmutableMap.of("create", "true")
        )) {
            Path worldRoot = zipFs.getPath("/").resolve(worldName);
            Files.createDirectories(worldRoot);
            Files.createFile(worldRoot.resolve("level.dat"));
            Path regionFolder = worldRoot.resolve("region");
            Files.createDirectories(regionFolder);
            Files.write(regionFolder.resolve("r.0.0.mca"), REGION_DATA);
            Files.write(regionFolder.resolve("r.1.1.mcr"), REGION_DATA);
        }
        return zipFile;
    }

    private static Path createTimestampedArchiveWithWorld(Path root, String timestamp, String worldName)
        throws IOException {
        Path zipFile = root.resolve(timestamp + ".zip");
        Files.deleteIfExists(zipFile);
        try (FileSystem zipFs = FileSystems.newFileSystem(
            URI.create("jar:" + zipFile.toUri() + "!/"),
            ImmutableMap.of("create", "true")
        )) {
            Path worldRoot = zipFs.getPath("/").resolve(worldName);
            Files.createDirectories(worldRoot);
            Files.createFile(worldRoot.resolve("level.dat"));
            Path regionFolder = worldRoot.resolve("region");
            Files.createDirectories(regionFolder);
            Files.write(regionFolder.resolve("r.0.0.mca"), REGION_DATA);
            Files.write(regionFolder.resolve("r.1.1.mcr"), REGION_DATA);
        }
        return zipFile;
    }

    private static Path createTimestampedArchiveWithWorldUnderWorldDir(Path root, String worldName, String timestamp)
        throws IOException {
        Path worldDir = root.resolve(worldName);
        Files.createDirectories(worldDir);
        Path zipFile = worldDir.resolve(timestamp + ".zip");
        Files.deleteIfExists(zipFile);
        try (FileSystem zipFs = FileSystems.newFileSystem(
            URI.create("jar:" + zipFile.toUri() + "!/"),
            ImmutableMap.of("create", "true")
        )) {
            Path worldRoot = zipFs.getPath("/").resolve(worldName);
            Files.createDirectories(worldRoot);
            Files.createFile(worldRoot.resolve("level.dat"));
            Path regionFolder = worldRoot.resolve("region");
            Files.createDirectories(regionFolder);
            Files.write(regionFolder.resolve("r.0.0.mca"), REGION_DATA);
            Files.write(regionFolder.resolve("r.1.1.mcr"), REGION_DATA);
        }
        return zipFile;
    }

    private static void deleteTree(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return;
        }
        FileVisitor<Path> deleter = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(root, deleter);
    }

    private static void registerChunkBlockTypes(LinCompoundTag chunkTag) {
        String serialized = chunkTag.toString();
        Pattern pattern = Pattern.compile("minecraft:[a-z0-9_\\-]+");
        Matcher matcher = pattern.matcher(serialized);
        Set<String> ids = new HashSet<>();
        while (matcher.find()) {
            ids.add(matcher.group());
        }
        for (String id : ids) {
            if (BlockType.REGISTRY.get(id) == null) {
                BlockType.REGISTRY.register(id, new BlockType(id));
            }
        }
    }
}
