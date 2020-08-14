/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.world.storage;

import com.sk89q.worldedit.world.DataException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents the chunk store used by Minecraft Alpha.
 */
public class FileLegacyChunkStore extends LegacyChunkStore {

    private final File path;

    /**
     * Create an instance. The passed path is the folder to read the
     * chunk files from.
     *
     * @param path path to a folder
     */
    public FileLegacyChunkStore(File path) {
        this.path = path;
    }

    /**
     * Get the input stream for a chunk file.
     *
     * @param f1 the first part of the pathname
     * @param f2 the second part of the pathname
     * @param name the name of the file
     * @return an input stream
     * @throws DataException if there is an error getting data for this chunk
     */
    @Override
    protected InputStream getInputStream(String f1, String f2, String name) throws DataException {
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
