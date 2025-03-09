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

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.internal.util.collection.ChunkSectionPosSet;
import com.sk89q.worldedit.internal.wna.NativeBlockState;
import com.sk89q.worldedit.internal.wna.NativeChunk;
import com.sk89q.worldedit.internal.wna.NativeChunkSection;
import com.sk89q.worldedit.internal.wna.NativePosition;
import com.sk89q.worldedit.internal.wna.NativeWorld;
import com.sk89q.worldedit.internal.wna.WNASharedImpl;
import com.sk89q.worldedit.neoforge.internal.ExtendedChunk;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;

@Mixin(LevelChunk.class)
@Implements(@Interface(iface = NativeChunk.class, prefix = "nc$"))
public abstract class MixinNativeChunk extends ChunkAccess {
    @Unique
    private static final Set<Heightmap.Types> HEIGHTMAPS = EnumSet.of(
        Heightmap.Types.WORLD_SURFACE,
        Heightmap.Types.OCEAN_FLOOR,
        Heightmap.Types.MOTION_BLOCKING,
        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
    );

    public MixinNativeChunk(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> biomeRegistry, long inhabitedTime, @org.jetbrains.annotations.Nullable LevelChunkSection[] sections, @org.jetbrains.annotations.Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, biomeRegistry, inhabitedTime, sections, blendingData);
    }

    @Shadow
    public abstract FullChunkStatus getFullStatus();

    @Shadow
    public abstract BlockState getBlockState(BlockPos pos);

    @Shadow
    public abstract @Nullable BlockState setBlockState(BlockPos pos, BlockState state, boolean moved);

    @Shadow
    public abstract Level getLevel();

    @Shadow
    public abstract void removeBlockEntity(BlockPos pos);

    @Shadow
    public abstract @Nullable BlockEntity getBlockEntity(BlockPos pos, LevelChunk.EntityCreationType creationType);

    @Shadow
    public abstract void addAndRegisterBlockEntity(BlockEntity blockEntity);

    @Shadow
    protected abstract <T extends BlockEntity> void updateBlockEntityTicker(T blockEntity);

    @Shadow public abstract void markUnsaved();

    public NativeWorld nc$getWorld() {
        return (NativeWorld) getLevel();
    }

    public boolean nc$isTicking() {
        return getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING);
    }

    public NativePosition nc$getWorldPos(int offsetX, int offsetY, int offsetZ) {
        return (NativePosition) getPos().getBlockAt(offsetX, offsetY, offsetZ);
    }

    public NativeBlockState nc$getBlockState(NativePosition blockPos) {
        return (NativeBlockState) getBlockState((BlockPos) blockPos);
    }

    public @Nullable NativeBlockState nc$setBlockState(NativePosition blockPos, NativeBlockState newState, boolean update) {
        return (NativeBlockState) ((ExtendedChunk) this).setBlockState(
            (BlockPos) blockPos, (BlockState) newState, false, update
        );
    }

    public void nc$markSectionChanged(int index, ChunkSectionPosSet changed) {
        ServerChunkCache serverChunkCache = (ServerChunkCache) getLevel().getChunkSource();
        ChunkHolder holder = serverChunkCache.getVisibleChunkIfPresent(getPos().toLong());
        if (holder != null) {
            if (holder.changedBlocksPerSection[index] == null) {
                holder.hasChangedSections = true;
                holder.changedBlocksPerSection[index] = new ShortOpenHashSet(changed.asSectionPosEncodedShorts());
            } else {
                holder.changedBlocksPerSection[index].addAll(changed.asSectionPosEncodedShorts());
            }
            // Trick to get the holder into the broadcast set
            ((ServerChunkCache) getLevel().getChunkSource()).onChunkReadyToSend(holder);
        }
    }

    public void nc$updateHeightmaps() {
        Heightmap.primeHeightmaps(this, HEIGHTMAPS);
    }

    public void nc$updateLightingForSectionAirChange(int index, boolean onlyAir) {
        getLevel().getLightEngine().updateSectionStatus(
            SectionPos.of(getPos(), getLevel().getSectionYFromSectionIndex(index)),
            onlyAir
        );
    }

    public void nc$removeSectionBlockEntity(int chunkX, int chunkY, int chunkZ) {
        removeBlockEntity(getPos().getBlockAt(chunkX, chunkY, chunkZ));
    }

    @SuppressWarnings("deprecation")
    public void nc$initializeBlockEntity(int chunkX, int chunkY, int chunkZ, NativeBlockState newState) {
        BlockPos pos = getPos().getBlockAt(chunkX, chunkY, chunkZ);
        BlockEntity blockEntity = this.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
        BlockState nativeState = (BlockState) newState;
        if (blockEntity == null) {
            blockEntity = ((EntityBlock) nativeState.getBlock()).newBlockEntity(pos, nativeState);
            if (blockEntity != null) {
                this.addAndRegisterBlockEntity(blockEntity);
            }
        } else {
            blockEntity.setBlockState(nativeState);
            this.updateBlockEntityTicker(blockEntity);
        }
    }

    public NativeChunkSection nc$getChunkSection(int index) {
        return (NativeChunkSection) getSection(index);
    }

    public NativeChunkSection nc$setChunkSection(int index, NativeChunkSection section, ChunkSectionPosSet modifiedBlocks) {
        Preconditions.checkPositionIndex(index, getSectionsCount());
        LevelChunkSection[] chunkSections = getSections();
        var oldSection = (NativeChunkSection) chunkSections[index];
        chunkSections[index] = (LevelChunkSection) section;
        WNASharedImpl.postChunkSectionReplacement((NativeChunk) this, index, oldSection, section, modifiedBlocks);
        markUnsaved();
        return oldSection;
    }
}
