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

package com.sk89q.worldedit.bukkit.adapter.impl.v1_21_3.wna;

import com.sk89q.worldedit.bukkit.adapter.impl.v1_21_3.PaperweightAdapter;
import com.sk89q.worldedit.internal.wna.NativeAdapter;
import com.sk89q.worldedit.internal.wna.NativeBlockState;
import com.sk89q.worldedit.internal.wna.NativeChunk;
import com.sk89q.worldedit.internal.wna.NativePosition;
import com.sk89q.worldedit.internal.wna.NativeWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.enginehub.linbus.tree.LinCompoundTag;

public final class PaperweightNativeWorld implements NativeWorld {

    private final PaperweightAdapter adapter;
    private final NativeAdapter nativeAdapter;
    final ServerLevel delegate;

    public PaperweightNativeWorld(PaperweightAdapter adapter, NativeAdapter nativeAdapter, ServerLevel delegate) {
        this.adapter = adapter;
        this.nativeAdapter = nativeAdapter;
        this.delegate = delegate;
    }

    @Override
    public NativeAdapter getAdapter() {
        return nativeAdapter;
    }

    @Override
    public int getSectionIndex(int y) {
        return delegate.getSectionIndex(y);
    }

    @Override
    public int getYForSectionIndex(int index) {
        return SectionPos.sectionToBlockCoord(delegate.getSectionYFromSectionIndex(index));
    }

    @Override
    public NativeChunk getChunk(int chunkX, int chunkZ) {
        return new PaperweightNativeChunk(this, delegate.getChunk(chunkX, chunkZ));
    }

    @Override
    public void notifyBlockUpdate(NativePosition pos, NativeBlockState oldState, NativeBlockState newState) {
        delegate.sendBlockUpdated(
            PaperweightAdapter.adaptPos(pos),
            ((PaperweightNativeBlockState) oldState).delegate,
            ((PaperweightNativeBlockState) newState).delegate,
            Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS
        );
    }

    @Override
    public void markBlockChanged(NativePosition pos) {
        delegate.getChunkSource().blockChanged(PaperweightAdapter.adaptPos(pos));
    }

    @Override
    public void updateLightingForBlock(NativePosition position) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("updateSkyLightSources");
        /* Paper removes the update here */
        profilerFiller.popPush("queueCheckLight");
        delegate.getChunkSource().getLightEngine().checkBlock(PaperweightAdapter.adaptPos(position));
        profilerFiller.pop();
    }

    @Override
    public boolean updateTileEntity(NativePosition position, LinCompoundTag tag) {
        CompoundTag nativeTag = (CompoundTag) adapter.fromNative(tag);
        BlockPos nativePos = PaperweightAdapter.adaptPos(position);
        BlockEntity tileEntity = delegate.getChunkAt(nativePos).getBlockEntity(nativePos);
        if (tileEntity == null) {
            return false;
        }
        tileEntity.loadWithComponents(nativeTag, delegate.registryAccess());
        tileEntity.setChanged();
        return true;
    }

    @Override
    public void notifyNeighbors(
        NativePosition pos, NativeBlockState oldState, NativeBlockState newState, boolean events
    ) {
        BlockPos nativePos = PaperweightAdapter.adaptPos(pos);
        if (events) {
            delegate.updateNeighborsAt(nativePos, ((PaperweightNativeBlockState) oldState).delegate.getBlock());
        } else {
            // When we don't want events, manually run the physics without them.
            Block block = ((PaperweightNativeBlockState) oldState).delegate.getBlock();
            fireNeighborChanged(nativePos, delegate, block, nativePos.west());
            fireNeighborChanged(nativePos, delegate, block, nativePos.east());
            fireNeighborChanged(nativePos, delegate, block, nativePos.below());
            fireNeighborChanged(nativePos, delegate, block, nativePos.above());
            fireNeighborChanged(nativePos, delegate, block, nativePos.north());
            fireNeighborChanged(nativePos, delegate, block, nativePos.south());
        }
        BlockState nativeNewState = ((PaperweightNativeBlockState) newState).delegate;
        if (nativeNewState.hasAnalogOutputSignal()) {
            delegate.updateNeighbourForOutputSignal(nativePos, nativeNewState.getBlock());
        }
    }

    private void fireNeighborChanged(BlockPos pos, ServerLevel world, Block block, BlockPos neighborPos) {
        world.getBlockState(neighborPos).handleNeighborChanged(world, neighborPos, block, null, false);
    }

    @Override
    public void updateBlock(NativePosition pos, NativeBlockState oldState, NativeBlockState newState) {
        BlockPos nativePos = PaperweightAdapter.adaptPos(pos);
        BlockState nativeOldState = ((PaperweightNativeBlockState) oldState).delegate;
        BlockState nativeNewState = ((PaperweightNativeBlockState) newState).delegate;
        nativeOldState.onRemove(delegate, nativePos, nativeNewState, false);
        nativeNewState.onPlace(delegate, nativePos, nativeOldState, false);
    }

    @Override
    public void updateNeighbors(
        NativePosition pos, NativeBlockState oldState, NativeBlockState newState, int recursionLimit, boolean events
    ) {
        BlockPos nativePos = PaperweightAdapter.adaptPos(pos);
        BlockState nativeOldState = ((PaperweightNativeBlockState) oldState).delegate;
        BlockState nativeNewState = ((PaperweightNativeBlockState) newState).delegate;
        nativeOldState.updateIndirectNeighbourShapes(delegate, nativePos, Block.UPDATE_CLIENTS, recursionLimit);
        if (events) {
            BlockPhysicsEvent event = new BlockPhysicsEvent(
                CraftBlock.at(delegate, nativePos),
                CraftBlockData.fromData(nativeNewState)
            );
            delegate.getCraftServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }
        nativeNewState.updateNeighbourShapes(delegate, nativePos, Block.UPDATE_CLIENTS, recursionLimit);
        nativeNewState.updateIndirectNeighbourShapes(delegate, nativePos, Block.UPDATE_CLIENTS, recursionLimit);
    }

    @Override
    public void onBlockStateChange(NativePosition pos, NativeBlockState oldState, NativeBlockState newState) {
        delegate.onBlockStateChange(
            PaperweightAdapter.adaptPos(pos),
            ((PaperweightNativeBlockState) oldState).delegate,
            ((PaperweightNativeBlockState) newState).delegate
        );
    }
}
