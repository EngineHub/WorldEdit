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
public class Vector {
    protected final double x, y, z;

    /**
     * Construct the Vector object.
     *
     * @param x
     * @param y
     * @param z
     */
    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Construct the Vector object.
     *
     * @param x
     * @param y
     * @param z
     */
    public Vector(int x, int y, int z) {
        this.x = (double)x;
        this.y = (double)y;
        this.z = (double)z;
    }

    /**
     * Construct the Vector object.
     *
     * @param x
     * @param y
     * @param z
     */
    public Vector(float x, float y, float z) {
        this.x = (double)x;
        this.y = (double)y;
        this.z = (double)z;
    }

    /**
     * Construct the Vector object.
     *
     * @param pt
     */
    public Vector(Vector pt) {
        this.x = pt.x;
        this.y = pt.y;
        this.z = pt.z;
    }

    /**
     * Construct the Vector object.
     */
    public Vector() {
        this.x = 0;
        this.y = 0;
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
        return (int)Math.round(x);
    }

    /**
     * Set X.
     *
     * @param x
     * @return new vector
     */
    public Vector setX(double x) {
        return new Vector(x, y, z);
    }

    /**
     * Set X.
     *
     * @param x
     * @return new vector
     */
    public Vector setX(int x) {
        return new Vector(x, y, z);
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @return the y
     */
    public int getBlockY() {
        return (int)Math.round(y);
    }

    /**
     * Set Y.
     *
     * @param y
     * @return new vector
     */
    public Vector setY(double y) {
        return new Vector(x, y, z);
    }

    /**
     * Set Y.
     *
     * @param y
     * @return new vector
     */
    public Vector setY(int y) {
        return new Vector(x, y, z);
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
        return (int)Math.round(z);
    }

    /**
     * Set Z.
     *
     * @param z
     * @return new vector
     */
    public Vector setZ(double z) {
        return new Vector(x, y, z);
    }

    /**
     * Set Z.
     *
     * @param z
     * @return new vector
     */
    public Vector setZ(int z) {
        return new Vector(x, y, z);
    }

    /**
     * Adds two points.
     *
     * @param other
     * @return New point
     */
    public Vector add(Vector other) {
        return new Vector(x + other.x, y + other.y, z + other.z);
    }

    /**
     * Adds two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector add(double x, double y, double z) {
        return new Vector(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Adds two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector add(int x, int y, int z) {
        return new Vector(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Adds points.
     *
     * @param others
     * @return New point
     */
    public Vector add(Vector ... others) {
        double newX = x, newY = y, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX += others[i].x;
            newY += others[i].y;
            newZ += others[i].z;
        }
        return new Vector(newX, newY, newZ);
    }

    /**
     * Subtracts two points.
     *
     * @param other
     * @return New point
     */
    public Vector subtract(Vector other) {
        return new Vector(x - other.x, y - other.y, z - other.z);
    }

