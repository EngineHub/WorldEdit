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

package com.sk89q.worldedit.extent.world;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.SimulatedExtent;
import com.sk89q.worldedit.function.operation.AbstractOperation;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.util.Vectors;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Sets blocks over several passes to increase speed.
 */
public class MultiPassBlockSetExtent extends AbstractDelegateExtent<SimulatedExtent> {

    private final Queue<Vector> positions = new ArrayDeque<Vector>();
    private boolean notifyAndLight = true;
    private Vector2D lastChunk;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     */
    public MultiPassBlockSetExtent(SimulatedExtent extent) {
        super(extent);
    }

    /**
     * Get whether there should be a pass where blocks are re-lighted and
     * adjacent blocks are notified of changes.
     *
     * @return true if relighting and notification is enabled
     */
    public boolean getNotifyAndLight() {
        return notifyAndLight;
    }

    /**
     * Set whether there should be a pass where blocks are re-lighted and
     * adjacent blocks are notified of changes.
     *
     * @param notifyAndLight true if relighting and notification is enabled
     */
    public void setNotifyAndLight(boolean notifyAndLight) {
        this.notifyAndLight = notifyAndLight;
    }

    @Override
    public boolean setBlock(Vector location, BaseBlock block) throws WorldEditException {
        if (getExtent().setBlock(location, block, false)) {
            if (getNotifyAndLight()) {
                lastChunk = Vectors.toChunkVector(location);
                positions.offer(location);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected Operation thisInterleaveOperation() {
        return createOperation(true);
    }

    @Override
    protected Operation thisFinalizeOperation() {
        return createOperation(false);
    }

    private Operation createOperation(final boolean opportunistic) {
        return new AbstractOperation() {
            @Override
            public boolean isOpportunistic() {
                return opportunistic;
            }

            @Override
            public Operation resume(RunContext run) throws WorldEditException {
                Vector position;

                while (true) {
                    if (opportunistic) {
                        position = positions.peek();

                        if (position != null && Vectors.toChunkVector(position).equalsBlock(lastChunk)) {
                            return null;
                        }
                    }

                    position = positions.poll(); // Remove from queue

                    if (position == null) {
                        break;
                    }

                    getExtent().notifyAndLightBlock(position, 0);

                    if (!run.shouldContinue()) {
                        return opportunistic ? null : this;
                    }
                }

                return null;
            }

            @Override
            public void cancel() {
            }
        };
    }
}
