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

package com.sk89q.worldedit.fabric.internal;

import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.internal.wna.NativeAdapter;
import com.sk89q.worldedit.internal.wna.NativeBlockState;
import com.sk89q.worldedit.internal.wna.NativePosition;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import net.minecraft.core.BlockPos;

public final class FabricNativeAdapter implements NativeAdapter {
    public static final FabricNativeAdapter INSTANCE = new FabricNativeAdapter();

    private FabricNativeAdapter() {
    }

    @Override
    public NativeBlockState toNative(BlockState state) {
        return (NativeBlockState) FabricAdapter.adapt(state);
    }

    @Override
    public BlockState fromNative(NativeBlockState state) {
        return FabricAdapter.adapt((net.minecraft.world.level.block.state.BlockState) state);
    }

    @Override
    public NativePosition newBlockPos(BlockVector3 pos) {
        return (NativePosition) new BlockPos(pos.x(), pos.y(), pos.z());
    }
}
