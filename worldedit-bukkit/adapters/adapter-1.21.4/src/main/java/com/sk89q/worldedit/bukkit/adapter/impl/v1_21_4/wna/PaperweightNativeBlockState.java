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

import com.sk89q.worldedit.bukkit.adapter.impl.v1_21_4.PaperweightAdapter;
import com.sk89q.worldedit.internal.wna.NativeBlockState;
import com.sk89q.worldedit.internal.wna.NativePosition;
import com.sk89q.worldedit.internal.wna.NativeWorld;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class PaperweightNativeBlockState implements NativeBlockState {
    public final BlockState delegate;

    public PaperweightNativeBlockState(BlockState delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isSame(NativeBlockState other) {
        return this.delegate == ((PaperweightNativeBlockState) other).delegate;
    }

    @Override
    public boolean isSameBlockType(NativeBlockState other) {
        return delegate.getBlock() == ((PaperweightNativeBlockState) other).delegate.getBlock();
    }

    @Override
    public boolean hasBlockEntity() {
        return delegate.hasBlockEntity();
    }

    @Override
    public NativeBlockState updateFromNeighbourShapes(NativeWorld world, NativePosition position) {
        return new PaperweightNativeBlockState(Block.updateFromNeighbourShapes(
            delegate, ((PaperweightNativeWorld) world).delegate, PaperweightAdapter.adaptPos(position)
        ));
    }
}
