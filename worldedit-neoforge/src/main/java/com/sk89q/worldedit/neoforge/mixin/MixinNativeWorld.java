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

import com.sk89q.worldedit.internal.wna.NativeAdapter;
import com.sk89q.worldedit.internal.wna.NativeBlockState;
import com.sk89q.worldedit.internal.wna.NativeChunk;
import com.sk89q.worldedit.internal.wna.NativePosition;
import com.sk89q.worldedit.internal.wna.NativeWorld;
import com.sk89q.worldedit.neoforge.internal.NBTConverter;
import com.sk89q.worldedit.neoforge.internal.NeoForgeNativeAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerLevel.class)
@Implements(@Interface(iface = NativeWorld.class, prefix = "nw$"))
public abstract class MixinNativeWorld extends Level {
    public MixinNativeWorld(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, bl, bl2, l, i);
    }

    public NativeAdapter nw$getAdapter() {
        return NeoForgeNativeAdapter.INSTANCE;
    }

    public int nw$getSectionIndex(int y) {
        return super.getSectionIndex(y);
    }

    public int nw$getYForSectionIndex(int index) {
        return SectionPos.sectionToBlockCoord(super.getSectionYFromSectionIndex(index));
    }

    public NativeChunk nw$getChunk(int chunkX, int chunkZ) {
        return (NativeChunk) getChunk(chunkX, chunkZ);
    }

    public void nw$notifyBlockUpdate(NativePosition pos, NativeBlockState oldState, NativeBlockState newState) {
        sendBlockUpdated(
            (BlockPos) pos, (BlockState) oldState, (BlockState) newState,
            Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS
        );
    }

    public void nw$markBlockChanged(NativePosition pos) {
        ((ServerChunkCache) getChunkSource()).blockChanged((BlockPos) pos);
    }

    public void nw$updateLightingForBlock(NativePosition position) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("updateSkyLightSources");
        getChunk(position.x() >> 4, position.z() >> 4).getSkyLightSources()
            .update(this, position.x() & 0xF, position.y(), position.z() & 0xF);
        profilerFiller.popPush("queueCheckLight");
        getChunkSource().getLightEngine().checkBlock((BlockPos) position);
        profilerFiller.pop();
    }

    public boolean nw$updateTileEntity(NativePosition position, LinCompoundTag tag) {
        CompoundTag nativeTag = NBTConverter.toNative(tag);
        BlockPos nativePos = (BlockPos) position;
        BlockEntity tileEntity = getChunkAt(nativePos).getBlockEntity(nativePos);
        if (tileEntity == null) {
            return false;
        }
        tileEntity.loadWithComponents(nativeTag, registryAccess());
        tileEntity.setChanged();
        return true;
    }

    public void nw$notifyNeighbors(NativePosition pos, NativeBlockState oldState, NativeBlockState newState, boolean events) {
        BlockPos nativePos = (BlockPos) pos;
        blockUpdated(nativePos, ((BlockState) oldState).getBlock());
        BlockState nativeNewState = (BlockState) newState;
        if (nativeNewState.hasAnalogOutputSignal()) {
            updateNeighbourForOutputSignal(nativePos, nativeNewState.getBlock());
        }
    }

    public void nw$updateBlock(NativePosition pos, NativeBlockState oldState, NativeBlockState newState) {
        BlockPos nativePos = (BlockPos) pos;
        BlockState nativeOldState = (BlockState) oldState;
        BlockState nativeNewState = (BlockState) newState;
        nativeOldState.onRemove(this, nativePos, nativeNewState, false);
        nativeNewState.onPlace(this, nativePos, nativeOldState, false);
    }

    public void nw$updateNeighbors(
        NativePosition pos, NativeBlockState oldState, NativeBlockState newState, int recursionLimit, boolean events
    ) {
        BlockPos nativePos = (BlockPos) pos;
        BlockState nativeOldState = (BlockState) oldState;
        BlockState nativeNewState = (BlockState) newState;
        nativeOldState.updateIndirectNeighbourShapes(this, nativePos, Block.UPDATE_CLIENTS, recursionLimit);
        nativeNewState.updateNeighbourShapes(this, nativePos, Block.UPDATE_CLIENTS, recursionLimit);
        nativeNewState.updateIndirectNeighbourShapes(this, nativePos, Block.UPDATE_CLIENTS, recursionLimit);
    }

    public void nw$onBlockStateChange(NativePosition pos, NativeBlockState oldState, NativeBlockState newState) {
        onBlockStateChange((BlockPos) pos, (BlockState) oldState, (BlockState) newState);
    }
}
