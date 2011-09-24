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

/**
 * Represents the chunk store used by Minecraft alpha.
 *
 * @author sk89q
 */
public class FileLegacyChunkStore extends LegacyChunkStore {
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
    public FileLegacyChunkStore(File path) {
        this.path = path;
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
    @Override
    protected InputStream getInputStream(String f1, String f2, String name)
            throws DataException, IOException {
        String file = f1 + File.separator + f2 + File.separator + name;
        try {
            return new FileInputStream(new File(path, file));
        } catch (FileNotFoundException e) {
            throw new MissingChunkException();
        }
    }

    @Override
    public boolean isValid() {
        return true; // Yeah, oh well
    }
}
