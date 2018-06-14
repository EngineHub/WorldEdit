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
import com.sk89q.worldedit.blocks.type.BlockType;
import com.sk89q.worldedit.blocks.type.BlockTypes;
import com.sk89q.worldedit.foundation.Block;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.world.registry.BundledBlockData;
import com.sk89q.worldedit.world.registry.state.State;
import com.sk89q.worldedit.world.registry.state.value.StateValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Represents a mutable "snapshot" of a block.
 *
 * <p>An instance of this block contains all the information needed to
 * accurately reproduce the block, provided that the instance was
 * made correctly. In some implementations, it may not be possible to get a
 * snapshot of blocks correctly, so, for example, the NBT data for a block
 * may be missing.</p>
 *
 * <p>A peculiar detail of this class is that it accepts {@code -1} as a
 * valid data value. This is due to legacy reasons: WorldEdit uses -1
 * as a "wildcard" block value, even though a {@link Mask} would be
 * more appropriate.</p>
 */
@SuppressWarnings("deprecation")
public class BaseBlock extends Block implements TileEntityBlock {

    // Instances of this class should be _as small as possible_ because there will
    // be millions of instances of this object.

    private BlockType blockType;
    private Map<State, StateValue> states;
    @Nullable
    private CompoundTag nbtData;

    /**
     * Construct a block with the given ID and a data value of 0.
     *
     * @param id ID value
     * @see #setId(int)
     */
    @Deprecated
    public BaseBlock(int id) {
        internalSetId(id);
        internalSetData(0);
        this.states = new HashMap<>();
    }

    /**
     * Construct a block with the given type and default data.
     *
     * @param blockType The block type
     */
    public BaseBlock(BlockType blockType) {
        internalSetType(blockType);
        this.states = new HashMap<>();
    }

    /**
     * Construct a block with the given ID and data value.
     *
     * @param id ID value
     * @param data data value
     * @see #setId(int)
     * @see #setData(int)
     */
    @Deprecated
    public BaseBlock(int id, int data) {
        internalSetId(id);
        internalSetData(data);
        this.states = new HashMap<>();
    }

    /**
     * Construct a block with the given ID and data value.
     *
     * @param blockType The block type
     * @param states The states
     * @see #setId(int)
     * @see #setData(int)
     */
    public BaseBlock(BlockType blockType, Map<State, StateValue> states) {
        internalSetType(blockType);
        setStates(states);
    }

    /**
     * Construct a block with the given ID, data value and NBT data structure.
     *
     * @param id ID value
     * @param data data value
     * @param nbtData NBT data, which may be null
     */
    @Deprecated
    public BaseBlock(int id, int data, @Nullable CompoundTag nbtData) {
        internalSetId(id);
        setData(data);
        setNbtData(nbtData);
        this.states = new HashMap<>();
    }

    /**
     * Construct a block with the given ID, data value and NBT data structure.
     *
     * @param blockType The block type
     * @param states The states
     * @param nbtData NBT data, which may be null
     */
    public BaseBlock(BlockType blockType, Map<State, StateValue> states, @Nullable CompoundTag nbtData) {
        setType(blockType);
        setStates(states);
        setNbtData(nbtData);
    }

    /**
     * Create a clone of another block.
     *
     * @param other the other block
     */
    public BaseBlock(BaseBlock other) {
        this(other.getType(), other.getStates(), other.getNbtData());
    }

    /**
     * Get the legacy numerical ID of the block.
     *
     * @return legacy numerical ID
     */
    @Override
    @Deprecated
    public int getId() {
        return this.blockType.getLegacyId();
    }

    /**
     * Set the block ID.
     *
     * @param type block type
     */
    protected final void internalSetType(BlockType type) {
        if (type == null) {
            throw new IllegalArgumentException("You must provide a BlockType");
        }

        this.blockType = type;
    }

    /**
     * Set the block ID.
     *
     * @param id block id
     */
    @Override
    @Deprecated
    public void setId(int id) {
        internalSetId(id);
    }

    @Deprecated
    private void internalSetId(int id) {
        BlockType type = BlockTypes.getBlockType(BundledBlockData.getInstance().fromLegacyId(id));
        internalSetType(type);
    }

    /**
     * Set the block type.
     *
     * @param type block type
     */
    public void setType(BlockType type) {
        internalSetType(type);
    }

    /**
     * Get the block's data value.
     *
     * Broken - do not use
     *
     * @return data value (0-15)
     */
    @Override
    @Deprecated
    public int getData() {
        return 0;
    }

    /**
     * Gets a map of state to statevalue
     *
     * @return The state map
     */
    public Map<State, StateValue> getStates() {
        return Collections.unmodifiableMap(states);
    }

    /**
     * Sets the states of this block.
     *
     * @param states The states
     */
    private void setStates(Map<State, StateValue> states) {
        this.states = states;
    }

