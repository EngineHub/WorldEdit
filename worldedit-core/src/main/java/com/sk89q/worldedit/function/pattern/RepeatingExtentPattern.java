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

package com.sk89q.worldedit.function.pattern;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.block.BlockStateHolder;

/**
 * Returns the blocks from {@link Extent}, repeating when out of bounds.
 */
public class RepeatingExtentPattern extends AbstractPattern {

    private Extent extent;
    private Vector offset;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     * @param offset the offset
     */
    public RepeatingExtentPattern(Extent extent, Vector offset) {
        setExtent(extent);
        setOffset(offset);
    }

    /**
     * Get the extent.
     *
     * @return the extent
     */
    public Extent getExtent() {
        return extent;
    }

    /**
     * Set the extent.
     *
     * @param extent the extent
     */
    public void setExtent(Extent extent) {
        checkNotNull(extent);
        this.extent = extent;
    }

    /**
     * Get the offset.
     *
     * @return the offset
     */
    public Vector getOffset() {
        return offset;
    }

    /**
     * Set the offset.
     *
     * @param offset the offset
     */
    public void setOffset(Vector offset) {
        checkNotNull(offset);
        this.offset = offset;
    }

    @Override
    public BlockStateHolder apply(Vector position) {
        Vector base = position.add(offset);
        Vector size = extent.getMaximumPoint().subtract(extent.getMinimumPoint()).add(1, 1, 1);
        int x = base.getBlockX() % size.getBlockX();
        int y = base.getBlockY() % size.getBlockY();
        int z = base.getBlockZ() % size.getBlockZ();
        return extent.getFullBlock(new Vector(x, y, z));
    }

}
