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

package com.sk89q.worldedit.function;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.visitor.LayerVisitor;

/**
 * A function that takes a position and a depth.
 */
public interface LayerFunction {

    /**
     * Returns whether the given block should be "passed through" when
     * conducting the ground search.
     *
     * @param position return whether the given block is the ground
     * @return true if the search should stop
     */
    boolean isGround(Vector position);

    /**
     * Apply the function to the given position.
     *
     * <p>The depth would be the number of blocks from the surface if
     * a {@link LayerVisitor} was used.</p>
     *
     * @param position the position
     * @param depth the depth as a number starting from 0
     * @return true whether this method should be called for further layers
     * @throws WorldEditException thrown on an error
     */
    boolean apply(Vector position, int depth) throws WorldEditException;
}
