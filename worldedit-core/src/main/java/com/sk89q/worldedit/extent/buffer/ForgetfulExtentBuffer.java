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

package com.sk89q.worldedit.extent.buffer;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.AbstractRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Buffers changes to an {@link Extent} and allows later retrieval for
 * actual application of the changes.
 *
 * <p>This buffer will not attempt to return results from the buffer when
 * accessor methods (such as {@link #getBlock(Vector)}) are called.</p>
 */
public class ForgetfulExtentBuffer extends AbstractDelegateExtent implements Pattern {

    private final Map<BlockVector, BlockStateHolder> buffer = new LinkedHashMap<>();
    private final Mask mask;
    private Vector min = null;
    private Vector max = null;

    /**
     * Create a new extent buffer that will buffer every change.
     *
     * @param delegate the delegate extent for {@link Extent#getBlock(Vector)}, etc. calls
     */
    public ForgetfulExtentBuffer(Extent delegate) {
        this(delegate, Masks.alwaysTrue());
    }

    /**
     * Create a new extent buffer that will buffer changes that meet the criteria
     * of the given mask.
     *
     * @param delegate the delegate extent for {@link Extent#getBlock(Vector)}, etc. calls
     * @param mask the mask
     */
    public ForgetfulExtentBuffer(Extent delegate, Mask mask) {
        super(delegate);
        checkNotNull(delegate);
        checkNotNull(mask);
        this.mask = mask;
    }

    @Override
    public boolean setBlock(Vector location, BlockStateHolder block) throws WorldEditException {
        // Update minimum
        if (min == null) {
            min = location;
        } else {
            min = Vector.getMinimum(min, location);
        }

        // Update maximum
        if (max == null) {
            max = location;
        } else {
            max = Vector.getMaximum(max, location);
        }

        BlockVector blockVector = location.toBlockVector();
        if (mask.test(blockVector)) {
            buffer.put(blockVector, block);
            return true;
        } else {
            return getExtent().setBlock(location, block);
        }
    }

    @Override
    public BlockStateHolder apply(Vector pos) {
        BlockStateHolder block = buffer.get(pos.toBlockVector());
        if (block != null) {
            return block;
        } else {
            return BlockTypes.AIR.getDefaultState();
        }
    }

    /**
     * Return a region representation of this buffer.
     *
     * @return a region
     */
    public Region asRegion() {
        return new AbstractRegion(null) {
            @Override
            public Vector getMinimumPoint() {
                return min != null ? min : new Vector();
            }

            @Override
            public Vector getMaximumPoint() {
                return max != null ? max : new Vector();
            }

            @Override
            public void expand(Vector... changes) throws RegionOperationException {
                throw new UnsupportedOperationException("Cannot change the size of this region");
            }

            @Override
            public void contract(Vector... changes) throws RegionOperationException {
                throw new UnsupportedOperationException("Cannot change the size of this region");
            }

            @Override
            public boolean contains(Vector position) {
                return buffer.containsKey(position.toBlockVector());
            }

            @Override
            public Iterator<BlockVector> iterator() {
                return buffer.keySet().iterator();
            }
        };
    }
}
