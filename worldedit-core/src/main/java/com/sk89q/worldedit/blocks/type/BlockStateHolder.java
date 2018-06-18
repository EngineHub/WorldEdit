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

import com.sk89q.worldedit.world.registry.state.State;
import com.sk89q.worldedit.world.registry.state.value.StateValue;

import java.util.Map;

public interface BlockStateHolder<T extends BlockStateHolder> {

    /**
     * Get the block type
     *
     * @return The type
     */
    BlockType getBlockType();

    /**
     * Returns a BlockState with the given state and value applied.
     *
     * @param state The state
     * @param value The value
     * @return The modified state, or same if could not be applied
     */
    T with(State state, StateValue value);

    /**
     * Gets the value at the given state
     *
     * @param state The state
     * @return The value
     */
    StateValue getState(State state);

    /**
     * Gets an immutable collection of the states.
     *
     * @return The states
     */
    Map<State, StateValue> getStates();

    /**
     * Checks if the type is the same, and if the matched states are the same.
     *
     * @param o other block
     * @return true if equal
     */
    boolean equalsFuzzy(BlockStateHolder o);
}
