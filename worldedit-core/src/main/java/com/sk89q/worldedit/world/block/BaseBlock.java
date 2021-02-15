/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.world.block;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.util.nbt.TagStringIO;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a "snapshot" of a block with NBT Data.
 *
 * <p>An instance of this block contains all the information needed to
 * accurately reproduce the block, provided that the instance was
 * made correctly. In some implementations, it may not be possible to get a
 * snapshot of blocks correctly, so, for example, the NBT data for a block
 * may be missing.</p>
 */
public class BaseBlock implements BlockStateHolder<BaseBlock>, TileEntityBlock {

    private final BlockState blockState;
    @Nullable
    private final LazyReference<CompoundBinaryTag> nbtData;

    /**
     * Construct a block with a state.
     *
     * @param blockState The blockstate
     */
    protected BaseBlock(BlockState blockState) {
        this.blockState = blockState;
        this.nbtData = null;
    }

    /**
     * Construct a block with the given ID, data value and NBT data structure.
     *
     * @param state The block state
     * @param nbtData NBT data, which must be provided
     */
    @Deprecated
    protected BaseBlock(BlockState state, CompoundTag nbtData) {
        this(state, LazyReference.from(checkNotNull(nbtData)::asBinaryTag));
    }


    /**
     * Construct a block with the given ID, data value and NBT data structure.
     *
     * @param state The block state
     * @param nbtData NBT data, which must be provided
     */
    protected BaseBlock(BlockState state, LazyReference<CompoundBinaryTag> nbtData) {
        checkNotNull(nbtData);
        this.blockState = state;
        this.nbtData = nbtData;
    }

    /**
     * Gets a map of state to state values.
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
        return this.blockState.with(property, value).toBaseBlock(getNbtReference());
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
    public String getNbtId() {
        LazyReference<CompoundBinaryTag> nbtData = this.nbtData;
        if (nbtData == null) {
            return "";
        }
        return nbtData.getValue().getString("id");
    }

    @Nullable
    @Override
    public LazyReference<CompoundBinaryTag> getNbtReference() {
        return this.nbtData;
    }

    @Override
    public void setNbtReference(@Nullable LazyReference<CompoundBinaryTag> nbtData) {
        throw new UnsupportedOperationException("This class is immutable.");
    }

    /**
     * Checks whether the type ID and data value are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BaseBlock)) {
            if (nbtData == null && o instanceof BlockStateHolder) {
                return Objects.equals(toImmutableState(), ((BlockStateHolder<?>) o).toImmutableState());
            }
            return false;
        }

        final BaseBlock otherBlock = (BaseBlock) o;

        return this.blockState.equalsFuzzy(otherBlock.blockState) && Objects.equals(getNbt(), otherBlock.getNbt());
    }

    /**
     * Checks if the type is the same, and if the matched states are the same.
     *
     * @param o other block
     * @return true if equal
     */
    @Override
    public boolean equalsFuzzy(BlockStateHolder<?> o) {
        return this.blockState.equalsFuzzy(o);
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
    public BaseBlock toBaseBlock(LazyReference<CompoundBinaryTag> compoundTag) {
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
        CompoundBinaryTag nbtData = getNbt();
        if (nbtData != null) {
            ret += nbtData.hashCode();
        }
        return ret;
    }

    @Override
    public String toString() {
        String nbtString = "";
        CompoundBinaryTag nbtData = getNbt();
        if (nbtData != null) {
            try {
                nbtString = TagStringIO.get().asString(nbtData);
            } catch (IOException e) {
                WorldEdit.logger.error("Failed to serialize NBT of Block", e);
            }
        }

        return blockState.getAsString() + nbtString;
    }

}
