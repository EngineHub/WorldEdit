package com.sk89q.worldedit.world.snapshot.experimental.fs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.io.file.ArchiveNioSupports;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import com.sk89q.worldedit.world.storage.ChunkStoreHelper;
import com.sk89q.worldedit.world.storage.McRegionReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("A FS Snapshot Database")
class FileSystemSnapshotDatabaseTest {

    private static byte[] REGION_DATA;
    private static CompoundTag CHUNK_DATA;
    private static BlockVector3 CHUNK_POS;
    private static final String WORLD_ALPHA = "World Alpha";
    private static final String WORLD_BETA = "World Beta";

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZonedDateTime TIME_ONE = Instant.parse("2018-01-01T12:00:00.00Z")
        .atZone(ZoneId.systemDefault());
    private static final ZonedDateTime TIME_TWO = TIME_ONE.minusDays(1);

    /**
     * Create a sample world in the given directory, with the given world name.
     *
     * @param directory the directory to put the world folder in
     * @param worldName the name of the world
     * @return the world folder
     * @throws IOException if an IO error occurs
     */
    private static Path putWorldIn(Path directory, String worldName) throws IOException {
        Path worldDir = directory.resolve(worldName);
        Files.createDirectories(worldDir);
        Files.createFile(worldDir.resolve("level.dat"));
        Files.createDirectory(worldDir.resolve("region"));
        Files.write(worldDir.resolve("region").resolve("r.0.0.mca"), REGION_DATA);
        return worldDir;
    }

    private static Path putWorldZipIn(Path directory, String worldName) throws IOException {
        Path tempDir = Files.createTempDirectory("worldedit-fs-snap-db" + worldName);
        Path temp = tempDir.resolve(worldName + ".zip");
        try {
            Files.deleteIfExists(temp);
            try (FileSystem zipFs = FileSystems.newFileSystem(
                URI.create("jar:" + temp.toUri() + "!/"),
                ImmutableMap.of("create", "true")
            )) {
                putWorldIn(zipFs.getPath("/"), worldName);
            }
            Path zipFile = directory.resolve(worldName + ".zip");
            Files.copy(temp, zipFile);
            return zipFile;
        } finally {
            Files.deleteIfExists(temp);
            Files.deleteIfExists(tempDir);
        }
    }

    private static Path putTimestampDir(Path directory, ZonedDateTime time) throws IOException {
        Path timestampedDir = directory.resolve(time.format(FORMATTER));
        Files.createDirectories(timestampedDir);
        return timestampedDir;
    }

    private static Path putTimestampZip(Path directory, ZonedDateTime time) throws IOException {
        Path zipFile = directory.resolve(time.format(FORMATTER) + ".zip");
        try (FileSystem zipFs = FileSystems.newFileSystem(
            URI.create("jar:" + zipFile.toUri() + "!/"),
            ImmutableMap.of("create", "true")
        )) {
            putTimestampDir(zipFs.getPath("/"), time);
        }
        return zipFile;
    }

    private static Path getRootOfArchive(Path archive) throws IOException {
        return ArchiveNioSupports.tryOpenAsDir(archive)
            .orElseThrow(() -> new AssertionError("No archive opener for " + archive));
    }

    @BeforeAll
    static void setUpStatic() throws IOException, DataException {
        try (InputStream in = Resources.getResource("world_region.mca.gzip").openStream();
             GZIPInputStream gzIn = new GZIPInputStream(in)) {
            REGION_DATA = ByteStreams.toByteArray(gzIn);
        }
        // Find the single chunk
        McRegionReader reader = new McRegionReader(new ByteArrayInputStream(REGION_DATA));
        BlockVector2 chunkPos = IntStream.range(0, 32).mapToObj(
            x -> IntStream.range(0, 32).filter(z -> reader.hasChunk(x, z))
                .mapToObj(z -> BlockVector2.at(x, z))
        ).flatMap(Function.identity())
            .findAny()
            .orElseThrow(() -> new AssertionError("No chunk in region file."));
        CHUNK_DATA = ChunkStoreHelper.readCompoundTag(() -> reader.getChunkInputStream(chunkPos));
        CHUNK_POS = chunkPos.toBlockVector3();
    }

