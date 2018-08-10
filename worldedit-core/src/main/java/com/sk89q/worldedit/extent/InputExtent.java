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
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.block.BlockState;

/**
 * Provides the current state of blocks, entities, and so on.
 */
public interface InputExtent {

    /**
     * Get a snapshot of the block at the given location.
     *
     * <p>If the given position is out of the bounds of the extent, then the behavior
     * is undefined (an air block could be returned). However, {@code null}
     * should <strong>not</strong> be returned.</p>
     *
     * <p>The returned block is immutable and is a snapshot of the block at the time
     * of call. It has no position attached to it, so it could be reused in
     * {@link Pattern}s and so on.</p>
     *
     * @param position position of the block
     * @return the block
     */
    BlockState getBlock(Vector position);

    /**
     * Get a immutable snapshot of the block at the given location.
     *
     * @param position position of the block
     * @return the block
     */
    BaseBlock getFullBlock(Vector position);

    /**
     * Get the biome at the given location.
     *
     * <p>If there is no biome available, then the ocean biome should be
     * returned.</p>
     *
     * @param position the (x, z) location to check the biome at
     * @return the biome at the location
     */
    BaseBiome getBiome(Vector2D position);

}
