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

package com.sk89q.worldedit.extent.world.internal;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Watchdog;
import com.sk89q.worldedit.extent.AbstractBufferingExtent;
import com.sk89q.worldedit.extent.world.SideEffectExtent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.internal.util.collection.ChunkSectionMask;
import com.sk89q.worldedit.internal.wna.NativeBlockState;
import com.sk89q.worldedit.internal.wna.NativeChunk;
import com.sk89q.worldedit.internal.wna.NativeChunkSection;
import com.sk89q.worldedit.internal.wna.NativePosition;
import com.sk89q.worldedit.internal.wna.NativeWorld;
import com.sk89q.worldedit.internal.wna.WNASharedImpl;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.enginehub.linbus.tree.LinCompoundTag;

import java.util.Map;
import javax.annotation.Nullable;

/**
 * A special extent that buffers all changes as {@link NativeChunkSection chunk sections}. This allows highly optimized
 * block setting, but has a higher likelihood of different semantics compared to traditional block setting.
 */
public class SectionBufferingExtent extends AbstractBufferingExtent {

    private static long getTopSectionKey(BlockVector3 sectionPos) {
        return ((long) sectionPos.x() & 0xFF_FF_FF_FFL) | (((long) sectionPos.z() & 0xFF_FF_FF_FFL) << 32);
    }

    private record SectionData(
        NativeChunkSection section,
        ChunkSectionMask modified
    ) {
    }

    private final Long2ObjectMap<Int2ObjectArrayMap<SectionData>> sectionTable = new Long2ObjectLinkedOpenHashMap<>();
    private final BlockMap<LinCompoundTag> blockEntityMap = BlockMap.create();
    private final NativeWorld nativeWorld;
    private final SideEffectExtent sideEffectExtent;
    private boolean enabled;

    public SectionBufferingExtent(NativeWorld nativeWorld, SideEffectExtent sideEffectExtent) {
        this(nativeWorld, sideEffectExtent, true);
    }

