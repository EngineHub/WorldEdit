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

package com.sk89q.worldedit.forge.internal;

import com.sk89q.worldedit.forge.ForgeAdapter;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.internal.wna.WorldNativeAccess;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerChunkProvider;

import java.lang.ref.WeakReference;
import java.util.Objects;
import javax.annotation.Nullable;

public class ForgeWorldNativeAccess implements WorldNativeAccess<Chunk, BlockState, BlockPos> {
    private static final int UPDATE = 1;
    private static final int NOTIFY = 2;

    private final WeakReference<World> world;
    private SideEffectSet sideEffectSet;

    public ForgeWorldNativeAccess(WeakReference<World> world) {
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
    public Chunk getChunk(int x, int z) {
        return getWorld().getChunk(x, z);
    }

    @Override
    public BlockState toNative(com.sk89q.worldedit.world.block.BlockState state) {
        int stateId = BlockStateIdAccess.getBlockStateId(state);
        return BlockStateIdAccess.isValidInternalId(stateId)
            ? Block.getStateById(stateId)
            : ForgeAdapter.adapt(state);
    }

    @Override
    public BlockState getBlockState(Chunk chunk, BlockPos position) {
        return chunk.getBlockState(position);
    }

    @Nullable
    @Override
    public BlockState setBlockState(Chunk chunk, BlockPos position, BlockState state) {
        return chunk.setBlockState(position, state, false);
    }

    @Override
    public BlockState getValidBlockForPosition(BlockState block, BlockPos position) {
        return Block.getValidBlockForPosition(block, getWorld(), position);
    }

    @Override
    public BlockPos getPosition(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    @Override
    public void updateLightingForBlock(BlockPos position) {
        getWorld().getChunkProvider().getLightManager().checkBlock(position);
    }

    @Override
    public boolean updateTileEntity(BlockPos position, CompoundBinaryTag tag) {
        CompoundNBT nativeTag = NBTConverter.toNative(tag);
        return TileEntityUtils.setTileEntity(getWorld(), position, nativeTag);
    }

    @Override
    public void notifyBlockUpdate(BlockPos position, BlockState oldState, BlockState newState) {
        getWorld().notifyBlockUpdate(position, oldState, newState, UPDATE | NOTIFY);
    }

    @Override
    public boolean isChunkTicking(Chunk chunk) {
        return chunk.getLocationType().isAtLeast(ChunkHolder.LocationType.TICKING);
    }

    @Override
    public void markBlockChanged(BlockPos position) {
        ((ServerChunkProvider) getWorld().getChunkProvider()).markBlockChanged(position);
    }

    @Override
    public void notifyNeighbors(BlockPos pos, BlockState oldState, BlockState newState) {
        World world = getWorld();
        if (sideEffectSet.shouldApply(SideEffect.EVENTS)) {
            world.notifyNeighborsOfStateChange(pos, oldState.getBlock());
        } else {
            // Manually update each side
            Block block = oldState.getBlock();
            world.neighborChanged(pos.west(), block, pos);
            world.neighborChanged(pos.east(), block, pos);
            world.neighborChanged(pos.down(), block, pos);
            world.neighborChanged(pos.up(), block, pos);
            world.neighborChanged(pos.north(), block, pos);
            world.neighborChanged(pos.south(), block, pos);
        }
        if (newState.hasComparatorInputOverride()) {
            world.updateComparatorOutputLevel(pos, newState.getBlock());
        }
    }

    @Override
    public void updateNeighbors(BlockPos pos, BlockState oldState, BlockState newState, int recursionLimit) {
        World world = getWorld();
        oldState.func_241483_b_(world, pos, NOTIFY, recursionLimit);
        newState.func_241482_a_(world, pos, NOTIFY, recursionLimit);
        newState.func_241483_b_(world, pos, NOTIFY, recursionLimit);
    }

    @Override
    public void onBlockStateChange(BlockPos pos, BlockState oldState, BlockState newState) {
        getWorld().onBlockStateChange(pos, oldState, newState);
    }
}
