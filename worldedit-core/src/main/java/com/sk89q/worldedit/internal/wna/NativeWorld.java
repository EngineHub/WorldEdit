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

import org.enginehub.linbus.tree.LinCompoundTag;

/**
 * The equivalent of {@link com.sk89q.worldedit.world.World}, but in the platform's base.
 */
public interface NativeWorld {
    /**
     * Hacky way to get the adapter. Ideally would be part of an internal platform API or something.
     *
     * @return the adapter
     */
    NativeAdapter getAdapter();

    int getSectionIndex(int y);

    int getYForSectionIndex(int index);

    NativeChunk getChunk(int chunkX, int chunkZ);

    void notifyBlockUpdate(NativePosition pos, NativeBlockState oldState, NativeBlockState newState);

    void markBlockChanged(NativePosition pos);

    void updateLightingForBlock(NativePosition position);

    boolean updateTileEntity(NativePosition position, LinCompoundTag tag);

    void notifyNeighbors(NativePosition pos, NativeBlockState oldState, NativeBlockState newState, boolean events);

    void updateBlock(NativePosition pos, NativeBlockState oldState, NativeBlockState newState);

    void updateNeighbors(NativePosition pos, NativeBlockState oldState, NativeBlockState newState, int recursionLimit, boolean events);

    void onBlockStateChange(NativePosition pos, NativeBlockState oldState, NativeBlockState newState);
}
