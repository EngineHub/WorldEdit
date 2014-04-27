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

/**
 * Utility methods for {@link Vector}s.
 */
public final class Vectors {

    private Vectors() {
    }

    /**
     * Create a new {@link Vector} using Euler angles specified in degrees,
     * but with no roll.
     *
     * @param yaw the yaw
     * @param pitch the pitch
     * @return a new {@link Vector}
     */
    public static Vector fromEulerDeg(double yaw, double pitch) {
        final double yawRadians = Math.toRadians(yaw);
        final double pitchRadians = Math.toRadians(pitch);
        return fromEulerRad(yawRadians, pitchRadians);
    }

    /**
     * Create a new {@link Vector} using Euler angles specified in radians,
     * but with no roll.
     *
     * @param yaw the yaw
     * @param pitch the pitch
     * @return a new {@link Vector}
     */
    public static Vector fromEulerRad(double yaw, double pitch) {
        final double y = -Math.sin(pitch);

        final double h = Math.cos(pitch);

        final double x = -h * Math.sin(yaw);
        final double z = h * Math.cos(yaw);

        return new Vector(x, y, z);
    }

}
