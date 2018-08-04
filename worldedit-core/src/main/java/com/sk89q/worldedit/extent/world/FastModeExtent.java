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

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements "fast mode" which may skip physics, lighting, etc.
 */
public class FastModeExtent extends AbstractDelegateExtent {

    private final World world;
    private final Set<BlockVector2D> dirtyChunks = new HashSet<>();
    private boolean enabled = true;

    /**
     * Create a new instance with fast mode enabled.
     *
     * @param world the world
     */
    public FastModeExtent(World world) {
        this(world, true);
    }

    /**
     * Create a new instance.
     *
     * @param world the world
     * @param enabled true to enable fast mode
     */
    public FastModeExtent(World world, boolean enabled) {
        super(world);
        checkNotNull(world);
        this.world = world;
        this.enabled = enabled;
    }

    /**
     * Return whether fast mode is enabled.
     *
     * @return true if fast mode is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set fast mode enable status.
     *
     * @param enabled true to enable fast mode
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean setBlock(Vector location, BlockStateHolder block) throws WorldEditException {
        if (enabled) {
            dirtyChunks.add(new BlockVector2D(location.getBlockX() >> 4, location.getBlockZ() >> 4));
            return world.setBlock(location, block, false);
        } else {
            return world.setBlock(location, block, true);
        }
    }

    @Override
    protected Operation commitBefore() {
        return new Operation() {
            @Override
            public Operation resume(RunContext run) throws WorldEditException {
                if (!dirtyChunks.isEmpty()) {
                    world.fixAfterFastMode(dirtyChunks);
                }
                return null;
            }

            @Override
            public void cancel() {
            }

            @Override
            public void addStatusMessages(List<String> messages) {
            }
        };
    }

}
