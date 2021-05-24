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
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.internal.wna.WorldNativeAccess;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.lang.ref.WeakReference;
import java.util.Objects;
import javax.annotation.Nullable;

public class FabricWorldNativeAccess implements WorldNativeAccess<WorldChunk, BlockState, BlockPos> {
    private static final int UPDATE = 1;
    private static final int NOTIFY = 2;

    private final WeakReference<World> world;
    private SideEffectSet sideEffectSet;

    public FabricWorldNativeAccess(WeakReference<World> world) {
        this.world = world;
    }

    private World getWorld() {
        return Objects.requireNonNull(world.get(), "The reference to the world was lost");
    }

    @Override
    public void setCurrentSideEffectSet(SideEffectSet sideEffectSet) {
        this.sideEffectSet = sideEffectSet;
    }

    @Override
    public WorldChunk getChunk(int x, int z) {
        return getWorld().getChunk(x, z);
    }

    @Override
    public BlockState toNative(com.sk89q.worldedit.world.block.BlockState state) {
        int stateId = BlockStateIdAccess.getBlockStateId(state);
        return BlockStateIdAccess.isValidInternalId(stateId)
            ? Block.getStateFromRawId(stateId)
            : FabricAdapter.adapt(state);
    }

    @Override
    public BlockState getBlockState(WorldChunk chunk, BlockPos position) {
        return chunk.getBlockState(position);
    }

    @Nullable
    @Override
    public BlockState setBlockState(WorldChunk chunk, BlockPos position, BlockState state) {
        if (chunk instanceof ExtendedChunk) {
            return ((ExtendedChunk) chunk).setBlockState(
                position, state, false, sideEffectSet.shouldApply(SideEffect.UPDATE)
            );
        }
        return chunk.setBlockState(position, state, false);
    }

    @Override
    public BlockState getValidBlockForPosition(BlockState block, BlockPos position) {
        return Block.postProcessState(block, getWorld(), position);
    }

    @Override
    public BlockPos getPosition(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    @Override
    public void updateLightingForBlock(BlockPos position) {
        getWorld().getChunkManager().getLightingProvider().checkBlock(position);
    }

    @Override
    public boolean updateTileEntity(BlockPos position, CompoundBinaryTag tag) {
        CompoundTag nativeTag = NBTConverter.toNative(tag);
        BlockEntity tileEntity = getWorld().getWorldChunk(position).getBlockEntity(position);
        if (tileEntity == null) {
            return false;
        }
        tileEntity.setLocation(getWorld(), position);
        tileEntity.fromTag(getWorld().getBlockState(position), nativeTag);
        return true;
    }

    @Override
    public void notifyBlockUpdate(BlockPos position, BlockState oldState, BlockState newState) {
        getWorld().updateListeners(position, oldState, newState, UPDATE | NOTIFY);
    }

    @Override
    public boolean isChunkTicking(WorldChunk chunk) {
        return chunk.getLevelType().isAfter(ChunkHolder.LevelType.TICKING);
    }

    @Override
    public void markBlockChanged(BlockPos position) {
        ((ServerChunkManager) getWorld().getChunkManager()).markForUpdate(position);
    }

    @Override
    public void notifyNeighbors(BlockPos pos, BlockState oldState, BlockState newState) {
        getWorld().updateNeighbors(pos, oldState.getBlock());
        if (newState.hasComparatorOutput()) {
            getWorld().updateComparators(pos, newState.getBlock());
        }
    }

    @Override
    public void updateNeighbors(BlockPos pos, BlockState oldState, BlockState newState, int recursionLimit) {
        World world = getWorld();
        oldState.prepare(world, pos, NOTIFY, recursionLimit);
        newState.updateNeighbors(world, pos, NOTIFY, recursionLimit);
        newState.prepare(world, pos, NOTIFY, recursionLimit);
    }

    @Override
    public void onBlockStateChange(BlockPos pos, BlockState oldState, BlockState newState) {
        getWorld().onBlockChanged(pos, oldState, newState);
    }
}
