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

import com.google.common.collect.ComparisonChain;
import com.sk89q.worldedit.math.transform.AffineTransform;

import java.util.Comparator;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An immutable 3-dimensional vector.
 */
public final class Vector3 {

    public static final Vector3 ZERO = new Vector3(0, 0, 0);
    public static final Vector3 UNIT_X = new Vector3(1, 0, 0);
    public static final Vector3 UNIT_Y = new Vector3(0, 1, 0);
    public static final Vector3 UNIT_Z = new Vector3(0, 0, 1);
    public static final Vector3 ONE = new Vector3(1, 1, 1);

    public static Vector3 at(double x, double y, double z) {
        // switch for efficiency on typical cases
        // in MC y is rarely 0/1 on selections
        int yTrunc = (int) y;
        switch (yTrunc) {
            case 0:
                if (x == 0 && y == 0 && z == 0) {
                    return ZERO;
                }
                break;
            case 1:
                if (x == 1 && y == 1 && z == 1) {
                    return ONE;
                }
                break;
            default:
                break;
        }
        return new Vector3(x, y, z);
    }

    // thread-safe initialization idiom
    private static final class YzxOrderComparator {
        private static final Comparator<Vector3> YZX_ORDER = (a, b) -> {
            return ComparisonChain.start()
                    .compare(a.y, b.y)
                    .compare(a.z, b.z)
                    .compare(a.x, b.x)
                    .result();
        };
    }

    /**
     * Returns a comparator that sorts vectors first by Y, then Z, then X.
     *
     * <p>
     * Useful for sorting by chunk block storage order.
     * </p>
     */
    public static Comparator<Vector3> sortByCoordsYzx() {
        return YzxOrderComparator.YZX_ORDER;
    }

    private final double x;
    private final double y;
    private final double z;

    /**
     * Construct an instance.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    private Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
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
     * Set the X coordinate.
     *
     * @param x the new X
     * @return a new vector
     */
    public Vector3 withX(double x) {
        return Vector3.at(x, y, z);
    }

