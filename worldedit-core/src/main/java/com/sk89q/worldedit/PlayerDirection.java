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

package com.sk89q.worldedit;

import com.sk89q.worldedit.util.Direction;

/**
 * The player's direction.
 *
 * <p>In the future, this class will be replaced with {@link Direction}.</p>
 */
public enum PlayerDirection {

    NORTH(new Vector(0, 0, -1), true),
    NORTH_EAST((new Vector(1, 0, -1)).normalize(), false),
    EAST(new Vector(1, 0, 0), true),
    SOUTH_EAST((new Vector(1, 0, 1)).normalize(), false),
    SOUTH(new Vector(0, 0, 1), true),
    SOUTH_WEST((new Vector(-1, 0, 1)).normalize(), false),
    WEST(new Vector(-1, 0, 0), true),
    NORTH_WEST((new Vector(-1, 0, -1)).normalize(), false),
    UP(new Vector(0, 1, 0), true),
    DOWN(new Vector(0, -1, 0), true);

    private final Vector dir;
    private final boolean isOrthogonal;

    PlayerDirection(Vector vec, boolean isOrthogonal) {
        this.dir = vec;
        this.isOrthogonal = isOrthogonal;
    }

    public Vector vector() {
        return dir;
    }

    public boolean isOrthogonal() {
        return isOrthogonal;
    }

}
