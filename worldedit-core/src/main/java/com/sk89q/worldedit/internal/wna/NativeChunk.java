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

import com.sk89q.worldedit.internal.util.collection.ChunkSectionPosSet;

import javax.annotation.Nullable;

/**
 * Represents a chunk in the world. Made of {@link NativeChunkSection PlatformChunkSections}.
 */
public interface NativeChunk {
    NativeWorld getWorld();

    boolean isTicking();

    NativePosition getWorldPos(int offsetX, int offsetY, int offsetZ);

    NativeBlockState getBlockState(NativePosition blockPos);

    @Nullable
    NativeBlockState setBlockState(NativePosition blockPos, NativeBlockState newState, boolean update);

    void markSectionChanged(int index, ChunkSectionPosSet changed);

    void updateHeightmaps();

    void updateLightingForSectionAirChange(int index, boolean onlyAir);

    void removeSectionBlockEntity(int chunkX, int chunkY, int chunkZ);

    void initializeBlockEntity(int chunkX, int chunkY, int chunkZ, NativeBlockState newState);

    /**
     * Get the chunk section at the given index.
     *
     * @param index the index, from 0 to the max height divided by 16
     * @return the chunk section
     */
    NativeChunkSection getChunkSection(int index);

    /**
     * Replaces a chunk section in the given chunk. This method is also responsible for updating heightmaps
     * and creating block entities, to keep consistency with {@link #setBlockState(NativePosition, NativeBlockState, boolean)}
     * (the method we used to use). This is usually easily done by calling
     * {@link WNASharedImpl#postChunkSectionReplacement(NativeChunk, int, NativeChunkSection, NativeChunkSection, ChunkSectionPosSet)}.
     *
     * @param index the index, from 0 to the max height divided by 16
     * @param section the new chunk section
     * @param modifiedBlocks the set of modified blocks
     * @return the old chunk section
     */
    NativeChunkSection setChunkSection(int index, NativeChunkSection section, ChunkSectionPosSet modifiedBlocks);
}
