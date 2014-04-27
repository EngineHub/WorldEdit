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

package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.Vectors;
import com.sk89q.worldedit.world.World;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapts between Bukkit and WorldEdit equivalent objects.
 */
final class BukkitAdapter {

    private BukkitAdapter() {
    }

    /**
     * Create a WorldEdit world from a Bukkit world.
     *
     * @param world the Bukkit world
     * @return a WorldEdit world
     */
    public static World adapt(org.bukkit.World world) {
        checkNotNull(world);
        return new BukkitWorld(world);
    }

    /**
     * Create a WorldEdit location from a Bukkit location.
     *
     * @param location the Bukkit location
     * @return a WorldEdit location
     */
    public static Location adapt(org.bukkit.Location location) {
        checkNotNull(location);
        Vector position = BukkitUtil.toVector(location);
        Vector direction = Vectors.fromEulerDeg(location.getYaw(), location.getPitch());
        return new com.sk89q.worldedit.util.Location(adapt(location.getWorld()), position, direction);
    }

}
