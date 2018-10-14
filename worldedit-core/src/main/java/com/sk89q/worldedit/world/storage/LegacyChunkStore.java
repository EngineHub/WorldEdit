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
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Represents chunk stores that use Alpha's file format for storing chunks.
 * The code to resolve the filename is already implemented in this class
 * and an inheriting class merely needs to implement getInputStream().
 */
public abstract class LegacyChunkStore extends ChunkStore {

    /**
     * Get the filename of a chunk.
     *
     * @param position chunk position
     * @param separator folder separator character
     * @return pathname
     */
    public static String getFilename(BlockVector2 position, String separator) {
        int x = position.getBlockX();
        int z = position.getBlockZ();

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
     * @param position chunk position
     * @return pathname
     */
    public static String getFilename(BlockVector2 position) {
        return getFilename(position, File.separator);
    }

    @Override
    public CompoundTag getChunkTag(BlockVector2 position, World world) throws DataException, IOException {
        int x = position.getBlockX();
        int z = position.getBlockZ();

        String folder1 = Integer.toString(divisorMod(x, 64), 36);
        String folder2 = Integer.toString(divisorMod(z, 64), 36);
        String filename = "c." + Integer.toString(x, 36)
                + "." + Integer.toString(z, 36) + ".dat";

        InputStream stream = getInputStream(folder1, folder2, filename);
        Tag tag;

        try (NBTInputStream nbt = new NBTInputStream(new GZIPInputStream(stream))) {
            tag = nbt.readNamedTag().getTag();
            if (!(tag instanceof CompoundTag)) {
                throw new ChunkStoreException("CompoundTag expected for chunk; got "
                        + tag.getClass().getName());
            }

            return (CompoundTag) tag;
        }
    }

    private static int divisorMod(int a, int n) {
        return (int) (a - n * Math.floor(Math.floor(a) / (double) n));
    }

    /**
     * Get the input stream for a chunk file.
     *
     * @param f1 the first part of the path
     * @param f2 the second part of the path
     * @param name the name
     * @return an input stream
     * @throws IOException
     */
    protected abstract InputStream getInputStream(String f1, String f2, String name) throws IOException, DataException;

}
