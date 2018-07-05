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

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.sk89q.worldedit.registry.state.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable class that represents the state a block can be in.
 */
@SuppressWarnings("unchecked")
public class BlockState implements BlockStateHolder<BlockState> {

    private final BlockType blockType;
    private final Map<Property<?>, Object> values;
    private final boolean fuzzy;

    // Neighbouring state table.
    private Table<Property<?>, Object, BlockState> states;

    BlockState(BlockType blockType) {
        this.blockType = blockType;
        this.values = new HashMap<>();
        this.fuzzy = false;
    }

    /**
     * Creates a fuzzy BlockState. This can be used for partial matching.
     *
     * @param blockType The block type
     * @param values The block state values
     */
    public BlockState(BlockType blockType, Map<Property<?>, Object> values) {
        this.blockType = blockType;
        this.values = values;
        this.fuzzy = true;
    }

    public void populate(Map<Map<Property<?>, Object>, BlockState> stateMap) {
        final Table<Property<?>, Object, BlockState> states = HashBasedTable.create();

        for(final Map.Entry<Property<?>, Object> entry : this.values.entrySet()) {
            final Property property = entry.getKey();

            property.getValues().forEach(value -> {
                if(value != entry.getValue()) {
                    states.put(property, value, stateMap.get(this.withValue(property, value)));
                }
            });
        }

        this.states = states.isEmpty() ? states : ArrayTable.create(states);
    }

    private <V> Map<Property<?>, Object> withValue(final Property<V> property, final V value) {
        final Map<Property<?>, Object> values = Maps.newHashMap(this.values);
        values.put(property, value);
        return values;
    }

    @Override
    public BlockType getBlockType() {
        return this.blockType;
    }

    @Override
    public <V> BlockState with(final Property<V> property, final V value) {
        if (fuzzy) {
            return setState(property, value);
        } else {
            BlockState result = states.get(property, value);
            return result == null ? this : result;
        }
    }

    @Override
    public <V> V getState(final Property<V> property) {
        return (V) this.values.get(property);
    }

    @Override
    public Map<Property<?>, Object> getStates() {
        return Collections.unmodifiableMap(this.values);
    }

    public BlockState toFuzzy() {
        return new BlockState(this.getBlockType(), new HashMap<>());
    }

    @Override
    public boolean equalsFuzzy(BlockStateHolder o) {
        if (!getBlockType().equals(o.getBlockType())) {
            return false;
        }

        List<Property> differingProperties = new ArrayList<>();
        for (Object state : o.getStates().keySet()) {
            if (getState((Property) state) == null) {
                differingProperties.add((Property) state);
            }
        }
        for (Property property : getStates().keySet()) {
            if (o.getState(property) == null) {
                differingProperties.add(property);
            }
        }

        for (Property property : differingProperties) {
            if (!getState(property).equals(o.getState(property))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public BlockState toImmutableState() {
        return this;
    }

    /**
     * Internal method used for creating the initial BlockState.
     *
     * Sets a value. DO NOT USE THIS.
     *
     * @param property The state
     * @param value The value
     * @return The blockstate, for chaining
     */
    private <V> BlockState setState(final Property<V> property, final V value) {
        this.values.put(property, value);
        return this;
    }
}
