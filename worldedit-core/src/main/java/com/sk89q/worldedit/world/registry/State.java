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

package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.blocks.BaseBlock;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Describes a state property of a block.
 *
 * <p>Example states include "variant" (indicating material or type) and
 * "facing" (indicating orientation).</p>
 */
public interface State {

    /**
     * Return a map of available values for this state.
     *
     * <p>Keys are the value of state and map values describe that
     * particular state value.</p>
     *
     * @return the map of state values
     */
    Map<String, ? extends StateValue> valueMap();

    /**
     * Get the value that the block is set to.
     *
     * @param block the block
     * @return the state, otherwise null if the block isn't set to any of the values
     */
    @Nullable
    StateValue getValue(BaseBlock block);

    /**
     * Returns whether this state contains directional data.
     *
     * @return true if directional data is available
     */
    boolean hasDirection();

}
