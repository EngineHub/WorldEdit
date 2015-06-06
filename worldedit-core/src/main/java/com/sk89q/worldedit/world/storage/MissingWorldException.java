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

package com.sk89q.worldedit.world.storage;

import javax.annotation.Nullable;

/**
 * Thrown if the world is missing.
 */
public class MissingWorldException extends ChunkStoreException {

    private String worldName;

    public MissingWorldException() {
        super();
    }

    public MissingWorldException(String worldName) {
        super();
        this.worldName = worldName;
    }

    public MissingWorldException(String msg, String worldName) {
        super(msg);
        this.worldName = worldName;
    }

    /**
     * Get name of the world in question. May be null if unknown.
     *
     * @return the world name
     */
    @Nullable
    public String getWorldName() {
        return worldName;
    }
}
