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
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.io.file.ArchiveDir;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static com.sk89q.worldedit.world.snapshot.experimental.fs.FileSystemSnapshotDatabaseTest.CHUNK_POS;
import static com.sk89q.worldedit.world.snapshot.experimental.fs.FileSystemSnapshotDatabaseTest.CHUNK_TAG;
import static com.sk89q.worldedit.world.snapshot.experimental.fs.FileSystemSnapshotDatabaseTest.TIME_ONE;
import static com.sk89q.worldedit.world.snapshot.experimental.fs.FileSystemSnapshotDatabaseTest.TIME_TWO;
import static com.sk89q.worldedit.world.snapshot.experimental.fs.FileSystemSnapshotDatabaseTest.WORLD_ALPHA;
import static com.sk89q.worldedit.world.snapshot.experimental.fs.FileSystemSnapshotDatabaseTest.WORLD_BETA;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

enum FSSDTestType {
    EMPTY {
        @Override
        List<DynamicTest> getTests(FSSDContext context) {
            return ImmutableList.of(
                dynamicTest("return an empty stream from getSnapshots(worldName)",
                    () -> context.db.getSnapshots(WORLD_ALPHA)),
                dynamicTest("return an empty optional from getSnapshot(name)",
                    () -> context.db.getSnapshot(context.nameUri(WORLD_ALPHA)))
            );
        }
    },
    WORLD_ONLY_DIR {
        @Override
        List<DynamicTest> getTests(FSSDContext context) throws IOException {
            Path worldFolder = EntryMaker.WORLD_DIR.createEntry(context.db.getRoot(), WORLD_ALPHA);
            Files.setLastModifiedTime(worldFolder, FileTime.from(TIME_ONE.toInstant()));
            return singleSnapTest(context, WORLD_ALPHA, TIME_ONE);
        }
    },
    WORLD_ONLY_DIM_DIR {
        @Override
        List<DynamicTest> getTests(FSSDContext context) throws IOException {
            int dim = ThreadLocalRandom.current().nextInt();
            Path worldFolder = EntryMaker.WORLD_DIM_DIR
                .createEntry(context.db.getRoot(), new EntryMaker.DimInfo(WORLD_ALPHA, dim));
            Files.setLastModifiedTime(worldFolder, FileTime.from(TIME_ONE.toInstant()));
            return singleSnapTest(context, WORLD_ALPHA, TIME_ONE);
        }
    },
    WORLD_ONLY_NO_REGION_DIR {
        @Override
        List<DynamicTest> getTests(FSSDContext context) throws IOException {
            Path worldFolder = EntryMaker.WORLD_NO_REGION_DIR
                .createEntry(context.db.getRoot(), WORLD_ALPHA);
            Files.setLastModifiedTime(worldFolder, FileTime.from(TIME_ONE.toInstant()));
            return singleSnapTest(context, WORLD_ALPHA, TIME_ONE);
        }
    },
    WORLD_LEGACY_DIR {
        @Override
        List<DynamicTest> getTests(FSSDContext context) throws IOException {
            Path worldFolder = EntryMaker.WORLD_LEGACY_DIR
                .createEntry(context.db.getRoot(), WORLD_ALPHA);
            Files.setLastModifiedTime(worldFolder, FileTime.from(TIME_ONE.toInstant()));
            return singleSnapTest(context, WORLD_ALPHA, TIME_ONE);
        }
    },
    WORLD_ONLY_ARCHIVE {
        @Override
        List<DynamicTest> getTests(FSSDContext context) throws IOException {
            Path worldArchive = EntryMaker.WORLD_ARCHIVE
                .createEntry(context.db.getRoot(), WORLD_ALPHA);
            try (ArchiveDir rootOfArchive = context.getRootOfArchive(worldArchive)) {
                Files.setLastModifiedTime(
                    rootOfArchive.getPath(),
                    FileTime.from(TIME_ONE.toInstant())
                );
            }
            return singleSnapTest(context, WORLD_ALPHA + ".zip", TIME_ONE);
        }
    },
    TIMESTAMPED_DIR {
        @Override
        List<? extends DynamicNode> getTests(FSSDContext context) throws IOException {
            Path root = context.db.getRoot();
            Path timestampedDir = EntryMaker.TIMESTAMPED_DIR
                .createEntry(root, TIME_ONE);
            EntryMaker.WORLD_DIR.createEntry(timestampedDir, WORLD_ALPHA);
            EntryMaker.WORLD_ARCHIVE.createEntry(timestampedDir, WORLD_BETA);
            return ImmutableList.of(
                dynamicContainer("world dir",
                    singleSnapTest(context,
                        root.relativize(timestampedDir) + File.separator + WORLD_ALPHA,
                        TIME_ONE)
                ),
                dynamicContainer("world archive",
                    singleSnapTest(context,
                        root.relativize(timestampedDir) + File.separator + WORLD_BETA + ".zip",
                        TIME_ONE)
                )
            );
        }
    },
    TIMESTAMPED_ARCHIVE {
        @Override
        List<? extends DynamicNode> getTests(FSSDContext context) throws IOException {
            Path root = context.db.getRoot();
            Path timestampedArchive = EntryMaker.TIMESTAMPED_ARCHIVE
                .createEntry(root, TIME_ONE);
            try (ArchiveDir timestampedDir = context.getRootOfArchive(timestampedArchive)) {
                EntryMaker.WORLD_DIR.createEntry(timestampedDir.getPath(), WORLD_ALPHA);
                EntryMaker.WORLD_ARCHIVE.createEntry(timestampedDir.getPath(), WORLD_BETA);
            }
            return ImmutableList.of(
                dynamicContainer("world dir",
                    singleSnapTest(context,
                        root.relativize(timestampedArchive) + File.separator + WORLD_ALPHA,
                        TIME_ONE)
                )
            );
        }
    },
    WORLD_DIR_TIMESTAMPED_DIR {
        @Override
        List<? extends DynamicNode> getTests(FSSDContext context) throws IOException {
            Path root = context.db.getRoot();
            Path timestampedDirA = EntryMaker.TIMESTAMPED_DIR
                .createEntry(root.resolve(WORLD_ALPHA), TIME_ONE);
            Path timestampedDirB = EntryMaker.TIMESTAMPED_DIR
                .createEntry(root.resolve(WORLD_BETA), TIME_ONE);
            EntryMaker.WORLD_DIR.createEntry(timestampedDirA, WORLD_ALPHA);
            EntryMaker.WORLD_ARCHIVE.createEntry(timestampedDirB, WORLD_BETA);
            return ImmutableList.of(
                dynamicContainer("world dir",
                    singleSnapTest(context,
                        root.relativize(timestampedDirA) + File.separator + WORLD_ALPHA,
                        TIME_ONE)
                ),
                dynamicContainer("world archive",
                    singleSnapTest(context,
                        root.relativize(timestampedDirB) + File.separator + WORLD_BETA + ".zip",
                        TIME_ONE)
                )
            );
        }
    },
    TWO_TIMESTAMPED_WORLD_DIR {
        @Override
        List<DynamicTest> getTests(FSSDContext context) throws IOException {
            Path root = context.db.getRoot();
            Path timestampedDirA = EntryMaker.TIMESTAMPED_DIR
                .createEntry(root, TIME_ONE);
            EntryMaker.WORLD_DIR.createEntry(timestampedDirA, WORLD_ALPHA);
            Path timestampedDirB = EntryMaker.TIMESTAMPED_DIR
                .createEntry(root, TIME_TWO);
            EntryMaker.WORLD_DIR.createEntry(timestampedDirB, WORLD_ALPHA);
            return ImmutableList.of(
                dynamicTest("list both snapshots in order (newest first)", () -> {
                    List<Snapshot> snapshots = context.db
                        .getSnapshotsNewestFirst(WORLD_ALPHA).collect(toList());
                    assertEquals(2, snapshots.size());
                    assertValidSnapshot(TIME_ONE, snapshots.get(0));
                    assertValidSnapshot(TIME_TWO, snapshots.get(1));
                }),
                dynamicTest("list both snapshots in order (oldest first)", () -> {
                    List<Snapshot> snapshots = context.db
                        .getSnapshotsOldestFirst(WORLD_ALPHA).collect(toList());
                    assertEquals(2, snapshots.size());
                    assertValidSnapshot(TIME_TWO, snapshots.get(0));
                    assertValidSnapshot(TIME_ONE, snapshots.get(1));
                }),
                dynamicTest("list only 1 if getting AFTER 2", () -> {
                    List<Snapshot> snapshots = context.db
                        .getSnapshotsAfter(WORLD_ALPHA, TIME_TWO).collect(toList());
                    assertEquals(1, snapshots.size());
                    assertValidSnapshot(TIME_ONE, snapshots.get(0));
                }),
                dynamicTest("list only 2 if getting BEFORE 1", () -> {
                    List<Snapshot> snapshots = context.db
                        .getSnapshotsBefore(WORLD_ALPHA, TIME_ONE).collect(toList());
                    assertEquals(1, snapshots.size());
                    assertValidSnapshot(TIME_TWO, snapshots.get(0));
                }),
                dynamicTest("list both if AFTER time before 2", () -> {
                    List<Snapshot> snapshots = context.db
                        .getSnapshotsAfter(WORLD_ALPHA, TIME_TWO.minusSeconds(1))
                        .collect(toList());
                    assertEquals(2, snapshots.size());
                    // sorted newest first
                    assertValidSnapshot(TIME_ONE, snapshots.get(0));
                    assertValidSnapshot(TIME_TWO, snapshots.get(1));
                }),
                dynamicTest("list both if BEFORE time after 1", () -> {
                        List<Snapshot> snapshots = context.db
                            .getSnapshotsBefore(WORLD_ALPHA, TIME_ONE.plusSeconds(1))
                            .collect(toList());
                        assertEquals(2, snapshots.size());
                        // sorted oldest first
                        assertValidSnapshot(TIME_TWO, snapshots.get(0));
                        assertValidSnapshot(TIME_ONE, snapshots.get(1));
                    }
                )
            );
        }
    },
    TWO_WORLD_ONLY_DIR {
        @Override
        List<? extends DynamicNode> getTests(FSSDContext context) throws IOException {
            Path worldFolderA = EntryMaker.WORLD_DIR
                .createEntry(context.db.getRoot(), WORLD_ALPHA);
            Files.setLastModifiedTime(worldFolderA, FileTime.from(TIME_ONE.toInstant()));
            Path worldFolderB = EntryMaker.WORLD_DIR
                .createEntry(context.db.getRoot(), WORLD_BETA);
            Files.setLastModifiedTime(worldFolderB, FileTime.from(TIME_TWO.toInstant()));
            return Stream.of(
                singleSnapTest(context, WORLD_ALPHA, TIME_ONE),
                singleSnapTest(context, WORLD_BETA, TIME_TWO)
            ).flatMap(List::stream).collect(toList());
        }
    };

