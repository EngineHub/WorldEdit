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

import com.sk89q.worldedit.world.DataException;
import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

/**
 * Represents the chunk store used by Minecraft but zipped. Uses
 * the replacement classes for java.util.zip.* from TrueZip.
 */
public class TrueZipMcRegionChunkStore extends McRegionChunkStore {

    protected File zipFile;
    protected ZipFile zip;
    protected String folder;

    /**
     * Create an instance. The folder argument lets you choose a folder or
     * path to look into in the ZIP for the files. Use a blank string for
     * the folder to not look into a subdirectory.
     *
     * @param zipFile the ZIP file
     * @param folder the folder to look into
     * @throws IOException
     * @throws ZipException 
     */
    public TrueZipMcRegionChunkStore(File zipFile, String folder) throws IOException, ZipException {
        this.zipFile = zipFile;
        this.folder = folder;

        zip = new ZipFile(zipFile);
    }

    /**
     * Create an instance. The subfolder containing the chunk data will
     * be detected.
     *
     * @param zipFile the ZIP file
     * @throws IOException
     * @throws ZipException
     */
    public TrueZipMcRegionChunkStore(File zipFile) throws IOException, ZipException {
        this.zipFile = zipFile;

        zip = new ZipFile(zipFile);
    }

    /**
     * Get the input stream for a chunk file.
     *
     * @param name the name
     * @param worldName the world name
     * @return an input stream
     * @throws IOException
     * @throws DataException
     */
    @Override
    @SuppressWarnings("unchecked")
    protected InputStream getInputStream(String name, String worldName) throws IOException, DataException {
        // Detect subfolder for the world's files
        if (folder != null) {
            if (!folder.isEmpty()) {
                name = folder + "/" + name;
            }
        } else {
            Pattern pattern = Pattern.compile(".*\\.mc[ra]$");
            // World pattern
            Pattern worldPattern = Pattern.compile(worldName + "[\\\\/].*");
            for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {
                ZipEntry testEntry = e.nextElement();
                // Check for world
                if (worldPattern.matcher(testEntry.getName()).matches()) {
                    // Check for file
                    if (pattern.matcher(testEntry.getName()).matches()) {
                        folder = testEntry.getName().substring(0, testEntry.getName().lastIndexOf('/'));
                        if (folder.endsWith("poi")) continue;
                        name = folder + "/" + name;
                        break;
                    }
                }
            }

            // Check if world is found
            if (folder == null) {
                throw new MissingWorldException("Target world is not present in ZIP.", worldName);
            }
        }

        ZipEntry entry = getEntry(name);
        if (entry == null) {
            if (name.endsWith(".mca")) { // try old mcr format
                entry = getEntry(name.replace(".mca", ".mcr"));
                if (entry == null) {
                    throw new MissingChunkException();
                }
            } else {
                throw new MissingChunkException();
            }
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
     * @param file the file
     * @return an entry
     */
    private ZipEntry getEntry(String file) {
        ZipEntry entry = zip.getEntry(file);
        if (entry != null) {
            return entry;
        }
        return zip.getEntry(file.replace("/", "\\"));
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isValid() {
        for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {

            ZipEntry testEntry = e.nextElement();

            if (testEntry.getName().matches(".*\\.mcr$") || testEntry.getName().matches(".*\\.mca$")) {
                return true;
            }
        }

        return false;
    }

}
