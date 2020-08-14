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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;

import java.io.IOException;
import java.io.InputStream;

public abstract class McRegionChunkStore extends ChunkStore {

    protected String curFilename = null;
    protected McRegionReader cachedReader = null;

    /**
     * Get the filename of a region file.
     *
     * @param position chunk position
     * @return the filename
     */
    public static String getFilename(BlockVector2 position) {
        int x = position.getBlockX();
        int z = position.getBlockZ();

        return "r." + (x >> 5) + "." + (z >> 5) + ".mca";
    }

    protected McRegionReader getReader(BlockVector2 pos, String worldname) throws DataException, IOException {
        String filename = getFilename(pos);
        if (curFilename != null) {
            if (curFilename.equals(filename)) {
                return cachedReader;
            } else {
                try {
                    cachedReader.close();
                } catch (IOException ignored) {
                }
            }
        }
        InputStream stream = getInputStream(filename, worldname);
        cachedReader = new McRegionReader(stream);
        //curFilename = filename;
        return cachedReader;
    }

    @Override
    public CompoundTag getChunkTag(BlockVector2 position, World world) throws DataException, IOException {
        return ChunkStoreHelper.readCompoundTag(() -> {
            McRegionReader reader = getReader(position, world.getName());

            return reader.getChunkInputStream(position);
        });
    }

    /**
     * Get the input stream for a chunk file.
     *
     * @param name the name of the chunk file
     * @param worldName the world name
     * @return an input stream
     * @throws IOException if there is an error getting the chunk data
     */
    protected abstract InputStream getInputStream(String name, String worldName) throws IOException, DataException;

    @Override
    public void close() throws IOException {
        if (cachedReader != null) {
            cachedReader.close();
        }
    }

}
