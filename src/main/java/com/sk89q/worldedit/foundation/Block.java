// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.foundation;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.data.DataException;

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
 * </p>
 * Currently, {@link BaseBlock} is used throughout WorldEdit and implementations, but
 * eventually an API-breaking transition will occur to switch to this object instead.
 * As-is, the definition of this class is complete, but may need changes in MC 1.4
 * because data values may be eradicated.
 */
public class Block implements TileEntityBlock {
    
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
    public Block(int id) {
        setId(id);
        setData(0);
    }
    
    /**
     * Construct a block with the given ID and data value.
     * 
     * @param id ID value
     * @param data data value
     * @see #setId(int)
     * @see #setData(int)
     */
    public Block(int id, int data) {
        setId(id);
        setData(data);
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
    public Block(int id, int data, CompoundTag nbtData) throws DataException {
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
    public void setId(int id) {
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
    public void setData(int data) {
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
