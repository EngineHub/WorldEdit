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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.world.World;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a location in a world with has a direction.
 * </p>
 * Like {@code Vectors}, {@code Locations} are immutable and mutator methods
 * will create a new copy.
 * </p>
 * At the moment, but this may change in the future, {@link #hashCode()} and
 * {@link #equals(Object)} are subject to minor differences caused by
 * floating point errors.
 */
public class Location {

    private final World world;
    private final Vector position;
    private final Vector direction;

    /**
     * Create a new instance in the given world at 0, 0, 0 with a
     * direction vector of 0, 0, 0.
     *
     * @param world the world
     */
    public Location(World world) {
        this(world, new Vector(), new Vector());
    }

    /**
     * Create a new instance in the given world with the given coordinates
     * with a direction vector of 0, 0, 0.
     *
     * @param world the world
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    public Location(World world, double x, double y, double z) {
        this(world, new Vector(x, y, z), new Vector());
    }

    /**
     * Create a new instance in the given world with the given position
     * vector and a direction vector of 0, 0, 0.
     *
     * @param world the world
     * @param position the position vector
     */
    public Location(World world, Vector position) {
        this(world, position, new Vector());
    }

    /**
     * Create a new instance in the given world with the given coordinates
     * and the given direction vector.
     *
     * @param world the world
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @param direction the direction vector
     */
    public Location(World world, double x, double y, double z, Vector direction) {
        this(world, new Vector(x, y, z), direction);
    }

    /**
     * Create a new instance in the given world with the given position vector
     * and the given direction vector.
     *
     * @param world the world
     * @param position the position vector
     * @param direction the direction vector
     */
    public Location(World world, Vector position, Vector direction) {
        checkNotNull(world);
        checkNotNull(position);
        checkNotNull(direction);
        this.world = world;
        this.position = position;
        this.direction = direction;
    }

    /**
     * Get the world.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Create a clone of this object with the given world.
     *
     * @param world the new world
     * @return the new instance
     */
    public Location setWorld(World world) {
        return new Location(world, position, getDirection());
    }

    /**
     * Get the direction.
     * </p>
     * The direction vector <em>may</em> be a null vector. It may or may not
     * be a unit vector.
     *
     * @return the direction
     */
    public Vector getDirection() {
        return direction;
    }

    /**
     * Create a clone of this object with the given direction.
     *
     * @param direction the new direction
     * @return the new instance
     */
    public Location setDirection(Vector direction) {
        return new Location(world, position, direction);
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
        return new Location(world, position.setX(x), direction);
    }

    /**
     * Return a copy of this object with the X component of the new object
     * set to the given value.
     *
     * @param x the new value for the X component
     * @return a new immutable instance
     */
    public Location setX(int x) {
        return new Location(world, position.setX(x), direction);
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
        return new Location(world, position.setY(y), direction);
    }

    /**
     * Return a copy of this object with the Y component of the new object
     * set to the given value.
     *
     * @param y the new value for the Y component
     * @return a new immutable instance
     */
    public Location setY(int y) {
        return new Location(world, position.setY(y), direction);
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
        return new Location(world, position.setZ(z), direction);
    }

    /**
     * Return a copy of this object with the Z component of the new object
     * set to the given value.
     *
     * @param z the new value for the Y component
     * @return a new immutable instance
     */
    public Location setZ(int z) {
        return new Location(world, position.setZ(z), direction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (!direction.equals(location.direction)) return false;
        if (!position.equals(location.position)) return false;
        if (!world.equals(location.world)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = world.hashCode();
        result = 31 * result + position.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }

}
