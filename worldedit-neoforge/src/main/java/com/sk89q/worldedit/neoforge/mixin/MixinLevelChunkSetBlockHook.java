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

package com.sk89q.worldedit.neoforge.mixin;

import com.sk89q.worldedit.neoforge.internal.ExtendedChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import javax.annotation.Nullable;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunkSetBlockHook extends ChunkAccess implements ExtendedChunk {
    private boolean shouldUpdate = true;

    public MixinLevelChunkSetBlockHook(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, long l, @org.jetbrains.annotations.Nullable LevelChunkSection[] levelChunkSections, @org.jetbrains.annotations.Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, registry, l, levelChunkSections, blendingData);
    }

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
            from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")
        ),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;onPlace(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V")
    )
    public void setBlockStateHook(BlockState target, Level world, BlockPos pos, BlockState old, boolean move) {
        boolean localShouldUpdate;
        MinecraftServer server = world.getServer();
        if (server == null || Thread.currentThread() != server.getRunningThread()) {
            // We're not on the server thread for some reason, WorldEdit will never be here
            // so we'll just ignore our flag
            localShouldUpdate = true;
        } else {
            localShouldUpdate = shouldUpdate;
        }
        if (localShouldUpdate) {
            target.onPlace(world, pos, old, move);
        }
    }
}
