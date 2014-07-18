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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

import javax.annotation.Nullable;

/**
 * Describes a possible value for a {@code State}.
 */
public interface StateValue {

    /**
     * Return whether this state is set on the given block.
     *
     * @param block the block
     * @return true if this value is set
     */
    boolean isSet(BaseBlock block);

    /**
     * Set the state to this value on the given block.
     *
     * @param block the block to change
     * @return true if the value was set successfully
     */
    boolean set(BaseBlock block);

    /**
     * Return the direction associated with this value.
     *
     * @return the direction, otherwise null
     */
    @Nullable
    Vector getDirection();

}
