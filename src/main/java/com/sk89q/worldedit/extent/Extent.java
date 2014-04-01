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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.extent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.pattern.Pattern;

import javax.annotation.Nullable;

/**
 * A world, portion of a world, clipboard, or other object that can have blocks set or
 * entities placed.
 */
public interface Extent {

    /**
     * Get a snapshot of the block at the given location.
     * </p>
     * If the given position is out of the bounds of the extent, then the behavior
     * is undefined (an air block could be returned). However, <code>null</code>
     * should <strong>not</strong> be returned.
     * </p>
     * The returned block is mutable and is a snapshot of the block at the time
     * of call. It has no position attached to it, so it could be reused in
     * {@link Pattern}s and so on.
     * </p>
     * Calls to this method can actually be quite expensive, so cache results
     * whenever it is possible, while being aware of the mutability aspect.
     * The cost, however, depends on the implementation and particular extent.
     *
     * @param position position of the block
     * @return the block, or null if the block does not exist
     */
    BaseBlock getBlock(Vector position);

    /**
     * Get the block ID at the given location.
     *
     * @param position position of the block
     * @return the block ID
     */
    int getBlockType(Vector position);

    /**
     * Get the data value of the block at the given location.
     *
     * @param position position of the block
     * @return the block data value
     */
    int getBlockData(Vector position);

    /**
     * Change the block at the given location to the given block. The operation may
     * not tie the given {@link BaseBlock} to the world, so future changes to the
     * {@link BaseBlock} do not affect the world until this method is called again.
     * </p>
     * The return value of this method indicates whether the change was probably
     * successful. It may not be successful if, for example, the location is out
     * of the bounds of the extent. It may be unsuccessful if the block passed
     * is the same as the one in the world. However, the return value is only an
     * estimation and it may be incorrect, but it could be used to count, for
     * example, the approximate number of changes.
     *
     * @param position position of the block
     * @param block block to set
     * @return true if the block was successfully set (return value may not be accurate)
     */
    boolean setBlock(Vector position, BaseBlock block) throws WorldEditException;

    /**
     * Return an {@link Operation} that should be called to tie up loose ends
     * (such as to commit changes in a buffer).
     *
     * @return an operation or null if there is none to execute
     */
    @Nullable Operation commit();

}
