package com.sk89q.worldedit.fabric.internal;

import com.sk89q.worldedit.util.SideEffect;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;

public interface ExtendedChunk {
    /**
     * {@link Chunk#setBlockState(BlockPos, BlockState, boolean)} with the extra
     * {@link SideEffect#UPDATE} flag.
     *
     * @param pos the position to set
     * @param state the state to set
     * @param moved I honestly have no idea and can't be bothered to investigate, we pass {@code
     *     false}
     * @param update the update flag, see side-effect for details
     * @return the old block state, or {@code null} if unchanged
     */
    @Nullable
    BlockState setBlockState(BlockPos pos, BlockState state, boolean moved, boolean update);
}
