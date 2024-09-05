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

package com.sk89q.worldedit.internal.wna;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.internal.util.collection.ChunkSectionMask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTagType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Shared implementation bits of {@link NativeWorld}. Platforms may call into this to use common code.
 */
public class WNASharedImpl {
    public static <B extends BlockStateHolder<B>> boolean setBlock(
        NativeWorld nativeWorld, BlockVector3 position, B block, SideEffectSet sideEffects
    ) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);

        // First set the block
        NativeChunk chunk = nativeWorld.getChunk(position.x() >> 4, position.z() >> 4);
        NativePosition pos = nativeWorld.getAdapter().newBlockPos(position);
        NativeBlockState old = chunk.getBlockState(pos);
        NativeBlockState newState = nativeWorld.getAdapter().toNative(block.toImmutableState());
        // change block prior to placing if it should be fixed
        if (sideEffects.shouldApply(SideEffect.VALIDATION)) {
            newState = newState.updateFromNeighbourShapes(nativeWorld, pos);
        }
        NativeBlockState lastValue = chunk.setBlockState(pos, newState, sideEffects.shouldApply(SideEffect.UPDATE));
        boolean successful = lastValue != null;

        // Create the TileEntity
        if (successful || old == newState) {
            if (block instanceof BaseBlock baseBlock) {
                successful |= updateTileEntity(nativeWorld, baseBlock.getNbt(), pos);
            }
        }

        if (successful) {
            if (sideEffects.getState(SideEffect.LIGHTING) == SideEffect.State.ON) {
                nativeWorld.updateLightingForBlock(pos);
            }
            markAndNotifyBlock(nativeWorld, pos, chunk, old, newState, sideEffects);
        }

        return successful;
    }

    public static boolean updateTileEntity(NativeWorld nativeWorld, LinCompoundTag tag, NativePosition pos) {
        if (tag == null) {
            return false;
        }
        LinCompoundTag.Builder tagBuilder = tag.toBuilder()
            .putInt("x", pos.x())
            .putInt("y", pos.y())
            .putInt("z", pos.z());
        String nbtId = extractNbtId(tag);
        if (!nbtId.isBlank()) {
            tagBuilder.putString("id", nbtId);
        }
        tag = tagBuilder.build();

        // update if TE changed as well
        return nativeWorld.updateTileEntity(pos, tag);
    }

    public static String extractNbtId(LinCompoundTag tag) {
        LinStringTag idTag = tag.findTag("id", LinTagType.stringTag());
        return idTag != null ? idTag.value() : "";
    }

    public static void applySideEffects(
        NativeWorld nativeWorld, SideEffectSet sideEffectSet, BlockVector3 position, BlockState previousType
    ) {
        NativePosition pos = nativeWorld.getAdapter().newBlockPos(position);
        NativeChunk chunk = nativeWorld.getChunk(position.x() >> 4, position.z() >> 4);
        NativeBlockState oldData = nativeWorld.getAdapter().toNative(previousType);
        NativeBlockState newData = chunk.getBlockState(pos);

        applySideEffectsNoLookups(nativeWorld, chunk, sideEffectSet, pos, oldData, newData);
    }

    public static void applySideEffectsNoLookups(
        NativeWorld nativeWorld, NativeChunk chunk, SideEffectSet sideEffectSet, NativePosition pos,
        NativeBlockState oldData, NativeBlockState newData
    ) {
        if (sideEffectSet.shouldApply(SideEffect.UPDATE)) {
            nativeWorld.updateBlock(pos, oldData, newData);
        }

        if (sideEffectSet.getState(SideEffect.LIGHTING) == SideEffect.State.ON) {
            nativeWorld.updateLightingForBlock(pos);
        }

        markAndNotifyBlock(nativeWorld, pos, chunk, oldData, newData, sideEffectSet);
    }

    /**
     * This is a heavily modified function stripped from MC to apply WorldEdit-modifications.
     *
     * <p>
     * See NeoForge's Level.markAndNotifyBlock
     * </p>
     */
    public static void markAndNotifyBlock(
        NativeWorld nativeWorld, NativePosition pos, NativeChunk chunk, NativeBlockState oldState, NativeBlockState newState,
        SideEffectSet sideEffectSet
    ) {
        // Removed redundant branches

        if (chunk.isTicking()) {
            if (sideEffectSet.shouldApply(SideEffect.ENTITY_AI)) {
                nativeWorld.notifyBlockUpdate(pos, oldState, newState);
            } else if (sideEffectSet.shouldApply(SideEffect.NETWORK)) {
                // If we want to skip entity AI, just mark the block for sending
                nativeWorld.markBlockChanged(pos);
            }
        }

        if (sideEffectSet.shouldApply(SideEffect.NEIGHBORS)) {
            nativeWorld.notifyNeighbors(pos, oldState, newState, sideEffectSet.shouldApply(SideEffect.EVENTS));
        }

        // Make connection updates optional
        if (sideEffectSet.shouldApply(SideEffect.NEIGHBORS)) {
            nativeWorld.updateNeighbors(pos, oldState, newState, 512, sideEffectSet.shouldApply(SideEffect.EVENTS));
        }

        // Seems used only for PoI updates
        if (sideEffectSet.shouldApply(SideEffect.POI_UPDATE)) {
            nativeWorld.onBlockStateChange(pos, oldState, newState);
        }
    }

    /**
     * After a chunk section replacement, this function can be called to update the heightmaps, block entities, etc.
     * to keep consistency with {@link NativeChunk#setBlockState(NativePosition, NativeBlockState, boolean)}. Doing this allows
     * skipping redundant updates caused by multiple set calls, and filtering out unwanted side effects.
     *
     * @param chunk the chunk
     * @param index the replaced section index
     * @param oldSection the old section
     * @param newSection the new section
     * @param modifiedBlocks the mask of modified blocks
     */
    public static void postChunkSectionReplacement(
        NativeChunk chunk, int index, NativeChunkSection oldSection, NativeChunkSection newSection,
        ChunkSectionMask modifiedBlocks
    ) {
        modifiedBlocks.forEach((secX, secY, secZ) -> {
            NativeBlockState oldState = oldSection.getBlock(secX, secY, secZ);
            NativeBlockState newState = newSection.getBlock(secX, secY, secZ);
            int chunkY = chunk.getWorld().getYForSectionIndex(index) + secY;
            // We skip heightmaps, they're optimized at a higher level to a single call.

            // We skip onRemove here, will call in UPDATE side effect if necessary.

            if (!oldState.isSameBlockType(newState) && oldState.hasBlockEntity()) {
                chunk.removeSectionBlockEntity(secX, chunkY, secZ);
            }

            // We skip onPlace here, will call in UPDATE side effect if necessary.

            if (newState.hasBlockEntity()) {
                chunk.initializeBlockEntity(secX, chunkY, secZ, newState);
            }
        });

        boolean wasOnlyAir = oldSection.isOnlyAir();
        boolean onlyAir = newSection.isOnlyAir();
        if (wasOnlyAir != onlyAir) {
            chunk.updateLightingForSectionAirChange(index, onlyAir);
        }
    }

    private WNASharedImpl() {
    }
}
