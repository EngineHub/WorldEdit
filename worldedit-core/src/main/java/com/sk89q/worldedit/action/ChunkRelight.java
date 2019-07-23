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

package com.sk89q.worldedit.action;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.World;

/**
 * Re-light the given chunk.
 */
public class ChunkRelight implements ChunkWorldAction {

    public static ChunkRelight create(BlockVector2 position) {
        return new ChunkRelight(position);
    }

    private final BlockVector2 position;

    private ChunkRelight(BlockVector2 position) {
        this.position = position;
    }

    @Override
    public BlockVector2 getPosition() {
        return position;
    }

    @Override
    public void apply(World world) throws WorldEditException {
        world.fixLighting(ImmutableList.of(position));
    }
}
