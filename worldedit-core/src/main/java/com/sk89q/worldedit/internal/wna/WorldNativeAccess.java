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

import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.util.nbt.IntBinaryTag;
import com.sk89q.worldedit.util.nbt.StringBinaryTag;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Natively access and perform operations on the world.
 *
 * @param <NC> the native chunk type
 * @param <NBS> the native block state type
 * @param <NP> the native position type
 */
public interface WorldNativeAccess<NC, NBS, NP> {

    default <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);
        setCurrentSideEffectSet(sideEffects);

        int x = position.getBlockX();
        int y = position.getBlockY();
        int z = position.getBlockZ();

        // First set the block
        NC chunk = getChunk(x >> 4, z >> 4);
        NP pos = getPosition(x, y, z);
        NBS old = getBlockState(chunk, pos);
        NBS newState = toNative(block.toImmutableState());
        // change block prior to placing if it should be fixed
        if (sideEffects.shouldApply(SideEffect.VALIDATION)) {
            newState = getValidBlockForPosition(newState, pos);
        }
        NBS lastValue = setBlockState(chunk, pos, newState);
        boolean successful = lastValue != null;

        // Create the TileEntity
        if (successful || old == newState) {
            if (block instanceof BaseBlock) {
                BaseBlock baseBlock = (BaseBlock) block;
                CompoundBinaryTag tag = baseBlock.getNbt();
                if (tag != null) {
                    tag = tag.put(ImmutableMap.of(
                        "id", StringBinaryTag.of(baseBlock.getNbtId()),
                        "x", IntBinaryTag.of(position.getX()),
                        "y", IntBinaryTag.of(position.getY()),
                        "z", IntBinaryTag.of(position.getZ())
                    ));

                    // update if TE changed as well
                    successful = updateTileEntity(pos, tag);
                }
            }
        }

        if (successful) {
            if (sideEffects.getState(SideEffect.LIGHTING) == SideEffect.State.ON) {
                updateLightingForBlock(pos);
            }
            markAndNotifyBlock(pos, chunk, old, newState, sideEffects);
        }

        return successful;
    }

    default void applySideEffects(BlockVector3 position, BlockState previousType, SideEffectSet sideEffectSet) {
        setCurrentSideEffectSet(sideEffectSet);
        NP pos = getPosition(position.getX(), position.getY(), position.getZ());
        NC chunk = getChunk(position.getX() >> 4, position.getZ() >> 4);
        NBS oldData = toNative(previousType);
        NBS newData = getBlockState(chunk, pos);

        if (sideEffectSet.getState(SideEffect.LIGHTING) == SideEffect.State.ON) {
            updateLightingForBlock(pos);
        }

        markAndNotifyBlock(pos, chunk, oldData, newData, sideEffectSet);
    }

    // state-keeping functions for WNA
    // may be thread-unsafe, as this is single-threaded code

    /**
     * Receive the current side-effect set from the high level call.
     *
     * <p>
     * This allows the implementation to branch on the side-effects internally.
     * </p>
     *
     * @param sideEffectSet the set of side-effects
     */
    default void setCurrentSideEffectSet(SideEffectSet sideEffectSet) {
    }

    // access functions

    NC getChunk(int x, int z);

    NBS toNative(BlockState state);

    NBS getBlockState(NC chunk, NP position);

    @Nullable
    NBS setBlockState(NC chunk, NP position, NBS state);

    NBS getValidBlockForPosition(NBS block, NP position);

    NP getPosition(int x, int y, int z);

    void updateLightingForBlock(NP position);

    boolean updateTileEntity(NP position, CompoundBinaryTag tag);

    void notifyBlockUpdate(NP position, NBS oldState, NBS newState);

    boolean isChunkTicking(NC chunk);

    void markBlockChanged(NP position);

    void notifyNeighbors(NP pos, NBS oldState, NBS newState);

    void updateNeighbors(NP pos, NBS oldState, NBS newState, int recursionLimit);

    void onBlockStateChange(NP pos, NBS oldState, NBS newState);

    /**
     * This is a heavily modified function stripped from MC to apply WorldEdit-modifications.
     *
     * <p>
     * See Forge's World.markAndNotifyBlock
     * </p>
     */
    default void markAndNotifyBlock(NP pos, NC chunk, NBS oldState, NBS newState, SideEffectSet sideEffectSet) {
        NBS blockState1 = getBlockState(chunk, pos);
        if (blockState1 != newState) {
            return;
        }

        // Remove redundant branches
        if (isChunkTicking(chunk)) {
            if (sideEffectSet.shouldApply(SideEffect.ENTITY_AI)) {
                notifyBlockUpdate(pos, oldState, newState);
            } else if (sideEffectSet.shouldApply(SideEffect.NETWORK)) {
                // If we want to skip entity AI, just mark the block for sending
                markBlockChanged(pos);
            }
        }

        if (sideEffectSet.shouldApply(SideEffect.NEIGHBORS)) {
            notifyNeighbors(pos, oldState, newState);
        }

        // Make connection updates optional
        if (sideEffectSet.shouldApply(SideEffect.NEIGHBORS)) {
            updateNeighbors(pos, oldState, newState, 512);
        }

        // Seems used only for PoI updates
        if (sideEffectSet.shouldApply(SideEffect.POI_UPDATE)) {
            onBlockStateChange(pos, oldState, blockState1);
        }
    }

}
