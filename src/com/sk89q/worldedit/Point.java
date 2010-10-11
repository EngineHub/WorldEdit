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
 * @author Albert
 */
public final class Point {
    private final double x, y, z;

    /**
     * Construct the Point object.
     *
     * @param x
     * @param y
     * @param z
     */
    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Construct the Point object.
     *
     * @param x
     * @param y
     * @param z
     */
    public Point(int x, int y, int z) {
        this.x = (double)x;
        this.y = (double)y;
        this.z = (double)z;
    }

    /**
     * Construct the Point object.
     *
     * @param x
     * @param y
     * @param z
     */
    public Point(float x, float y, float z) {
        this.x = (double)x;
        this.y = (double)y;
        this.z = (double)z;
    }

    /**
     * Construct the Point object.
     */
    public Point() {
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
        return (int)x;
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
        return (int)y;
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
        return (int)z;
    }

    /**
     * Adds two points.
     *
     * @param other
     * @return New point
     */
    public Point add(Point other) {
        return new Point(x + other.x, y + other.y, z + other.z);
    }

    /**
     * Adds two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Point add(double x, double y, double z) {
        return new Point(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Adds two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Point add(int x, int y, int z) {
        return new Point(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Adds points.
     *
     * @param others
     * @return New point
     */
    public Point add(Point ... others) {
        double newX = x, newY = y, newZ = z;

        for (int i = 0; i < others.length; i++) {
            newX += others[i].x;
            newY += others[i].y;
            newZ += others[i].z;
        }
        return new Point(newX, newY, newZ);
    }

    /**
     * Subtracts two points.
     *
     * @param other
     * @return New point
     */
    public Point subtract(Point other) {
        return new Point(x - other.x, y - other.y, z - other.z);
    }

    /**
     * Subtract two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Point subtract(double x, double y, double z) {
        return new Point(this.x - x, this.y - y, this.z - z);
    }

    /**
     * Subtract two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Point subtract(int x, int y, int z) {
        return new Point(this.x - x, this.y - y, this.z - z);
    }

    /**
     * Subtract points.
     *
     * @param others
     * @return New point
     */
    public Point subtract(Point ... others) {
        double newX = x, newY = y, newZ = z;

        for (int i = 0; i < others.length; i++) {
            newX -= others[i].x;
            newY -= others[i].y;
            newZ -= others[i].z;
        }
        return new Point(newX, newY, newZ);
    }

    /**
     * Multiplies two points.
     *
     * @param other
     * @return New point
     */
    public Point multiply(Point other) {
        return new Point(x * other.x, y * other.y, z * other.z);
    }

    /**
     * Multiply two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Point multiply(double x, double y, double z) {
        return new Point(this.x * x, this.y * y, this.z * z);
    }

    /**
     * Multiply two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Point multiply(int x, int y, int z) {
        return new Point(this.x * x, this.y * y, this.z * z);
    }

    /**
     * Multiply points.
     *
     * @param others
     * @return New point
     */
    public Point multiply(Point ... others) {
        double newX = x, newY = y, newZ = z;

        for (int i = 0; i < others.length; i++) {
            newX *= others[i].x;
            newY *= others[i].y;
            newZ *= others[i].z;
        }
        return new Point(newX, newY, newZ);
    }

    /**
     * Divide two points.
     *
     * @param other
     * @return New point
     */
    public Point divide(Point other) {
        return new Point(x / other.x, y / other.y, z / other.z);
    }

    /**
     * Divide two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Point divide(double x, double y, double z) {
        return new Point(this.x / x, this.y / y, this.z / z);
    }

    /**
     * Divide two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Point divide(int x, int y, int z) {
        return new Point(this.x / x, this.y / y, this.z / z);
    }

    /**
     * Get a block point from a point.
     * 
     * @param x
     * @param y
     * @param z
     * @return point
     */
    public static Point toBlockPoint(double x, double y, double z) {
        return new Point((int)Math.floor(x),
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
        if (!(obj instanceof Point)) {
            return false;
        }
        Point other = (Point)obj;
        return other.x == this.x && other.y == this.y && other.z == this.z;

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
}
