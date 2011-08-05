// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileMcRegionChunkStore extends McRegionChunkStore {
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
    public FileMcRegionChunkStore(File path) {
        this.path = path;
    }

    @Override
    protected InputStream getInputStream(String name, String world) throws IOException,
            DataException {
        String fileName = "region" + File.separator + name;
        File file = new File(path, fileName);
        if (!file.exists()) {
            file = new File(path, "DIM-1" + File.separator + fileName);
        }
        
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new MissingChunkException();
        }
    }

    @Override
    public boolean isValid() {
        return new File(path, "region").isDirectory() ||
                new File(path, "DIM-1" + File.separator + "region").isDirectory();
    }

}
