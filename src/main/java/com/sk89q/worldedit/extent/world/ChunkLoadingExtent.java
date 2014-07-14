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
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.World;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Automatically loads chunks when blocks are accessed.
 */
public class ChunkLoadingExtent extends AbstractDelegateExtent {

    private final World world;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     * @param world the world
     * @param enabled does nothing
     * @deprecated Removed 'enabled' option
     */
    @Deprecated
    public ChunkLoadingExtent(Extent extent, World world, boolean enabled) {
        this(extent, world);
    }

    /**
     * Create a new instance with chunk loading enabled.
     *
     * @param extent the extent
     * @param world the world
     */
    public ChunkLoadingExtent(Extent extent, World world) {
        super(extent);
        checkNotNull(world);
        this.world = world;
    }

    @Override
    public boolean setBlock(Vector location, BaseBlock block) throws WorldEditException {
        world.checkLoadedChunk(location);
        return super.setBlock(location, block);
    }
}
