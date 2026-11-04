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

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A mask that matches block types by their registry ID using glob syntax.
 */
public final class BlockTypeGlobMask extends AbstractExtentMask {

    private static final Pattern VALID_GLOB = Pattern.compile("[a-z0-9_:?*/]+");

    private final Pattern matcher;

    /**
     * Create a new mask that matches block type registry IDs.
     *
     * @param extent the extent
     * @param glob the glob pattern
     */
    public BlockTypeGlobMask(Extent extent, String glob) {
        super(extent);
        checkArgument(VALID_GLOB.matcher(glob).matches(), "Invalid block type glob");
        this.matcher = StringUtil.convertGlobToRegex(glob);
    }

    @Override
    public boolean test(BlockVector3 vector) {
        return matcher.matcher(getExtent().getBlock(vector).getBlockType().id()).matches();
    }
}