    /**
     * Subtract two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector subtract(double x, double y, double z) {
        return new Vector(this.x - x, this.y - y, this.z - z);
    }

    /**
     * Subtract two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector subtract(int x, int y, int z) {
        return new Vector(this.x - x, this.y - y, this.z - z);
    }

    /**
     * Subtract points.
     *
     * @param others
     * @return New point
     */
    public Vector subtract(Vector ... others) {
        double newX = x, newY = y, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX -= others[i].x;
            newY -= others[i].y;
            newZ -= others[i].z;
        }
        return new Vector(newX, newY, newZ);
    }

    /**
     * Multiplies two points.
     *
     * @param other
     * @return New point
     */
    public Vector multiply(Vector other) {
        return new Vector(x * other.x, y * other.y, z * other.z);
    }

    /**
     * Multiply two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector multiply(double x, double y, double z) {
        return new Vector(this.x * x, this.y * y, this.z * z);
    }

    /**
     * Multiply two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector multiply(int x, int y, int z) {
        return new Vector(this.x * x, this.y * y, this.z * z);
    }

    /**
     * Multiply points.
     *
     * @param others
     * @return New point
     */
    public Vector multiply(Vector ... others) {
        double newX = x, newY = y, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX *= others[i].x;
            newY *= others[i].y;
            newZ *= others[i].z;
        }
        return new Vector(newX, newY, newZ);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector multiply(double n) {
        return new Vector(this.x * n, this.y * n, this.z * n);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector multiply(float n) {
        return new Vector(this.x * n, this.y * n, this.z * n);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector multiply(int n) {
        return new Vector(this.x * n, this.y * n, this.z * n);
    }

    /**
     * Divide two points.
     *
     * @param other
     * @return New point
     */
    public Vector divide(Vector other) {
        return new Vector(x / other.x, y / other.y, z / other.z);
    }

    /**
     * Divide two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector divide(double x, double y, double z) {
        return new Vector(this.x / x, this.y / y, this.z / z);
    }

    /**
     * Divide two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector divide(int x, int y, int z) {
        return new Vector(this.x / x, this.y / y, this.z / z);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector divide(int n) {
        return new Vector(x / n, y / n, z / n);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector divide(double n) {
        return new Vector(x / n, y / n, z / n);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector divide(float n) {
        return new Vector(x / n, y / n, z / n);
    }

    /**
     * Get the length of the vector.
     *
     * @return distance
     */
    public double length() {
        return Math.sqrt(Math.pow(x, 2) +
                Math.pow(y, 2) +
                Math.pow(z, 2));
    }

    /**
     * Get the distance away from a point.
     *
     * @param pt
     * @return distance
     */
    public double distance(Vector pt) {
        return Math.sqrt(Math.pow(pt.x - x, 2) +
                Math.pow(pt.y - y, 2) +
                Math.pow(pt.z - z, 2));
    }

    /**
     * Get the distance away from a point, squared.
     *
     * @param pt
     * @return distance
     */
    public double distanceSq(Vector pt) {
        return Math.pow(pt.x - x, 2) +
                Math.pow(pt.y - y, 2) +
                Math.pow(pt.z - z, 2);
    }

    /**
     * Get the normalized vector.
     *
     * @return vector
     */
    public Vector normalize() {
        return divide(length());
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min
     * @param max
     * @return
     */
    public boolean containedWithin(Vector min, Vector max) {
        return x >= min.getX() && x <= max.getX()
                && y >= min.getY() && y <= max.getY()
                && z >= min.getZ() && z <= max.getZ();
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min
     * @param max
     * @return
     */
    public boolean containedWithinBlock(Vector min, Vector max) {
        return getBlockX() >= min.getBlockX() && getBlockX() <= max.getBlockX()
                && getBlockY() >= min.getBlockY() && getBlockY() <= max.getBlockY()
                && getBlockZ() >= min.getBlockZ() && getBlockZ() <= max.getBlockY();
    }

    /**
     * Clamp the Y component.
     * 
     * @param min
     * @param max
     * @return
     */
    public Vector clampY(int min, int max) {
        return new Vector(x, Math.max(min, Math.min(max, y)), z);
    }

    /**
     * 2D transformation.
     * 
     * @param angle in degrees
     * @param aboutX
     * @param aboutZ
     * @param translateX
     * @param translateZ
     * @return
     */
    public Vector transform2D(double angle,
            double aboutX, double aboutZ, double translateX, double translateZ) {
        angle = Math.toRadians(angle);
        double x = this.x;
        double z = this.z;
        double x2 = x * Math.cos(angle) - z * Math.sin(angle);
        double z2 = x * Math.sin(angle) + z * Math.cos(angle);
        return new Vector(x2 + aboutX + translateX,
                          y,
                          z2 + aboutZ + translateZ);
    }

    /**
     * Get a block point from a point.
     * 
     * @param x
     * @param y
     * @param z
     * @return point
     */
    public static Vector toBlockPoint(double x, double y, double z) {
        return new Vector((int)Math.floor(x),
                         (int)Math.floor(y),
                         (int)Math.floor(z));
    }

    /**
     * Get a block point from a point.
     * 
     * @return point
     */
    public BlockVector toBlockPoint() {
        return new BlockVector((int)Math.floor(x),
                 (int)Math.floor(y),
                 (int)Math.floor(z));
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
        Vector other = (Vector)obj;
        return other.getX() == this.x && other.getY() == this.y && other.getZ() == this.z;

    }

    /**
     * Gets the hash code.
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return ((new Double(x)).hashCode() >> 13) ^
                ((new Double(y)).hashCode() >> 7) ^
                (new Double(z)).hashCode();
    }

    /**
     * Returns string representation "(x, y, z)".
     *
     * @return string
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    /**
     * Gets a BlockVector version.
     * 
     * @return BlockVector
     */
    public BlockVector toBlockVector() {
        return new BlockVector(this);
    }
    
    /**
     * Gets the minimum components of two vectors.
     * 
     * @param v1
     * @param v2
     * @return minimum
     */
    public static Vector getMinimum(Vector v1, Vector v2) {
        return new Vector(
                Math.min(v1.getX(), v2.getX()),
                Math.min(v1.getY(), v2.getY()),
                Math.min(v1.getZ(), v2.getZ()));
    }
    
    /**
     * Gets the maximum components of two vectors.
     * 
     * @param v1
     * @param v2
     * @return maximum
     */
    public static Vector getMaximum(Vector v1, Vector v2) {
        return new Vector(
                Math.max(v1.getX(), v2.getX()),
                Math.max(v1.getY(), v2.getY()),
                Math.max(v1.getZ(), v2.getZ()));
    }
}
