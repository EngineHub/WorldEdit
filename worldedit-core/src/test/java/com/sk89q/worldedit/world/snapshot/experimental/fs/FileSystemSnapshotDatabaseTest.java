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

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.io.file.ArchiveNioSupport;
import com.sk89q.worldedit.util.io.file.ArchiveNioSupports;
import com.sk89q.worldedit.util.io.file.TrueVfsArchiveNioSupport;
import com.sk89q.worldedit.util.io.file.ZipArchiveNioSupport;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.storage.ChunkStoreHelper;
import com.sk89q.worldedit.world.storage.McRegionReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

@DisplayName("A FS Snapshot Database")
class FileSystemSnapshotDatabaseTest {

    static byte[] REGION_DATA;
    static byte[] CHUNK_DATA;
    static CompoundTag CHUNK_TAG;
    static BlockVector3 CHUNK_POS;
    static final String WORLD_ALPHA = "World Alpha";
    static final String WORLD_BETA = "World Beta";

    static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ss");
    static final ZonedDateTime TIME_ONE = Instant.parse("2018-01-01T12:00:00.00Z")
        .atZone(ZoneId.systemDefault());
    static final ZonedDateTime TIME_TWO = TIME_ONE.minusDays(1);

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
            CHUNK_TAG = ChunkStoreHelper.readCompoundTag(() -> new GZIPInputStream(
                new ByteArrayInputStream(CHUNK_DATA)
            ));
            CHUNK_POS = chunkPos.toBlockVector3();
        } finally {
            reader.close();
        }
    }

    private static Path newTempDb() throws IOException {
        return Files.createTempDirectory("worldedit-fs-snap-db");
    }

    private static void deleteTree(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
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
        });
    }

    @DisplayName("makes the root directory absolute if needed")
    @Test
    void rootIsAbsoluteDir() throws IOException {
        Path root = newTempDb();
        try {
            Path relative = root.getFileSystem().getPath("relative");
            Files.createDirectories(relative);
            FileSystemSnapshotDatabase db2 = new FileSystemSnapshotDatabase(relative,
                ArchiveNioSupports.combined());
            assertEquals(root.getFileSystem().getPath(".").toRealPath()
                .resolve(relative), db2.getRoot());
            Path absolute = root.resolve("absolute");
            Files.createDirectories(absolute);
            FileSystemSnapshotDatabase db3 = new FileSystemSnapshotDatabase(absolute,
                ArchiveNioSupports.combined());
            assertEquals(absolute, db3.getRoot());
        } finally {
            deleteTree(root);
        }
    }

    @DisplayName("with a specific NIO support:")
    @TestFactory
    Stream<DynamicNode> withSpecificNioSupport() {
        return Stream.of(
            ZipArchiveNioSupport.getInstance(), TrueVfsArchiveNioSupport.getInstance()
        )
            .map(nioSupport -> {
                Stream<? extends DynamicNode> nodes = Stream.of(FSSDTestType.values())
                    .flatMap(type -> {
                        try {
                            return getTests(nioSupport, type);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                return dynamicContainer(
                    nioSupport.getClass().getSimpleName() + ", can, for format:",
                    nodes
                );
            });
    }

    private static Stream<? extends DynamicNode> getTests(ArchiveNioSupport nioSupport,
                                                          FSSDTestType type) throws IOException {
        Path root = newTempDb();
        try {
            Path dbRoot = root.resolve("snapshots");
            Files.createDirectories(dbRoot);
            // we leak `root` here, but I can't see a good way to clean it up.
            return type.getNamedTests(new FSSDContext(nioSupport, dbRoot));
        } catch (Throwable t) {
            deleteTree(root);
            throw t;
        }
    }

}
