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

package com.sk89q.worldedit.util;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.world.storage.ChunkStore;

/**
 * Vector related utility methods.
 */
public final class Vectors {

    private Vectors() {
    }

    /**
     * Get the chunk location in chunk coordinates for a given block location.
     *
     * @param vector the vector
     * @return in chunk coordinates
     */
    public static Vector2D toChunkVector(Vector vector) {
        return new Vector2D(vector.getBlockX() >> ChunkStore.CHUNK_SHIFTS, vector.getBlockZ() >> ChunkStore.CHUNK_SHIFTS);
    }

}
