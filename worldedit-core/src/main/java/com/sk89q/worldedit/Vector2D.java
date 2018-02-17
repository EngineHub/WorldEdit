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

import com.sk89q.worldedit.math.transform.AffineTransform;

/**
 * An immutable 2-dimensional vector.
 */
public class Vector2D {
    public static final Vector2D ZERO = new Vector2D(0, 0);
    public static final Vector2D UNIT_X = new Vector2D(1, 0);
    public static final Vector2D UNIT_Z = new Vector2D(0, 1);
    public static final Vector2D ONE = new Vector2D(1, 1);

    protected final double x, z;

    /**
     * Construct an instance.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    public Vector2D(double x, double z) {
        this.x = x;
        this.z = z;
    }

    /**
     * Construct an instance.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    public Vector2D(int x, int z) {
        this.x = (double) x;
        this.z = (double) z;
    }

    /**
     * Construct an instance.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    public Vector2D(float x, float z) {
        this.x = (double) x;
        this.z = (double) z;
    }

    /**
     * Copy another vector.
     *
     * @param other the other vector
     */
    public Vector2D(Vector2D other) {
        this.x = other.x;
        this.z = other.z;
    }

    /**
     * Construct a new instance with X and Z coordinates set to 0.
     *
     * <p>One can also refer to a static {@link #ZERO}.</p>
     */
    public Vector2D() {
        this.x = 0;
        this.z = 0;
    }

    /**
     * Get the X coordinate.
     *
     * @return the x coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Get the X coordinate rounded.
     *
     * @return the x coordinate
     */
    public int getBlockX() {
        return (int) Math.round(x);
    }

    /**
     * Set the X coordinate.
     *
     * @param x the new X
     * @return a new vector
     */
    public Vector2D setX(double x) {
        return new Vector2D(x, z);
    }

    /**
     * Set the X coordinate.
     *
     * @param x the new X
     * @return a new vector
     */
    public Vector2D setX(int x) {
        return new Vector2D(x, z);
    }

    /**
     * Get the Z coordinate.
     *
     * @return the z coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Get the Z coordinate rounded.
     *
     * @return the z coordinate
     */
    public int getBlockZ() {
        return (int) Math.round(z);
    }

    /**
     * Set the Z coordinate.
     *
     * @param z the new Z
     * @return a new vector
     */
    public Vector2D setZ(double z) {
        return new Vector2D(x, z);
    }

    /**
     * Set the Z coordinate.
     *
     * @param z the new Z
     * @return a new vector
     */
    public Vector2D setZ(int z) {
        return new Vector2D(x, z);
    }

