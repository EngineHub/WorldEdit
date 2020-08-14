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

package com.sk89q.worldedit.function.block;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Replaces blocks with a given pattern.
 */
public class BlockReplace implements RegionFunction {

    private final Extent extent;
    private final Pattern pattern;

    /**
     * Create a new instance.
     *
     * @param extent an extent
     * @param pattern a pattern
     */
    public BlockReplace(Extent extent, Pattern pattern) {
        checkNotNull(extent);
        checkNotNull(pattern);
        this.extent = extent;
        this.pattern = pattern;
    }

    @Override
    public boolean apply(BlockVector3 position) throws WorldEditException {
        return extent.setBlock(position, pattern.applyBlock(position));
    }

}
