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

package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.Vector;

import javax.annotation.Nullable;

/**
 * A mask that returns true whenever the block at the location is not
 * an air block (it contains some other block).
 */
public class ExistingBlockMask extends AbstractExtentMask {

    /**
     * Create a new existing block map.
     *
     * @param extent the extent to check
     */
    public ExistingBlockMask(Extent extent) {
        super(extent);
    }

    @Override
    public boolean test(Vector vector) {
        return getExtent().getBlock(vector).getBlockType() != BlockTypes.AIR;
    }

    @Nullable
    @Override
    public Mask2D toMask2D() {
        return null;
    }

}
