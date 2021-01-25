/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.function.pattern;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Returns the blocks from {@link Extent}, repeating when out of bounds.
 */
public class RepeatingExtentPattern extends AbstractExtentPattern {

    private final BlockVector3 size;
    private BlockVector3 origin;
    private BlockVector3 offset;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     * @param offset the offset
     */
    public RepeatingExtentPattern(Extent extent, BlockVector3 origin, BlockVector3 offset) {
        super(extent);
        setOrigin(origin);
        setOffset(offset);
        size = extent.getMaximumPoint().subtract(extent.getMinimumPoint()).add(1, 1, 1);
    }

    /**
     * Get the offset.
     *
     * @return the offset
     */
    public BlockVector3 getOffset() {
        return offset;
    }

    /**
     * Set the offset.
     *
     * @param offset the offset
     */
    public void setOffset(BlockVector3 offset) {
        checkNotNull(offset);
        this.offset = offset;
    }

    /**
     * Get the origin.
     *
     * @return the origin
     */
    public BlockVector3 getOrigin() {
        return origin;
    }

    /**
     * Set the origin.
     *
     * @param origin the origin
     */
    public void setOrigin(BlockVector3 origin) {
        checkNotNull(origin);
        this.origin = origin;
    }

    @Override
    public BaseBlock applyBlock(BlockVector3 position) {
        BlockVector3 base = position.add(offset);
        int x = Math.floorMod(base.getBlockX(), size.getBlockX());
        int y = Math.floorMod(base.getBlockY(), size.getBlockY());
        int z = Math.floorMod(base.getBlockZ(), size.getBlockZ());
        return getExtent().getFullBlock(BlockVector3.at(x, y, z).add(origin));
    }

}
