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

import com.sk89q.worldedit.registry.state.Property;

import java.util.HashMap;
import java.util.Map;

/**
 * A Fuzzy BlockState. Used for partial matching.
 *
 * Immutable, construct with {@link FuzzyBlockState.Builder}.
 */
public class FuzzyBlockState extends BlockState {

    FuzzyBlockState(BlockType blockType) {
        super(blockType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public BlockState toImmutableState() {
        BlockState state = getBlockType().getDefaultState();
        for (Map.Entry<Property<?>, Object> entry : getStates().entrySet()) {
            state = state.with((Property<Object>) entry.getKey(), entry.getValue());
        }
        return getBlockType().getDefaultState();
    }

    /**
     * Gets an instance of a builder.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for FuzzyBlockState
     */
    public static class Builder {
        private BlockState internalState;
        private Map<Property<?>, Object> values = new HashMap<>();

        /**
         * The type of the Fuzzy BlockState
         *
         * @param type The type
         * @return The builder, for chaining
         */
        public Builder type(BlockType type) {
            checkNotNull(type);
            internalState = type.getDefaultState();
            return this;
        }

        /**
         * The type of the Fuzzy BlockState
         *
         * @param state The state
         * @return The builder, for chaining
         */
        public Builder type(BlockState state) {
            checkNotNull(state);
            internalState = state;
            return this;
        }

        /**
         * Adds a property to the fuzzy BlockState
         *
         * @param property The property
         * @param value The value
         * @param <V> The property type
         * @return The builder, for chaining
         */
        public <V> Builder withProperty(Property<V> property, V value) {
            checkNotNull(property);
            checkNotNull(value);
            checkNotNull(internalState, "The type must be set before the properties!");
            values.put(property, value);
            return this;
        }

        /**
         * Builds a FuzzyBlockState from this builder.
         *
         * @return The fuzzy BlockState
         */
        public FuzzyBlockState build() {
            checkNotNull(internalState);
            FuzzyBlockState blockState = new FuzzyBlockState(internalState.getBlockType());
            for (Map.Entry<Property<?>, Object> entry : values.entrySet()) {
                blockState.setState(entry.getKey(), entry.getValue());
            }
            return blockState;
        }

        /**
         * Resets the builder.
         *
         * @return The builder, for chaining
         */
        public Builder reset() {
            this.internalState = null;
            this.values.clear();
            return this;
        }
    }
}
