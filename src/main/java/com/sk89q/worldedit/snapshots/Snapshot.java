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
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

/**
 *
 * @author sk89q
 */
public class Snapshot implements Comparable<Snapshot> {

    protected static Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    /**
     * Stores snapshot file.
     */
    protected File file;
    /**
     * Name of the snapshot;
     */
    protected String name;
    /**
     * Stores the date associated with the snapshot.
     */
    protected Calendar date;

    /**
     * Construct a snapshot restoration operation.
     *
     * @param repo
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
     * @throws DataException
     */
    public ChunkStore getChunkStore() throws IOException, DataException {
        ChunkStore chunkStore = _getChunkStore();

        logger.info("WorldEdit: Using " + chunkStore.getClass().getCanonicalName()
                + " for loading snapshot '" + file.getAbsolutePath() + "'");

        return chunkStore;
    }

    /**
     * Get a chunk store.
     *
     * @return
     * @throws IOException
     * @throws DataException
     */
    public ChunkStore _getChunkStore() throws IOException, DataException {
        if (file.getName().toLowerCase().endsWith(".zip")) {
            try {
                ChunkStore chunkStore = new TrueZipMcRegionChunkStore(file);

                if (!chunkStore.isValid()) {
                    return new TrueZipLegacyChunkStore(file);
                }

                return chunkStore;
            } catch (NoClassDefFoundError e) {
                ChunkStore chunkStore = new ZippedMcRegionChunkStore(file);

                if (!chunkStore.isValid()) {
                    return new ZippedLegacyChunkStore(file);
                }

                return chunkStore;
            }
        } else if (file.getName().toLowerCase().endsWith(".tar.bz2")
                || file.getName().toLowerCase().endsWith(".tar.gz")
                || file.getName().toLowerCase().endsWith(".tar")) {
            try {
                ChunkStore chunkStore = new TrueZipMcRegionChunkStore(file);

                if (!chunkStore.isValid()) {
                    return new TrueZipLegacyChunkStore(file);
                }

                return chunkStore;
            } catch (NoClassDefFoundError e) {
                throw new DataException("TrueZIP is required for .tar support");
            }
        } else {
            ChunkStore chunkStore = new FileMcRegionChunkStore(file);

            if (!chunkStore.isValid()) {
                return new FileLegacyChunkStore(file);
            }

            return chunkStore;
        }
    }

    /**
     * Check the zip/tar file it contains the given world.
     *
     * @return true if the zip/tar file contains the given world
     */
    public boolean containsWorld(String worldname) {
        try {
            if (file.getName().toLowerCase().endsWith(".zip")) {
                ZipFile entry = new ZipFile(file);
                return entry.getEntry(worldname) != null;
            } else if (file.getName().toLowerCase().endsWith(".tar.bz2")
                    || file.getName().toLowerCase().endsWith(".tar.gz")
                    || file.getName().toLowerCase().endsWith(".tar")) {
                try {
                    de.schlichtherle.util.zip.ZipFile entry = new de.schlichtherle.util.zip.ZipFile(file);

                    return entry.getEntry(worldname) != null;
                } catch (NoClassDefFoundError e) {
                    throw new DataException("TrueZIP is required for .tar support");
                }
            } else {
                return (file.getName().equalsIgnoreCase(worldname));
            }
        } catch (IOException ex) {
            // Skip the file, but print an error
            logger.info("Could not load snapshot: "
                    + file.getPath());
        } catch (DataException ex) {
            // No truezip, so tar file not supported.
            // Dont print, just skip the file.
        }
        return false;
    }

    /**
     * Get the snapshot's name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get the file for the snapshot.
     *
     * @return
     */
    public File getFile() {
        return file;
    }

    /**
     * Get the date associated with this snapshot.
     *
     * @return
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * Set the date of the snapshot.
     *
     * @param date
     */
    public void setDate(Calendar date) {
        this.date = date;
    }

    public int compareTo(Snapshot o) {
        if (o.date == null || date == null) {
            // Remove the folder from the name
            int i = name.indexOf("/"), j = o.name.indexOf("/");
            return name.substring((i > 0 ? 0 : i)).compareTo(o.name.substring((j > 0 ? 0 : j)));
        } else {
            return date.compareTo(o.date);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Snapshot) {
            return file.equals(((Snapshot) o).file);
        }
        return false;
    }
}
