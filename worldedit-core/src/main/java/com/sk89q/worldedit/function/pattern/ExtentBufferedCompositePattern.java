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

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.buffer.ExtentBuffer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A pattern that composes multiple patterns consecutively, ensuring that changes from one
 * pattern are realized by the subsequent one(s). For best results, use an {@link ExtentBuffer}
 * to avoid changing blocks in an underlying extent (e.g. the world).
 */
public class ExtentBufferedCompositePattern extends AbstractExtentPattern {

    private final Pattern[] patterns;

    /**
     * Construct a new instance of this pattern.
     *
     * <p>Note that all patterns passed which are ExtentPatterns should use the same extent as the one
     * passed to this constructor, or block changes may not be realized by those patterns.</p>
     *
     * @param extent the extent to buffer changes to
     * @param patterns the patterns to apply, in order
     */
    public ExtentBufferedCompositePattern(Extent extent, Pattern... patterns) {
        super(extent);
        checkArgument(patterns.length != 0, "patterns cannot be empty");
        this.patterns = patterns;
    }

    @Override
    public BaseBlock apply(BlockVector3 position) {
        BaseBlock lastBlock = null;
        for (Pattern pattern : patterns) {
            lastBlock = pattern.apply(position);
            try {
                getExtent().setBlock(position, lastBlock);
            } catch (WorldEditException ignored) { // buffer doesn't throw
            }
        }
        return lastBlock;
    }
}
