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

package com.sk89q.worldedit.math;

import com.sk89q.worldedit.math.transform.AffineTransform;

import java.util.Comparator;

/**
 * An immutable 2-dimensional vector.
 */
public final class BlockVector2 {

    public static final BlockVector2 ZERO = new BlockVector2(0, 0);
    public static final BlockVector2 UNIT_X = new BlockVector2(1, 0);
    public static final BlockVector2 UNIT_Z = new BlockVector2(0, 1);
    public static final BlockVector2 ONE = new BlockVector2(1, 1);

    /**
     * A comparator for BlockVector2ds that orders the vectors by rows, with x as the
     * column and z as the row.
     *
     * <p>
     * For example, if x is the horizontal axis and z is the vertical axis, it
     * sorts like so:
     * </p>
     *
     * <pre>
     * 0123
     * 4567
     * 90ab
     * cdef
     * </pre>
     */
    public static final Comparator<BlockVector2> COMPARING_GRID_ARRANGEMENT =
        Comparator.comparingInt(BlockVector2::getZ).thenComparingInt(BlockVector2::getX);

    public static BlockVector2 at(double x, double z) {
        return at((int) Math.floor(x), (int) Math.floor(z));
    }

    public static BlockVector2 at(int x, int z) {
        switch (x) {
            case 0:
                if (z == 0) {
                    return ZERO;
                }
                break;
            case 1:
                if (z == 1) {
                    return ONE;
                }
                break;
            default:
                break;
        }
        return new BlockVector2(x, z);
    }

    private final int x;
    private final int z;

    /**
     * Construct an instance.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    private BlockVector2(int x, int z) {
        this.x = x;
        this.z = z;
    }

    /**
     * Get the X coordinate.
     *
     * @return the x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Get the X coordinate.
     *
     * @return the x coordinate
     */
    public int getBlockX() {
        return x;
    }

    /**
     * Set the X coordinate.
     *
     * @param x the new X
     * @return a new vector
     */
    public BlockVector2 withX(int x) {
        return BlockVector2.at(x, z);
    }

    /**
     * Get the Z coordinate.
     *
     * @return the z coordinate
     */
    public int getZ() {
        return z;
    }

    /**
     * Get the Z coordinate.
     *
     * @return the z coordinate
     */
    public int getBlockZ() {
        return z;
    }

    /**
     * Set the Z coordinate.
     *
     * @param z the new Z
     * @return a new vector
     */
    public BlockVector2 withZ(int z) {
        return BlockVector2.at(x, z);
    }

    /**
     * Add another vector to this vector and return the result as a new vector.
     *
     * @param other the other vector
     * @return a new vector
     */
    public BlockVector2 add(BlockVector2 other) {
        return add(other.x, other.z);
    }

    /**
     * Add another vector to this vector and return the result as a new vector.
     *
     * @param x the value to add
     * @param z the value to add
     * @return a new vector
     */
    public BlockVector2 add(int x, int z) {
        return BlockVector2.at(this.x + x, this.z + z);
    }

    /**
     * Add a list of vectors to this vector and return the
     * result as a new vector.
     *
     * @param others an array of vectors
     * @return a new vector
     */
    public BlockVector2 add(BlockVector2... others) {
        int newX = x;
        int newZ = z;

        for (BlockVector2 other : others) {
            newX += other.x;
            newZ += other.z;
        }

        return BlockVector2.at(newX, newZ);
    }

    /**
     * Subtract another vector from this vector and return the result
     * as a new vector.
     *
     * @param other the other vector
     * @return a new vector
     */
    public BlockVector2 subtract(BlockVector2 other) {
        return subtract(other.x, other.z);
    }

    /**
     * Subtract another vector from this vector and return the result
     * as a new vector.
     *
     * @param x the value to subtract
     * @param z the value to subtract
     * @return a new vector
     */
    public BlockVector2 subtract(int x, int z) {
        return BlockVector2.at(this.x - x, this.z - z);
    }

    /**
     * Subtract a list of vectors from this vector and return the result
     * as a new vector.
     *
     * @param others an array of vectors
     * @return a new vector
     */
    public BlockVector2 subtract(BlockVector2... others) {
        int newX = x;
        int newZ = z;

        for (BlockVector2 other : others) {
            newX -= other.x;
            newZ -= other.z;
        }

        return BlockVector2.at(newX, newZ);
    }

    /**
     * Multiply this vector by another vector on each component.
     *
     * @param other the other vector
     * @return a new vector
     */
    public BlockVector2 multiply(BlockVector2 other) {
        return multiply(other.x, other.z);
    }

    /**
     * Multiply this vector by another vector on each component.
     *
     * @param x the value to multiply
     * @param z the value to multiply
     * @return a new vector
     */
    public BlockVector2 multiply(int x, int z) {
        return BlockVector2.at(this.x * x, this.z * z);
    }

    /**
     * Multiply this vector by zero or more vectors on each component.
     *
     * @param others an array of vectors
     * @return a new vector
     */
    public BlockVector2 multiply(BlockVector2... others) {
        int newX = x;
        int newZ = z;

        for (BlockVector2 other : others) {
            newX *= other.x;
            newZ *= other.z;
        }

        return BlockVector2.at(newX, newZ);
    }

    /**
     * Perform scalar multiplication and return a new vector.
     *
     * @param n the value to multiply
     * @return a new vector
     */
    public BlockVector2 multiply(int n) {
        return multiply(n, n);
    }

    /**
     * Divide this vector by another vector on each component.
     *
     * @param other the other vector
     * @return a new vector
     */
    public BlockVector2 divide(BlockVector2 other) {
        return divide(other.x, other.z);
    }

