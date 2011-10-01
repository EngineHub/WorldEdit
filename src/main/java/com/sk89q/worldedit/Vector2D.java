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
 *
 * @author sk89q
 */
public class Vector2D {
    protected final double x, z;

    /**
     * Construct the Vector2D object.
     *
     * @param x
     * @param z
     */
    public Vector2D(double x, double z) {
        this.x = x;
        this.z = z;
    }

    /**
     * Construct the Vector2D object.
     *
     * @param x
     * @param z
     */
    public Vector2D(int x, int z) {
        this.x = (double)x;
        this.z = (double)z;
    }

    /**
     * Construct the Vector2D object.
     *
     * @param x
     * @param z
     */
    public Vector2D(float x, float z) {
        this.x = (double)x;
        this.z = (double)z;
    }

    /**
     * Construct the Vector2D object.
     *
     * @param pt
     */
    public Vector2D(Vector2D pt) {
        this.x = pt.x;
        this.z = pt.z;
    }

    /**
     * Construct the Vector2D object.
     */
    public Vector2D() {
        this.x = 0;
        this.z = 0;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @return the x
     */
    public int getBlockX() {
        return (int) Math.round(x);
    }

    /**
     * Set X.
     *
     * @param x
     * @return new vector
     */
    public Vector2D setX(double x) {
        return new Vector2D(x, z);
    }

    /**
     * Set X.
     *
     * @param x
     * @return new vector
     */
    public Vector2D setX(int x) {
        return new Vector2D(x, z);
    }

    /**
     * @return the z
     */
    public double getZ() {
        return z;
    }

    /**
     * @return the z
     */
    public int getBlockZ() {
        return (int) Math.round(z);
    }

    /**
     * Set Z.
     *
     * @param z
     * @return new vector
     */
    public Vector2D setZ(double z) {
        return new Vector2D(x, z);
    }

    /**
     * Set Z.
     *
     * @param z
     * @return new vector
     */
    public Vector2D setZ(int z) {
        return new Vector2D(x, z);
    }

    /**
     * Gets a BlockVector version.
     *
     * @return BlockVector
     */
    public BlockVector2D toBlockVector2D() {
        return new BlockVector2D(this);
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
        Vector other = (Vector) obj;
        return other.x == this.x && other.z == this.z;

    }

    /**
     * Gets the hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return ((new Double(x)).hashCode() >> 13) ^
                (new Double(z)).hashCode();
    }

    /**
     * Returns string representation "(x, y, z)".
     *
     * @return string
     */
    @Override
    public String toString() {
        return "(" + x + ", " + z + ")";
    }
}
