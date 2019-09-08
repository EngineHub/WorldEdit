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

// $Id$

package com.sk89q.worldedit.world.snapshot;

import com.sk89q.worldedit.world.storage.MissingWorldException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FilenameFilter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A repository contains zero or more snapshots.
 */
public class SnapshotRepository {

    protected File dir;
    protected List<SnapshotDateParser> dateParsers = new ArrayList<>();

    /**
     * Create a new instance of a repository.
     *
     * @param dir the directory
     */
    public SnapshotRepository(File dir) {
        this.dir = dir;

        // If folder doesn't exist, make it
        dir.mkdirs();

        dateParsers.add(new YYMMDDHHIISSParser());
        dateParsers.add(new ModificationTimerParser());
    }

    /**
     * Create a new instance of a repository.
     *
     * @param dir the directory
     */
    public SnapshotRepository(String dir) {
        this(new File(dir));
    }

    /**
     * Get a list of snapshots in a directory. The newest snapshot is
     * near the top of the array.
     *
     * @param newestFirst true to get the newest first
     * @return a list of snapshots
     */
    public List<Snapshot> getSnapshots(boolean newestFirst, String worldName) throws MissingWorldException {
        FilenameFilter filter = (dir, name) -> {
            File f = new File(dir, name);
            return isValidSnapshot(f);
        };

        File[] snapshotFiles = dir.listFiles();
        if (snapshotFiles == null) {
            throw new MissingWorldException(worldName);
        }
        List<Snapshot> list = new ArrayList<>(snapshotFiles.length);

        for (File file : snapshotFiles) {
            if (isValidSnapshot(file)) {
                Snapshot snapshot = new Snapshot(this, file.getName());
                if (snapshot.containsWorld(worldName)) {
                    detectDate(snapshot);
                    list.add(snapshot);
                }
            } else if (file.isDirectory() && file.getName().equalsIgnoreCase(worldName)) {
                for (String name : file.list(filter)) {
                    Snapshot snapshot = new Snapshot(this, file.getName() + "/" + name);
                    detectDate(snapshot);
                    list.add(snapshot);
                }
            }
        }

        if (newestFirst) {
            list.sort(Collections.reverseOrder());
        } else {
            Collections.sort(list);
        }

        return list;
    }

    /**
     * Get the first snapshot after a date.
     *
     * @param date a date
     * @return a snapshot or null
     */
    @Nullable
    public Snapshot getSnapshotAfter(ZonedDateTime date, String world) throws MissingWorldException {
        List<Snapshot> snapshots = getSnapshots(true, world);
        Snapshot last = null;

        for (Snapshot snapshot : snapshots) {
            if (snapshot.getDate() != null && snapshot.getDate().compareTo(date) < 0) {
                return last;
            }

            last = snapshot;
        }

        return last;
    }

    /**
     * Get the first snapshot before a date.
     *
     * @param date a date
     * @return a snapshot or null
     */
    @Nullable
    public Snapshot getSnapshotBefore(ZonedDateTime date, String world) throws MissingWorldException {
        List<Snapshot> snapshots = getSnapshots(false, world);
        Snapshot last = null;

        for (Snapshot snapshot : snapshots) {
            if (snapshot.getDate().compareTo(date) > 0) {
                return last;
            }

            last = snapshot;
        }

        return last;
    }

    /**
     * Attempt to detect a snapshot's date and assign it.
     *
     * @param snapshot the snapshot
     */
    protected void detectDate(Snapshot snapshot) {
        for (SnapshotDateParser parser : dateParsers) {
            Calendar date = parser.detectDate(snapshot.getFile());
            if (date != null) {
                snapshot.setDate(date.toInstant().atZone(ZoneOffset.UTC));
                return;
            }
        }

        snapshot.setDate(null);
    }

    /**
     * Get the default snapshot.
     *
     * @param world the world name
     * @return a snapshot or null
     */
    @Nullable
    public Snapshot getDefaultSnapshot(String world) throws MissingWorldException {
        List<Snapshot> snapshots = getSnapshots(true, world);

        if (snapshots.isEmpty()) {
            return null;
        }

        return snapshots.get(0);
    }

    /**
     * Check to see if a snapshot is valid.
     *
     * @param snapshot a snapshot name
     * @return whether it is a valid snapshot
     */
    public boolean isValidSnapshotName(String snapshot) {
        return isValidSnapshot(new File(dir, snapshot));
    }

    /**
     * Check to see if a snapshot is valid.
     *
     * @param file the file to the snapshot
     * @return whether it is a valid snapshot
     */
    protected boolean isValidSnapshot(File file) {
        if (!file.getName().matches("^[A-Za-z0-9_\\- \\./\\\\'\\$@~!%\\^\\*\\(\\)\\[\\]\\+\\{\\},\\?]+$")) {
            return false;
        }

        if (file.isDirectory() && new File(file, "level.dat").exists()) {
            return true;
        }
        if (file.isFile()) {
            String lowerCaseFileName = file.getName().toLowerCase(Locale.ROOT);
            return lowerCaseFileName.endsWith(".zip")
                || lowerCaseFileName.endsWith(".tar.bz2")
                || lowerCaseFileName.endsWith(".tar.gz")
                || lowerCaseFileName.endsWith(".tar");
        }
        return false;
    }

    /**
     * Get a snapshot.
     *
     * @param name the name of the snapshot
     * @return a snapshot
     * @throws InvalidSnapshotException if the snapshot is invalid
     */
    public Snapshot getSnapshot(String name) throws InvalidSnapshotException {
        if (!isValidSnapshotName(name)) {
            throw new InvalidSnapshotException();
        }

        return new Snapshot(this, name);
    }

    /**
     * Get the snapshot directory.
     *
     * @return a path
     */
    public File getDirectory() {
        return dir;
    }

}
