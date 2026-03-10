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

package com.sk89q.worldedit.world.snapshot.experimental;

import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

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
     * Get a snapshot from its info.
     *
     * @param info the snapshot info, as previously obtained from this database
     * @return the snapshot if still available
     */
    default Optional<Snapshot> getSnapshot(SnapshotInfo info) throws IOException {
        return getSnapshot(info.getName());
    }

    /**
     * Get all snapshot infos by world, unsorted. The stream should be
     * {@linkplain Stream#close() closed}, as it may allocate filesystem or network resources.
     *
     * @param worldName the name of the world
     * @return a stream of all snapshot infos for the given world in this database
     */
    Stream<SnapshotInfo> getSnapshotInfos(String worldName) throws IOException;

    default Stream<SnapshotInfo> getSnapshotInfosNewestFirst(String worldName) throws IOException {
        return getSnapshotInfos(worldName).sorted(Comparator.<SnapshotInfo>naturalOrder().reversed());
    }

    default Stream<SnapshotInfo> getSnapshotInfosOldestFirst(String worldName) throws IOException {
        return getSnapshotInfos(worldName).sorted();
    }
}
