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

import com.sk89q.worldedit.data.*;
import java.io.*;

/**
 *
 * @author sk89q
 */
public class Snapshot {
    /**
     * Stores snapshot file.
     */
    private File file;
    /**
     * Name of the snapshot;
     */
    private String name;

    /**
     * Construct a snapshot restoration operation.
     * 
     * @param editSession
     * @param dir
     * @param snapshot
     */
    public Snapshot(SnapshotRepository repo, String snapshot) {
        file = new File(repo.getDirectory(), snapshot);
        name = snapshot;
    }

    /**
     * Get a chunk store.
     * 
     * @return
     * @throws IOException
     */
    public ChunkStore getChunkStore() throws IOException {
        if (file.getName().toLowerCase().endsWith(".zip")) {
            try {
                return new TrueZipAlphaChunkStore(file);
            } catch (NoClassDefFoundError e) {
                return new ZippedAlphaChunkStore(file);
            }
        } else {
            return new AlphaChunkStore(file);
        }
    }

    /**
     * Get the snapshot's name.
     * 
     * @return
     */
    public String getName() {
        return name;
    }
}
