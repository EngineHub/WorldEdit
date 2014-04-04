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

package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.CuboidClipboard.FlipDirection;
import com.sk89q.worldedit.foundation.Block;
import com.sk89q.worldedit.world.DataException;

import java.util.Collection;

/**
 * Represents a mutable copy of a block that is not tied to any 'real' block in a world.
 * A single instance of this can be set to multiple locations and each location would
 * have a copy of this instance's data.
 * </p>
 * Implementations can and should extend this class to allow native implementations
 * of NBT data handling, primarily for performance reasons. Subclasses can only convert
 * from and to WorldEdit-native NBT structures when absolutely necessary (a.k.a. when
 * {@link #getNbtData()} and {@link #setNbtData(CompoundTag)} are called). When
 * overriding the NBT methods, {@link #getNbtId()} should be overridden too, otherwise
 * the default implementation will invoke {@link #getNbtData()}, a potentially costly
 * operation when it is not needed. Implementations may want to cache converted NBT data
 * structures if possible.
 */
public class BaseBlock extends Block implements TileEntityBlock {

    /**
     * Indicates the highest possible block ID (inclusive) that can be used. This value
     * is subject to change depending on the implementation, but internally this class
     * only supports a range of 4096 IDs (for space reasons), which coincides with the
     * number of possible IDs that official Minecraft supports as of version 1.3.
     */
    public static final int MAX_ID = 4095;

    /**
     * Indicates the maximum data value (inclusive) that can be used. Minecraft 1.4 may
     * abolish usage of data values and this value may be removed in the future.
     */
    public static final int MAX_DATA = 15;

    // Instances of this class should be _as small as possible_ because there will
    // be millions of instances of this object.

    private short id;
    private short data;
    private CompoundTag nbtData;

    /**
     * Construct a block with the given ID and a data value of 0.
     *
     * @param id ID value
     * @see #setId(int)
     */
    public BaseBlock(int id) {
        internalSetId(id);
        internalSetData(0);
    }

    /**
     * Construct a block with the given ID and data value.
     *
     * @param id ID value
     * @param data data value
     * @see #setId(int)
     * @see #setData(int)
     */
    public BaseBlock(int id, int data) {
        internalSetId(id);
        internalSetData(data);
    }

    /**
     * Construct a block with the given ID, data value, and NBT data structure.
     *
     * @param id ID value
     * @param data data value
     * @param nbtData NBT data
     * @throws DataException if possibly the data is invalid
     * @see #setId(int)
     * @see #setData(int)
     * @see #setNbtData(CompoundTag)
     */
    public BaseBlock(int id, int data, CompoundTag nbtData) throws DataException {
        setId(id);
        setData(data);
        setNbtData(nbtData);
    }

    /**
     * Get the ID of the block.
     *
     * @return ID (between 0 and {@link #MAX_ID})
     */
    public int getId() {
        return id;
    }

    /**
     * Set the block ID.
     *
     * @param id block id (between 0 and {@link #MAX_ID}).
     */
    protected final void internalSetId(int id) {
        if (id > MAX_ID) {
            throw new IllegalArgumentException("Can't have a block ID above "
                    + MAX_ID + " (" + id + " given)");
        }

        if (id < 0) {
            throw new IllegalArgumentException("Can't have a block ID below 0");
        }

        this.id = (short) id;
    }

    /**
     * Set the block ID.
     *
     * @param id block id (between 0 and {@link #MAX_ID}).
     */
    public void setId(int id) {
        internalSetId(id);
    }

    /**
     * Get the block's data value.
     *
     * @return data value (0-15)
     */
    public int getData() {
        return data;
    }

    /**
     * Set the block's data value.
     *
     * @param data block data value (between 0 and {@link #MAX_DATA}).
     */
    protected final void internalSetData(int data) {
        if (data > MAX_DATA) {
            throw new IllegalArgumentException(
                    "Can't have a block data value above " + MAX_DATA + " ("
                            + data + " given)");
        }

        if (data < -1) {
            throw new IllegalArgumentException("Can't have a block data value below -1");
        }

        this.data = (short) data;
    }

    /**
     * Set the block's data value.
     *
     * @param data block data value (between 0 and {@link #MAX_DATA}).
     */
    public void setData(int data) {
        internalSetData(data);
    }

    /**
     * Set both the block's ID and data value.
     *
     * @param id ID value
     * @param data data value
     * @see #setId(int)
     * @see #setData(int)
     */
    public void setIdAndData(int id, int data) {
        setId(id);
        setData(data);
    }

    /**
     * Returns whether the data value is -1, indicating that this block is to be
     * used as a wildcard matching block.
     *
     * @return true if the data value is -1
     */
    public boolean hasWildcardData() {
        return getData() == -1;
    }

    @Override
    public boolean hasNbtData() {
        return getNbtData() != null;
    }

    @Override
    public String getNbtId() {
        CompoundTag nbtData = getNbtData();
        if (nbtData == null) {
            return "";
        }
        Tag idTag = nbtData.getValue().get("id");
        if (idTag != null && idTag instanceof StringTag) {
            return ((StringTag) idTag).getValue();
        } else {
            return "";
        }
    }

    @Override
    public CompoundTag getNbtData() {
        return nbtData;
    }

    @Override
    public void setNbtData(CompoundTag nbtData) throws DataException {
        this.nbtData = nbtData;
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

    @Override
    public int hashCode() {
        int ret = getId() << 3;
        if (getData() != (byte) -1) ret |= getData();
        return ret;
    }

    @Override
    public String toString() {
        return "Block{ID:" + getId() + ", Data: " + getData() + "}";
    }

}
