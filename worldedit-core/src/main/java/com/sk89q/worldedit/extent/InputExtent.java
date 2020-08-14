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

package com.sk89q.worldedit.extent;

import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
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
    BlockState getBlock(BlockVector3 position);

    /**
     * Get a immutable snapshot of the block at the given location.
     *
     * @param position position of the block
     * @return the block
     */
    BaseBlock getFullBlock(BlockVector3 position);

    /**
     * Get the biome at the given location.
     *
     * <p>If there is no biome available, then the ocean biome should be
     * returned.</p>
     *
     * @param position the (x, z) location to check the biome at
     * @return the biome at the location
     * @deprecated Biomes in Minecraft are 3D now, use {@link InputExtent#getBiome(BlockVector3)}
     */
    @Deprecated
    default BiomeType getBiome(BlockVector2 position) {
        return getBiome(position.toBlockVector3());
    }

    /**
     * Get the biome at the given location.
     *
     * <p>
     *     If there is no biome available, then the ocean biome should be
     *     returned.
     * </p>
     *
     * <p>
     *     As implementation varies per Minecraft version, this may not exactly get
     *     this positions biome. On versions prior to 1.15, this will get the entire
     *     column. On later versions it will get the 4x4x4 cube's biome.
     * </p>
     *
     * @param position the (x, y, z) location to check the biome at
     * @return the biome at the location
     * @apiNote This must be overridden by new subclasses. See {@link NonAbstractForCompatibility}
     *          for details
     */
    @NonAbstractForCompatibility(
        delegateName = "getBiome",
        delegateParams = { BlockVector2.class }
    )
    default BiomeType getBiome(BlockVector3 position) {
        DeprecationUtil.checkDelegatingOverride(getClass());

        return getBiome(position.toBlockVector2());
    }
}
