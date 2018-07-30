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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * A mask that checks whether blocks at the given positions are matched by
 * a block in a list.
 *
 * <p>This mask checks for both an exact block ID and data value match, as well
 * for a block with the same ID but a data value of -1.</p>
 */
public class BlockMask extends AbstractExtentMask {

    private final Set<BlockStateHolder> blocks = new HashSet<>();

    /**
     * Create a new block mask.
     *
     * @param extent the extent
     * @param blocks a list of blocks to match
     */
    public BlockMask(Extent extent, Collection<BlockStateHolder> blocks) {
        super(extent);
        checkNotNull(blocks);
        this.blocks.addAll(blocks);
    }

    /**
     * Create a new block mask.
     *
     * @param extent the extent
     * @param block an array of blocks to match
     */
    public BlockMask(Extent extent, BlockStateHolder... block) {
        this(extent, Arrays.asList(checkNotNull(block)));
    }

    /**
     * Add the given blocks to the list of criteria.
     *
     * @param blocks a list of blocks
     */
    public void add(Collection<BlockStateHolder> blocks) {
        checkNotNull(blocks);
        this.blocks.addAll(blocks);
    }

    /**
     * Add the given blocks to the list of criteria.
     *
     * @param block an array of blocks
     */
    public void add(BlockStateHolder... block) {
        add(Arrays.asList(checkNotNull(block)));
    }

    /**
     * Get the list of blocks that are tested with.
     *
     * @return a list of blocks
     */
    public Collection<BlockStateHolder> getBlocks() {
        return blocks;
    }

    @Override
    public boolean test(Vector vector) {
        BlockStateHolder block = getExtent().getBlock(vector);
        for (BlockStateHolder testBlock : blocks) {
            if (testBlock.equalsFuzzy(block)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public Mask2D toMask2D() {
        return null;
    }

}
