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

import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.storage.ChunkStore;
import com.sk89q.worldedit.world.storage.FileLegacyChunkStore;
import com.sk89q.worldedit.world.storage.FileMcRegionChunkStore;
import com.sk89q.worldedit.world.storage.TrueZipLegacyChunkStore;
import com.sk89q.worldedit.world.storage.TrueZipMcRegionChunkStore;
import com.sk89q.worldedit.world.storage.ZippedLegacyChunkStore;
import com.sk89q.worldedit.world.storage.ZippedMcRegionChunkStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.zip.ZipFile;

/**
 * A snapshot is a backup.
 */
public class Snapshot implements Comparable<Snapshot> {

    protected static Logger logger = LoggerFactory.getLogger(Snapshot.class);

    protected File file;
    protected String name;
    protected Calendar date;

    /**
     * Construct a snapshot restoration operation.
     *
     * @param repo a repository
     * @param snapshot a snapshot name
     */
    public Snapshot(SnapshotRepository repo, String snapshot) {
        file = new File(repo.getDirectory(), snapshot);
        name = snapshot;
    }

    /**
     * Get a chunk store.
     *
     * @return a chunk store
     * @throws IOException
     * @throws DataException
     */
    public ChunkStore getChunkStore() throws IOException, DataException {
        ChunkStore chunkStore = internalGetChunkStore();

        logger.info("WorldEdit: Using " + chunkStore.getClass().getCanonicalName()
                + " for loading snapshot '" + file.getAbsolutePath() + "'");

        return chunkStore;
    }

    /**
     * Get a chunk store.
     *
     * @return a chunk store
     * @throws IOException
     * @throws DataException
     */
    private ChunkStore internalGetChunkStore() throws IOException, DataException {
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
                try (ZipFile entry = new ZipFile(file)) {
                    return (entry.getEntry(worldname) != null
                            || entry.getEntry(worldname + "/level.dat") != null);
                }
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
     * @return the name of the snapshot
     */
    public String getName() {
        return name;
    }

    /**
     * Get the file for the snapshot.
     *
     * @return path to the snapshot
     */
    public File getFile() {
        return file;
    }

    /**
     * Get the date associated with this snapshot.
     *
     * @return date for the snapshot
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * Set the date of the snapshot.
     *
     * @param date the date of the snapshot
     */
    public void setDate(Calendar date) {
        this.date = date;
    }

    @Override
    public int compareTo(Snapshot o) {
        if (o.date == null || date == null) {
            // Remove the folder from the name
            int i = name.indexOf('/'), j = o.name.indexOf('/');
            return name.substring((i > 0 ? 0 : i)).compareTo(o.name.substring((j > 0 ? 0 : j)));
        } else {
            return date.compareTo(o.date);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Snapshot && file.equals(((Snapshot) o).file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
