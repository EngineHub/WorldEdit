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

import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.action.BlockPlacement;
import com.sk89q.worldedit.action.PerformSideEffects;
import com.sk89q.worldedit.action.WorldAction;
import com.sk89q.worldedit.reorder.arrange.Arranger;
import com.sk89q.worldedit.reorder.arrange.ArrangerContext;
import com.sk89q.worldedit.reorder.arrange.SimpleAttributeKey;
import com.sk89q.worldedit.reorder.buffer.MutableArrayWorldActionBuffer;
import com.sk89q.worldedit.reorder.buffer.MutableWorldActionBuffer;
import com.sk89q.worldedit.reorder.buffer.WorldActionBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements post-simulation re-ordering, which performs all side-effects
 * upon flush.
 */
public final class FastReorderArranger implements Arranger {

    private static final SimpleAttributeKey<List<PerformSideEffects>> DELAYED_EFFECTS
        = SimpleAttributeKey.create("delayedEffects", ArrayList::new);

    @Override
    public void onWrite(ArrangerContext context, WorldActionBuffer buffer) {
        List<PerformSideEffects> sideEffects = DELAYED_EFFECTS.get(context);
        MutableWorldActionBuffer copy = MutableArrayWorldActionBuffer.allocate(buffer.remaining());
        while (buffer.hasRemaining()) {
            WorldAction placement = buffer.get();
            if (placement instanceof BlockPlacement) {
                BlockPlacement bp = (BlockPlacement) placement;
                if (bp.getSideEffects().size() > 0) {
                    sideEffects.add(PerformSideEffects.create(bp.getPosition(), bp.getSideEffects()));
                    bp = bp.withSideEffects(ImmutableSet.of());
                }
                copy.put(bp);
            } else {
                copy.put(placement);
            }
        }
        copy.flip();
        context.write(copy);
    }

    @Override
    public void onFlush(ArrangerContext context) {
        List<PerformSideEffects> sideEffects = DELAYED_EFFECTS.get(context);
        if (!sideEffects.isEmpty()) {
            context.write(MutableArrayWorldActionBuffer.wrap(
                sideEffects.toArray(new WorldAction[0])
            ));
        }
        context.flush();
    }
}
