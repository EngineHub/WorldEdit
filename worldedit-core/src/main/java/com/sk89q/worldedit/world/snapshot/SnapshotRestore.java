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

package com.sk89q.worldedit.world.snapshot;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.chunk.Chunk;
import com.sk89q.worldedit.world.storage.ChunkStore;
import com.sk89q.worldedit.world.storage.MissingChunkException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A snapshot restore operation.
 */
public class SnapshotRestore {

    private final Map<BlockVector2, ArrayList<BlockVector3>> neededChunks = new LinkedHashMap<>();
    private final ChunkStore chunkStore;
    private final EditSession editSession;
    private ArrayList<BlockVector2> missingChunks;
    private ArrayList<BlockVector2> errorChunks;
    private String lastErrorMessage;

    /**
     * Construct the snapshot restore operation.
     *
     * @param chunkStore The {@link ChunkStore} to restore from
     * @param editSession The {@link EditSession} to restore to
     * @param region The {@link Region} to restore to
     */
    public SnapshotRestore(ChunkStore chunkStore, EditSession editSession, Region region) {
        this.chunkStore = chunkStore;
        this.editSession = editSession;

        if (region instanceof CuboidRegion) {
            findNeededCuboidChunks(region);
        } else {
            findNeededChunks(region);
        }
    }

    /**
     * Find needed chunks in the axis-aligned bounding box of the region.
     *
     * @param region The {@link Region} to iterate
     */
    private void findNeededCuboidChunks(Region region) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        // First, we need to group points by chunk so that we only need
        // to keep one chunk in memory at any given moment
        for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                    BlockVector3 pos = BlockVector3.at(x, y, z);
                    checkAndAddBlock(pos);
                }
            }
        }
    }

    /**
     * Find needed chunks in the region.
     *
     * @param region The {@link Region} to iterate
     */
    private void findNeededChunks(Region region) {
        // First, we need to group points by chunk so that we only need
        // to keep one chunk in memory at any given moment
        for (BlockVector3 pos : region) {
            checkAndAddBlock(pos);
        }
    }

    private void checkAndAddBlock(BlockVector3 pos) {
        if (editSession.getMask() != null && !editSession.getMask().test(pos)) {
            return;
        }

        BlockVector2 chunkPos = ChunkStore.toChunk(pos);

        // Unidentified chunk
        if (!neededChunks.containsKey(chunkPos)) {
            neededChunks.put(chunkPos, new ArrayList<>());
        }

        neededChunks.get(chunkPos).add(pos);
    }

    /**
     * Get the number of chunks that are needed.
     *
     * @return a number of chunks
     */
    public int getChunksAffected() {
        return neededChunks.size();
    }

    /**
     * Restores to world.
     *
     * @throws MaxChangedBlocksException if the max block change limit is exceeded
     */
    public void restore() throws MaxChangedBlocksException {

        missingChunks = new ArrayList<>();
        errorChunks = new ArrayList<>();

        // Now let's start restoring!
        for (Map.Entry<BlockVector2, ArrayList<BlockVector3>> entry : neededChunks.entrySet()) {
            BlockVector2 chunkPos = entry.getKey();
            Chunk chunk;

            try {
                chunk = chunkStore.getChunk(chunkPos, editSession.getWorld());
                // Good, the chunk could be at least loaded

                // Now just copy blocks!
                for (BlockVector3 pos : entry.getValue()) {
                    try {
                        editSession.setBlock(pos, chunk.getBlock(pos));
                    } catch (DataException e) {
                        // this is a workaround: just ignore for now
                    }
                }
            } catch (MissingChunkException me) {
                missingChunks.add(chunkPos);
            } catch (IOException | DataException me) {
                errorChunks.add(chunkPos);
                lastErrorMessage = me.getMessage();
            }
        }
    }

    /**
     * Get a list of the missing chunks. restore() must have been called
     * already.
     *
     * @return a list of coordinates
     */
    public List<BlockVector2> getMissingChunks() {
        return missingChunks;
    }

    /**
     * Get a list of the chunks that could not have been loaded for other
     * reasons. restore() must have been called already.
     *
     * @return a list of coordinates
     */
    public List<BlockVector2> getErrorChunks() {
        return errorChunks;
    }

    /**
     * Checks to see where the backup succeeded in any capacity. False will
     * be returned if no chunk could be successfully loaded.
     *
     * @return true if there was total failure
     */
    public boolean hadTotalFailure() {
        return missingChunks.size() + errorChunks.size() == getChunksAffected();
    }

    /**
     * Get the last error message.
     *
     * @return a message
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

}
