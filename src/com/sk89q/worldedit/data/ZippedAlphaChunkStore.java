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

package com.sk89q.worldedit.data;

import java.io.*;
import java.util.zip.*;
import java.util.Enumeration;

/**
 * Represents the chunk store used by Minecraft alpha but zipped.
 *
 * @author sk89q
 */
public class ZippedAlphaChunkStore extends NestedFileChunkStore {
    /**
     * ZIP file.
     */
    private File zipFile;
    /**
     * Actual ZIP.
     */
    private ZipFile zip;
    /**
     * Folder inside the ZIP file to read from, if any.
     */
    private String folder;

    /**
     * Create an instance. The folder argument lets you choose a folder or
     * path to look into in the ZIP for the files. Use a blank string for
     * the folder to not look into a subdirectory.
     *
     * @param zipFile
     * @param folder
     * @throws IOException
     * @throws ZIPException
     */
    public ZippedAlphaChunkStore(File zipFile, String folder)
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
     * @param folder
     * @throws IOException
     * @throws ZIPException
     */
    public ZippedAlphaChunkStore(File zipFile)
            throws IOException, ZipException {
        this.zipFile = zipFile;

        zip = new ZipFile(zipFile);
    }

    /**
     * Get the input stream for a chunk file.
     *
     * @param f1
     * @param f2
     * @param name
     * @return
     * @throws IOException
     * @throws DataException
     */
    protected InputStream getInputStream(String f1, String f2, String name)
            throws IOException, DataException {
        String file = f1 + "/" + f2 + "/" + name;

        // Detect subfolder for the world's files
        if (folder != null) {
            if (!folder.equals("")) {
                file = folder + "/" + file;
            }
        } else {
            ZipEntry testEntry = zip.getEntry("level.dat");

            // So, the data is not in the root directory
            if (testEntry == null) {
                // Let's try a world/ sub-directory
                testEntry = zip.getEntry("world/level.dat");

                // So not there either...
                if (testEntry == null) {
                    for (Enumeration e = zip.entries(); e.hasMoreElements(); ) {
                        testEntry = (ZipEntry)e.nextElement();

                        // Whoo, found level.dat!
                        if (testEntry.getName().matches(".+/level\\.dat")) {
                            file = testEntry.getName().replaceAll("level\\.dat$", "")
                                    + file;
                            break;
                        }
                    }
                } else {
                    file = "world/" + file;
                }
            }
        }

        ZipEntry entry = zip.getEntry(file);
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
     * Close resources.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        zip.close();
    }
}
