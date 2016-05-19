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

package com.sk89q.worldedit.internal.helper;

import com.sk89q.worldedit.util.Direction;

/**
 * Utility methods for working with directions in Minecraft.
 */
public final class MCDirections {

    private MCDirections() {
    }

    public static Direction fromHanging(int i) {
        switch (i) {
            case 0:
                return Direction.SOUTH;
            case 1:
                return Direction.WEST;
            case 2:
                return Direction.NORTH;
            case 3:
                return Direction.EAST;
            default:
                return Direction.NORTH;
        }
    }

    public static int toHanging(Direction direction) {
        switch (direction) {
            case SOUTH:
                return 0;
            case WEST:
                return 1;
            case NORTH:
                return 2;
            case EAST:
                return 3;
            default:
                return 0;
        }
    }

    public static int fromLegacyHanging(byte i) {
        switch (i) {
            case 0: return 2;
            case 1: return 1;
            case 2: return 0;
            default: return 3;
        }
    }

    public static byte toLegacyHanging(int i) {
        switch (i) {
            case 0: return (byte) 2;
            case 1: return (byte) 1;
            case 2: return (byte) 0;
            default: return (byte) 3;
        }
    }

    public static Direction fromRotation(int i) {
        switch (i) {
            case 0:
                return Direction.SOUTH;
            case 1:
                return Direction.SOUTH_SOUTHWEST;
            case 2:
                return Direction.SOUTHWEST;
            case 3:
                return Direction.WEST_SOUTHWEST;
            case 4:
                return Direction.WEST;
            case 5:
                return Direction.WEST_NORTHWEST;
            case 6:
                return Direction.NORTHWEST;
            case 7:
                return Direction.NORTH_NORTHWEST;
            case 8:
                return Direction.NORTH;
            case 9:
                return Direction.NORTH_NORTHEAST;
            case 10:
                return Direction.NORTHEAST;
            case 11:
                return Direction.EAST_NORTHEAST;
            case 12:
                return Direction.EAST;
            case 13:
                return Direction.EAST_SOUTHEAST;
            case 14:
                return Direction.SOUTHEAST;
            case 15:
                return Direction.SOUTH_SOUTHEAST;
            default:
                return Direction.NORTH;
        }
    }

    public static int toRotation(Direction direction) {
        switch (direction) {
            case SOUTH:
                return 0;
            case SOUTH_SOUTHWEST:
                return 1;
            case SOUTHWEST:
                return 2;
            case WEST_SOUTHWEST:
                return 3;
            case WEST:
                return 4;
            case WEST_NORTHWEST:
                return 5;
            case NORTHWEST:
                return 6;
            case NORTH_NORTHWEST:
                return 7;
            case NORTH:
                return 8;
            case NORTH_NORTHEAST:
                return 9;
            case NORTHEAST:
                return 10;
            case EAST_NORTHEAST:
                return 11;
            case EAST:
                return 12;
            case EAST_SOUTHEAST:
                return 13;
            case SOUTHEAST:
                return 14;
            case SOUTH_SOUTHEAST:
                return 15;
            default:
                return 0;
        }
    }

}
