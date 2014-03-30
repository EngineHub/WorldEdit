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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.extent;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.AbstractRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Buffers changes to an {@link Extent} and allows later retrieval of buffered
 * changes for actual setting.
 */
public class ExtentBuffer implements Extent, Pattern {

    private static final BaseBlock AIR = new BaseBlock(BlockID.AIR);

    private final Extent delegate;
    private final Map<BlockVector, BaseBlock> buffer = new LinkedHashMap<BlockVector, BaseBlock>();
    private final Mask mask;
    private Vector min = null;
    private Vector max = null;

    /**
     * Create a new extent buffer that will buffer every change.
     *
     * @param delegate the delegate extent for {@link Extent#getBlock(Vector)}, etc. calls
     */
    public ExtentBuffer(Extent delegate) {
        this(delegate, Masks.alwaysTrue());
    }

    /**
     * Create a new extent buffer that will buffer changes that meet the criteria
     * of the given mask.
     *
     * @param delegate the delegate extent for {@link Extent#getBlock(Vector)}, etc. calls
     * @param mask the mask
     */
    public ExtentBuffer(Extent delegate, Mask mask) {
        checkNotNull(delegate);
        checkNotNull(mask);
        this.delegate = delegate;
        this.mask = mask;
    }

    @Override
    public BaseBlock getBlock(Vector location) {
        return delegate.getBlock(location);
    }

    @Override
    public int getBlockType(Vector location) {
        return delegate.getBlockType(location);
    }

    @Override
    public int getBlockData(Vector location) {
        return delegate.getBlockData(location);
    }

    @Override
    public boolean setBlock(Vector location, BaseBlock block, boolean notifyAdjacent) throws WorldEditException {
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
            return delegate.setBlock(location, block, notifyAdjacent);
        }
    }

    @Override
    public BaseBlock apply(Vector pos) {
        BaseBlock block = buffer.get(pos.toBlockVector());
        if (block != null) {
            return block;
        } else {
            return AIR;
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
            public boolean contains(Vector pt) {
                return buffer.containsKey(pt.toBlockVector());
            }

            @Override
            public Iterator<BlockVector> iterator() {
                return buffer.keySet().iterator();
            }
        };
    }
}
