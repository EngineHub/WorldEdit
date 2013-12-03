// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit.blocks;

import com.sk89q.worldedit.CuboidClipboard.FlipDirection;
import com.sk89q.worldedit.foundation.Block;

import java.util.Collection;

/**
 * Represents a block.
 *
 * @see Block new class to replace this one
 * @author sk89q
 */
public class BaseBlock extends Block {
    
    /**
     * Construct the block with its type, with default data value 0.
     *
     * @param type type ID of block
     */
    public BaseBlock(int type) {
        this(type, 0);
    }

    /**
     * Construct the block with its type and data.
     *
     * @param type type ID of block
     * @param data data value
     */
    public BaseBlock(int type, int data) {
        super(type, data);
    }

    /**
     * Get the type of block.
     * 
     * @return the type
     */
    public int getType() {
        return getId();
    }

    /**
     * Set the type of block.
     * 
     * @param type the type to set
     */
    public void setType(int type) {
        setId(type);
    }

    /**
     * Returns true if it's air.
     *
     * @return if air
     */
    public boolean isAir() {
        return getType() == BlockID.AIR;
    }

    /**
     * Rotate this block 90 degrees.
     * 
     * @return new data value
     */
    public int rotate90() {
        int newData = BlockData.rotate90(getType(), getData());
        setData(newData);
        return newData;
    }

    /**
     * Rotate this block -90 degrees.
     * 
     * @return new data value
     */
    public int rotate90Reverse() {
        int newData = BlockData.rotate90Reverse(getType(), getData());
        setData((short) newData);
        return newData;
    }

    /**
     * Cycle the damage value of the block forward or backward
     *
     * @param increment 1 for forward, -1 for backward
     * @return new data value
     */
    public int cycleData(int increment) {
        int newData = BlockData.cycle(getType(), getData(), increment);
        setData((short) newData);
        return newData;
    }

    /**
     * Flip this block.
     * 
     * @return this block
     */
    public BaseBlock flip() {
        setData((short) BlockData.flip(getType(), getData()));
        return this;
    }

    /**
     * Flip this block.
     * 
     * @param direction direction to flip in
     * @return this block
     */
    public BaseBlock flip(FlipDirection direction) {
        setData((short) BlockData.flip(getType(), getData(), direction));
        return this;
    }

    /**
     * Checks whether the type ID and data value are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BaseBlock)) {
            return false;
        }

        final BaseBlock otherBlock = (BaseBlock) o;
        if (getType() != otherBlock.getType()) {
            return false;
        }

        return getData() == otherBlock.getData();
    }

    /**
     * Checks if the type is the same, and if data is the same if only data != -1.
     * 
     * @param o other block
     * @return true if equal
     */
    public boolean equalsFuzzy(BaseBlock o) {
        return (getType() == o.getType()) && (getData() == o.getData() || getData() == -1 || o.getData() == -1);
    }

    /**
     * @param iter
     * @return
     * @deprecated This method is silly, use {@link #containsFuzzy(java.util.Collection, BaseBlock)} instead.
     */
    @Deprecated
    public boolean inIterable(Iterable<BaseBlock> iter) {
        for (BaseBlock block : iter) {
            if (block.equalsFuzzy(this)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsFuzzy(Collection<BaseBlock> collection, BaseBlock o) {
        // allow masked data in the searchBlocks to match various types
        for (BaseBlock b : collection) {
            if (b.equalsFuzzy(o)) {
                return true;
            }
        }
        return false;
    }
}
