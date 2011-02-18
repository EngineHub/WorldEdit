// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.snapshots;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author sk89q
 */
public class SnapshotRepository {
    /**
     * Stores the directory the snapshots come from.
     */
    private File dir;

    /**
     * Create a new instance of a repository.
     *
     * @param dir
     */
    public SnapshotRepository(File dir) {
        this.dir = dir;
    }

    /**
     * Create a new instance of a repository.
     *
     * @param dir
     */
    public SnapshotRepository(String dir) {
        this.dir = new File(dir);
    }

    /**
     * Get a list of snapshots in a directory. The newest snapshot is
     * near the top of the array.
     *
     * @return
     */
    public Snapshot[] getSnapshots() {
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                File f = new File(dir, name);
                return isValidSnapshot(f);
            }
        };

        String[] snapshotNames = dir.list(filter);

        if (snapshotNames == null || snapshotNames.length == 0) {
            return new Snapshot[0];
        }
        
        Snapshot[] snapshots = new Snapshot[snapshotNames.length];

        Arrays.sort(snapshotNames, Collections.reverseOrder());

        int i = 0;
        for (String name : snapshotNames) {
            snapshots[i] = new Snapshot(this, name);
            i++;
        }

        return snapshots;
    }

    /**
     * Get the default snapshot.
     *
     * @return
     */
    public Snapshot getDefaultSnapshot() {
        Snapshot[] snapshots = getSnapshots();

        if (snapshots.length == 0) {
            return null;
        }

        return snapshots[0];
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
    public boolean isValidSnapshot(File f) {
        if (!f.getName().matches("^[A-Za-z0-9_\\- \\./\\\\'\\$@~!%\\^\\*\\(\\)\\[\\]\\+\\{\\},\\?]+$")) {
            return false;
        }

        return (f.isDirectory() && (new File(f, "level.dat")).exists())
                || (f.isFile() && (
                    f.getName().toLowerCase().endsWith(".zip")
                    || f.getName().toLowerCase().endsWith(".tar.bz2")
                    || f.getName().toLowerCase().endsWith(".tar.gz")
                    || f.getName().toLowerCase().endsWith(".tar")
                    ));
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
