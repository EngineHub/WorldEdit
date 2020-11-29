package com.sk89q.worldedit.fabric.mixin;

import com.sk89q.worldedit.fabric.internal.ExtendedChunk;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import javax.annotation.Nullable;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk implements Chunk, ExtendedChunk {
    private boolean shouldUpdate = true;

    @Nullable
    @Override
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean moved, boolean update) {
        // save the state for the hook
        shouldUpdate = update;
        try {
            return setBlockState(pos, state, moved);
        } finally {
            // restore natural mode
            shouldUpdate = true;
        }
    }

    @Redirect(
        method = "setBlockState",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;resetBlock()V")
        ),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onBlockAdded(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V", ordinal = 0)
    )
    public void setBlockStateHook(BlockState target, World world, BlockPos pos, BlockState old, boolean move) {
        if (shouldUpdate) {
            target.onBlockAdded(world, pos, old, move);
        }
    }
}