    // Unique FS root so we can run concurrently.
    private Path root;
    private Path workingDir;
    private FileSystemSnapshotDatabase db;

    private Path path(String first, String... more) {
        Path p = root.resolve(Paths.get(first, more));
        checkArgument(p.startsWith(root), "Escaping root!");
        return p;
    }

    private URI nameUri(String name) {
        return db.getRoot().resolve(name).toUri();
    }

    private Snapshot requireSnapshot(String name) throws IOException {
        return requireSnapshot(name, db.getSnapshot(nameUri(name)).orElse(null));
    }

    private Snapshot requireListsSnapshot(String name) throws IOException {
        // World name is the last element of the path
        String worldName = Paths.get(name).getFileName().toString();
        // Without an extension
        worldName = worldName.split("\\.")[0];
        List<Snapshot> snapshots = db.getSnapshots(worldName).collect(toList());
        assertTrue(1 >= snapshots.size(),
            "Too many snapshots matched for " + worldName);
        return requireSnapshot(name, snapshots.stream().findAny().orElse(null));
    }

    private Snapshot requireSnapshot(String name, @Nullable Snapshot snapshot) {
        assertNotNull(snapshot, "No snapshot for " + name);
        assertEquals(name, snapshot.getInfo().getDisplayName());
        return snapshot;
    }

    @BeforeEach
    void setUp() throws IOException {
        root = Files.createTempDirectory("worldedit-fs-snap-db");
        Path root = path("snapshots");
        Files.createDirectories(root);
        db = new FileSystemSnapshotDatabase(root);
        workingDir = Paths.get(".").toRealPath();
    }

    @AfterEach
    void tearDown() throws IOException {
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
        Path relative = path("relative");
        Files.createDirectories(relative);
        FileSystemSnapshotDatabase db2 = new FileSystemSnapshotDatabase(relative);
        assertEquals(workingDir.resolve(relative), db2.getRoot());
        Path absolute = root.resolve("absolute");
        Files.createDirectories(absolute);
        FileSystemSnapshotDatabase db3 = new FileSystemSnapshotDatabase(absolute);
        assertEquals(absolute, db3.getRoot());
    }

    @DisplayName("with nothing in its root folder")
    @Nested
    class EmptyRoot {

        @DisplayName("returns an empty stream from getSnapshots(worldName)")
        @Test
        void returnsEmptyStreamFromGetSnapshots() throws IOException {
            assertEquals(ImmutableList.of(), db.getSnapshots(WORLD_ALPHA).collect(toList()));
        }

        @DisplayName("returns an empty optional from getSnapshot(name)")
        @Test
        void returnsEmptyOptionalFromGetSnapshot() throws IOException {
            assertEquals(Optional.empty(), db.getSnapshot(nameUri(WORLD_ALPHA)));
        }
    }

    private static void assertValidSnapshot(ZonedDateTime time, Snapshot snapshot) throws IOException, DataException {
        assertEquals(time, snapshot.getInfo().getDateTime());
        assertEquals(CHUNK_DATA.toString(), snapshot.getChunkTag(CHUNK_POS).toString());
    }

    @DisplayName("with two timestamped world directory snapshots")
    @Nested
    class TwoTimestampedWorldDir {

        private Path timestampedDirA;
        private Path timestampedDirB;

        @BeforeEach
        void setUp() throws IOException {
            timestampedDirA = putTimestampDir(db.getRoot(), TIME_ONE);
            timestampedDirB = putTimestampDir(db.getRoot(), TIME_TWO);
            putWorldIn(timestampedDirA, WORLD_ALPHA);
            putWorldIn(timestampedDirB, WORLD_ALPHA);
        }

        @DisplayName("lists both snapshots in order (newest first)")
        @Test
        void listsBothSnapshotsInOrderNewFirst() throws IOException, DataException {
            List<Snapshot> snapshots = db.getSnapshotsNewestFirst(WORLD_ALPHA).collect(toList());
            assertEquals(2, snapshots.size());
            assertValidSnapshot(TIME_ONE, snapshots.get(0));
            assertValidSnapshot(TIME_TWO, snapshots.get(1));
        }

