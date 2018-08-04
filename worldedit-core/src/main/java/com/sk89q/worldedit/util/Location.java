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

package com.sk89q.worldedit.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.Extent;

/**
 * Represents a location in a world with has a direction.
 *
 * <p>Like {@code Vectors}, {@code Locations} are immutable and mutator methods
 * will create a new copy.</p>
 *
 * <p>At the moment, but this may change in the future, {@link #hashCode()} and
 * {@link #equals(Object)} are subject to minor differences caused by
 * floating point errors.</p>
 */
public class Location {

    private final Extent extent;
    private final Vector position;
    private final float pitch;
    private final float yaw;

    /**
     * Create a new instance in the given extent at 0, 0, 0 with a
     * direction vector of 0, 0, 0.
     *
     * @param extent the extent
     */
    public Location(Extent extent) {
        this(extent, new Vector(), new Vector());
    }

    /**
     * Create a new instance in the given extent with the given coordinates
     * with a direction vector of 0, 0, 0.
     *
     * @param extent the extent
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    public Location(Extent extent, double x, double y, double z) {
        this(extent, new Vector(x, y, z), new Vector());
    }

    /**
     * Create a new instance in the given extent with the given position
     * vector and a direction vector of 0, 0, 0.
     *
     * @param extent the extent
     * @param position the position vector
     */
    public Location(Extent extent, Vector position) {
        this(extent, position, new Vector());
    }

    /**
     * Create a new instance in the given extent with the given coordinates
     * and the given direction vector.
     *
     * @param extent the extent
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @param direction the direction vector
     */
    public Location(Extent extent, double x, double y, double z, Vector direction) {
        this(extent, new Vector(x, y, z), direction);
    }

    /**
     * Create a new instance in the given extent with the given coordinates
     * and the given direction vector.
     *
     * @param extent the extent
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @param yaw the yaw, in degrees
     * @param pitch the pitch, in degrees
     */
    public Location(Extent extent, double x, double y, double z, float yaw, float pitch) {
        this(extent, new Vector(x, y, z), yaw, pitch);
    }

    /**
     * Create a new instance in the given extent with the given position vector
     * and the given direction vector.
     *
     * @param extent the extent
     * @param position the position vector
     * @param direction the direction vector
     */
    public Location(Extent extent, Vector position, Vector direction) {
        this(extent, position, direction.toYaw(), direction.toPitch());
    }

