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

package com.sk89q.worldedit.world.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import java.util.zip.ZipException;
import java.util.Enumeration;

import com.sk89q.worldedit.world.DataException;
import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipFile;

/**
 * Represents the chunk store used by Minecraft but zipped. Uses
 * the replacement classes for java.util.zip.* from TrueZip.
 *
 * @author sk89q
 */
public class TrueZipMcRegionChunkStore extends McRegionChunkStore {

    /**
     * ZIP file.
     */
    protected File zipFile;
    /**
     * Actual ZIP.
     */
    protected ZipFile zip;
    /**
     * Folder inside the ZIP file to read from, if any.
     */
    protected String folder;

    /**
     * Create an instance. The folder argument lets you choose a folder or
     * path to look into in the ZIP for the files. Use a blank string for
     * the folder to not look into a subdirectory.
     *
     * @param zipFile
     * @param folder
     * @throws IOException
     * @throws ZipException 
     */
    public TrueZipMcRegionChunkStore(File zipFile, String folder)
            throws IOException, ZipException {
        this.zipFile = zipFile;
        this.folder = folder;

        zip = new ZipFile(zipFile);
    }

    /**
     * Create an instance. The subfolder containing the chunk data will
     * be detected.
     *
     * @param zipFile
     * @throws IOException
     * @throws ZipException
     */
    public TrueZipMcRegionChunkStore(File zipFile)
            throws IOException, ZipException {
        this.zipFile = zipFile;

        zip = new ZipFile(zipFile);
    }

    /**
     * Get the input stream for a chunk file.
     *
     * @param name
     * @return
     * @throws IOException
     * @throws DataException
     */
    @Override
    @SuppressWarnings("unchecked")
    protected InputStream getInputStream(String name, String worldname)
            throws IOException, DataException {

        // Detect subfolder for the world's files
        if (folder != null) {
            if (!folder.equals("")) {
                name = folder + "/" + name;
            }
        } else {
            Pattern pattern = Pattern.compile(".*\\.mc[ra]$");
            // World pattern
            Pattern worldPattern = Pattern.compile(worldname + "\\$");
            for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {
                ZipEntry testEntry = (ZipEntry) e.nextElement();
                // Check for world
                if (worldPattern.matcher(worldname).matches()) {
                    // Check for file
                    if (pattern.matcher(testEntry.getName()).matches()) {
                        folder = testEntry.getName().substring(0, testEntry.getName().lastIndexOf("/"));
                        name = folder + "/" + name;
                        break;
                    }
                }
            }

            // Check if world is found
            if (folder == null) {
                throw new MissingWorldException("Target world is not present in ZIP.", worldname);
            }
        }

        ZipEntry entry = getEntry(name);
        if (entry == null) {
            throw new MissingChunkException();
        }
        try {
            return zip.getInputStream(entry);
        } catch (ZipException e) {
            throw new IOException("Failed to read " + name + " in ZIP");
        }
    }

    /**
     * Get an entry from the ZIP, trying both types of slashes.
     * 
     * @param file
     * @return
     */
    private ZipEntry getEntry(String file) {
        ZipEntry entry = zip.getEntry(file);
        if (entry != null) {
            return entry;
        }
        return zip.getEntry(file.replace("/", "\\"));
    }

    /**
     * Close resources.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        zip.close();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isValid() {
        for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {

            ZipEntry testEntry = e.nextElement();

            if (testEntry.getName().matches(".*\\.mcr$") || testEntry.getName().matches(".*\\.mca$")) { // TODO: does this need a separate class?
                return true;
            }
        }

        return false;
    }
}
