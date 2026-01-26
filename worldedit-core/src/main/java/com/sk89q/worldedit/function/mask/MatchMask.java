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

package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;

/**
 * A mask that returns true if the two given extents have the same block at the given position, with offset support.
 */
public final class MatchMask extends AbstractMask {

    private final Extent extent;
    private final Extent matchExtent;
    private final BlockVector3 offset;

    /**
     * Create a new match mask.
     *
     * <p>
     * This will assume an offset of zero. To specify an offset, use {@link #MatchMask(Extent, Extent, BlockVector3)}.
     * </p>
     *
     * @param extent The base extent
     * @param matchExtent The match extent
     */
    public MatchMask(Extent extent, Extent matchExtent) {
        this(extent, matchExtent, BlockVector3.ZERO);
    }

    /**
     * Create a new match mask.
     *
     * @param extent The base extent
     * @param matchExtent The match extent
     * @param offset The offset of comparisons applied to the match extent
     */
    public MatchMask(Extent extent, Extent matchExtent, BlockVector3 offset) {
        this.extent = extent;
        this.matchExtent = matchExtent;
        this.offset = offset;
    }

    @Override
    public boolean test(BlockVector3 vector) {
        return extent.getBlock(vector).equals(matchExtent.getBlock(vector.add(offset)));
    }
}
