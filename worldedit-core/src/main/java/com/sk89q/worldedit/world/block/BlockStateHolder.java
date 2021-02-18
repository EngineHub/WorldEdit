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
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public interface BlockStateHolder<B extends BlockStateHolder<B>> extends Pattern {

    /**
     * Get the block type.
     *
     * @return The type
     */
    BlockType getBlockType();

    /**
     * Returns a BlockState with the given state and value applied.
     *
     * @param property The state
     * @param value The value
     * @return The modified state, or same if could not be applied
     */
    <V> B with(final Property<V> property, final V value);

    /**
     * Gets the value for the given state.
     *
     * @param property The state
     * @return The value
     */
    <V> V getState(Property<V> property);

    /**
     * Gets an immutable collection of the states.
     *
     * @return The states
     */
    Map<Property<?>, Object> getStates();

    /**
     * Checks if the type is the same, and if the matched states are the same.
     *
     * @param o other block
     * @return true if equal
     */
    boolean equalsFuzzy(BlockStateHolder<?> o);

    /**
     * Returns an immutable {@link BlockState} from this BlockStateHolder.
     *
     * @return A BlockState
     */
    BlockState toImmutableState();

    /**
     * Gets a {@link BaseBlock} from this BlockStateHolder.
     *
     * @return The BaseBlock
     */
    BaseBlock toBaseBlock();

    /**
     * Gets a {@link BaseBlock} from this BlockStateHolder.
     *
     * @param compoundTag The NBT Data to apply
     * @return The BaseBlock
     * @deprecated Use {@link BlockStateHolder#toBaseBlock(LazyReference)}.
     */
    @Deprecated
    default BaseBlock toBaseBlock(CompoundTag compoundTag) {
        return toBaseBlock(compoundTag == null ? null : LazyReference.from(compoundTag::asBinaryTag));
    }

    /**
     * Gets a {@link BaseBlock} from this BlockStateHolder.
     *
     * @param compoundTag The NBT Data to apply
     * @return The BaseBlock
     * @apiNote This must be overridden by new subclasses. See {@link NonAbstractForCompatibility}
     *          for details
     */
    @NonAbstractForCompatibility(
        delegateName = "toBaseBlock",
        delegateParams = { CompoundTag.class }
    )
    default BaseBlock toBaseBlock(LazyReference<CompoundBinaryTag> compoundTag) {
        DeprecationUtil.checkDelegatingOverride(getClass());

        return toBaseBlock(compoundTag == null ? null : new CompoundTag(compoundTag.getValue()));
    }

    /**
     * Gets a {@link BaseBlock} from this BlockStateHolder.
     *
     * @param compoundTag The NBT Data to apply
     * @return The BaseBlock
     */
    default BaseBlock toBaseBlock(CompoundBinaryTag compoundTag) {
        return toBaseBlock(compoundTag == null ? null : LazyReference.computed(compoundTag));
    }

    @Override
    default BaseBlock applyBlock(BlockVector3 position) {
        return toBaseBlock();
    }

    default String getAsString() {
        if (getStates().isEmpty()) {
            return this.getBlockType().getId();
        } else {
            String properties = getStates().entrySet().stream()
                .map(entry -> entry.getKey().getName()
                    + "="
                    + entry.getValue().toString().toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(","));
            return this.getBlockType().getId() + "[" + properties + "]";
        }
    }
}