    /**
     * Gets the State for this Block.
     *
     * @param state The state to get the value for
     * @return The state value
     */
    public StateValue getState(State state) {
        return states.get(state);
    }

    /**
     * Sets a state to a specific value
     *
     * @param state The state
     * @param stateValue The value
     */
    public void setState(State state, StateValue stateValue) {
        this.states.put(state, stateValue);
    }

    /**
     * Set the block's data value.
     *
     * @param data block data value
     */
    @Deprecated
    protected final void internalSetData(int data) {
    }

    /**
     * Set the block's data value.
     *
     * @param data block data value
     */
    @Override
    @Deprecated
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
    @Override
    @Deprecated
    public void setIdAndData(int id, int data) {
        setId(id);
        setData(data);
    }

    /**
     * Returns whether there are no matched states.
     *
     * @return true if there are no matched states
     */
    @Override
    public boolean hasWildcardData() {
        return getStates().isEmpty();
    }

    public boolean hasWildcardDataFor(State state) {
        return getState(state) == null;
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
        if (idTag instanceof StringTag) {
            return ((StringTag) idTag).getValue();
        } else {
            return "";
        }
    }

    @Nullable
    @Override
    public CompoundTag getNbtData() {
        return nbtData;
    }

    @Override
    public void setNbtData(@Nullable CompoundTag nbtData) {
        this.nbtData = nbtData;
    }

    /**
     * Get the type of block.
     * 
     * @return the type
     */
    public BlockType getType() {
        return this.blockType;
    }

    /**
     * Returns true if it's air.
     *
     * @return if air
     */
    public boolean isAir() {
        return getType() == BlockTypes.AIR;
    }

    /**
     * Rotate this block 90 degrees.
     *
     * @return new data value
     * @deprecated Use {@link BlockData#rotate90(int, int)}
     */
    @Deprecated
    public int rotate90() {
        int newData = BlockData.rotate90(getType().getLegacyId(), getData());
        setData(newData);
        return newData;
    }

    /**
     * Rotate this block -90 degrees.
     * 
     * @return new data value
     * @deprecated Use {@link BlockData#rotate90Reverse(int, int)}
     */
    @Deprecated
    public int rotate90Reverse() {
        int newData = BlockData.rotate90Reverse(getType().getLegacyId(), getData());
        setData((short) newData);
        return newData;
    }

    /**
     * Cycle the damage value of the block forward or backward
     *
     * @param increment 1 for forward, -1 for backward
     * @return new data value
     * @deprecated Use {@link BlockData#cycle(int, int, int)}
     */
    @Deprecated
    public int cycleData(int increment) {
        int newData = BlockData.cycle(getType().getLegacyId(), getData(), increment);
        setData((short) newData);
        return newData;
    }

    /**
     * Flip this block.
     * 
     * @return this block
     * @deprecated Use {@link BlockData#flip(int, int)}
     */
    @Deprecated
    public BaseBlock flip() {
        setData((short) BlockData.flip(getType().getLegacyId(), getData()));
        return this;
    }

    /**
     * Flip this block.
     * 
     * @param direction direction to flip in
     * @return this block
     * @deprecated Use {@link BlockData#flip(int, int, FlipDirection)}
     */
    @Deprecated
    public BaseBlock flip(FlipDirection direction) {
        setData((short) BlockData.flip(getType().getLegacyId(), getData(), direction));
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

        return getType() == otherBlock.getType() && getData() == otherBlock.getData();

    }

    /**
     * Checks if the type is the same, and if the matched states are the same.
     * 
     * @param o other block
     * @return true if equal
     */
    public boolean equalsFuzzy(BaseBlock o) {
        if (!getType().equals(o.getType())) {
            return false;
        }

        List<State> differingStates = new ArrayList<>();
        for (State state : o.getStates().keySet()) {
            if (getState(state) == null) {
                differingStates.add(state);
            }
        }
        for (State state : getStates().keySet()) {
            if (o.getState(state) == null) {
                differingStates.add(state);
            }
        }

        for (State state : differingStates) {
            if (!getState(state).equals(o.getState(state))) {
                return false;
            }
        }

        return true;
    }

    /**
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

    /**
     * @deprecated Use {@link Blocks#containsFuzzy(Collection, BaseBlock)}
     */
    @Deprecated
    public static boolean containsFuzzy(Collection<BaseBlock> collection, BaseBlock o) {
        return Blocks.containsFuzzy(collection, o);
    }

    @Override
    public int hashCode() {
        int ret = getType().hashCode() << 3;
        ret += getStates().hashCode();
        return ret;
    }

    @Override
    public String toString() {
        return "Block{Type:" + getType().getId() + ", States: " + getStates().toString() + "}";
    }

}