    /**
     * Create a new instance in the given extent with the given position vector
     * and the given direction vector.
     *
     * @param extent the extent
     * @param position the position vector
     * @param yaw the yaw, in degrees
     * @param pitch the pitch, in degrees
     */
    public Location(Extent extent, Vector position, float yaw, float pitch) {
        checkNotNull(extent);
        checkNotNull(position);
        this.extent = extent;
        this.position = position;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    /**
     * Get the extent.
     *
     * @return the extent
     */
    public Extent getExtent() {
        return extent;
    }

    /**
     * Create a clone of this object with the given extent.
     *
     * @param extent the new extent
     * @return the new instance
     */
    public Location setExtent(Extent extent) {
        return new Location(extent, position, getDirection());
    }

    /**
     * Get the yaw in degrees.
     *
     * @return the yaw in degrees
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Create a clone of this object with the given yaw.
     *
     * @param yaw the new yaw
     * @return the new instance
     */
    public Location setYaw(float yaw) {
        return new Location(extent, position, yaw, pitch);
    }

    /**
     * Get the pitch in degrees.
     *
     * @return the pitch in degrees
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Create a clone of this object with the given pitch.
     *
     * @param pitch the new yaw
     * @return the new instance
     */
    public Location setPitch(float pitch) {
        return new Location(extent, position, yaw, pitch);
    }

    /**
     * Create a clone of this object with the given yaw and pitch.
     *
     * @param yaw the new yaw
     * @param pitch the new pitch
     * @return the new instance
     */
    public Location setDirection(float yaw, float pitch) {
        return new Location(extent, position, yaw, pitch);
    }

    /**
     * Get the direction vector.
     *
     * @return the direction vector
     */
    public Vector getDirection() {
        double yaw = Math.toRadians(this.getYaw());
        double pitch = Math.toRadians(this.getPitch());
        double xz = Math.cos(pitch);
        return new Vector(
                -xz * Math.sin(yaw),
                -Math.sin(pitch),
                xz * Math.cos(yaw));
    }

    /**
     * Get the direction as a {@link Direction}.
     *
     * @return The direction
     */
    public Direction getDirectionEnum() {
        return Direction.findClosest(getDirection(), Direction.Flag.ALL);
    }

    /**
     * Create a clone of this object with the given direction.
     *
     * @param direction the new direction
     * @return the new instance
     */
    public Location setDirection(Vector direction) {
        return new Location(extent, position, direction.toYaw(), direction.toPitch());
    }

    /**
     * Get a {@link Vector} form of this location's position.
     *
     * @return a vector
     */
    public Vector toVector() {
        return position;
    }

    /**
     * Get the X component of the position vector.
     *
     * @return the X component
     */
    public double getX() {
        return position.getX();
    }

    /**
     * Get the rounded X component of the position vector.
     *
     * @return the rounded X component
     */
    public int getBlockX() {
        return position.getBlockX();
    }

    /**
     * Return a copy of this object with the X component of the new object
     * set to the given value.
     *
     * @param x the new value for the X component
     * @return a new immutable instance
     */
    public Location setX(double x) {
        return new Location(extent, position.setX(x), yaw, pitch);
    }

    /**
     * Return a copy of this object with the X component of the new object
     * set to the given value.
     *
     * @param x the new value for the X component
     * @return a new immutable instance
     */
    public Location setX(int x) {
        return new Location(extent, position.setX(x), yaw, pitch);
    }

    /**
     * Get the Y component of the position vector.
     *
     * @return the Y component
     */
    public double getY() {
        return position.getY();
    }

    /**
     * Get the rounded Y component of the position vector.
     *
     * @return the rounded Y component
     */
    public int getBlockY() {
        return position.getBlockY();
    }

    /**
     * Return a copy of this object with the Y component of the new object
     * set to the given value.
     *
     * @param y the new value for the Y component
     * @return a new immutable instance
     */
    public Location setY(double y) {
        return new Location(extent, position.setY(y), yaw, pitch);
    }

    /**
     * Return a copy of this object with the Y component of the new object
     * set to the given value.
     *
     * @param y the new value for the Y component
     * @return a new immutable instance
     */
    public Location setY(int y) {
        return new Location(extent, position.setY(y), yaw, pitch);
    }

    /**
     * Get the Z component of the position vector.
     *
     * @return the Z component
     */
    public double getZ() {
        return position.getZ();
    }

    /**
     * Get the rounded Z component of the position vector.
     *
     * @return the rounded Z component
     */
    public int getBlockZ() {
        return position.getBlockZ();
    }

    /**
     * Return a copy of this object with the Z component of the new object
     * set to the given value.
     *
     * @param z the new value for the Y component
     * @return a new immutable instance
     */
    public Location setZ(double z) {
        return new Location(extent, position.setZ(z), yaw, pitch);
    }

    /**
     * Return a copy of this object with the Z component of the new object
     * set to the given value.
     *
     * @param z the new value for the Y component
     * @return a new immutable instance
     */
    public Location setZ(int z) {
        return new Location(extent, position.setZ(z), yaw, pitch);
    }

    /**
     * Return a copy of this object with the position set to the given value.
     *
     * @param position The new position
     * @return a new immutable instance
     */
    public Location setPosition(Vector position) {
        return new Location(extent, position, yaw, pitch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (Double.doubleToLongBits(pitch) != Double.doubleToLongBits(location.pitch)) return false;
        if (Double.doubleToLongBits(yaw) != Double.doubleToLongBits(location.yaw)) return false;
        if (!position.equals(location.position)) return false;
        if (!extent.equals(location.extent)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = extent.hashCode();
        result = 31 * result + position.hashCode();
        result = 31 * result + Float.floatToIntBits(this.pitch);
        result = 31 * result + Float.floatToIntBits(this.yaw);
        return result;
    }

}
