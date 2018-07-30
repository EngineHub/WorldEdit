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

package com.sk89q.worldedit.function.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.FlatRegionFunction;

/**
 * Offsets the position parameter by adding a given offset vector.
 */
public class FlatRegionOffset implements FlatRegionFunction {

    private Vector2D offset;
    private final FlatRegionFunction function;

    /**
     * Create a new instance.
     *
     * @param offset the offset
     * @param function the function that is called with the offset position
     */
    public FlatRegionOffset(Vector2D offset, FlatRegionFunction function) {
        checkNotNull(function);
        setOffset(offset);
        this.function = function;
    }

    /**
     * Get the offset that is added to the position.
     *
     * @return the offset
     */
    public Vector2D getOffset() {
        return offset;
    }

    /**
     * Set the offset that is added to the position.
     *
     * @param offset the offset
     */
    public void setOffset(Vector2D offset) {
        checkNotNull(offset);
        this.offset = offset;
    }

    @Override
    public boolean apply(Vector2D position) throws WorldEditException {
        return function.apply(position.add(offset));
    }
}
