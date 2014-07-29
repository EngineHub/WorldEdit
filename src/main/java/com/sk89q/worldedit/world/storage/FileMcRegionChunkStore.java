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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public class FileMcRegionChunkStore extends McRegionChunkStore {

    private File path;

    /**
     * Create an instance. The passed path is the folder to read the
     * chunk files from.
     * 
     * @param path a path
     */
    public FileMcRegionChunkStore(File path) {
        this.path = path;
    }

    @Override
    protected InputStream getInputStream(String name, String world) throws IOException, DataException {
        Pattern ext = Pattern.compile(".*\\.mc[ra]$"); // allow either file extension, both work the same
        File file = null;
        File[] files = new File(path, "region").listFiles();

        if (files == null) {
            throw new FileNotFoundException();
        }

        for (File f : files) {
            String tempName = f.getName().replaceFirst("mcr$", "mca"); // matcher only does one at a time
            if (ext.matcher(f.getName()).matches() && name.equalsIgnoreCase(tempName)) {
                // get full original path now
                file = new File(path + File.separator + "region" + File.separator + f.getName());
                break;
            }
        }

        try {
            if (file == null) throw new FileNotFoundException();
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