    /**
     * Divide this vector by another vector on each component.
     *
     * @param x the value to divide by
     * @param z the value to divide by
     * @return a new vector
     */
    public BlockVector2 divide(int x, int z) {
        return BlockVector2.at(this.x / x, this.z / z);
    }

    /**
     * Perform scalar division and return a new vector.
     *
     * @param n the value to divide by
     * @return a new vector
     */
    public BlockVector2 divide(int n) {
        return divide(n, n);
    }

    /**
     * Shift all components right.
     *
     * @param x the value to shift x by
     * @param z the value to shift z by
     * @return a new vector
     */
    public BlockVector2 shr(int x, int z) {
        return at(this.x >> x, this.z >> z);
    }

    /**
     * Shift all components right by {@code n}.
     *
     * @param n the value to shift by
     * @return a new vector
     */
    public BlockVector2 shr(int n) {
        return shr(n, n);
    }

    /**
     * Get the length of the vector.
     *
     * @return length
     */
    public double length() {
        return Math.sqrt(lengthSq());
    }

    /**
     * Get the length, squared, of the vector.
     *
     * @return length, squared
     */
    public int lengthSq() {
        return x * x + z * z;
    }

    /**
     * Get the distance between this vector and another vector.
     *
     * @param other the other vector
     * @return distance
     */
    public double distance(BlockVector2 other) {
        return Math.sqrt(distanceSq(other));
    }

    /**
     * Get the distance between this vector and another vector, squared.
     *
     * @param other the other vector
     * @return distance
     */
    public int distanceSq(BlockVector2 other) {
        int dx = other.x - x;
        int dz = other.z - z;
        return dx * dx + dz * dz;
    }

    /**
     * Get the normalized vector, which is the vector divided by its
     * length, as a new vector.
     *
     * @return a new vector
     */
    public BlockVector2 normalize() {
        double len = length();
        double x = this.x / len;
        double z = this.z / len;
        return BlockVector2.at(x, z);
    }

    /**
     * Gets the dot product of this and another vector.
     *
     * @param other the other vector
     * @return the dot product of this and the other vector
     */
    public int dot(BlockVector2 other) {
        return x * other.x + z * other.z;
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min the minimum point (X, Y, and Z are the lowest)
     * @param max the maximum point (X, Y, and Z are the lowest)
     * @return true if the vector is contained
     */
    public boolean containedWithin(BlockVector2 min, BlockVector2 max) {
        return x >= min.x && x <= max.x
                && z >= min.z && z <= max.z;
    }

    /**
     * Floors the values of all components.
     *
     * @return a new vector
     */
    public BlockVector2 floor() {
        // already floored, kept for feature parity with Vector2
        return this;
    }

    /**
     * Rounds all components up.
     *
     * @return a new vector
     */
    public BlockVector2 ceil() {
        // already raised, kept for feature parity with Vector2
        return this;
    }

    /**
     * Rounds all components to the closest integer.
     *
     * <p>Components &lt; 0.5 are rounded down, otherwise up.</p>
     *
     * @return a new vector
     */
    public BlockVector2 round() {
        // already rounded, kept for feature parity with Vector2
        return this;
    }

    /**
     * Returns a vector with the absolute values of the components of
     * this vector.
     *
     * @return a new vector
     */
    public BlockVector2 abs() {
        return BlockVector2.at(Math.abs(x), Math.abs(z));
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
    public BlockVector2 transform2D(double angle, double aboutX, double aboutZ, double translateX, double translateZ) {
        angle = Math.toRadians(angle);
        double x = this.x - aboutX;
        double z = this.z - aboutZ;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x2 = x * cos - z * sin;
        double z2 = x * sin + z * cos;
        return BlockVector2.at(
                x2 + aboutX + translateX,
                z2 + aboutZ + translateZ);
    }

    /**
     * Gets the minimum components of two vectors.
     *
     * @param v2 the second vector
     * @return minimum
     */
    public BlockVector2 getMinimum(BlockVector2 v2) {
        return new BlockVector2(
            Math.min(x, v2.x),
            Math.min(z, v2.z)
        );
    }

    /**
     * Gets the maximum components of two vectors.
     *
     * @param v2 the second vector
     * @return maximum
     */
    public BlockVector2 getMaximum(BlockVector2 v2) {
        return new BlockVector2(
            Math.max(x, v2.x),
            Math.max(z, v2.z)
        );
    }

    public Vector2 toVector2() {
        return Vector2.at(x, z);
    }

    /**
     * Creates a 3D vector by adding a zero Y component to this vector.
     *
     * @return a new vector
     */
    public Vector3 toVector3() {
        return toVector3(0);
    }

    /**
     * Creates a 3D vector by adding the specified Y component to this vector.
     *
     * @param y the Y component
     * @return a new vector
     */
    public Vector3 toVector3(double y) {
        return Vector3.at(x, y, z);
    }

    /**
     * Creates a 3D vector by adding a zero Y component to this vector.
     *
     * @return a new vector
     */
    public BlockVector3 toBlockVector3() {
        return toBlockVector3(0);
    }

    /**
     * Creates a 3D vector by adding the specified Y component to this vector.
     *
     * @param y the Y component
     * @return a new vector
     */
    public BlockVector3 toBlockVector3(int y) {
        return BlockVector3.at(x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlockVector2)) {
            return false;
        }

        BlockVector2 other = (BlockVector2) obj;
        return other.x == this.x && other.z == this.z;

    }

    @Override
    public int hashCode() {
        return (x << 16) ^ z;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + z + ")";
    }

    /**
     * Returns a string representation that is supported by the parser.
     * @return string
     */
    public String toParserString() {
        return x + "," + z;
    }
}
