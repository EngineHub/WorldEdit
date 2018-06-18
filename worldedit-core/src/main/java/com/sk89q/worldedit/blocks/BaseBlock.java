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
import com.sk89q.worldedit.blocks.type.BlockState;
import com.sk89q.worldedit.blocks.type.BlockStateHolder;
import com.sk89q.worldedit.blocks.type.BlockType;
import com.sk89q.worldedit.blocks.type.BlockTypes;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.world.registry.BundledBlockData;
import com.sk89q.worldedit.world.registry.state.State;
import com.sk89q.worldedit.world.registry.state.value.StateValue;

import java.util.Map;
import java.util.Objects;

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
public class BaseBlock implements BlockStateHolder<BaseBlock>, TileEntityBlock {

    // Instances of this class should be _as small as possible_ because there will
    // be millions of instances of this object.

    private BlockState blockState;
    @Nullable
    private CompoundTag nbtData;

    /**
     * Construct a block with the given ID and a data value of 0.
     *
     * @param id ID value
     */
    @Deprecated
    public BaseBlock(int id) {
        try {
            this.blockState = BlockTypes.getBlockType(BundledBlockData.getInstance().fromLegacyId(id)).getDefaultState();
        } catch (Exception e) {
            System.out.println(id);
            System.out.println(BundledBlockData.getInstance().fromLegacyId(id));
            e.printStackTrace();
        }
    }

    /**
     * Construct a block with a state.
     *
     * @param blockState The blockstate
     */
    public BaseBlock(BlockState blockState) {
        this.blockState = blockState;
    }

    /**
     * Construct a block with the given type and default data.
     *
     * @param blockType The block type
     */
    public BaseBlock(BlockType blockType) {
        this.blockState = blockType.getDefaultState();
    }

    /**
     * Construct a block with the given ID, data value and NBT data structure.
     *
     * @param state The block state
     * @param nbtData NBT data, which may be null
     */
    public BaseBlock(BlockState state, @Nullable CompoundTag nbtData) {
        this.blockState = state;
        setNbtData(nbtData);
    }

    /**
     * Construct a block with the given ID and data value.
     *
     * @param id ID value
     * @param data data value
     */
    @Deprecated
    public BaseBlock(int id, int data) {
        this(id);
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
        this(id);
        setNbtData(nbtData);
    }

    /**
     * Create a clone of another block.
     *
     * @param other the other block
     */
    public BaseBlock(BaseBlock other) {
        this(other.getState(), other.getNbtData());
    }

    /**
     * Get the block state
     *
     * @return The block state
     */
    public BlockState getState() {
        return this.blockState;
    }

    /**
     * Get the legacy numerical ID of the block.
     *
     * @return legacy numerical ID
     */
    @Deprecated
    public int getId() {
        return this.blockState.getBlockType().getLegacyId();
    }

    /**
     * Get the block's data value.
     *
     * Broken - do not use
     *
     * @return data value (0-15)
     */
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
        return this.blockState.getStates();
    }

    @Override
    public BlockType getBlockType() {
        return this.blockState.getBlockType();
    }

    @Override
    public BaseBlock with(State state, StateValue value) {
        return new BaseBlock(this.blockState.with(state, value), getNbtData());
    }

    /**
     * Gets the State for this Block.
     *
     * @param state The state to get the value for
     * @return The state value
     */
    public StateValue getState(State state) {
        return this.blockState.getState(state);
    }

    /**
     * Set the block's data value.
     *
     * @param data block data value
     */
    @Deprecated
    public void setData(int data) {
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
        return this.nbtData;
    }

    @Override
    public void setNbtData(@Nullable CompoundTag nbtData) {
        throw new UnsupportedOperationException("This class is immutable.");
    }

    /**
     * Returns true if it's air.
     *
     * @return if air
     */
    public boolean isAir() {
        return getBlockType() == BlockTypes.AIR;
    }

    /**
     * Rotate this block 90 degrees.
     *
     * @return new data value
     * @deprecated Use {@link BlockData#rotate90(int, int)}
     */
    @Deprecated
    public int rotate90() {
        int newData = BlockData.rotate90(getBlockType().getLegacyId(), getData());
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
        int newData = BlockData.rotate90Reverse(getBlockType().getLegacyId(), getData());
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
        int newData = BlockData.cycle(getBlockType().getLegacyId(), getData(), increment);
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
        setData((short) BlockData.flip(getBlockType().getLegacyId(), getData()));
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
        setData((short) BlockData.flip(getBlockType().getLegacyId(), getData(), direction));
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

        return this.getState().equals(otherBlock.getState()) && Objects.equals(getNbtData(), otherBlock.getNbtData());

    }

    /**
     * Checks if the type is the same, and if the matched states are the same.
     * 
     * @param o other block
     * @return true if equal
     */
    @Override
    public boolean equalsFuzzy(BlockStateHolder o) {
        return this.getState().equalsFuzzy(o);
    }

    @Override
    public int hashCode() {
        int ret = getState().hashCode() << 3;
        if (hasNbtData()) {
            ret += getNbtData().hashCode();
        }
        return ret;
    }

    @Override
    public String toString() {
        return "Block{State: " + this.getState().toString() + ", NBT: " + String.valueOf(getNbtData()) + "}";
    }

}
