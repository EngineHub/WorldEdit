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

package com.sk89q.worldedit.internal.helper;

import com.sk89q.worldedit.util.Direction;

/**
 * Utility methods for working with directions in Minecraft.
 */
public final class MCDirections {

    private MCDirections() {
    }

    public static Direction fromHanging(int i) {
        return switch (i) {
            case 0 -> Direction.DOWN;
            case 1 -> Direction.UP;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.WEST;
            case 5 -> Direction.EAST;
            default -> Direction.DOWN;
        };
    }

    public static int toHanging(Direction direction) {
        return switch (direction) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
            default -> 0;
        };
    }

    public static Direction fromPre13Hanging(int i) {
        return fromHorizontalHanging(i);
    }

    public static Direction fromHorizontalHanging(int i) {
        return switch (i) {
            case 0 -> Direction.SOUTH;
            case 1 -> Direction.WEST;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.EAST;
            default -> Direction.NORTH;
        };
    }

    public static int toHorizontalHanging(Direction direction) {
        return switch (direction) {
            case SOUTH -> 0;
            case WEST -> 1;
            case NORTH -> 2;
            case EAST -> 3;
            default -> 0;
        };
    }

    public static int fromLegacyHanging(byte i) {
        return switch (i) {
            case 0 -> 2;
            case 1 -> 1;
            case 2 -> 0;
            default -> 3;
        };
    }

    public static Direction fromRotation(int i) {
        return switch (i) {
            case 0 -> Direction.SOUTH;
            case 1 -> Direction.SOUTH_SOUTHWEST;
            case 2 -> Direction.SOUTHWEST;
            case 3 -> Direction.WEST_SOUTHWEST;
            case 4 -> Direction.WEST;
            case 5 -> Direction.WEST_NORTHWEST;
            case 6 -> Direction.NORTHWEST;
            case 7 -> Direction.NORTH_NORTHWEST;
            case 8 -> Direction.NORTH;
            case 9 -> Direction.NORTH_NORTHEAST;
            case 10 -> Direction.NORTHEAST;
            case 11 -> Direction.EAST_NORTHEAST;
            case 12 -> Direction.EAST;
            case 13 -> Direction.EAST_SOUTHEAST;
            case 14 -> Direction.SOUTHEAST;
            case 15 -> Direction.SOUTH_SOUTHEAST;
            default -> Direction.NORTH;
        };
    }

    public static int toRotation(Direction direction) {
        return switch (direction) {
            case SOUTH -> 0;
            case SOUTH_SOUTHWEST -> 1;
            case SOUTHWEST -> 2;
            case WEST_SOUTHWEST -> 3;
            case WEST -> 4;
            case WEST_NORTHWEST -> 5;
            case NORTHWEST -> 6;
            case NORTH_NORTHWEST -> 7;
            case NORTH -> 8;
            case NORTH_NORTHEAST -> 9;
            case NORTHEAST -> 10;
            case EAST_NORTHEAST -> 11;
            case EAST -> 12;
            case EAST_SOUTHEAST -> 13;
            case SOUTHEAST -> 14;
            case SOUTH_SOUTHEAST -> 15;
            default -> 0;
        };
    }

}
