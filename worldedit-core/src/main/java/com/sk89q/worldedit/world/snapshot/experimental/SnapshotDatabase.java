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

package com.sk89q.worldedit.world.snapshot.experimental;

import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static com.sk89q.worldedit.util.collection.MoreStreams.takeWhile;

/**
 * Handler for querying snapshot storage.
 */
public interface SnapshotDatabase {

    /**
     * Get the URI scheme handled by this database.
     */
    String getScheme();

    /**
     * Get a snapshot by name.
     *
     * @param name the name of the snapshot
     * @return the snapshot if available
     */
    Optional<Snapshot> getSnapshot(URI name) throws IOException;

    /**
     * Get all snapshots by world, unsorted. The stream should be
     * {@linkplain Stream#close() closed}, as it may allocate filesystem or network resources.
     *
     * @param worldName the name of the world
     * @return a stream of all snapshots for the given world in this database
     */
    Stream<Snapshot> getSnapshots(String worldName) throws IOException;

    default Stream<Snapshot> getSnapshotsNewestFirst(String worldName) throws IOException {
        return getSnapshots(worldName).sorted(SnapshotComparator.getInstance().reversed());
    }

    default Stream<Snapshot> getSnapshotsOldestFirst(String worldName) throws IOException {
        return getSnapshots(worldName).sorted(SnapshotComparator.getInstance());
    }

    default Stream<Snapshot> getSnapshotsBefore(String worldName, ZonedDateTime date) throws IOException {
        return takeWhile(
            // sorted from oldest -> newest, so all `before` are at the front
            getSnapshotsOldestFirst(worldName),
            snap -> snap.getInfo().getDateTime().isBefore(date)
        );
    }

    default Stream<Snapshot> getSnapshotsAfter(String worldName, ZonedDateTime date) throws IOException {
        return takeWhile(
            // sorted from newest -> oldest, so all `after` are at the front
            getSnapshotsNewestFirst(worldName),
            snap -> snap.getInfo().getDateTime().isAfter(date)
        );
    }

}
