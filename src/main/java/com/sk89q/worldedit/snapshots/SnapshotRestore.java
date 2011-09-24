package com.sk89q.worldedit.snapshots;
// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.regions.*;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.data.*;
import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author sk89q
 */
public class SnapshotRestore {
    /**
     * Store a list of chunks that are needed and the points in them.
     */
    private Map<BlockVector2D,ArrayList<Vector>> neededChunks =
            new LinkedHashMap<BlockVector2D,ArrayList<Vector>>();
    /**
     * Chunk store.
     */
    private ChunkStore chunkStore;
    /**
     * Count of the number of missing chunks.
     */
    private ArrayList<Vector2D> missingChunks;
    /**
     * Count of the number of chunks that could be loaded for other reasons.
     */
    private ArrayList<Vector2D> errorChunks;
    /**
     * Last error message.
     */
    private String lastErrorMessage;

    /**
     * Construct the snapshot restore operation.
     *
     * @param chunkStore
     * @param region
     */
    public SnapshotRestore(ChunkStore chunkStore, Region region) {
        this.chunkStore = chunkStore;

        if (region instanceof CuboidRegion) {
            findNeededCuboidChunks(region);
        } else {
            findNeededChunks(region);
        }
    }

    /**
     * Find needed chunks in the cuboid of the region.
     *
     * @param region
     */
    private void findNeededCuboidChunks(Region region) {
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        // First, we need to group points by chunk so that we only need
        // to keep one chunk in memory at any given moment
        for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                    Vector pos = new Vector(x, y, z);
                    BlockVector2D chunkPos = ChunkStore.toChunk(pos);

                    // Unidentified chunk
                    if (!neededChunks.containsKey(chunkPos)) {
                        neededChunks.put(chunkPos, new ArrayList<Vector>());
                    }

                    neededChunks.get(chunkPos).add(pos);
                }
            }
        }
    }

    /**
     * Find needed chunks in the region.
     *
     * @param region
     */
    private void findNeededChunks(Region region) {
        // First, we need to group points by chunk so that we only need
        // to keep one chunk in memory at any given moment
        for (Vector pos : region) {
            BlockVector2D chunkPos = ChunkStore.toChunk(pos);

            // Unidentified chunk
            if (!neededChunks.containsKey(chunkPos)) {
                neededChunks.put(chunkPos, new ArrayList<Vector>());
            }

            neededChunks.get(chunkPos).add(pos);
        }
    }

    /**
     * Get the number of chunks that are needed.
     *
     * @return
     */
    public int getChunksAffected() {
        return neededChunks.size();
    }

    /**
     * Restores to world.
     *
     * @param editSession
     * @throws MaxChangedBlocksException
     */
    public void restore(EditSession editSession)
            throws MaxChangedBlocksException {

        missingChunks = new ArrayList<Vector2D>();
        errorChunks = new ArrayList<Vector2D>();

        // Now let's start restoring!
        for (Map.Entry<BlockVector2D,ArrayList<Vector>> entry :
                neededChunks.entrySet()) {
            BlockVector2D chunkPos = entry.getKey();
            Chunk chunk;

            try {
                chunk = chunkStore.getChunk(chunkPos, editSession.getWorld().getName());
                // Good, the chunk could be at least loaded

                // Now just copy blocks!
                for (Vector pos : entry.getValue()) {
                    BaseBlock block = chunk.getBlock(pos);
                    editSession.setBlock(pos, block);
                }
            } catch (MissingChunkException me) {
                missingChunks.add(chunkPos);
            } catch (MissingWorldException me) {
                errorChunks.add(chunkPos);
                lastErrorMessage = me.getMessage();
            } catch (DataException de) {
                errorChunks.add(chunkPos);
                lastErrorMessage = de.getMessage();
            } catch (IOException ioe) {
                errorChunks.add(chunkPos);
                lastErrorMessage = ioe.getMessage();
            }
        }
    }

    /**
     * Get a list of the missing chunks. restore() must have been called
     * already.
     *
     * @return
     */
    public List<Vector2D> getMissingChunks() {
        return missingChunks;
    }

    /**
     * Get a list of the chunks that could not have been loaded for other
     * reasons. restore() must have been called already.
     *
     * @return
     */
    public List<Vector2D> getErrorChunks() {
        return errorChunks;
    }

    /**
     * Checks to see where the backup succeeded in any capacity. False will
     * be returned if no chunk could be successfully loaded.
     *
     * @return
     */
    public boolean hadTotalFailure() {
        return missingChunks.size() + errorChunks.size() == getChunksAffected();
    }

    /**
     * @return the lastErrorMessage
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}
