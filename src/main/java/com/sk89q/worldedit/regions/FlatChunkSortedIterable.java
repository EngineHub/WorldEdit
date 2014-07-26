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

package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.Vector2D;

import java.util.Iterator;

/**
 * Provides an {@code Iterator} over locations that sorts all visits
 * by chunk. All the positions within a chunk are exhausted before moving
 * onto the next chunk.
 *
 * @see ChunkSortedIterable the 3D version of this class
 */
public interface FlatChunkSortedIterable {

    /**
     * Get a chunk shorted iterator.
     *
     * <p>All the positions within a chunk are exhausted before moving
     * onto the next chunk.</p>
     *
     * @return an iterator
     */
    Iterator<? extends Vector2D> flatChunkSortedIterator();

}