    List<DynamicTest> singleSnapTest(FSSDContext context, String name,
                                                    ZonedDateTime time) {
        return ImmutableList.of(
            dynamicTest("return a valid snapshot for " + name, () -> {
                try (Snapshot snapshot = context.requireSnapshot(name)) {
                    assertValidSnapshot(time, snapshot);
                }
            }),
            dynamicTest("list a valid snapshot for " + name, () -> {
                try (Snapshot snapshot = context.requireListsSnapshot(name)) {
                    assertValidSnapshot(time, snapshot);
                }
            })
        );
    }

    private static void assertValidSnapshot(ZonedDateTime time,
                                            Snapshot snapshot) throws IOException, DataException {
        assertEquals(time, snapshot.getInfo().getDateTime());
        // MCA file
        assertEquals(CHUNK_TAG.toString(), snapshot.getChunkTag(CHUNK_POS).toString());
        // MCR file
        BlockVector3 offsetChunkPos = CHUNK_POS.add(32, 0, 32);
        assertEquals(CHUNK_TAG.toString(), snapshot.getChunkTag(offsetChunkPos).toString());
    }

    abstract List<? extends DynamicNode> getTests(FSSDContext context) throws IOException;

    Stream<DynamicNode> getNamedTests(FSSDContext context) throws IOException {
        return Stream.of(dynamicContainer(
            name(),
            URI.create("method:" + getClass().getName()
                + "#getTests(" + FSSDContext.class.getName() + ")"),
            getTests(context).stream()
        ));
    }

}
