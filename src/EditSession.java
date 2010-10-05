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

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import com.sk89q.worldedit.*;

/**
 * This class can wrap all block editing operations into one "edit session" that
 * stores the state of the blocks before modification. This allows for easy
 * undo or redo. In addition to that, this class can use a "queue mode" that
 * will know how to handle some special types of items such as signs and
 * torches. For example, torches must be placed only after there is already
 * a block below it, otherwise the torch will be placed as an item.
 *
 * @author sk89q
 */
public class EditSession {
    /**
     * Stores the original blocks before modification.
     */
    private HashMap<Point<Integer>,Integer> original = new HashMap<Point<Integer>,Integer>();
    /**
     * Stores the current blocks.
     */
    private HashMap<Point<Integer>,Integer> current = new HashMap<Point<Integer>,Integer>();
    /**
     * Queue.
     */
    private HashMap<Point<Integer>,Integer> queue = new HashMap<Point<Integer>,Integer>();
    /**
     * The maximum number of blocks to change at a time. If this number is
     * exceeded, a MaxChangedBlocksException exception will be
     * raised. -1 indicates no limit.
     */
    private int maxBlocks = -1;
    /**
     * Indicates whether some types of blocks should be queued for best
     * reproduction.
     */
    private boolean queued = false;
    /**
     * List of object types to queue.
     */
    private static HashSet<Integer> queuedBlocks = new HashSet<Integer>();

    static {
        queuedBlocks.add(50); // Torch
        queuedBlocks.add(37); // Yellow flower
        queuedBlocks.add(38); // Red rose
        queuedBlocks.add(39); // Brown mushroom
        queuedBlocks.add(40); // Red mushroom
        queuedBlocks.add(59); // Crops
        queuedBlocks.add(63); // Sign
        queuedBlocks.add(75); // Redstone torch (off)
        queuedBlocks.add(76); // Redstone torch (on)
        queuedBlocks.add(84); // Reed
    }

    /**
     * Default constructor. There is no maximum blocks limit.
     */
    public EditSession() {
    }

    /**
     * Construct the object with a maximum number of blocks.
     */
    public EditSession(int maxBlocks) {
        if (maxBlocks < -1) {
            throw new IllegalArgumentException("Max blocks must be >= -1");
        }
        this.maxBlocks = maxBlocks;
    }

    /**
     * Sets a block without changing history.
     * 
     * @param x
     * @param y
     * @param z
     * @param blockType
     * @return Whether the block changed
     */
    private boolean rawSetBlock(int x, int y, int z, int blockType) {
        return etc.getMCServer().e.d(x, y, z, blockType);
    }
    
    /**
     * Sets the block at position x, y, z with a block type. If queue mode is
     * enabled, blocks may not be actually set in world until flushQueue()
     * is called.
     *
     * @param x
     * @param y
     * @param z
     * @param blockType
     * @return Whether the block changed -- not entirely dependable
     */
    public boolean setBlock(int x, int y, int z, int blockType)
        throws MaxChangedBlocksException {
        Point<Integer> pt = new Point<Integer>(x, y, z);
        
        if (!original.containsKey(pt)) {
            original.put(pt, getBlock(x, y, z));

            if (maxBlocks != -1 && original.size() > maxBlocks) {
                throw new MaxChangedBlocksException(maxBlocks);
            }
        }
        
        current.put(pt, blockType);
        
        return smartSetBlock(x, y, z, blockType);
    }

    /**
     * Actually set the block. Will use queue.
     * 
     * @param x
     * @param y
     * @param z
     * @param blockType
     * @return
     */
    private boolean smartSetBlock(int x, int y, int z, int blockType) {
        Point<Integer> pt = new Point<Integer>(x, y, z);
        
        if (queued) {
            if (blockType != 0 && queuedBlocks.contains(blockType)
                    && rawGetBlock(x, y - 1, z) == 0) {
                queue.put(pt, blockType);
                return getBlock(x, y, z) != blockType;
            } else if (blockType == 0
                    && queuedBlocks.contains(rawGetBlock(x, y + 1, z))) {
                rawSetBlock(x, y + 1, z, 0); // Prevent items from being dropped
            }
        }

        return rawSetBlock(x, y, z, blockType);
    }

    /**
     * Gets the block type at a position x, y, z.
     *
     * @param x
     * @param y
     * @param z
     * @return Block type
     */
    public int getBlock(int x, int y, int z) {
        // In the case of the queue, the block may have not actually been
        // changed yet
        if (queued) {
            Point<Integer> pt = new Point<Integer>(x, y, z);
            if (current.containsKey(pt)) {
                return current.get(pt);
            }
        }
        return etc.getMCServer().e.a(x, y, z);
    }

    /**
     * Gets the block type at a position x, y, z.
     *
     * @param x
     * @param y
     * @param z
     * @return Block type
     */
    public int rawGetBlock(int x, int y, int z) {
        return etc.getMCServer().e.a(x, y, z);
    }

    /**
     * Restores all blocks to their initial state.
     */
    public void undo() {
        for (Map.Entry<Point<Integer>,Integer> entry : original.entrySet()) {
            Point pt = (Point)entry.getKey();
            smartSetBlock((Integer)pt.getX(), (Integer)pt.getY(),(Integer)pt.getZ(),
                    (int)entry.getValue());
        }
        flushQueue();
    }

    /**
     * Sets to new state.
     */
    public void redo() {
        for (Map.Entry<Point<Integer>,Integer> entry : current.entrySet()) {
            Point pt = (Point)entry.getKey();
            smartSetBlock((Integer)pt.getX(), (Integer)pt.getY(),(Integer)pt.getZ(),
                    (int)entry.getValue());
        }
        flushQueue();
    }

    /**
     * Get the number of changed blocks.
     * 
     */
    public int size() {
        return original.size();
    }

    /**
     * Get the maximum number of blocks that can be changed. -1 will be
     * returned if disabled.
     *
     * @return
     */
    public int getBlockChangeLimit() {
        return maxBlocks;
    }

    /**
     * Set the maximum number of blocks that can be changed.
     * 
     * @param maxBlocks -1 to disable
     */
    public void setBlockChangeLimit(int maxBlocks) {
        if (maxBlocks < -1) {
            throw new IllegalArgumentException("Max blocks must be >= -1");
        }
        this.maxBlocks = maxBlocks;
    }

    /**
     * Returns queue status.
     * 
     * @return
     */
    public boolean isQueueEnabled() {
        return queued;
    }

    /**
     * Queue certain types of block for better reproduction of those blocks.
     */
    public void enableQueue() {
        queued = true;
    }

    /**
     * Disable the queue. This will flush the queue.
     */
    public void disableQueue() {
        if (queued != false) {
            flushQueue();
        }
        queued = false;
    }

    /**
     * Finish off the queue.
     */
    public void flushQueue() {
        if (!queued) { return; }
        
        for (Map.Entry<Point<Integer>,Integer> entry : queue.entrySet()) {
            Point pt = (Point)entry.getKey();
            rawSetBlock((Integer)pt.getX(), (Integer)pt.getY(),(Integer)pt.getZ(),
                        (int)entry.getValue());
        }
    }
}
