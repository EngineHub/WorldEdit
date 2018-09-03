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
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.chunk.AnvilChunk;
import com.sk89q.worldedit.world.chunk.AnvilChunk13;
import com.sk89q.worldedit.world.chunk.Chunk;
import com.sk89q.worldedit.world.chunk.OldChunk;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * Represents chunk storage mechanisms.
 */
public abstract class ChunkStore implements Closeable {

    /**
     * The DataVersion for Minecraft 1.13
     */
    public static final int DATA_VERSION_MC_1_13 = 1519;

    /**
     * >> to chunk
     * << from chunk
     */
    public static final int CHUNK_SHIFTS = 4;

    /**
     * Convert a position to a chunk.
     *
     * @param position the position
     * @return chunk coordinates
     */
    public static BlockVector2D toChunk(Vector position) {
        int chunkX = (int) Math.floor(position.getBlockX() / 16.0);
        int chunkZ = (int) Math.floor(position.getBlockZ() / 16.0);

        return new BlockVector2D(chunkX, chunkZ);
    }

    /**
     * Get the tag for a chunk.
     *
     * @param position the position of the chunk
     * @return tag
     * @throws DataException thrown on data error
     * @throws IOException thrown on I/O error
     */
    public abstract CompoundTag getChunkTag(Vector2D position, World world) throws DataException, IOException;

    /**
     * Get a chunk at a location.
     *
     * @param position the position of the chunk
     * @return a chunk
     * @throws ChunkStoreException thrown if there is an error from the chunk store
     * @throws DataException thrown on data error
     * @throws IOException thrown on I/O error
     */
    public Chunk getChunk(Vector2D position, World world) throws DataException, IOException {
        CompoundTag rootTag = getChunkTag(position, world);

        Map<String, Tag> children = rootTag.getValue();
        CompoundTag tag = null;

        // Find Level tag
        for (Map.Entry<String, Tag> entry : children.entrySet()) {
            if (entry.getKey().equals("Level")) {
                if (entry.getValue() instanceof CompoundTag) {
                    tag = (CompoundTag) entry.getValue();
                    break;
                } else {
                    throw new ChunkStoreException("CompoundTag expected for 'Level'; got " + entry.getValue().getClass().getName());
                }
            }
        }

        if (tag == null) {
            throw new ChunkStoreException("Missing root 'Level' tag");
        }

        int dataVersion = rootTag.getInt("DataVersion");
        if (dataVersion >= DATA_VERSION_MC_1_13) {
            return new AnvilChunk13(tag);
        }

        Map<String, Tag> tags = tag.getValue();
        if (tags.containsKey("Sections")) {
            return new AnvilChunk(world, tag);
        }

        return new OldChunk(world, tag);
    }

    public void close() throws IOException {
    }

    /**
     * Returns whether the chunk store is of this type.
     *
     * @return true if valid
     */
    public abstract boolean isValid();

}
