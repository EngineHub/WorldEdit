package com.sk89q.worldedit.internal.block;

import com.sk89q.worldedit.world.block.BlockState;

import java.util.OptionalInt;

public class BlockStateIdAcess {

    public interface Provider {

        OptionalInt getBlockStateId(BlockState holder);
    }

    private static Provider blockStateStateId;

    public static void setBlockStateStateId(Provider blockStateStateId) {
        BlockStateIdAcess.blockStateStateId = blockStateStateId;
    }

    public static OptionalInt getBlockStateId(BlockState holder) {
        return blockStateStateId.getBlockStateId((BlockState) holder);
    }

}
