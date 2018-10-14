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

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import javax.annotation.Nullable;

/**
 * Accepts block and entity changes.
 */
public interface OutputExtent {

    /**
     * Change the block at the given location to the given block. The operation may
     * not tie the given {@link BlockStateHolder} to the world, so future changes to the
     * {@link BlockStateHolder} do not affect the world until this method is called again.
     *
     * <p>The return value of this method indicates whether the change was probably
     * successful. It may not be successful if, for example, the location is out
     * of the bounds of the extent. It may be unsuccessful if the block passed
     * is the same as the one in the world. However, the return value is only an
     * estimation and it may be incorrect, but it could be used to count, for
     * example, the approximate number of changes.</p>
     *
     * @param position position of the block
     * @param block block to set
     * @return true if the block was successfully set (return value may not be accurate)
     * @throws WorldEditException thrown on an error
     */
    boolean setBlock(BlockVector3 position, BlockStateHolder block) throws WorldEditException;

    /**
     * Set the biome.
     *
     * @param position the (x, z) location to set the biome at
     * @param biome the biome to set to
     * @return true if the biome was successfully set (return value may not be accurate)
     */
    boolean setBiome(BlockVector2 position, BaseBiome biome);

    /**
     * Return an {@link Operation} that should be called to tie up loose ends
     * (such as to commit changes in a buffer).
     *
     * @return an operation or null if there is none to execute
     */
    @Nullable Operation commit();

}