    public SectionBufferingExtent(NativeWorld nativeWorld, SideEffectExtent sideEffectExtent, boolean enabled) {
        super(sideEffectExtent);
        this.nativeWorld = nativeWorld;
        this.sideEffectExtent = sideEffectExtent;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean commitRequired() {
        return enabled;
    }

    private BlockVector3 toSectionPos(BlockVector3 position) {
        return BlockVector3.at(
            position.x() >> 4,
            nativeWorld.getSectionIndex(position.y()),
            position.z() >> 4
        );
    }

    private @Nullable SectionData getSectionData(BlockVector3 sectionPos) {
        Int2ObjectArrayMap<SectionData> sections = sectionTable.get(getTopSectionKey(sectionPos));
        if (sections == null) {
            return null;
        }
        return sections.get(sectionPos.y());
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
        if (!enabled) {
            return setDelegateBlock(location, block);
        }
        if (!sideEffectExtent.isPostEditSimulationEnabled()) {
            throw new IllegalStateException("SectionBufferingExtent requires SideEffectExtent to have post-edit simulation enabled");
        }
        BlockVector3 sectionPos = toSectionPos(location);
        SectionData data = getSectionData(sectionPos);
        if (data != null) {
            NativeBlockState newState = nativeWorld.getAdapter().toNative(block.toImmutableState());
            NativeBlockState oldState = data.section.getThenSetBlock(
                location.x() & 0xF, location.y() & 0xF, location.z() & 0xF,
                newState
            );
            if (oldState == newState && block.toBaseBlock().getNbt() == null) {
                // Optimize out the change if it's the same
                return false;
            }
        } else {
            if (block.toBaseBlock().getNbt() == null) {
                BaseBlock existingState = getExtent().getFullBlock(location);
                if (existingState.toImmutableState() == block && existingState.getNbt() == null) {
                    // Optimize out the change if it's the same
                    return false;
                }
            }
            data = new SectionData(
                nativeWorld.getChunk(sectionPos.x(), sectionPos.z()).getChunkSection(sectionPos.y()).copy(),
                new ChunkSectionMask()
            );
            sectionTable.computeIfAbsent(getTopSectionKey(sectionPos), k -> new Int2ObjectArrayMap<>())
                .put(sectionPos.y(), data);
            data.section.getThenSetBlock(
                location.x() & 0xF, location.y() & 0xF, location.z() & 0xF,
                nativeWorld.getAdapter().toNative(block.toImmutableState())
            );
        }

        LinCompoundTag nbt = block.toBaseBlock().getNbt();
        if (nbt != null) {
            blockEntityMap.put(location, nbt);
        }

        data.modified.set(location.x() & 0xF, location.y() & 0xF, location.z() & 0xF);

        return true;
    }

    @Override
    protected BaseBlock getBufferedFullBlock(BlockVector3 position) {
        if (!enabled) {
            // Early exit if we're not enabled.
            return null;
        }
        BlockVector3 sectionPos = toSectionPos(position);
        SectionData data = getSectionData(sectionPos);
        if (data == null) {
            return null;
        }
        NativeBlockState state = data.section.getBlock(position.x() & 0xF, position.y() & 0xF, position.z() & 0xF);
        return nativeWorld.getAdapter().fromNative(state).toBaseBlock(blockEntityMap.get(position));
    }

    @Override
    protected Operation commitBefore() {
        if (!commitRequired()) {
            return null;
        }
        return new Operation() {
            @Override
            public Operation resume(RunContext run) {
                finishOperation();
                return null;
            }

            @Override
            public void cancel() {
            }
        };
    }

    private void finishOperation() {
        record SectionDataWithOld(
            NativeChunkSection section,
            NativeChunkSection oldSection,
            ChunkSectionMask modified
        ) {
        }


        Watchdog watchdog = WorldEdit.getInstance().getPlatformManager()
            .queryCapability(Capability.GAME_HOOKS).getWatchdog();

        Long2ObjectMap<Int2ObjectArrayMap<SectionDataWithOld>> effectData = new Long2ObjectLinkedOpenHashMap<>();

        int ops = 0;
        for (Long2ObjectMap.Entry<Int2ObjectArrayMap<SectionData>> entry : sectionTable.long2ObjectEntrySet()) {
            int chunkX = (int) entry.getLongKey();
            int chunkZ = (int) (entry.getLongKey() >> 32);
            NativeChunk chunk = nativeWorld.getChunk(chunkX, chunkZ);
            Int2ObjectArrayMap<SectionDataWithOld> oldSections = new Int2ObjectArrayMap<>(entry.getValue().size());
            for (Int2ObjectArrayMap.Entry<SectionData> sectionEntry : entry.getValue().int2ObjectEntrySet()) {
                SectionData data = sectionEntry.getValue();
                NativeChunkSection old = chunk.setChunkSection(sectionEntry.getIntKey(), data.section, data.modified);
                oldSections.put(sectionEntry.getIntKey(), new SectionDataWithOld(data.section, old, data.modified));
            }
            effectData.put(entry.getLongKey(), oldSections);

            chunk.updateHeightmaps();

            if (watchdog != null) {
                ops = updateWatchdog(ops, watchdog);
            }
        }
        sectionTable.clear();

        for (Map.Entry<BlockVector3, LinCompoundTag> entry : blockEntityMap.entrySet()) {
            BlockVector3 position = entry.getKey();
            LinCompoundTag tag = entry.getValue();
            NativePosition pos = nativeWorld.getAdapter().newBlockPos(position);
            WNASharedImpl.updateTileEntity(nativeWorld, tag, pos);

            if (watchdog != null) {
                ops = updateWatchdog(ops, watchdog);
            }
        }
        blockEntityMap.clear();

        SideEffectSet sideEffectSet = sideEffectExtent.getSideEffectSet();
        SideEffectSet perBlockEffects = sideEffectSet
            .with(SideEffect.NETWORK, SideEffect.State.OFF);
        for (Long2ObjectMap.Entry<Int2ObjectArrayMap<SectionDataWithOld>> entry : effectData.long2ObjectEntrySet()) {
            int chunkX = (int) entry.getLongKey();
            int chunkZ = (int) (entry.getLongKey() >> 32);
            NativeChunk chunk = nativeWorld.getChunk(chunkX, chunkZ);
            for (Int2ObjectArrayMap.Entry<SectionDataWithOld> sectionEntry : entry.getValue().int2ObjectEntrySet()) {
                int index = sectionEntry.getIntKey();
                SectionDataWithOld data = sectionEntry.getValue();
                if (sideEffectSet.shouldApply(SideEffect.NETWORK)) {
                    chunk.markSectionChanged(index, data.modified);
                }
                int sectionBlockY = nativeWorld.getYForSectionIndex(index);
                data.modified.forEach((x, y, z) -> {
                    NativePosition pos = chunk.getWorldPos(x, sectionBlockY + y, z);
                    WNASharedImpl.applySideEffectsNoLookups(
                        nativeWorld, chunk, perBlockEffects, pos,
                        data.oldSection.getBlock(x, y, z), data.section.getBlock(x, y, z)
                    );
                });
            }

            if (watchdog != null) {
                ops = updateWatchdog(ops, watchdog);
            }
        }
    }

    private static int updateWatchdog(int ops, Watchdog watchdog) {
        ops++;
        if (ops == 100) {
            watchdog.tick();
            ops = 0;
        }
        return ops;
    }

}
