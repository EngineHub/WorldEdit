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

package com.sk89q.worldedit.extent.validation;

import static com.google.common.base.Preconditions.checkArgument;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockStateHolder;

/**
 * Limits the number of blocks that can be changed before a
 * {@link MaxChangedBlocksException} is thrown.
 */
public class BlockChangeLimiter extends AbstractDelegateExtent {

    private int limit;
    private int count = 0;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     * @param limit the limit (&gt;= 0) or -1 for no limit
     */
    public BlockChangeLimiter(Extent extent, int limit) {
        super(extent);
        setLimit(limit);
    }

    /**
     * Get the limit.
     *
     * @return the limit (&gt;= 0) or -1 for no limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Set the limit.
     *
     * @param limit the limit (&gt;= 0) or -1 for no limit
     */
    public void setLimit(int limit) {
        checkArgument(limit >= -1, "limit >= -1 required");
        this.limit = limit;
    }

    /**
     * Get the number of blocks that have been counted so far.
     *
     * @return the number of blocks
     */
    public int getCount() {
        return count;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
        if (limit >= 0) {
            if (count >= limit) {
                throw new MaxChangedBlocksException(limit);
            }
            count++;
        }
        return super.setBlock(location, block);
    }
}
