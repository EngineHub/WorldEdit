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
import com.sk89q.worldedit.*;

/**
 *
 * @author Albert
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
     * The maximum number of blocks to change at a time. If this number is
     * exceeded, a MaxChangedBlocksException exception will be
     * raised. -1 indicates no limit.
     */
    private int maxBlocks = -1;

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
     * Sets the block at position x, y, z with a block type.
     *
     * @param x
     * @param y
     * @param z
     * @param blockType
     * @return Whether the block changed
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
        return etc.getMCServer().e.a(x, y, z);
    }

    /**
     * Restores all blocks to their initial state.
     */
    public void undo() {
        for (Map.Entry<Point<Integer>,Integer> entry : original.entrySet()) {
            Point pt = (Point)entry.getKey();
            rawSetBlock((Integer)pt.getX(), (Integer)pt.getY(),(Integer)pt.getZ(),
                        (int)entry.getValue());
        }
    }

    /**
     * Sets to new state.
     */
    public void redo() {
        for (Map.Entry<Point<Integer>,Integer> entry : current.entrySet()) {
            Point pt = (Point)entry.getKey();
            rawSetBlock((Integer)pt.getX(), (Integer)pt.getY(),(Integer)pt.getZ(),
                        (int)entry.getValue());
        }
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
}
