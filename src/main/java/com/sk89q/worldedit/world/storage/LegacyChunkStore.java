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
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Represents chunk stores that use Alpha's file format for storing chunks.
 * The code to resolve the filename is already implemented in this class
 * and an inheriting class merely needs to implement getInputStream().
 *
 * @author sk89q
 */
public abstract class LegacyChunkStore extends ChunkStore {
    /**
     * Get the filename of a chunk.
     *
     * @param pos
     * @param separator
     * @return
     */
    public static String getFilename(Vector2D pos, String separator) {
        int x = pos.getBlockX();
        int z = pos.getBlockZ();

        String folder1 = Integer.toString(divisorMod(x, 64), 36);
        String folder2 = Integer.toString(divisorMod(z, 64), 36);
        String filename = "c." + Integer.toString(x, 36)
                + "." + Integer.toString(z, 36) + ".dat";

        return folder1 + separator + folder2 + separator + filename;
    }

    /**
     * Get the filename of a chunk, using the system's default path
     * separator.
     *
     * @param pos
     * @return
     */
    public static String getFilename(Vector2D pos) {
        return getFilename(pos, File.separator);
    }

    /**
     * Get the tag for a chunk.
     *
     * @param pos
     * @return tag
     * @throws DataException
     * @throws IOException
     */
    @Override
    public CompoundTag getChunkTag(Vector2D pos, World world) throws DataException, IOException {
        int x = pos.getBlockX();
        int z = pos.getBlockZ();

        String folder1 = Integer.toString(divisorMod(x, 64), 36);
        String folder2 = Integer.toString(divisorMod(z, 64), 36);
        String filename = "c." + Integer.toString(x, 36)
                + "." + Integer.toString(z, 36) + ".dat";

        InputStream stream = getInputStream(folder1, folder2, filename);
        NBTInputStream nbt = new NBTInputStream(
                new GZIPInputStream(stream));
        Tag tag;

        try {
            tag = nbt.readTag();
            if (!(tag instanceof CompoundTag)) {
                throw new ChunkStoreException("CompoundTag expected for chunk; got "
                        + tag.getClass().getName());
            }

            Map<String, Tag> children = (Map<String, Tag>) ((CompoundTag) tag).getValue();
            CompoundTag rootTag = null;

            // Find Level tag
            for (Map.Entry<String, Tag> entry : children.entrySet()) {
                if (entry.getKey().equals("Level")) {
                    if (entry.getValue() instanceof CompoundTag) {
                        rootTag = (CompoundTag) entry.getValue();
                        break;
                    } else {
                        throw new ChunkStoreException("CompoundTag expected for 'Level'; got "
                                + entry.getValue().getClass().getName());
                    }
                }
            }

            if (rootTag == null) {
                throw new ChunkStoreException("Missing root 'Level' tag");
            }

            return rootTag;
        } finally {
            nbt.close();
        }
    }

    /**
     * Modulus, divisor-style.
     *
     * @param a
     * @param n
     * @return
     */
    private static int divisorMod(int a, int n) {
        return (int) (a - n * Math.floor(Math.floor(a) / (double) n));
    }

    /**
     * Get the input stream for a chunk file.
     *
     * @param f1
     * @param f2
     * @param name
     * @return
     * @throws IOException
     */
    protected abstract InputStream getInputStream(String f1, String f2, String name)
            throws IOException, DataException;
}
