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

package com.sk89q.worldedit.function.visitor;

import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Visits adjacent points on the same X-Z plane as long as the points
 * pass the given mask, and then executes the provided region
 * function on the entire column.
 *
 * <p>This is used by {@code //fill}.</p>
 */
public class DownwardVisitor extends RecursiveVisitor {

    private final int baseY;

    /**
     * Create a new visitor.
     *
     * @param mask the mask
     * @param function the function
     * @param baseY the base Y
     */
    public DownwardVisitor(Mask mask, RegionFunction function, int baseY) {
        super(mask, function);
        checkNotNull(mask);

        this.baseY = baseY;

        Collection<BlockVector3> directions = getDirections();
        directions.clear();
        directions.add(BlockVector3.UNIT_X);
        directions.add(BlockVector3.UNIT_MINUS_X);
        directions.add(BlockVector3.UNIT_Z);
        directions.add(BlockVector3.UNIT_MINUS_Z);
        directions.add(BlockVector3.UNIT_MINUS_Y);
    }

    @Override
    protected boolean isVisitable(BlockVector3 from, BlockVector3 to) {
        int fromY = from.getBlockY();
        return (fromY == baseY || to.subtract(from).getBlockY() < 0) && super.isVisitable(from, to);
    }
}
