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
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Applies a {@link BlockBag} to operations.
 */
public class BlockBagExtent extends AbstractDelegateExtent {

    private Map<Integer, Integer> missingBlocks = new HashMap<Integer, Integer>();
    private BlockBag blockBag;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     * @param world the world
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
    public Map<Integer, Integer> popMissing() {
        Map<Integer, Integer> missingBlocks = this.missingBlocks;
        this.missingBlocks = new HashMap<Integer, Integer>();
        return missingBlocks;
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block) throws WorldEditException {
        if (blockBag != null) {
            BaseBlock lazyBlock = getExtent().getLazyBlock(position);
            int existing = lazyBlock.getType();
            final int type = block.getType();

            if (type > 0) {
                try {
                    blockBag.fetchPlacedBlock(type, 0);
                } catch (UnplaceableBlockException e) {
                    return false;
                } catch (BlockBagException e) {
                    if (!missingBlocks.containsKey(type)) {
                        missingBlocks.put(type, 1);
                    } else {
                        missingBlocks.put(type, missingBlocks.get(type) + 1);
                    }
                    return false;
                }
            }

            if (existing > 0) {
                try {
                    blockBag.storeDroppedBlock(existing, lazyBlock.getData());
                } catch (BlockBagException ignored) {
                }
            }
        }

        return super.setBlock(position, block);
    }
}
