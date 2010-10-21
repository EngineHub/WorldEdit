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

import de.schlichtherle.io.*;
import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;
import de.schlichtherle.io.FileOutputStream;
import java.io.*;

/**
 * Represents the chunk store used by Minecraft alpha.
 *
 * @author sk89q
 */
public class TrueZipAlphaChunkStore extends NestedFileChunkStore {
    /**
     * Folder to read from.
     */
    private File path;

    /**
     * Create an instance. The passed path is the folder to read the
     * chunk files from.
     *
     * @param path
     */
    public TrueZipAlphaChunkStore(java.io.File path) {
        this.path = new File(path);
        File root = findWorldPath(this.path);
        if (root != null) {
            this.path = root;
        }
    }

    /**
     * Create an instance. The passed path is the folder to read the
     * chunk files from.
     *
     * @param path
     */
    public TrueZipAlphaChunkStore(File path) {
        this.path = path;
        File root = findWorldPath(path);
        if (root != null) {
            this.path = root;
        }
    }

    /**
     * Create an instance. The passed path is the folder to read the
     * chunk files from.
     *
     * @param path
     */
    public TrueZipAlphaChunkStore(String path) {
        this.path = new File(path);
        File root = findWorldPath(this.path);
        if (root != null) {
            this.path = root;
        }
    }

    /**
     * Get the input stream for a chunk file.
     *
     * @param f1
     * @param f2
     * @param name
     * @return
     * @throws DataException
     * @throws IOException
     */
    protected InputStream getInputStream(String f1, String f2, String name)
            throws DataException, IOException {
        String file = f1 + File.separator + f2 + File.separator + name;
        try {
            return new FileInputStream(new File(path.getAbsolutePath(), file));
        } catch (FileNotFoundException e) {
            throw new MissingChunkException();
        }
    }

    /**
     * Find the root directory for the chunk files.
     *
     * @param path
     * @return
     */
    private File findWorldPath(File path) {
        if ((new File(path, "world")).exists()) {
            return new File(path, "world");
        }

        return searchForPath(path);
    }

    /**
     * Find the root directory for the chunk files.
     * 
     * @param path
     * @return
     */
    private File searchForPath(File path) {
        String[] children = path.list();
        // listFiles() returns java.io.File[]

        if (children == null) {
            return null;
        } else {
            for (String child : children) {
                File f = new File(path, child);
                
                if (f.isFile() && f.getName().equals("level.dat")) {
                    return path;
                } else if (f.isDirectory()) {
                    File res = findWorldPath(f);
                    if (res != null) {
                        return res;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Close the archive.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        try {
            path.umount();
        } catch (ArchiveException e) {
            throw new IOException(e);
        }
    }
}
