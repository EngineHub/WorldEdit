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
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.chunk.Chunk;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents chunk storage mechanisms.
 */
public abstract class ChunkStore implements Closeable {

    /**
     * The shift for converting to/from a chunk position.
     *
     * <p>
     * {@code >>} - to chunk
     * {@code <<} - from chunk
     * </p>
     */
    public static final int CHUNK_SHIFTS = 4;

    /**
     * The shift for converting to/from a 3D chunk position.
     *
     * <p>
     * {@code >>} - to Y of 3D-chunk
     * {@code <<} - from Y of 3D-chunk
     * </p>
     */
    public static final int CHUNK_SHIFTS_Y = 8;

    /**
     * Convert a position to a 3D-chunk. Y is counted in steps of 256.
     *
     * @param position the position
     * @return chunk coordinates
     */
    public static BlockVector3 toChunk3d(BlockVector3 position) {
        return position.shr(CHUNK_SHIFTS, CHUNK_SHIFTS_Y, CHUNK_SHIFTS);
    }

    /**
     * Convert a position to a chunk.
     *
     * @param position the position
     * @return chunk coordinates
     */
    public static BlockVector2 toChunk(BlockVector3 position) {
        return BlockVector2.at(position.getX() >> CHUNK_SHIFTS, position.getZ() >> CHUNK_SHIFTS);
    }

    /**
     * Get the tag for a chunk.
     *
     * @param position the position of the chunk
     * @return tag
     * @throws DataException thrown on data error
     * @throws IOException thrown on I/O error
     */
    public abstract CompoundTag getChunkTag(BlockVector2 position, World world) throws DataException, IOException;

    /**
     * Get a chunk at a location.
     *
     * @param position the position of the chunk
     * @return a chunk
     * @throws ChunkStoreException thrown if there is an error from the chunk store
     * @throws DataException thrown on data error
     * @throws IOException thrown on I/O error
     */
    public Chunk getChunk(BlockVector2 position, World world) throws DataException, IOException {
        CompoundTag rootTag = getChunkTag(position, world);
        return ChunkStoreHelper.getChunk(rootTag);
    }

    @Override
    public void close() throws IOException {
    }

    /**
     * Returns whether the chunk store is of this type.
     *
     * @return true if valid
     */
    public abstract boolean isValid();

}