        @DisplayName("lists both snapshots in order (oldest first)")
        @Test
        void listsBothSnapshotsInOrderOldFirst() throws IOException, DataException {
            List<Snapshot> snapshots = db.getSnapshotsOldestFirst(WORLD_ALPHA).collect(toList());
            assertEquals(2, snapshots.size());
            assertValidSnapshot(TIME_TWO, snapshots.get(0));
            assertValidSnapshot(TIME_ONE, snapshots.get(1));
        }

        @DisplayName("lists only 1 if getting AFTER 2")
        @Test
        void listsOneIfGetAfterTwo() throws IOException, DataException {
            List<Snapshot> snapshots = db.getSnapshotsAfter(WORLD_ALPHA, TIME_TWO).collect(toList());
            assertEquals(1, snapshots.size());
            assertValidSnapshot(TIME_ONE, snapshots.get(0));
        }

        @DisplayName("lists only 2 if getting BEFORE 1")
        @Test
        void listsTwoIfGetBeforeOne() throws IOException, DataException {
            List<Snapshot> snapshots = db.getSnapshotsBefore(WORLD_ALPHA, TIME_ONE).collect(toList());
            assertEquals(1, snapshots.size());
            assertValidSnapshot(TIME_TWO, snapshots.get(0));
        }

        @DisplayName("lists both if AFTER time before 2")
        @Test
        void listsBothIfAfterNearTwo() throws IOException, DataException {
            List<Snapshot> snapshots = db.getSnapshotsAfter(WORLD_ALPHA, TIME_TWO.minusSeconds(1))
                .collect(toList());
            assertEquals(2, snapshots.size());
            // sorted newest first
            assertValidSnapshot(TIME_ONE, snapshots.get(0));
            assertValidSnapshot(TIME_TWO, snapshots.get(1));
        }

        @DisplayName("lists both if BEFORE time after 1")
        @Test
        void listsBothIfBeforeNearOne() throws IOException, DataException {
            List<Snapshot> snapshots = db.getSnapshotsBefore(WORLD_ALPHA, TIME_ONE.plusSeconds(1))
                .collect(toList());
            assertEquals(2, snapshots.size());
            // sorted oldest first
            assertValidSnapshot(TIME_TWO, snapshots.get(0));
            assertValidSnapshot(TIME_ONE, snapshots.get(1));
        }
    }

    @DisplayName("with a world-only directory")
    @Nested
    class WorldOnlyDir {

        @BeforeEach
        void setUp() throws IOException {
            Path worldFolder = putWorldIn(db.getRoot(), WORLD_ALPHA);
            Files.setLastModifiedTime(worldFolder, FileTime.from(TIME_ONE.toInstant()));
        }

        @DisplayName("returns a valid snapshot")
        @Test
        void returnValidSnapshot() throws IOException, DataException {
            Snapshot snapshot = requireSnapshot(WORLD_ALPHA);
            assertValidSnapshot(TIME_ONE, snapshot);
        }

        @DisplayName("lists a valid snapshot")
        @Test
        void listValidSnapshot() throws IOException, DataException {
            Snapshot snapshot = requireListsSnapshot(WORLD_ALPHA);
            assertValidSnapshot(TIME_ONE, snapshot);
        }

    }

    @DisplayName("with two world-only directories")
    @Nested
    class TwoWorldOnlyDir {

        @BeforeEach
        void setUp() throws IOException {
            Path worldFolderA = putWorldIn(db.getRoot(), WORLD_ALPHA);
            Files.setLastModifiedTime(worldFolderA, FileTime.from(TIME_ONE.toInstant()));
            Path worldFolderB = putWorldIn(db.getRoot(), WORLD_BETA);
            Files.setLastModifiedTime(worldFolderB, FileTime.from(TIME_TWO.toInstant()));
        }

        @DisplayName("returns a valid snapshot for " + WORLD_ALPHA)
        @Test
        void returnValidSnapshotA() throws IOException, DataException {
            Snapshot snapshot = requireSnapshot(WORLD_ALPHA);
            assertValidSnapshot(TIME_ONE, snapshot);
        }

        @DisplayName("returns a valid snapshot for " + WORLD_BETA)
        @Test
        void returnValidSnapshotB() throws IOException, DataException {
            Snapshot snapshot = requireSnapshot(WORLD_BETA);
            assertValidSnapshot(TIME_TWO, snapshot);
        }