    /**
     * Add another vector to this vector and return the result as a new vector.
     *
     * @param other the other vector
     * @return a new vector
     */
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, z + other.z);
    }

    /**
     * Add another vector to this vector and return the result as a new vector.
     *
     * @param x the value to add
     * @param z the value to add
     * @return a new vector
     */
    public Vector2D add(double x, double z) {
        return new Vector2D(this.x + x, this.z + z);
    }

    /**
     * Add another vector to this vector and return the result as a new vector.
     *
     * @param x the value to add
     * @param z the value to add
     * @return a new vector
     */
    public Vector2D add(int x, int z) {
        return new Vector2D(this.x + x, this.z + z);
    }

    /**
     * Add a list of vectors to this vector and return the
     * result as a new vector.
     *
     * @param others an array of vectors
     * @return a new vector
     */
    public Vector2D add(Vector2D... others) {
        double newX = x, newZ = z;

        for (Vector2D other : others) {
            newX += other.x;
            newZ += other.z;
        }

        return new Vector2D(newX, newZ);
    }

    /**
     * Subtract another vector from this vector and return the result
     * as a new vector.
     *
     * @param other the other vector
     * @return a new vector
     */
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, z - other.z);
    }

    /**
     * Subtract another vector from this vector and return the result
     * as a new vector.
     *
     * @param x the value to subtract
     * @param z the value to subtract
     * @return a new vector
     */
    public Vector2D subtract(double x, double z) {
        return new Vector2D(this.x - x, this.z - z);
    }

    /**
     * Subtract another vector from this vector and return the result
     * as a new vector.
     *
     * @param x the value to subtract
     * @param z the value to subtract
     * @return a new vector
     */
    public Vector2D subtract(int x, int z) {
        return new Vector2D(this.x - x, this.z - z);
    }

    /**
     * Subtract a list of vectors from this vector and return the result
     * as a new vector.
     *
     * @param others an array of vectors
     * @return a new vector
     */
    public Vector2D subtract(Vector2D... others) {
        double newX = x, newZ = z;

        for (Vector2D other : others) {
            newX -= other.x;
            newZ -= other.z;
        }

        return new Vector2D(newX, newZ);
    }

    /**
     * Multiply this vector by another vector on each component.
     *
     * @param other the other vector
     * @return a new vector
     */
    public Vector2D multiply(Vector2D other) {
        return new Vector2D(x * other.x, z * other.z);
    }

    /**
     * Multiply this vector by another vector on each component.
     *
     * @param x the value to multiply
     * @param z the value to multiply
     * @return a new vector
     */
    public Vector2D multiply(double x, double z) {
        return new Vector2D(this.x * x, this.z * z);
    }

    /**
     * Multiply this vector by another vector on each component.
     *
     * @param x the value to multiply
     * @param z the value to multiply
     * @return a new vector
     */
    public Vector2D multiply(int x, int z) {
        return new Vector2D(this.x * x, this.z * z);
    }

    /**
     * Multiply this vector by zero or more vectors on each component.
     *
     * @param others an array of vectors
     * @return a new vector
     */
    public Vector2D multiply(Vector2D... others) {
        double newX = x, newZ = z;

        for (Vector2D other : others) {
            newX *= other.x;
            newZ *= other.z;
        }

        return new Vector2D(newX, newZ);
    }

    /**
     * Perform scalar multiplication and return a new vector.
     *
     * @param n the value to multiply
     * @return a new vector
     */
    public Vector2D multiply(double n) {
        return new Vector2D(this.x * n, this.z * n);
    }

    /**
     * Perform scalar multiplication and return a new vector.
     *
     * @param n the value to multiply
     * @return a new vector
     */
    public Vector2D multiply(float n) {
        return new Vector2D(this.x * n, this.z * n);
    }

    /**
     * Perform scalar multiplication and return a new vector.
     *
     * @param n the value to multiply
     * @return a new vector
     */
    public Vector2D multiply(int n) {
        return new Vector2D(this.x * n, this.z * n);
    }

    /**
     * Divide this vector by another vector on each component.
     *
     * @param other the other vector
     * @return a new vector
     */
    public Vector2D divide(Vector2D other) {
        return new Vector2D(x / other.x, z / other.z);
    }

    /**
     * Divide this vector by another vector on each component.
     *
     * @param x the value to divide by
     * @param z the value to divide by
     * @return a new vector
     */
    public Vector2D divide(double x, double z) {
        return new Vector2D(this.x / x, this.z / z);
    }

    /**
     * Divide this vector by another vector on each component.
     *
     * @param x the value to divide by
     * @param z the value to divide by
     * @return a new vector
     */
    public Vector2D divide(int x, int z) {
        return new Vector2D(this.x / x, this.z / z);
    }

    /**
     * Perform scalar division and return a new vector.
     *
     * @param n the value to divide by
     * @return a new vector
     */
    public Vector2D divide(int n) {
        return new Vector2D(x / n, z / n);
    }

    /**
     * Perform scalar division and return a new vector.
     *
     * @param n the value to divide by
     * @return a new vector
     */
    public Vector2D divide(double n) {
        return new Vector2D(x / n, z / n);
    }

    /**
     * Perform scalar division and return a new vector.
     *
     * @param n the value to divide by
     * @return a new vector
     */
    public Vector2D divide(float n) {
        return new Vector2D(x / n, z / n);
    }

    /**
     * Get the length of the vector.
     *
     * @return length
     */
    public double length() {
        return Math.sqrt(x * x + z * z);
    }

    /**
     * Get the length, squared, of the vector.
     *
     * @return length, squared
     */
    public double lengthSq() {
        return x * x + z * z;
    }

    /**
     * Get the distance between this vector and another vector.
     *
     * @param other the other vector
     * @return distance
     */
    public double distance(Vector2D other) {
        return Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.z - z, 2));
    }

    /**
     * Get the distance between this vector and another vector, squared.
     *
     * @param other the other vector
     * @return distance
     */
    public double distanceSq(Vector2D other) {
        return Math.pow(other.x - x, 2) +
                Math.pow(other.z - z, 2);
    }

    /**
     * Get the normalized vector, which is the vector divided by its
     * length, as a new vector.
     *
     * @return a new vector
     */
    public Vector2D normalize() {
        return divide(length());
    }

    /**
     * Gets the dot product of this and another vector.
     *
     * @param other the other vector
     * @return the dot product of this and the other vector
     */
    public double dot(Vector2D other) {
        return x * other.x + z * other.z;
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min the minimum point (X, Y, and Z are the lowest)
     * @param max the maximum point (X, Y, and Z are the lowest)
     * @return true if the vector is contained
     */
    public boolean containedWithin(Vector2D min, Vector2D max) {
        return x >= min.x && x <= max.x
                && z >= min.z && z <= max.z;
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min the minimum point (X, Y, and Z are the lowest)
     * @param max the maximum point (X, Y, and Z are the lowest)
     * @return true if the vector is contained
     */
    public boolean containedWithinBlock(Vector2D min, Vector2D max) {
        return getBlockX() >= min.getBlockX() && getBlockX() <= max.getBlockX()
                && getBlockZ() >= min.getBlockZ() && getBlockZ() <= max.getBlockZ();
    }

    /**
     * Floors the values of all components.
     *
     * @return a new vector
     */
    public Vector2D floor() {
        return new Vector2D(Math.floor(x), Math.floor(z));
    }

    /**
     * Rounds all components up.
     *
     * @return a new vector
     */
    public Vector2D ceil() {
        return new Vector2D(Math.ceil(x), Math.ceil(z));
    }

    /**
     * Rounds all components to the closest integer.
     *
     * <p>Components &lt; 0.5 are rounded down, otherwise up.</p>
     *
     * @return a new vector
     */
    public Vector2D round() {
        return new Vector2D(Math.floor(x + 0.5), Math.floor(z + 0.5));
    }

    /**
     * Returns a vector with the absolute values of the components of
     * this vector.
     *
     * @return a new vector
     */
    public Vector2D positive() {
        return new Vector2D(Math.abs(x), Math.abs(z));
    }

    /**
     * Perform a 2D transformation on this vector and return a new one.
     *
     * @param angle in degrees
     * @param aboutX about which x coordinate to rotate
     * @param aboutZ about which z coordinate to rotate
     * @param translateX what to add after rotation
     * @param translateZ what to add after rotation
     * @return a new vector
     * @see AffineTransform another method to transform vectors
     */
    public Vector2D transform2D(double angle, double aboutX, double aboutZ, double translateX, double translateZ) {
        angle = Math.toRadians(angle);
        double x = this.x - aboutX;
        double z = this.z - aboutZ;
        double x2 = x * Math.cos(angle) - z * Math.sin(angle);
        double z2 = x * Math.sin(angle) + z * Math.cos(angle);
        return new Vector2D(
            x2 + aboutX + translateX,
            z2 + aboutZ + translateZ
        );
    }

    /**
     * Returns whether this vector is collinear with another vector.
     *
     * @param other the other vector
     * @return true if collinear
     */
    public boolean isCollinearWith(Vector2D other) {
        if (x == 0 && z == 0) {
            // this is a zero vector
            return true;
        }

        final double otherX = other.x;
        final double otherZ = other.z;

        if (otherX == 0 && otherZ == 0) {
            // other is a zero vector
            return true;
        }

        if ((x == 0) != (otherX == 0)) return false;
        if ((z == 0) != (otherZ == 0)) return false;

        final double quotientX = otherX / x;
        if (!Double.isNaN(quotientX)) {
            return other.equals(multiply(quotientX));
        }

        final double quotientZ = otherZ / z;
        if (!Double.isNaN(quotientZ)) {
            return other.equals(multiply(quotientZ));
        }

        throw new RuntimeException("This should not happen");
    }

    /**
     * Create a new {@code BlockVector2D} from this vector.
     *
     * @return a new {@code BlockVector2D}
     */
    public BlockVector2D toBlockVector2D() {
        return new BlockVector2D(this);
    }

    /**
     * Creates a 3D vector by adding a zero Y component to this vector.
     *
     * @return a new vector
     */
    public Vector toVector() {
        return new Vector(x, 0, z);
    }

    /**
     * Creates a 3D vector by adding the specified Y component to this vector.
     *
     * @param y the Y component
     * @return a new vector
     */
    public Vector toVector(double y) {
        return new Vector(x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector2D)) {
            return false;
        }

        Vector2D other = (Vector2D) obj;
        return other.x == this.x && other.z == this.z;

    }

    @Override
    public int hashCode() {
        return ((int) x << 16) ^ (int) z;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + z + ")";
    }

    /**
     * Gets the minimum components of two vectors.
     *
     * @param v1 the first vector
     * @param v2 the second vector
     * @return minimum
     */
    public static Vector2D getMinimum(Vector2D v1, Vector2D v2) {
        return new Vector2D(
            Math.min(v1.x, v2.x),
            Math.min(v1.z, v2.z)
        );
    }

    /**
     * Gets the maximum components of two vectors.
     *
     * @param v1 the first vector
     * @param v2 the second vector
     * @return maximum
     */
    public static Vector2D getMaximum(Vector2D v1, Vector2D v2) {
        return new Vector2D(
            Math.max(v1.x, v2.x),
            Math.max(v1.z, v2.z)
        );
    }

}
