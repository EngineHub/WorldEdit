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

package com.sk89q.worldedit.reorder.buffer;

import com.sk89q.worldedit.util.LocatedBlock;

public interface MutablePlacementBuffer extends PlacementBuffer {

    @Override
    default boolean isReadOnly() {
        return false;
    }

    /**
     * {@code true} if this implementation is backed by an array which can be retrieved using
     * {@link #array()}.
     */
    boolean hasArray();

    /**
     * If this implementation {@linkplain #hasArray() has an array}, returns the backing array of
     * this buffer.
     *
     * @return the backing array, if present
     * @throws UnsupportedOperationException if there is no backing array
     */
    LocatedBlock[] array();

    /**
     * If this implementation {@linkplain #hasArray() has an array}, returns the offset into the
     * {@linkplain #array() backing array} of this buffer. This offset is added to position before
     * indexing into the array.
     *
     * @return the backing array offset, if present
     * @throws UnsupportedOperationException if there is no backing array
     */
    int arrayOffset();

    /**
     * Relative {@code put} method. Increments the position by one after inserting the element.
     *
     * @param placement the element to insert
     * @return {@code this}
     */
    PlacementBuffer put(LocatedBlock placement);

    /**
     * Relative bulk {@code put} method. Increments the position by {@code other.length} after
     * inserting the elements.
     *
     * @param placements the elements to insert
     * @return {@code this}
     */
    default PlacementBuffer put(LocatedBlock[] placements) {
        return put(placements, 0, placements.length);
    }

    /**
     * Relative bulk {@code put} method. Increments the position by {@code length} after
     * inserting the elements.
     *
     * @param placements the elements to insert
     * @param offset the offset to start from
     * @param length the number of elements to copy
     * @return {@code this}
     */
    PlacementBuffer put(LocatedBlock[] placements, int offset, int length);

    /**
     * Relative bulk {@code put} method. Increments the position by {@code other.remaining()} after
     * inserting the elements.
     *
     * @param placements the elements to insert
     * @return {@code this}
     */
    PlacementBuffer put(PlacementBuffer placements);

    /**
     * Absolute {@code put} method.
     *
     * @param placement the element to insert
     * @return {@code this}
     */
    PlacementBuffer put(int index, LocatedBlock placement);

}
