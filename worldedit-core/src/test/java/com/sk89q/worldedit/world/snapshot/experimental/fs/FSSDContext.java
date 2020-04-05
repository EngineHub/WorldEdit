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

import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.io.file.ArchiveDir;
import com.sk89q.worldedit.util.io.file.ArchiveNioSupport;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Context class for using a {@link FileSystemSnapshotDatabase}.
 */
class FSSDContext {

    final ArchiveNioSupport archiveNioSupport;
    final FileSystemSnapshotDatabase db;

    FSSDContext(ArchiveNioSupport archiveNioSupport, Path root) {
        this.archiveNioSupport = archiveNioSupport;
        this.db = new FileSystemSnapshotDatabase(root, archiveNioSupport);
    }

    Path path(String first, String... more) {
        Path p = db.getRoot().resolve(Paths.get(first, more));
        checkArgument(p.startsWith(db.getRoot()), "Escaping root!");
        return p;
    }

    URI nameUri(String name) {
        return FileSystemSnapshotDatabase.createUri(name);
    }

    Snapshot requireSnapshot(String name) throws IOException {
        return requireSnapshot(name, db.getSnapshot(nameUri(name)).orElse(null));
    }

    Snapshot requireListsSnapshot(String name) throws IOException {
        // World name is the last element of the path
        String worldName = Paths.get(name).getFileName().toString();
        // Without an extension
        worldName = worldName.split("\\.")[0];
        List<Snapshot> snapshots;
        try (Stream<Snapshot> snapshotStream = db.getSnapshots(worldName)) {
            snapshots = snapshotStream.collect(toList());
        }
        try {
            assertTrue(snapshots.size() <= 1,
                "Too many snapshots matched for " + worldName);
            return requireSnapshot(name, snapshots.stream().findAny().orElse(null));
        } catch (Throwable t) {
            Closer closer = Closer.create();
            snapshots.forEach(closer::register);
            throw closer.rethrowAndClose(t);
        }
    }

    Snapshot requireSnapshot(String name, @Nullable Snapshot snapshot) throws IOException {
        assertNotNull(snapshot, "No snapshot for " + name);
        try {
            assertEquals(name, snapshot.getInfo().getDisplayName());
        } catch (Throwable t) {
            Closer closer = Closer.create();
            closer.register(snapshot);
            throw closer.rethrowAndClose(t);
        }
        return snapshot;
    }

    ArchiveDir getRootOfArchive(Path archive) throws IOException {
        return archiveNioSupport.tryOpenAsDir(archive)
            .orElseThrow(() -> new AssertionError("No archive opener for " + archive));
    }
}
