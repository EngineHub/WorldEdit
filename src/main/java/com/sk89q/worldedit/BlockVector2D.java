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

package com.sk89q.worldedit;

/**
 * Extension of Vector2D that supports being compared as ints (for accuracy).
 *
 * @author sk89q
 */
public class BlockVector2D extends Vector2D {
    /**
     * Construct the Vector object.
     *
     * @param pt
     */
    public BlockVector2D(Vector2D pt) {
        super(pt);
    }

    /**
     * Construct the Vector object.
     *
     * @param x
     * @param z
     */
    public BlockVector2D(int x, int z) {
        super(x, z);
    }

    /**
     * Construct the Vector object.
     *
     * @param x
     * @param z
     */
    public BlockVector2D(float x, float z) {
        super(x, z);
    }

    /**
     * Construct the Vector object.
     *
     * @param x
     * @param z
     */
    public BlockVector2D(double x, double z) {
        super(x, z);
    }

    /**
     * Checks if another object is equivalent.
     *
     * @param obj
     * @return whether the other object is equivalent
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector2D)) {
            return false;
        }
        Vector2D other = (Vector2D)obj;
        return (int)other.x == (int)this.x && (int)other.z == (int)this.z;

    }

    /**
     * Gets the hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return (Integer.valueOf((int)x).hashCode() >> 13) ^
                Integer.valueOf((int)z).hashCode();
    }
}