        @DisplayName("list a valid snapshot for " + WORLD_ALPHA)
        @Test
        void listValidSnapshotA() throws IOException, DataException {
            Snapshot snapshot = requireListsSnapshot(WORLD_ALPHA);
            assertValidSnapshot(TIME_ONE, snapshot);
        }

        @DisplayName("list a valid snapshot for " + WORLD_BETA)
        @Test
        void listValidSnapshotB() throws IOException, DataException {
            Snapshot snapshot = requireListsSnapshot(WORLD_BETA);
            assertValidSnapshot(TIME_TWO, snapshot);
        }

    }

    @DisplayName("with a world-only archive")
    @Nested
    class WorldOnlyArchive {

        @BeforeEach
        void setUp() throws IOException {
            Path worldArchive = putWorldZipIn(db.getRoot(), WORLD_ALPHA);
            Files.setLastModifiedTime(
                getRootOfArchive(worldArchive),
                FileTime.from(TIME_ONE.toInstant())
            );
        }

        @DisplayName("returns a valid snapshot")
        @Test
        void returnValidSnapshot() throws IOException, DataException {
            Snapshot snapshot = requireSnapshot(WORLD_ALPHA + ".zip");
            assertValidSnapshot(TIME_ONE, snapshot);
        }

        @DisplayName("lists a valid snapshot")
        @Test
        void listValidSnapshot() throws IOException, DataException {
            Snapshot snapshot = requireListsSnapshot(WORLD_ALPHA + ".zip");
            assertValidSnapshot(TIME_ONE, snapshot);
        }

    }

    @DisplayName("with a timestamped directory")
    @Nested
    class TimestampedDirectory {

        private Path timestampedDir;

        @BeforeEach
        void setUp() throws IOException {
            timestampedDir = putTimestampDir(db.getRoot(), TIME_ONE);
        }

        @DisplayName("with a world folder inside")
        @Nested
        class WorldInside {

            @BeforeEach
            void setUp() throws IOException {
                putWorldIn(timestampedDir, WORLD_ALPHA);
            }

            @DisplayName("returns a valid snapshot")
            @Test
            void returnValidSnapshot() throws IOException, DataException {
                Snapshot snapshot = requireSnapshot(
                    db.getRoot().relativize(timestampedDir) + "/" + WORLD_ALPHA
                );
                assertValidSnapshot(TIME_ONE, snapshot);
            }

            @DisplayName("lists a valid snapshot for " + WORLD_ALPHA)
            @Test
            void listValidSnapshot() throws IOException, DataException {
                Snapshot snapshot = requireListsSnapshot(
                    db.getRoot().relativize(timestampedDir) + "/" + WORLD_ALPHA
                );
                assertValidSnapshot(TIME_ONE, snapshot);
            }
        }

        @DisplayName("with a world zip inside")
        @Nested
        class WorldZipInside {

            @BeforeEach
            void setUp() throws IOException {
                putWorldZipIn(timestampedDir, WORLD_ALPHA);
            }

            @DisplayName("returns a valid snapshot")
            @Test
            void returnValidSnapshot() throws IOException, DataException {
                Snapshot snapshot = requireSnapshot(
                    db.getRoot().relativize(timestampedDir) + "/" + WORLD_ALPHA + ".zip"
                );
                assertValidSnapshot(TIME_ONE, snapshot);
            }

            @DisplayName("lists a valid snapshot for " + WORLD_ALPHA)
            @Test
            void listValidSnapshot() throws IOException, DataException {
                Snapshot snapshot = requireListsSnapshot(
                    db.getRoot().relativize(timestampedDir) + "/" + WORLD_ALPHA + ".zip"
                );
                assertValidSnapshot(TIME_ONE, snapshot);
            }
        }

    }

    @DisplayName("with a timestamped archive")
    @Nested
    class TimestampedArchive {

        private Path timestampedArchive;
        private Path timestampedDir;

        @BeforeEach
        void setUp() throws IOException {
            timestampedArchive = putTimestampZip(db.getRoot(), TIME_ONE);
            timestampedDir = getRootOfArchive(timestampedArchive);
        }

