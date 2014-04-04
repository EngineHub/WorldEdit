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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.sk89q.worldedit.world.storage.MissingWorldException;

/**
 *
 * @author sk89q
 */
public class SnapshotRepository {

    /**
     * Stores the directory the snapshots come from.
     */
    protected File dir;
    /**
     * List of date parsers.
     */
    protected List<SnapshotDateParser> dateParsers = new ArrayList<SnapshotDateParser>();

    /**
     * Create a new instance of a repository.
     *
     * @param dir
     */
    public SnapshotRepository(File dir) {
        this.dir = dir;
        // If folder dont exist, make it.
        dir.mkdirs();

        dateParsers.add(new YYMMDDHHIISSParser());
        dateParsers.add(new ModificationTimerParser());
    }

    /**
     * Create a new instance of a repository.
     *
     * @param dir
     */
    public SnapshotRepository(String dir) {
        this(new File(dir));
    }

    /**
     * Get a list of snapshots in a directory. The newest snapshot is
     * near the top of the array.
     *
     * @param newestFirst
     * @return
     */
    public List<Snapshot> getSnapshots(boolean newestFirst, String worldname) throws MissingWorldException {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File f = new File(dir, name);
                return isValidSnapshot(f);
            }
        };

        File[] snapshotFiles = dir.listFiles();
        if (snapshotFiles == null) {
            throw new MissingWorldException(worldname);
        }
        List<Snapshot> list = new ArrayList<Snapshot>(snapshotFiles.length);

        for (File file : snapshotFiles) {
            if (isValidSnapshot(file)) {
                Snapshot snapshot = new Snapshot(this, file.getName());
                if (snapshot.containsWorld(worldname)) {
                    detectDate(snapshot);
                    list.add(snapshot);
                }
            } else if (file.isDirectory() && file.getName().equalsIgnoreCase(worldname)) {
                for (String name : file.list(filter)) {
                    Snapshot snapshot = new Snapshot(this, file.getName() + "/" + name);
                    detectDate(snapshot);
                    list.add(snapshot);
                }
            }
        }

        if (newestFirst) {
            Collections.sort(list, Collections.reverseOrder());
        } else {
            Collections.sort(list);
        }

        return list;
    }

    /**
     * Get the first snapshot after a date.
     *
     * @param date
     * @return
     */
    public Snapshot getSnapshotAfter(Calendar date, String world) throws MissingWorldException {
        List<Snapshot> snapshots = getSnapshots(true, world);
        Snapshot last = null;

        for (Snapshot snapshot : snapshots) {
            if (snapshot.getDate() != null
                    && snapshot.getDate().before(date)) {
                return last;
            }

            last = snapshot;
        }

        return last;
    }

    /**
     * Get the first snapshot before a date.
     *
     * @param date
     * @return
     */
    public Snapshot getSnapshotBefore(Calendar date, String world) throws MissingWorldException {
        List<Snapshot> snapshots = getSnapshots(false, world);
        Snapshot last = null;

        for (Snapshot snapshot : snapshots) {
            if (snapshot.getDate().after(date)) {
                return last;
            }

            last = snapshot;
        }

        return last;
    }

    /**
     * Attempt to detect a snapshot's date and assign it.
     *
     * @param snapshot
     */
    protected void detectDate(Snapshot snapshot) {
        for (SnapshotDateParser parser : dateParsers) {
            Calendar date = parser.detectDate(snapshot.getFile());
            if (date != null) {
                snapshot.setDate(date);
                return;
            }
        }

        snapshot.setDate(null);
    }

    /**
     * Get the default snapshot.
     *
     * @return
     */
    public Snapshot getDefaultSnapshot(String world) throws MissingWorldException {
        List<Snapshot> snapshots = getSnapshots(true, world);

        if (snapshots.size() == 0) {
            return null;
        }

        return snapshots.get(0);
    }

    /**
     * Check to see if a snapshot is valid.
     *
     * @param snapshot
     * @return whether it is a valid snapshot
     */
    public boolean isValidSnapshotName(String snapshot) {
        return isValidSnapshot(new File(dir, snapshot));
    }

    /**
     * Check to see if a snapshot is valid.
     *
     * @param f
     * @return whether it is a valid snapshot
     */
    protected boolean isValidSnapshot(File f) {
        if (!f.getName().matches("^[A-Za-z0-9_\\- \\./\\\\'\\$@~!%\\^\\*\\(\\)\\[\\]\\+\\{\\},\\?]+$")) {
            return false;
        }

        return (f.isDirectory() && (new File(f, "level.dat")).exists())
                || (f.isFile() && (f.getName().toLowerCase().endsWith(".zip")
                || f.getName().toLowerCase().endsWith(".tar.bz2")
                || f.getName().toLowerCase().endsWith(".tar.gz")
                || f.getName().toLowerCase().endsWith(".tar")));
    }

    /**
     * Get a snapshot.
     *
     * @param name
     * @return
     * @throws InvalidSnapshotException
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
     * @return
     */
    public File getDirectory() {
        return dir;
    }
}
