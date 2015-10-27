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

package com.sk89q.worldedit.forge;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.World;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

final class ForgeAdapter {

    private ForgeAdapter() {
    }

    public static World adapt(net.minecraft.world.World world) {
        return new ForgeWorld(world);
    }

    public static Vector adapt(Vec3 vector) {
        return new Vector(vector.xCoord, vector.yCoord, vector.zCoord);
    }

    public static int adapt(Direction face) {
        switch (face) {
            case NORTH: return ForgeDirection.NORTH.ordinal();
            case SOUTH: return ForgeDirection.SOUTH.ordinal();
            case WEST: return ForgeDirection.WEST.ordinal();
            case EAST: return ForgeDirection.EAST.ordinal();
            case UP: return ForgeDirection.UP.ordinal();
            case DOWN: return ForgeDirection.DOWN.ordinal();
            default: return ForgeDirection.UNKNOWN.ordinal();
        }
    }
}
