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

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 *
 * @author Albert
 */
public final class Point<T> {
    private final T x, y, z;

    /**
     * Construct the Point object.
     * 
     * @param x
     * @param y
     * @param z
     */
    public Point(T x, T y, T z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @return the x
     */
    public T getX() {
        return x;
    }


    /**
     * @return the y
     */
    public T getY() {
        return y;
    }

    /**
     * @return the z
     */
    public T getZ() {
        return z;
    }

    /**
     * Checks if another object is equivalent.
     * 
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point)) {
            return false;
        }
        Point other = (Point)obj;
        return new EqualsBuilder()
            .append(x, other.x)
            .append(y, other.y)
            .append(z, other.z)
            .isEquals();

    }

    /**
     * Gets the hash code.
     * 
     * @return
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(451, 41).
            append(x).
            append(y).
            append(z).
            toHashCode();
    }

}
