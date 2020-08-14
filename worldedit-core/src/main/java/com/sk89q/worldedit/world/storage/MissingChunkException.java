/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.world.storage;

import com.sk89q.worldedit.math.Vector2;

/**
 * Thrown if a chunk is missing.
 */
public class MissingChunkException extends ChunkStoreException {

    private Vector2 position;

    public MissingChunkException() {
        super();
    }

    public MissingChunkException(Vector2 position) {
        super();
        this.position = position;
    }

    /**
     * Get chunk position in question. May be null if unknown.
     *
     * @return a chunk position
     */
    public Vector2 getChunkPosition() {
        return position;
    }

}
