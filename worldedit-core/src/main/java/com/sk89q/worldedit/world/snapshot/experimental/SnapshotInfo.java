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

import com.google.common.collect.ComparisonChain;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Information about a snapshot, such as name and date.
 */
public final class SnapshotInfo implements Comparable<SnapshotInfo> {

    public static SnapshotInfo create(URI name, ZonedDateTime dateTime) {
        return new SnapshotInfo(name, dateTime);
    }

    private final URI name;
    private final ZonedDateTime dateTime;

    private SnapshotInfo(URI name, ZonedDateTime dateTime) {
        this.name = name;
        this.dateTime = dateTime;
    }

    public URI getName() {
        return name;
    }

    public String getDisplayName() {
        if (name.getScheme().equals("snapfs")) {
            // Stored raw as the scheme specific part
            return name.getSchemeSpecificPart();
        }
        return name.toString();
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SnapshotInfo that = (SnapshotInfo) o;
        return Objects.equals(name, that.name)
            && Objects.equals(dateTime, that.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dateTime);
    }

    @Override
    public String toString() {
        return "SnapshotInfo{"
            + "name='" + name + '\''
            + ",date=" + dateTime
            + '}';
    }

    @Override
    public int compareTo(SnapshotInfo o) {
        return ComparisonChain.start()
            .compare(dateTime, o.dateTime)
            .compare(name, o.name)
            .result();
    }
}
