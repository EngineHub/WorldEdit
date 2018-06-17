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

package com.sk89q.worldedit.blocks.type;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.sk89q.worldedit.world.registry.state.State;
import com.sk89q.worldedit.world.registry.state.value.SimpleStateValue;

import java.util.HashMap;
import java.util.Map;

/**
 * An immutable class that represents the state a block can be in.
 */
public class BlockState {

    private final BlockType blockType;
    private final Map<State<SimpleStateValue>, SimpleStateValue> values;

    // Neighbouring state table.
    private Table<State<SimpleStateValue>, SimpleStateValue, BlockState> states;

    BlockState(BlockType blockType) {
        this.blockType = blockType;
        this.values = new HashMap<>();
    }

    public void populate(Map<Map<State<SimpleStateValue>, SimpleStateValue>, BlockState> stateMap) {
        final Table<State<SimpleStateValue>, SimpleStateValue, BlockState> states = HashBasedTable.create();

        for(final Map.Entry<State<SimpleStateValue>, SimpleStateValue> entry : this.values.entrySet()) {
            final State<SimpleStateValue> state = entry.getKey();

            state.getValues().forEach(value -> {
                if(value != entry.getValue()) {
                    states.put(state, value, stateMap.get(this.withValue(state, value)));
                }
            });
        }

        this.states = states.isEmpty() ? states : ArrayTable.create(states);
    }

    private Map<State<SimpleStateValue>, SimpleStateValue> withValue(final State<SimpleStateValue> property, final SimpleStateValue value) {
        final Map<State<SimpleStateValue>, SimpleStateValue> values = Maps.newHashMap(this.values);
        values.put(property, value);
        return values;
    }

    /**
     * Get the block type
     *
     * @return The type
     */
    public BlockType getBlockType() {
        return this.blockType;
    }

    /**
     * Returns a BlockState with the given state and value applied.
     *
     * @param state The state
     * @param value The value
     * @return The modified state, or same if could not be applied
     */
    public BlockState with(State<SimpleStateValue> state, SimpleStateValue value) {
        BlockState result = states.get(state, value);
        return result == null ? this : result;
    }

    /**
     * Gets the value at the given state
     *
     * @param state The state
     * @return The value
     */
    public SimpleStateValue getState(State<SimpleStateValue> state) {
        return this.values.get(state);
    }

    /**
     * Internal method used for creating the initial BlockState.
     *
     * Sets a value. DO NOT USE THIS.
     *
     * @param state The state
     * @param value The value
     * @return The blockstate, for chaining
     */
    BlockState setState(State<SimpleStateValue> state, SimpleStateValue value) {
        this.values.put(state, value);
        return this;
    }
}
