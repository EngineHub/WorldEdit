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

package com.sk89q.worldedit.bukkit.adapter.impl.v1_21_4.wna;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.sk89q.worldedit.bukkit.adapter.impl.v1_21_4.PaperweightAdapter;
import com.sk89q.worldedit.bukkit.adapter.impl.v1_21_4.StaticRefraction;
import com.sk89q.worldedit.internal.util.collection.ChunkSectionPosSet;
import com.sk89q.worldedit.internal.wna.NativeBlockState;
import com.sk89q.worldedit.internal.wna.NativeChunk;
import com.sk89q.worldedit.internal.wna.NativeChunkSection;
import com.sk89q.worldedit.internal.wna.NativePosition;
import com.sk89q.worldedit.internal.wna.NativeWorld;
import com.sk89q.worldedit.internal.wna.WNASharedImpl;
import com.sk89q.worldedit.math.BlockVector3;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;

public record PaperweightNativeChunk(NativeWorld owner, LevelChunk delegate) implements NativeChunk {
    private static final MethodHandle GET_VISIBLE_CHUNK_IF_PRESENT;
    private static final MethodHandle GET_CHANGED_BLOCKS_PER_SECTION;
    private static final MethodHandle SET_HAS_CHANGED_SECTIONS;
    private static final MethodHandle UPDATE_BLOCK_ENTITY_TICKER;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            Method m = ServerChunkCache.class.getDeclaredMethod(
                StaticRefraction.GET_VISIBLE_CHUNK_IF_PRESENT, long.class
            );
            m.setAccessible(true);
            GET_VISIBLE_CHUNK_IF_PRESENT = lookup.unreflect(m);

            Field f = ChunkHolder.class.getDeclaredField(StaticRefraction.CHANGED_BLOCKS_PER_SECTION);
            f.setAccessible(true);
            GET_CHANGED_BLOCKS_PER_SECTION = lookup.unreflectGetter(f);

            f = ChunkHolder.class.getDeclaredField(StaticRefraction.HAS_CHANGED_SECTIONS);
            f.setAccessible(true);
            SET_HAS_CHANGED_SECTIONS = lookup.unreflectSetter(f);

            m = LevelChunk.class.getDeclaredMethod(StaticRefraction.UPDATE_BLOCK_ENTITY_TICKER, BlockEntity.class);
            m.setAccessible(true);
            UPDATE_BLOCK_ENTITY_TICKER = lookup.unreflect(m);
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private static final Set<Heightmap.Types> HEIGHTMAPS = EnumSet.of(
        Heightmap.Types.WORLD_SURFACE,
        Heightmap.Types.OCEAN_FLOOR,
        Heightmap.Types.MOTION_BLOCKING,
        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
    );

    @Override
    public NativeWorld getWorld() {
        return owner;
    }

    @Override
    public boolean isTicking() {
        return delegate.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING);
    }

    @Override
    public NativePosition getWorldPos(int offsetX, int offsetY, int offsetZ) {
        ChunkPos pos = delegate.getPos();
        return new BlockVector3(pos.getBlockX(offsetX), offsetY, pos.getBlockZ(offsetZ));
    }

    @Override
    public NativeBlockState getBlockState(NativePosition blockPos) {
        return new PaperweightNativeBlockState(delegate.getBlockState(PaperweightAdapter.adaptPos(blockPos)));
    }

    @Override
    public @Nullable NativeBlockState setBlockState(NativePosition blockPos, NativeBlockState newState, boolean update) {
        return new PaperweightNativeBlockState(delegate.setBlockState(
            PaperweightAdapter.adaptPos(blockPos),
            ((PaperweightNativeBlockState) newState).delegate(),
            false,
            update
        ));
    }

    @Override
    public void markSectionChanged(int index, ChunkSectionPosSet changed) {
        ServerChunkCache serverChunkCache = (ServerChunkCache) delegate.getLevel().getChunkSource();
        ChunkHolder holder;
        try {
            holder = (ChunkHolder) GET_VISIBLE_CHUNK_IF_PRESENT.invoke(
                serverChunkCache,
                delegate.getPos().toLong()
            );
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
        if (holder != null) {
            ShortSet[] changedBlocksPerSection;
            try {
                changedBlocksPerSection = (ShortSet[]) GET_CHANGED_BLOCKS_PER_SECTION.invoke(holder);
            } catch (Throwable e) {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
            if (changedBlocksPerSection[index] == null) {
                try {
                    SET_HAS_CHANGED_SECTIONS.invoke(holder, true);
                } catch (Throwable e) {
                    Throwables.throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
                changedBlocksPerSection[index] = new ShortOpenHashSet(changed.asSectionPosEncodedShorts());
            } else {
                changedBlocksPerSection[index].addAll(changed.asSectionPosEncodedShorts());
            }
            // Trick to get the holder into the broadcast set
            serverChunkCache.onChunkReadyToSend(holder);
        }
    }

    @Override
    public void updateHeightmaps() {
        Heightmap.primeHeightmaps(delegate, HEIGHTMAPS);
    }

    @Override
    public void updateLightingForSectionAirChange(int index, boolean onlyAir) {
        delegate.getLevel().getLightEngine().updateSectionStatus(
            SectionPos.of(delegate.getPos(), delegate.getLevel().getSectionYFromSectionIndex(index)),
            onlyAir
        );
    }

    @Override
    public void removeSectionBlockEntity(int chunkX, int chunkY, int chunkZ) {
        delegate.removeBlockEntity(delegate.getPos().getBlockAt(chunkX, chunkY, chunkZ));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initializeBlockEntity(int chunkX, int chunkY, int chunkZ, NativeBlockState newState) {
        BlockPos pos = delegate.getPos().getBlockAt(chunkX, chunkY, chunkZ);
        BlockEntity blockEntity = delegate.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
        BlockState nativeState = ((PaperweightNativeBlockState) newState).delegate();
        if (blockEntity == null) {
            blockEntity = ((EntityBlock) nativeState.getBlock()).newBlockEntity(pos, nativeState);
            if (blockEntity != null) {
                delegate.addAndRegisterBlockEntity(blockEntity);
            }
        } else {
            blockEntity.setBlockState(nativeState);
            try {
                UPDATE_BLOCK_ENTITY_TICKER.invoke(blockEntity);
            } catch (Throwable e) {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public NativeChunkSection getChunkSection(int index) {
        return new PaperweightNativeChunkSection(delegate.getSection(index));
    }

    @Override
    public NativeChunkSection setChunkSection(int index, NativeChunkSection section, ChunkSectionPosSet modifiedBlocks) {
        Preconditions.checkPositionIndex(index, delegate.getSectionsCount());
        LevelChunkSection[] chunkSections = delegate.getSections();
        var oldSection = new PaperweightNativeChunkSection(chunkSections[index]);
        chunkSections[index] = ((PaperweightNativeChunkSection) section).delegate();
        WNASharedImpl.postChunkSectionReplacement(this, index, oldSection, section, modifiedBlocks);
        delegate.markUnsaved();
        return oldSection;
    }
}
