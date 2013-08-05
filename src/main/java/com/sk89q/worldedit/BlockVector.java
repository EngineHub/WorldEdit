// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit;

/**
 * Extension of Vector that supports being compared as ints (for accuracy).
 *
 * @author sk89q
 */
public class BlockVector extends Vector {
    public static final BlockVector ZERO = new BlockVector(0, 0, 0);
    public static final BlockVector UNIT_X = new BlockVector(1, 0, 0);
    public static final BlockVector UNIT_Y = new BlockVector(0, 1, 0);
    public static final BlockVector UNIT_Z = new BlockVector(0, 0, 1);
    public static final BlockVector ONE = new BlockVector(1, 1, 1);

    /**
     * Construct the Vector object.
     *
     * @param pt
     */
    public BlockVector(Vector pt) {
        super(pt);
    }

    /**
     * Construct the Vector object.
     *
     * @param x
     * @param y
     * @param z
     */
    public BlockVector(int x, int y, int z) {
        super(x, y, z);
    }

    /**
     * Construct the Vector object.
     *
     *
     * @param x
     * @param y
     * @param z
     */
    public BlockVector(float x, float y, float z) {
        super(x, y, z);
    }

    /**
     * Construct the Vector object.
     *
     *
     * @param x
     * @param y
     * @param z
     */
    public BlockVector(double x, double y, double z) {
        super(x, y, z);
    }

    /**
     * Checks if another object is equivalent.
     *
     * @param obj
     * @return whether the other object is equivalent
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector)) {
            return false;
        }
        Vector other = (Vector) obj;
        return (int) other.getX() == (int) this.x && (int) other.getY() == (int) this.y
                && (int) other.getZ() == (int) this.z;

    }

    /**
     * Gets the hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return ((int) x << 19) ^
                ((int) y << 12) ^
                (int) z;
    }

    @Override
    public BlockVector toBlockVector() {
        return this;
    }
}
