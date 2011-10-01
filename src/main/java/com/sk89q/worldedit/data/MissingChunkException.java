// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.data;

import com.sk89q.worldedit.Vector2D;

/**
 *
 * @author sk89q
 */
public class MissingChunkException extends ChunkStoreException {
    private static final long serialVersionUID = 8013715483709973489L;

    private Vector2D pos;

    public MissingChunkException() {
        super();
    }

    public MissingChunkException(Vector2D pos) {
        super();
        this.pos = pos;
    }

    /**
     * Get chunk position in question. May be null if unknown.
     *
     * @return
     */
    public Vector2D getChunkPosition() {
        return pos;
    }
}
