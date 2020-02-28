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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An extent that sets blocks in the world, with a {@link SideEffectSet}.
 */
public class SideEffectExtent extends AbstractDelegateExtent {

    private final World world;
    private final Map<BlockVector3, BlockState> positions = BlockMap.create();
    private final Set<BlockVector2> dirtyChunks = new HashSet<>();
    private SideEffectSet sideEffectSet = SideEffectSet.defaults();
    private boolean postEditSimulation;

    /**
     * Create a new instance.
     *
     * @param world the world
     */
    public SideEffectExtent(World world) {
        super(world);
        checkNotNull(world);
        this.world = world;
    }

    public boolean isPostEditSimulationEnabled() {
        return postEditSimulation;
    }

    public void setPostEditSimulationEnabled(boolean enabled) {
        this.postEditSimulation = enabled;
    }

    public SideEffectSet getSideEffectSet() {
        return this.sideEffectSet;
    }

    public void setSideEffectSet(SideEffectSet sideEffectSet) {
        this.sideEffectSet = sideEffectSet;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
        if (sideEffectSet.getState(SideEffect.LIGHTING) == SideEffect.State.DELAYED) {
            dirtyChunks.add(BlockVector2.at(location.getBlockX() >> 4, location.getBlockZ() >> 4));
        }
        if (postEditSimulation) {
            positions.put(location, world.getBlock(location));
        }

        return world.setBlock(location, block, postEditSimulation ? SideEffectSet.none() : sideEffectSet);
    }

    public boolean commitRequired() {
        return postEditSimulation || !dirtyChunks.isEmpty();
    }

    @Override
    protected Operation commitBefore() {
        if (!commitRequired()) {
            return null;
        }
        return new Operation() {
            @Override
            public Operation resume(RunContext run) throws WorldEditException {
                if (!dirtyChunks.isEmpty()) {
                    world.fixAfterFastMode(dirtyChunks);
                }

                if (postEditSimulation) {
                    Iterator<Map.Entry<BlockVector3, BlockState>> positionIterator = positions.entrySet().iterator();
                    while (run.shouldContinue() && positionIterator.hasNext()) {
                        Map.Entry<BlockVector3, BlockState> position = positionIterator.next();
                        world.applySideEffects(position.getKey(), position.getValue(), sideEffectSet);
                        positionIterator.remove();
                    }

                    return !positions.isEmpty() ? this : null;
                }

                return null;
            }

            @Override
            public void cancel() {
            }
        };
    }
}