    /**
     * Get the Y coordinate.
     *
     * @return the y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Set the Y coordinate.
     *
     * @param y the new Y
     * @return a new vector
     */
    public Vector3 withY(double y) {
        return Vector3.at(x, y, z);
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
     * Set the Z coordinate.
     *
     * @param z the new Z
     * @return a new vector
     */
    public Vector3 withZ(double z) {
        return Vector3.at(x, y, z);
    }

    /**
     * Add another vector to this vector and return the result as a new vector.
     *
     * @param other the other vector
     * @return a new vector
     */
    public Vector3 add(Vector3 other) {
        return add(other.x, other.y, other.z);
    }

    /**
     * Add another vector to this vector and return the result as a new vector.
     *
     * @param x the value to add
     * @param y the value to add
     * @param z the value to add
     * @return a new vector
     */
    public Vector3 add(double x, double y, double z) {
        return Vector3.at(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Add a list of vectors to this vector and return the
     * result as a new vector.
     *
     * @param others an array of vectors
     * @return a new vector
     */
    public Vector3 add(Vector3... others) {
        double newX = x;
        double newY = y;
        double newZ = z;

        for (Vector3 other : others) {
            newX += other.x;
            newY += other.y;
            newZ += other.z;
        }

        return Vector3.at(newX, newY, newZ);
    }

    /**
     * Subtract another vector from this vector and return the result
     * as a new vector.
     *
     * @param other the other vector
     * @return a new vector
     */
    public Vector3 subtract(Vector3 other) {
        return subtract(other.x, other.y, other.z);
    }

    /**
     * Subtract another vector from this vector and return the result
     * as a new vector.
     *
     * @param x the value to subtract
     * @param y the value to subtract
     * @param z the value to subtract
     * @return a new vector
     */
    public Vector3 subtract(double x, double y, double z) {
        return Vector3.at(this.x - x, this.y - y, this.z - z);
    }

    /**
     * Subtract a list of vectors from this vector and return the result
     * as a new vector.
     *
     * @param others an array of vectors
     * @return a new vector
     */
    public Vector3 subtract(Vector3... others) {
        double newX = x;
        double newY = y;
        double newZ = z;

        for (Vector3 other : others) {
            newX -= other.x;
            newY -= other.y;
            newZ -= other.z;
        }

        return Vector3.at(newX, newY, newZ);
    }

    /**
     * Multiply this vector by another vector on each component.
     *
     * @param other the other vector
     * @return a new vector
     */
    public Vector3 multiply(Vector3 other) {
        return multiply(other.x, other.y, other.z);
    }

    /**
     * Multiply this vector by another vector on each component.
     *
     * @param x the value to multiply
     * @param y the value to multiply
     * @param z the value to multiply
     * @return a new vector
     */
    public Vector3 multiply(double x, double y, double z) {
        return Vector3.at(this.x * x, this.y * y, this.z * z);
    }

    /**
     * Multiply this vector by zero or more vectors on each component.
     *
     * @param others an array of vectors
     * @return a new vector
     */
    public Vector3 multiply(Vector3... others) {
        double newX = x;
        double newY = y;
        double newZ = z;

        for (Vector3 other : others) {
            newX *= other.x;
            newY *= other.y;
            newZ *= other.z;
        }

        return Vector3.at(newX, newY, newZ);
    }

    /**
     * Perform scalar multiplication and return a new vector.
     *
     * @param n the value to multiply
     * @return a new vector
     */
    public Vector3 multiply(double n) {
        return multiply(n, n, n);
    }

    /**
     * Divide this vector by another vector on each component.
     *
     * @param other the other vector
     * @return a new vector
     */
    public Vector3 divide(Vector3 other) {
        return divide(other.x, other.y, other.z);
    }

    /**
     * Divide this vector by another vector on each component.
     *
     * @param x the value to divide by
     * @param y the value to divide by
     * @param z the value to divide by
     * @return a new vector
     */
    public Vector3 divide(double x, double y, double z) {
        return Vector3.at(this.x / x, this.y / y, this.z / z);
    }

    /**
     * Perform scalar division and return a new vector.
     *
     * @param n the value to divide by
     * @return a new vector
     */
    public Vector3 divide(double n) {
        return divide(n, n, n);
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
    public double lengthSq() {
        return x * x + y * y + z * z;
    }

    /**
     * Get the distance between this vector and another vector.
     *
     * @param other the other vector
     * @return distance
     */
    public double distance(Vector3 other) {
        return Math.sqrt(distanceSq(other));
    }

    /**
     * Get the distance between this vector and another vector, squared.
     *
     * @param other the other vector
     * @return distance
     */
    public double distanceSq(Vector3 other) {
        double dx = other.x - x;
        double dy = other.y - y;
        double dz = other.z - z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Get the normalized vector, which is the vector divided by its
     * length, as a new vector.
     *
     * @return a new vector
     */
    public Vector3 normalize() {
        return divide(length());
    }

    /**
     * Gets the dot product of this and another vector.
     *
     * @param other the other vector
     * @return the dot product of this and the other vector
     */
    public double dot(Vector3 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    /**
     * Gets the cross product of this and another vector.
     *
     * @param other the other vector
     * @return the cross product of this and the other vector
     */
    public Vector3 cross(Vector3 other) {
        return new Vector3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min the minimum point (X, Y, and Z are the lowest)
     * @param max the maximum point (X, Y, and Z are the lowest)
     * @return true if the vector is contained
     */
    public boolean containedWithin(Vector3 min, Vector3 max) {
        return x >= min.x && x <= max.x && y >= min.y && y <= max.y && z >= min.z && z <= max.z;
    }

    /**
     * Clamp the Y component.
     *
     * @param min the minimum value
     * @param max the maximum value
     * @return a new vector
     */
    public Vector3 clampY(int min, int max) {
        checkArgument(min <= max, "minimum cannot be greater than maximum");
        if (y < min) {
            return Vector3.at(x, min, z);
        }
        if (y > max) {
            return Vector3.at(x, max, z);
        }
        return this;
    }

    /**
     * Floors the values of all components.
     *
     * @return a new vector
     */
    public Vector3 floor() {
        return Vector3.at(Math.floor(x), Math.floor(y), Math.floor(z));
    }

    /**
     * Rounds all components up.
     *
     * @return a new vector
     */
    public Vector3 ceil() {
        return Vector3.at(Math.ceil(x), Math.ceil(y), Math.ceil(z));
    }

    /**
     * Rounds all components to the closest integer.
     *
     * <p>Components &lt; 0.5 are rounded down, otherwise up.</p>
     *
     * @return a new vector
     */
    public Vector3 round() {
        return Vector3.at(Math.floor(x + 0.5), Math.floor(y + 0.5), Math.floor(z + 0.5));
    }

    /**
     * Returns a vector with the absolute values of the components of
     * this vector.
     *
     * @return a new vector
     */
    public Vector3 abs() {
        return Vector3.at(Math.abs(x), Math.abs(y), Math.abs(z));
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
    public Vector3 transform2D(double angle, double aboutX, double aboutZ, double translateX, double translateZ) {
        angle = Math.toRadians(angle);
        double x = this.x - aboutX;
        double z = this.z - aboutZ;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x2 = x * cos - z * sin;
        double z2 = x * sin + z * cos;

        return new Vector3(
            x2 + aboutX + translateX,
            y,
            z2 + aboutZ + translateZ
        );
    }

    /**
     * Get this vector's pitch as used within the game.
     *
     * @return pitch in radians
     */
    public double toPitch() {
        double x = getX();
        double z = getZ();

        if (x == 0 && z == 0) {
            return getY() > 0 ? -90 : 90;
        } else {
            double x2 = x * x;
            double z2 = z * z;
            double xz = Math.sqrt(x2 + z2);
            return Math.toDegrees(Math.atan(-getY() / xz));
        }
    }

    /**
     * Get this vector's yaw as used within the game.
     *
     * @return yaw in radians
     */
    public double toYaw() {
        double x = getX();
        double z = getZ();

        double t = Math.atan2(-x, z);
        double tau = 2 * Math.PI;

        return Math.toDegrees(((t + tau) % tau));
    }

    /**
     * Gets the minimum components of two vectors.
     *
     * @param v2 the second vector
     * @return minimum
     */
    public Vector3 getMinimum(Vector3 v2) {
        return new Vector3(
                Math.min(x, v2.x),
                Math.min(y, v2.y),
                Math.min(z, v2.z)
        );
    }

    /**
     * Gets the maximum components of two vectors.
     *
     * @param v2 the second vector
     * @return maximum
     */
    public Vector3 getMaximum(Vector3 v2) {
        return new Vector3(
                Math.max(x, v2.x),
                Math.max(y, v2.y),
                Math.max(z, v2.z)
        );
    }

    /**
     * Create a new {@code BlockVector} using the given components.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return a new {@code BlockVector}
     */
    public static BlockVector3 toBlockPoint(double x, double y, double z) {
        return BlockVector3.at(x, y, z);
    }

    /**
     * Create a new {@code BlockVector} from this vector.
     *
     * @return a new {@code BlockVector}
     */
    public BlockVector3 toBlockPoint() {
        return toBlockPoint(x, y, z);
    }

    /**
     * Creates a 2D vector by dropping the Y component from this vector.
     *
     * @return a new {@link Vector2}
     */
    public Vector2 toVector2() {
        return Vector2.at(x, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector3)) {
            return false;
        }

        Vector3 other = (Vector3) obj;
        return other.x == this.x && other.y == this.y && other.z == this.z;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + Double.hashCode(x);
        hash = 31 * hash + Double.hashCode(y);
        hash = 31 * hash + Double.hashCode(z);
        return hash;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    /**
     * Returns a string representation that is supported by the parser.
     * @return string
     */
    public String toParserString() {
        return x + "," + y + "," + z;
    }
}
