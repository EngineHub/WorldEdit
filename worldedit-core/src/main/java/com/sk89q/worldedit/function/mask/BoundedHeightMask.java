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

import static com.google.common.base.Preconditions.checkArgument;

import com.sk89q.worldedit.math.BlockVector3;

import javax.annotation.Nullable;

/**
 * Has the criteria where the Y value of passed positions must be within
 * a certain range of Y values (inclusive).
 */
public class BoundedHeightMask extends AbstractMask {

    private final int minY;
    private final int maxY;

    /**
     * Create a new bounded height mask.
     *
     * @param minY the minimum Y
     * @param maxY the maximum Y (must be equal to or greater than minY)
     */
    public BoundedHeightMask(int minY, int maxY) {
        checkArgument(minY <= maxY, "minY <= maxY required");
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public boolean test(BlockVector3 vector) {
        return vector.getY() >= minY && vector.getY() <= maxY;
    }

    @Nullable
    @Override
    public Mask2D toMask2D() {
        return null;
    }

}
