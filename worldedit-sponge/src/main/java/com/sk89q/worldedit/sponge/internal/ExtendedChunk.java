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

package com.sk89q.worldedit.sponge.internal;

import com.sk89q.worldedit.util.SideEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;

public interface ExtendedChunk {
    /**
     * {@link LevelChunk#setBlockState(BlockPos, BlockState, int)} with the extra
     * {@link SideEffect#UPDATE} flag.
     *
     * @param pos the position to set
     * @param state the state to set
     * @param flag I honestly have no idea and can't be bothered to investigate, we pass {@code
     *     false}
     * @param update the update flag, see side-effect for details
     * @return the old block state, or {@code null} if unchanged
     */
    @Nullable
    BlockState setBlockState(BlockPos pos, BlockState state, int flag, boolean update);
}
