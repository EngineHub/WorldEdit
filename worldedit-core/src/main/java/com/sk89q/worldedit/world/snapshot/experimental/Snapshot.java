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

package com.sk89q.worldedit.world.snapshot.experimental;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.chunk.Chunk;
import com.sk89q.worldedit.world.storage.ChunkStoreHelper;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents a world snapshot.
 */
public interface Snapshot extends Closeable {

    SnapshotInfo getInfo();

    /**
     * Get the chunk information for the given position. Implementations may ignore the Y-chunk
     * if its chunks are only stored in 2D.
     *
     * @param position the position of the chunk
     * @return the tag containing chunk data
     */
    CompoundTag getChunkTag(BlockVector3 position) throws DataException, IOException;

    /**
     * Get the chunk information for the given position.
     *
     * @see #getChunkTag(BlockVector3)
     * @see ChunkStoreHelper#getChunk(CompoundTag)
     */
    default Chunk getChunk(BlockVector3 position) throws DataException, IOException {
        return ChunkStoreHelper.getChunk(getChunkTag(position));
    }

    /**
     * Close this snapshot. This releases the IO handles used to load chunk information.
     */
    @Override
    void close() throws IOException;
}