        @DisplayName("with a world folder inside")
        @Nested
        class WorldInside {

            @BeforeEach
            void setUp() throws IOException {
                putWorldIn(timestampedDir, WORLD_ALPHA);
            }

            @DisplayName("returns a valid snapshot")
            @Test
            void returnValidSnapshot() throws IOException, DataException {
                Snapshot snapshot = requireSnapshot(
                    db.getRoot().relativize(timestampedArchive) + "/" + WORLD_ALPHA
                );
                assertValidSnapshot(TIME_ONE, snapshot);
            }

            @DisplayName("lists a valid snapshot for " + WORLD_ALPHA)
            @Test
            void listValidSnapshot() throws IOException, DataException {
                Snapshot snapshot = requireListsSnapshot(
                    db.getRoot().relativize(timestampedArchive) + "/" + WORLD_ALPHA
                );
                assertValidSnapshot(TIME_ONE, snapshot);
            }
        }

        @DisplayName("with a world zip inside")
        @Nested
        @Disabled("TrueVFS implementation currently does not support nesting archives")
        class WorldZipInside {

            @BeforeEach
            void setUp() throws IOException {
                putWorldZipIn(timestampedDir, WORLD_ALPHA);
            }

            @DisplayName("returns a valid snapshot")
            @Test
            void returnValidSnapshot() throws IOException, DataException {
                Snapshot snapshot = requireSnapshot(
                    db.getRoot().relativize(timestampedArchive) + "/" + WORLD_ALPHA + ".zip"
                );
                assertValidSnapshot(TIME_ONE, snapshot);
            }

            @DisplayName("lists a valid snapshot for " + WORLD_ALPHA)
            @Test
            void listValidSnapshot() throws IOException, DataException {
                Snapshot snapshot = requireListsSnapshot(
                    db.getRoot().relativize(timestampedArchive) + "/" + WORLD_ALPHA + ".zip"
                );
                assertValidSnapshot(TIME_ONE, snapshot);
            }
        }

    }

    @DisplayName("with a world/timestamped directory")
    @Nested
    class WorldTimestampedDirectory {

        private Path timestampedDir;

        @BeforeEach
        void setUp() throws IOException {
            timestampedDir = putTimestampDir(db.getRoot().resolve(WORLD_ALPHA), TIME_ONE);
        }

        @DisplayName("with a world folder inside")
        @Nested
        class WorldInside {

            @BeforeEach
            void setUp() throws IOException {
                putWorldIn(timestampedDir, WORLD_ALPHA);
            }

            @DisplayName("returns a valid snapshot")
            @Test
            void returnValidSnapshot() throws IOException, DataException {
                Snapshot snapshot = requireSnapshot(
                    db.getRoot().relativize(timestampedDir) + "/" + WORLD_ALPHA
                );
                assertValidSnapshot(TIME_ONE, snapshot);
            }

            @DisplayName("lists a valid snapshot for " + WORLD_ALPHA)
            @Test
            void listValidSnapshot() throws IOException, DataException {
                Snapshot snapshot = requireListsSnapshot(
                    db.getRoot().relativize(timestampedDir) + "/" + WORLD_ALPHA
                );
                assertValidSnapshot(TIME_ONE, snapshot);
            }
        }

        @DisplayName("with a world zip inside")
        @Nested
        class WorldZipInside {

            @BeforeEach
            void setUp() throws IOException {
                putWorldZipIn(timestampedDir, WORLD_ALPHA);
            }

            @DisplayName("returns a valid snapshot")
            @Test
            void returnValidSnapshot() throws IOException, DataException {
                Snapshot snapshot = requireSnapshot(
                    db.getRoot().relativize(timestampedDir) + "/" + WORLD_ALPHA + ".zip"
                );
                assertValidSnapshot(TIME_ONE, snapshot);
            }

            @DisplayName("lists a valid snapshot for " + WORLD_ALPHA)
            @Test
            void listValidSnapshot() throws IOException, DataException {
                Snapshot snapshot = requireListsSnapshot(
                    db.getRoot().relativize(timestampedDir) + "/" + WORLD_ALPHA + ".zip"
                );
                assertValidSnapshot(TIME_ONE, snapshot);
            }
        }

    }
}
