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

package com.sk89q.worldedit.world.storage;

import com.sk89q.worldedit.world.DataException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Represents the chunk store used by Minecraft alpha but zipped.
 */
public class ZippedLegacyChunkStore extends LegacyChunkStore {

    private ZipFile zip;
    private String folder;

    /**
     * Create an instance. The folder argument lets you choose a folder or
     * path to look into in the ZIP for the files. Use a blank string for
     * the folder to not look into a subdirectory.
     *
     * @param zipFile the zip file
     * @param folder the folder
     * @throws IOException
     * @throws ZipException
     */
    public ZippedLegacyChunkStore(File zipFile, String folder) throws IOException, ZipException {
        this.folder = folder;

        zip = new ZipFile(zipFile);
    }

    /**
     * Create an instance. The subfolder containing the chunk data will
     * be detected.
     *
     * @param zipFile the zip file
     * @throws IOException
     * @throws ZipException
     */
    public ZippedLegacyChunkStore(File zipFile) throws IOException, ZipException {
        zip = new ZipFile(zipFile);
    }

    /**
     * Get the input stream for a chunk file.
     *
     * @param f1 the first part of the path
     * @param f2 the second part of the path
     * @param name the name of the file
     * @return an input stream
     * @throws IOException
     * @throws DataException
     */
    @Override
    protected InputStream getInputStream(String f1, String f2, String name) throws IOException, DataException {
        String file = f1 + "/" + f2 + "/" + name;

        // Detect subfolder for the world's files
        if (folder != null) {
            if (!folder.isEmpty()) {
                file = folder + "/" + file;
            }
        } else {
            ZipEntry testEntry = zip.getEntry("level.dat");

            // So, the data is not in the root directory
            if (testEntry == null) {
                // Let's try a world/ sub-directory
                testEntry = getEntry("world/level.dat");

                Pattern pattern = Pattern.compile(".*[\\\\/]level\\.dat$");

                // So not there either...
                if (testEntry == null) {
                    for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {

                        testEntry = e.nextElement();

                        // Whoo, found level.dat!
                        if (pattern.matcher(testEntry.getName()).matches()) {
                            folder = testEntry.getName().replaceAll("level\\.dat$", "");
                            folder = folder.substring(0, folder.length() - 1);
                            file = folder + file;
                            break;
                        }
                    }
                } else {
                    file = "world/" + file;
                }
            }
        }

        ZipEntry entry = getEntry(file);
        if (entry == null) {
            throw new MissingChunkException();
        }
        try {
            return zip.getInputStream(entry);
        } catch (ZipException e) {
            throw new IOException("Failed to read " + file + " in ZIP");
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
    public boolean isValid() {
        return true; // Yeah, oh well
    }

}
