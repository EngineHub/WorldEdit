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

/**
 * @deprecated Use {@link com.sk89q.worldedit.util.Location} wherever possible
 */
@Deprecated
public class Location {
    private final LocalWorld world;
    private final Vector position;
    private final float yaw;
    private final float pitch;

    public Location(LocalWorld world, Vector position) {
        this(world, position, 0, 0);
    }

    public Location(LocalWorld world, Vector position, float yaw, float pitch) {
        this.world = world;
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public LocalWorld getWorld() {
        return world;
    }

    public Vector getPosition() {
        return position;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public Location setAngles(float yaw, float pitch) {
        return new Location(world, position, yaw, pitch);
    }

    public Location setPosition(Vector position) {
        return new Location(world, position, yaw, pitch);
    }

    public Location add(Vector other) {
        return setPosition(position.add(other));
    }

    public Location add(double x, double y, double z) {
        return setPosition(position.add(x, y, z));
    }

    public Vector getDirection() {
        final double yawRadians = Math.toRadians(yaw);
        final double pitchRadians = Math.toRadians(pitch);
        final double y = -Math.sin(pitchRadians);

        final double h = Math.cos(pitchRadians);

        final double x = -h * Math.sin(yawRadians);
        final double z = h * Math.cos(yawRadians);

        return new Vector(x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Location))
            return false;

        Location location = (Location) obj;
        if (!world.equals(location.world))
            return false;

        if (!position.equals(location.position))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return position.hashCode() + 19 * world.hashCode();
    }

    @Override
    public String toString() {
        return "World: " + world.getName() + ", Coordinates: " + position.toString()
                + ", Yaw: " + yaw + ", Pitch: " + pitch;
    }

    public static Location fromLookAt(LocalWorld world, Vector start, Vector lookAt) {
        final Vector diff = lookAt.subtract(start);

        return fromEye(world, start, diff);
    }

    public static Location fromEye(LocalWorld world, Vector start, Vector eye) {
        final double eyeX = eye.getX();
        final double eyeZ = eye.getZ();
        final float yaw = (float) Math.toDegrees(Math.atan2(-eyeX, eyeZ));
        final double length = Math.sqrt(eyeX * eyeX + eyeZ * eyeZ);
        final float pitch = (float) Math.toDegrees(Math.atan2(-eye.getY(), length));

        return new Location(world, start, yaw, pitch);
    }
}
