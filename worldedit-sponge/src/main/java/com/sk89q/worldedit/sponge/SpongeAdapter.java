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

package com.sk89q.worldedit.sponge;

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.sponge.nms.SpongeNMSWorld;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;

final class SpongeAdapter {

    private SpongeAdapter() {
    }

    public static World adapt(org.spongepowered.api.world.World world) {
        return new SpongeNMSWorld(world);
    }

    public static Location adapt(org.spongepowered.api.world.Location<org.spongepowered.api.world.World> loc, Vector3d rot) {
        Vector position = new Vector(loc.getX(), loc.getY(), loc.getZ());

        return new Location(SpongeAdapter.adapt(loc.getExtent()), position, (float) rot.getY(), (float) rot.getX());
    }
}
