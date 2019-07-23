/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.reorder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.action.BlockPlacement;
import com.sk89q.worldedit.action.ChunkRelight;
import com.sk89q.worldedit.action.PerformSideEffects;
import com.sk89q.worldedit.action.SideEffect;
import com.sk89q.worldedit.action.WorldAction;
import com.sk89q.worldedit.reorder.arrange.Arranger;
import com.sk89q.worldedit.reorder.arrange.ArrangerContext;
import com.sk89q.worldedit.reorder.arrange.SimpleAttributeKey;
import com.sk89q.worldedit.reorder.buffer.MutableArrayWorldActionBuffer;
import com.sk89q.worldedit.reorder.buffer.MutableWorldActionBuffer;
import com.sk89q.worldedit.reorder.buffer.WorldActionBuffer;
import com.sk89q.worldedit.util.collection.BlockMap;

import java.util.Set;

/**
 * Delays all lighting side-effects until after changes have been made.
 */
public final class DelayedLightingArranger implements Arranger {

    private static final SimpleAttributeKey<BlockMap<Boolean>> NEED_RELIGHT
        = SimpleAttributeKey.create("dirtyChunks", BlockMap::create);
    private static final Set<SideEffect> LIGHT = ImmutableSet.of(SideEffect.LIGHT);

    @Override
    public void onWrite(ArrangerContext context, WorldActionBuffer buffer) {
        BlockMap<Boolean> needRelight = NEED_RELIGHT.get(context);
        MutableWorldActionBuffer copy = MutableArrayWorldActionBuffer.allocate(buffer.remaining());
        while (buffer.hasRemaining()) {
            WorldAction placement = buffer.get();
            if (placement instanceof BlockPlacement) {
                BlockPlacement bp = (BlockPlacement) placement;
                if (bp.getSideEffects().contains(SideEffect.LIGHT)) {
                    bp = bp.withSideEffects(removeLight(bp.getSideEffects()));
                    needRelight.put(bp.getPosition(), true);
                }
                copy.put(bp);
            } else if (placement instanceof PerformSideEffects) {
                PerformSideEffects pse = (PerformSideEffects) placement;
                if (pse.getSideEffects().contains(SideEffect.LIGHT)) {
                    pse = pse.withSideEffects(removeLight(pse.getSideEffects()));
                    needRelight.put(pse.getPosition(), true);
                }
                if (pse.getSideEffects().size() > 0) {
                    copy.put(pse);
                }
            } else {
                copy.put(placement);
            }
        }
        copy.flip();
        context.write(copy);
    }

    private Set<SideEffect> removeLight(Set<SideEffect> sideEffects) {
        return Sets.filter(sideEffects, se -> se != SideEffect.LIGHT);
    }

    @Override
    public void onFlush(ArrangerContext context) {
        BlockMap<Boolean> needRelight = NEED_RELIGHT.get(context);
        if (!needRelight.isEmpty()) {
            context.write(MutableArrayWorldActionBuffer.wrap(
                needRelight.keySet().stream()
                    .map(x -> PerformSideEffects.create(x, LIGHT))
                    .toArray(WorldAction[]::new)
            ));
            needRelight.clear();
        }
        context.flush();
    }
}
