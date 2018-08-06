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

import javax.annotation.Nullable;

/**
 * A collection of cardinal, ordinal, and secondary-ordinal directions.
 */
public enum Direction {

    NORTH(new Vector(0, 0, -1), Flag.CARDINAL),
    EAST(new Vector(1, 0, 0), Flag.CARDINAL),
    SOUTH(new Vector(0, 0, 1), Flag.CARDINAL),
    WEST(new Vector(-1, 0, 0), Flag.CARDINAL),

    UP(new Vector(0, 1, 0), Flag.UPRIGHT),
    DOWN(new Vector(0, -1, 0), Flag.UPRIGHT),

    NORTHEAST(new Vector(1, 0, -1), Flag.ORDINAL),
    NORTHWEST(new Vector(-1, 0, -1), Flag.ORDINAL),
    SOUTHEAST(new Vector(1, 0, 1), Flag.ORDINAL),
    SOUTHWEST(new Vector(-1, 0, 1), Flag.ORDINAL),

    WEST_NORTHWEST(new Vector(-Math.cos(Math.PI / 8), 0, -Math.sin(Math.PI / 8)), Flag.SECONDARY_ORDINAL),
    WEST_SOUTHWEST(new Vector(-Math.cos(Math.PI / 8), 0, Math.sin(Math.PI / 8)), Flag.SECONDARY_ORDINAL),
    NORTH_NORTHWEST(new Vector(-Math.sin(Math.PI / 8), 0, -Math.cos(Math.PI / 8)), Flag.SECONDARY_ORDINAL),
    NORTH_NORTHEAST(new Vector(Math.sin(Math.PI / 8), 0, -Math.cos(Math.PI / 8)), Flag.SECONDARY_ORDINAL),
    EAST_NORTHEAST(new Vector(Math.cos(Math.PI / 8), 0, -Math.sin(Math.PI / 8)), Flag.SECONDARY_ORDINAL),
    EAST_SOUTHEAST(new Vector(Math.cos(Math.PI / 8), 0, Math.sin(Math.PI / 8)), Flag.SECONDARY_ORDINAL),
    SOUTH_SOUTHEAST(new Vector(Math.sin(Math.PI / 8), 0, Math.cos(Math.PI / 8)), Flag.SECONDARY_ORDINAL),
    SOUTH_SOUTHWEST(new Vector(-Math.sin(Math.PI / 8), 0, Math.cos(Math.PI / 8)), Flag.SECONDARY_ORDINAL);

    private final Vector direction;
    private final int flags;

    Direction(Vector vector, int flags) {
        this.direction = vector.normalize();
        this.flags = flags;
    }

    /**
     * Return true if the direction is of a cardinal direction (north, west
     * east, and south).
     *
     * <p>This evaluates as false for directions that have a non-zero
     * Y-component.</p>
     *
     * @return true if cardinal
     */
    public boolean isCardinal() {
        return (flags & Flag.CARDINAL) > 0;
    }

    /**
     * Return true if the direction is of an ordinal direction (northwest,
     * southwest, southeast, northeaast).
     *
     * @return true if ordinal
     */
    public boolean isOrdinal() {
        return (flags & Flag.ORDINAL) > 0;
    }

    /**
     * Return true if the direction is of a secondary ordinal direction
     * (north-northwest, north-northeast, south-southwest, etc.).
     *
     * @return true if secondary ordinal
     */
    public boolean isSecondaryOrdinal() {
        return (flags & Flag.SECONDARY_ORDINAL) > 0;
    }

    /**
     * Return whether Y component is non-zero.
     *
     * @return true if the Y component is non-zero
     */
    public boolean isUpright() {
        return (flags & Flag.UPRIGHT) > 0;
    }

    /**
     * Get the vector.
     *
     * @return the vector
     */
    public Vector toVector() {
        return direction;
    }

    /**
     * Find the closest direction to the given direction vector.
     *
     * @param vector the vector
     * @param flags the only flags that are permitted (use bitwise math)
     * @return the closest direction, or null if no direction can be returned
     */
    @Nullable
    public static Direction findClosest(Vector vector, int flags) {
        if ((flags & Flag.UPRIGHT) == 0) {
            vector = vector.setY(0);
        }
        vector = vector.normalize();

        Direction closest = null;
        double closestDot = -2;
        for (Direction direction : values()) {
            if ((~flags & direction.flags) > 0) {
                continue;
            }

            double dot = direction.toVector().dot(vector);
            if (dot >= closestDot) {
                closest = direction;
                closestDot = dot;
            }
        }

        return closest;
    }

    /**
     * Flags to use with {@link #findClosest(Vector, int)}.
     */
    public static final class Flag {
        public static int CARDINAL = 0x1;
        public static int ORDINAL = 0x2;
        public static int SECONDARY_ORDINAL = 0x4;
        public static int UPRIGHT = 0x8;

        public static int ALL = CARDINAL | ORDINAL | SECONDARY_ORDINAL | UPRIGHT;

        private Flag() {
        }
    }

}

