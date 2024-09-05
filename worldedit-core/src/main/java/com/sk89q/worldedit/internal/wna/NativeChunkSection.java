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

package com.sk89q.worldedit.internal.wna;

import com.sk89q.worldedit.world.registry.BlockMaterial;

/**
 * Represents a 16x16x16 section of a chunk.
 *
 * <p>
 * Mapped onto the platform representation of a chunk section. However, unlike the platform representation, this
 * interface is <strong>not thread-safe</strong>, as it is intended to be used in a single-threaded context.
 * Internally this uses the platform representation's unsafe methods.
 * </p>
 */
public interface NativeChunkSection {
    /**
     * Set a block in the section.
     *
     * @param i the x-coordinate, 0-15
     * @param j the y-coordinate, 0-15
     * @param k the z-coordinate, 0-15
     * @param blockState the block state
     * @return the old block state
     */
    NativeBlockState getThenSetBlock(int i, int j, int k, NativeBlockState blockState);

    /**
     * Get a block in the section.
     *
     * @param i the x-coordinate, 0-15
     * @param j the y-coordinate, 0-15
     * @param k the z-coordinate, 0-15
     * @return the block state
     */
    NativeBlockState getBlock(int i, int j, int k);

    /**
     * Get if this section is made of only air (specifically, {@link BlockMaterial#isAir()}).
     *
     * @return true if the section is only air
     */
    boolean isOnlyAir();

    /**
     * Copy the section.
     *
     * @return the copy
     */
    NativeChunkSection copy();
}
