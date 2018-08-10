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

package com.sk89q.worldedit.world.block;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.registry.state.Property;

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

    private BlockState blockState;
    @Nullable private CompoundTag nbtData;

    /**
     * Construct a block with a state.
     *
     * @param blockState The blockstate
     */
    protected BaseBlock(BlockState blockState) {
        this.blockState = blockState;
    }

    /**
     * Construct a block with the given ID, data value and NBT data structure.
     *
     * @param state The block state
     * @param nbtData NBT data, which must be provided
     */
    protected BaseBlock(BlockState state, CompoundTag nbtData) {
        checkNotNull(nbtData);
        this.blockState = state;
        this.nbtData = nbtData;
    }

    /**
     * Create a clone of another block.
     *
     * @param other the other block
     */
    public BaseBlock(BaseBlock other) {
        this(other.toImmutableState(), other.getNbtData());
    }

    /**
     * Gets a map of state to statevalue
     *
     * @return The state map
     */
    @Override
    public Map<Property<?>, Object> getStates() {
        return this.blockState.getStates();
    }

    @Override
    public BlockType getBlockType() {
        return this.blockState.getBlockType();
    }

    @Override
    public <V> BaseBlock with(Property<V> property, V value) {
        return new BaseBlock(this.blockState.with(property, value), getNbtData());
    }

    /**
     * Gets the State for this Block.
     *
     * @param property The state to get the value for
     * @return The state value
     */
    @Override
    public <V> V getState(Property<V> property) {
        return this.blockState.getState(property);
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
     * Checks whether the type ID and data value are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BaseBlock)) {
            return false;
        }

        final BaseBlock otherBlock = (BaseBlock) o;

        return this.toImmutableState().equals(otherBlock.toImmutableState()) && Objects.equals(getNbtData(), otherBlock.getNbtData());

    }

    /**
     * Checks if the type is the same, and if the matched states are the same.
     * 
     * @param o other block
     * @return true if equal
     */
    @Override
    public boolean equalsFuzzy(BlockStateHolder o) {
        return this.toImmutableState().equalsFuzzy(o);
    }

    @Override
    public BlockState toImmutableState() {
        return this.blockState;
    }

    @Override
    public BaseBlock toBaseBlock() {
        return this;
    }

    @Override
    public BaseBlock toBaseBlock(CompoundTag compoundTag) {
        if (compoundTag == null) {
            return this.blockState.toBaseBlock();
        } else if (compoundTag == this.nbtData) {
            return this;
        } else {
            return new BaseBlock(this.blockState, compoundTag);
        }
    }

    @Override
    public int hashCode() {
        int ret = toImmutableState().hashCode() << 3;
        if (hasNbtData()) {
            ret += getNbtData().hashCode();
        }
        return ret;
    }

    @Override
    public String toString() {
//        if (getNbtData() != null) { // TODO Maybe make some JSON serialiser to make this not awful.
//            return blockState.getAsString() + " {" + String.valueOf(getNbtData()) + "}";
//        } else {
            return blockState.getAsString();
//        }
    }

}
