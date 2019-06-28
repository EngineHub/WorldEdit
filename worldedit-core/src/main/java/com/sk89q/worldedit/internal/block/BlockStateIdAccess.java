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

package com.sk89q.worldedit.internal.block;

import com.sk89q.worldedit.world.block.BlockState;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkState;

public final class BlockStateIdAccess {

    private BlockStateIdAccess() {
    }

    public interface Provider {

        OptionalInt getBlockStateId(BlockState holder);
    }

    private static Provider blockStateStateId;

    public static void setBlockStateStateId(Provider blockStateStateId) {
        BlockStateIdAccess.blockStateStateId = blockStateStateId;
    }

    public static OptionalInt getBlockStateId(BlockState holder) {
        return blockStateStateId.getBlockStateId(holder);
    }

    public static @Nullable BlockState getBlockStateById(int id) {
        return id < blockStates.length ? blockStates[id] : null;
    }

    private static BlockState[] blockStates = new BlockState[2 << 13];

    public static void register(BlockState blockState) {
        OptionalInt id = getBlockStateId(blockState);
        if (id.isPresent()) {
            int i = id.getAsInt();
            if (i >= blockStates.length) {
                int curLength = blockStates.length;
                do {
                    curLength += curLength >> 1;
                } while (i >= curLength);
                blockStates = Arrays.copyOf(blockStates, curLength);
            }
            BlockState existing = blockStates[i];
            checkState(existing == null || existing == blockState,
                "BlockState %s is using the same block ID (%s) as BlockState %s",
                blockState, i, existing);
            blockStates[i] = blockState;
        }
    }

}
