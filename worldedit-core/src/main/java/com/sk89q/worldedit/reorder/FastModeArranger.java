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
import com.sk89q.worldedit.action.WorldAction;
import com.sk89q.worldedit.reorder.arrange.Arranger;
import com.sk89q.worldedit.reorder.arrange.ArrangerContext;
import com.sk89q.worldedit.reorder.buffer.MutableArrayWorldActionBuffer;
import com.sk89q.worldedit.reorder.buffer.MutableWorldActionBuffer;
import com.sk89q.worldedit.reorder.buffer.WorldActionBuffer;

/**
 * Implements "fast mode" which may skip physics, lighting, etc.
 */
public final class FastModeArranger implements Arranger {

    @Override
    public void onWrite(ArrangerContext context, WorldActionBuffer buffer) {
        MutableWorldActionBuffer copy = MutableArrayWorldActionBuffer.allocate(buffer.remaining());
        while (buffer.hasRemaining()) {
            WorldAction placement = buffer.get();
            if (placement instanceof BlockPlacement) {
                BlockPlacement bp = (BlockPlacement) placement;
                if (bp.getSideEffects().size() > 0) {
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
        context.flush();
    }
}
