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

package com.sk89q.worldedit.bukkit.adapter.impl.v1_21_4.wna;

import com.sk89q.worldedit.internal.wna.NativeBlockState;
import com.sk89q.worldedit.internal.wna.NativeChunkSection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

public final class PaperweightNativeChunkSection implements NativeChunkSection {
    final LevelChunkSection delegate;

    public PaperweightNativeChunkSection(LevelChunkSection delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isOnlyAir() {
        return delegate.hasOnlyAir();
    }

    @Override
    public NativeBlockState getThenSetBlock(int i, int j, int k, NativeBlockState blockState) {
        BlockState nativeState = ((PaperweightNativeBlockState) blockState).delegate;
        if (isOnlyAir() && nativeState.isAir()) {
            return blockState;
        }
        return new PaperweightNativeBlockState(delegate.setBlockState(i, j, k, nativeState, false));
    }

    @Override
    public NativeBlockState getBlock(int i, int j, int k) {
        return new PaperweightNativeBlockState(delegate.getBlockState(i, j, k));
    }

    @Override
    public NativeChunkSection copy() {
        return new PaperweightNativeChunkSection(new LevelChunkSection(
            delegate.getStates().copy(), delegate.getBiomes().copy()
        ));
    }
}
