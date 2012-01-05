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

/**
 * Represents a block.
 *
 * @author sk89q
 */
public class BaseBlock {
    /**
     * BaseBlock type.
     */
    private short type = 0;
    /**
     * BaseBlock data.
     */
    private byte data = 0;

    /**
     * Construct the block with its type.
     *
     * @param type
     */
    public BaseBlock(int type) {
        this.type = (short) type;
    }

    /**
     * Construct the block with its type and data.
     *
     * @param type
     * @param data
     */
    public BaseBlock(int type, int data) {
        this.type = (short) type;
        this.data = (byte) data;
    }

    /**
     * @return the type
     */
    public int getType() {
        return (int) type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = (short) type;
    }

    /**
     * @return the data
     */
    public int getData() {
        return (int) data;
    }

    /**
     * @param data the data to set
     */
    public void setData(int data) {
        this.data = (byte) data;
    }

    /**
     * Returns true if it's air.
     *
     * @return if air
     */
    public boolean isAir() {
        return type == BlockID.AIR;
    }

    /**
     * Rotate this block 90 degrees.
     */
    public int rotate90() {
        int newData = BlockData.rotate90(type, data);
        this.data = (byte) newData;
        return data;
    }

    /**
     * Rotate this block -90 degrees.
     */
    public int rotate90Reverse() {
        int newData = BlockData.rotate90Reverse(type, data);
        this.data = (byte) newData;
        return newData;
    }

    /**
     * Cycle the damage value of the block forward or backward
     *
     * @param increment 1 for forward, -1 for backward
     * @return new data value
     */
    public int cycleData(int increment) {
        int newData = BlockData.cycle(this.type, this.data, increment);
        this.data = (byte) newData;
        return newData;
    }

    /**
     * Flip this block.
     */
    public BaseBlock flip() {
        data = (byte) BlockData.flip(type, data);
        return this;
    }

    /**
     * Flip this block.
     * @param direction
     */
    public BaseBlock flip(FlipDirection direction) {
        data = (byte) BlockData.flip(type, data, direction);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BaseBlock)) {
            return false;
        }
        return (type == ((BaseBlock) o).type)
                && (data == ((BaseBlock) o).data || data == -1 || ((BaseBlock) o).data == -1);
    }

    @Override
    public int hashCode() {
        int ret = type << 3;
        if (data != (byte) -1) ret |= data;
        return ret;
    }

    @Override
    public String toString() {
        return "BaseBlock id: " + getType() + " with damage: " + getData();
    }

    public boolean inIterable(Iterable<BaseBlock> iter) {
        for (BaseBlock block : iter) {
            if (block.equals(this)) {
                return true;
            }
        }
        return false;
    }
}
