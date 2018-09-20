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

package com.sk89q.worldedit.world.chunk;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.block.BlockStateHolder;

/**
 * A 16 by 16 block chunk.
 */
public interface Chunk {
    
    /**
     * Get a block;
     *
     * @param position the position of the block
     * @return block the block
     * @throws DataException thrown on data error
     */
    BlockStateHolder getBlock(Vector position) throws DataException;

}
