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

/**
 * Represents the adjacency of one vector to another. Works similarly to
 * Bukkit's BlockFace class.
 *
 * @author wizjany
 */
public enum VectorFace {
    NORTH(-1, 0, 0),
    EAST(0, 0, -1),
    SOUTH(1, 0, 0),
    WEST(0, 0, 1),
    UP(0, 1, 0),
    DOWN(0, -1, 0),
    NORTH_EAST(NORTH, EAST),
    NORTH_WEST(NORTH, WEST),
    SOUTH_EAST(SOUTH, EAST),
    SOUTH_WEST(SOUTH, WEST),
    ABOVE_NORTH(UP, NORTH),
    BELOW_NORTH(DOWN, NORTH),
    ABOVE_SOUTH(UP, SOUTH),
    BELOW_SOUTH(DOWN, SOUTH),
    ABOVE_WEST(UP, WEST),
    BELOW_WEST(DOWN, WEST),
    ABOVE_EAST(UP, EAST),
    BELOW_EAST(DOWN, EAST),
    SELF(0, 0, 0);

    private int modX;
    private int modY;
    private int modZ;

    private VectorFace(final int modX, final int modY, final int modZ) {
        this.modX = modX;
        this.modY = modY;
        this.modZ = modZ;
    }

    private VectorFace(VectorFace face1, VectorFace face2) {
        this.modX = face1.getModX() + face2.getModX();
        this.modY = face1.getModY() + face2.getModY();
        this.modZ = face1.getModZ() + face2.getModZ();
    }

    public int getModX() {
        return modX;
    }

    public int getModZ() {
        return modZ;
    }

    public int getModY() {
        return modY;
    }

    public static VectorFace fromMods(int modX2, int modY2, int modZ2) {
        for (VectorFace face : values()) {
            if (face.getModX() == modX2
                    && face.getModY() == modY2
                    && face.getModZ() == modZ2) {
                return face;
            }
        }
        return VectorFace.SELF;
    }
}
