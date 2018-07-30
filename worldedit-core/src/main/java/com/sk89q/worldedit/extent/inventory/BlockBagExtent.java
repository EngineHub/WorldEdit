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

package com.sk89q.worldedit.extent.inventory;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Applies a {@link BlockBag} to operations.
 */
public class BlockBagExtent extends AbstractDelegateExtent {

    private Map<BlockType, Integer> missingBlocks = new HashMap<>();
    private BlockBag blockBag;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     * @param blockBag the block bag
     */
    public BlockBagExtent(Extent extent, @Nullable BlockBag blockBag) {
        super(extent);
        this.blockBag = blockBag;
    }

    /**
     * Get the block bag.
     *
     * @return a block bag, which may be null if none is used
     */
    public @Nullable BlockBag getBlockBag() {
        return blockBag;
    }

    /**
     * Set the block bag.
     *
     * @param blockBag a block bag, which may be null if none is used
     */
    public void setBlockBag(@Nullable BlockBag blockBag) {
        this.blockBag = blockBag;
    }

    /**
     * Gets the list of missing blocks and clears the list for the next
     * operation.
     *
     * @return a map of missing blocks
     */
    public Map<BlockType, Integer> popMissing() {
        Map<BlockType, Integer> missingBlocks = this.missingBlocks;
        this.missingBlocks = new HashMap<>();
        return missingBlocks;
    }

    @Override
    public boolean setBlock(Vector position, BlockStateHolder block) throws WorldEditException {
        if (blockBag != null) {
            BlockState existing = getExtent().getBlock(position);

            if (block.getBlockType() != BlockTypes.AIR) {
                try {
                    blockBag.fetchPlacedBlock(block.toImmutableState());
                } catch (UnplaceableBlockException e) {
                    return false;
                } catch (BlockBagException e) {
                    if (!missingBlocks.containsKey(block.getBlockType())) {
                        missingBlocks.put(block.getBlockType(), 1);
                    } else {
                        missingBlocks.put(block.getBlockType(), missingBlocks.get(block.getBlockType()) + 1);
                    }
                    return false;
                }
            }

            if (existing.getBlockType() != BlockTypes.AIR) {
                try {
                    blockBag.storeDroppedBlock(existing);
                } catch (BlockBagException ignored) {
                }
            }
        }

        return super.setBlock(position, block);
    }
}
