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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public abstract class McRegionChunkStore extends ChunkStore {

    protected String curFilename = null;
    protected McRegionReader cachedReader = null;

    /**
     * Get the filename of a region file.
     * 
     * @param position chunk position
     * @return the filename
     */
    public static String getFilename(Vector2D position) {
        int x = position.getBlockX();
        int z = position.getBlockZ();

        return "r." + (x >> 5) + "." + (z >> 5) + ".mca";
    }

    protected McRegionReader getReader(Vector2D pos, String worldname) throws DataException, IOException {
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
    public CompoundTag getChunkTag(Vector2D position, World world) throws DataException, IOException {
        McRegionReader reader = getReader(position, world.getName());

        InputStream stream = reader.getChunkInputStream(position);
        Tag tag;

        try (NBTInputStream nbt = new NBTInputStream(stream)) {
            tag = nbt.readNamedTag().getTag();
            if (!(tag instanceof CompoundTag)) {
                throw new ChunkStoreException("CompoundTag expected for chunk; got " + tag.getClass().getName());
            }

            Map<String, Tag> children = ((CompoundTag) tag).getValue();
            CompoundTag rootTag = null;

            // Find Level tag
            for (Map.Entry<String, Tag> entry : children.entrySet()) {
                if (entry.getKey().equals("Level")) {
                    if (entry.getValue() instanceof CompoundTag) {
                        rootTag = (CompoundTag) entry.getValue();
                        break;
                    } else {
                        throw new ChunkStoreException("CompoundTag expected for 'Level'; got " + entry.getValue().getClass().getName());
                    }
                }
            }

            if (rootTag == null) {
                throw new ChunkStoreException("Missing root 'Level' tag");
            }

            return rootTag;
        }
    }

    /**
     * Get the input stream for a chunk file.
     * 
     * @param name the name of the chunk file
     * @param worldName the world name
     * @return an input stream
     * @throws IOException
     */
    protected abstract InputStream getInputStream(String name, String worldName) throws IOException, DataException;

    @Override
    public void close() throws IOException {
        if (cachedReader != null) {
            cachedReader.close();
        }
    }

}
