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

package com.sk89q.worldedit.extent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.function.pattern.Pattern;

/**
 * Provides the current state of blocks, entities, and so on.
 */
public interface InputExtent {

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
     * If only basic information about the block is required, then use of
     * {@link #getLazyBlock(Vector)} is recommended.
     *
     * @param position position of the block
     * @return the block
     */
    BaseBlock getBlock(Vector position);

    /**
     * Get a lazy, immutable snapshot of the block at the given location that only
     * immediately contains information about the block's type (and metadata).
     * </p>
     * Further information (such as NBT data) will be available <strong>by the
     * time of access</strong>. Therefore, it is not recommended that
     * this method is used if the world is being simulated at the time of
     * call. If the block needs to be stored for future use, then this method should
     * definitely not be used. Moreover, the block that is returned is immutable (or
     * should be), and therefore modifications should not be attempted on it. If a
     * modifiable copy is required, then the block should be cloned.
     * </p>
     * This method exists because it is sometimes important to inspect the block
     * at a given location, but {@link #getBlock(Vector)} may be too expensive in
     * the underlying implementation. It is also not possible to implement
     * caching if the returned object is mutable, so this methods allows caching
     * implementations to be used.
     *
     * @param position position of the block
     * @return the block
     */
    BaseBlock getLazyBlock(Vector position);

}
