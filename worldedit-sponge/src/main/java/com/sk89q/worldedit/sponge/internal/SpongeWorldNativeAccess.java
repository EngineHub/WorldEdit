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

import com.sk89q.worldedit.internal.wna.WorldNativeAccess;
import com.sk89q.worldedit.sponge.SpongeAdapter;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.enginehub.linbus.tree.LinCompoundTag;

import java.lang.ref.WeakReference;
import java.util.Objects;
import javax.annotation.Nullable;

public class SpongeWorldNativeAccess implements WorldNativeAccess<LevelChunk, BlockState, BlockPos> {
    private static final int UPDATE = 1;
    private static final int NOTIFY = 2;

    private final WeakReference<ServerLevel> world;
    private SideEffectSet sideEffectSet;

    public SpongeWorldNativeAccess(WeakReference<ServerLevel> world) {
        this.world = world;
    }

    private ServerLevel getWorld() {
        return Objects.requireNonNull(world.get(), "The reference to the world was lost");
    }

    @Override
    public void setCurrentSideEffectSet(SideEffectSet sideEffectSet) {
        this.sideEffectSet = sideEffectSet;
    }

    @Override
    public LevelChunk getChunk(int x, int z) {
        return getWorld().getChunk(x, z);
    }

    @Override
    public BlockState toNative(com.sk89q.worldedit.world.block.BlockState state) {
        return (BlockState) SpongeAdapter.adapt(state);
    }

    @Override
    public BlockState getBlockState(LevelChunk chunk, BlockPos position) {
        return chunk.getBlockState(position);
    }

    @Nullable
    @Override
    public BlockState setBlockState(LevelChunk chunk, BlockPos position, BlockState state) {
        if (chunk instanceof ExtendedChunk) {
            return ((ExtendedChunk) chunk).setBlockState(
                position, state, false, sideEffectSet.shouldApply(SideEffect.UPDATE)
            );
        }
        return chunk.setBlockState(position, state, false);
    }

    @Override
    public BlockState getValidBlockForPosition(BlockState block, BlockPos position) {
        return Block.updateFromNeighbourShapes(block, getWorld(), position);
    }

    @Override
    public BlockPos getPosition(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    @Override
    public void updateLightingForBlock(BlockPos position) {
        getWorld().getChunkSource().getLightEngine().checkBlock(position);
    }

    @Override
    public boolean updateTileEntity(BlockPos position, LinCompoundTag tag) {
        CompoundTag nativeTag = NbtAdapter.adaptNMSToWorldEdit(tag);
        BlockEntity tileEntity = getWorld().getChunk(position).getBlockEntity(position);
        if (tileEntity == null) {
            return false;
        }
        tileEntity.setLevel(getWorld());
        tileEntity.loadWithComponents(nativeTag, getWorld().registryAccess());
        return true;
    }

    @Override
    public void notifyBlockUpdate(LevelChunk chunk, BlockPos position, BlockState oldState, BlockState newState) {
        if (chunk.getSections()[getWorld().getSectionIndex(position.getY())] != null) {
            getWorld().sendBlockUpdated(position, oldState, newState, UPDATE | NOTIFY);
        }
    }

    @Override
    public boolean isChunkTicking(LevelChunk chunk) {
        return chunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING);
    }

    @Override
    public void markBlockChanged(LevelChunk chunk, BlockPos position) {
        if (chunk.getSections()[getWorld().getSectionIndex(position.getY())] != null) {
            getWorld().getChunkSource().blockChanged(position);
        }
    }

    @Override
    public void notifyNeighbors(BlockPos pos, BlockState oldState, BlockState newState) {
        getWorld().updateNeighborsAt(pos, oldState.getBlock());
        if (newState.hasAnalogOutputSignal()) {
            getWorld().updateNeighbourForOutputSignal(pos, newState.getBlock());
        }
    }

    @Override
    public void updateBlock(BlockPos pos, BlockState oldState, BlockState newState) {
        ServerLevel world = getWorld();
        newState.onPlace(world, pos, oldState, false);
    }

    @Override
    public void updateNeighbors(BlockPos pos, BlockState oldState, BlockState newState, int recursionLimit) {
        ServerLevel world = getWorld();
        oldState.updateNeighbourShapes(world, pos, NOTIFY, recursionLimit);
        newState.updateIndirectNeighbourShapes(world, pos, NOTIFY, recursionLimit);
        newState.updateNeighbourShapes(world, pos, NOTIFY, recursionLimit);
    }

    @Override
    public void onBlockStateChange(BlockPos pos, BlockState oldState, BlockState newState) {
        getWorld().onBlockStateChange(pos, oldState, newState);
    }
}
