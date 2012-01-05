// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit.bukkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import com.sk89q.worldedit.*;

public class BukkitUtil {
    private BukkitUtil() {
    }

    private static final Map<World, LocalWorld> wlw = new HashMap<World, LocalWorld>();

    public static LocalWorld getLocalWorld(World w) {
        LocalWorld lw = wlw.get(w);
        if (lw == null) {
            lw = new BukkitWorld(w);
            wlw.put(w, lw);
        }
        return lw;
    }

    public static BlockVector toVector(Block block) {
        return new BlockVector(block.getX(), block.getY(), block.getZ());
    }

    public static BlockVector toVector(BlockFace face) {
        return new BlockVector(face.getModX(), face.getModY(), face.getModZ());
    }

    public static BlockWorldVector toWorldVector(Block block) {
        return new BlockWorldVector(getLocalWorld(block.getWorld()), block.getX(), block.getY(), block.getZ());
    }

    public static Vector toVector(Location loc) {
        return new Vector(loc.getX(), loc.getY(), loc.getZ());
    }

    public static Vector toVector(org.bukkit.util.Vector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Location toLocation(WorldVector pt) {
        return new Location(toWorld(pt), pt.getX(), pt.getY(), pt.getZ());
    }

    public static Location toLocation(World world, Vector pt) {
        return new Location(world, pt.getX(), pt.getY(), pt.getZ());
    }

    public static Location center(Location loc) {
        return new Location(
                loc.getWorld(),
                loc.getBlockX() + 0.5,
                loc.getBlockY() + 0.5,
                loc.getBlockZ() + 0.5,
                loc.getPitch(),
                loc.getYaw()
        );
    }

    public static Player matchSinglePlayer(Server server, String name) {
        List<Player> players = server.matchPlayer(name);
        if (players.size() == 0) {
            return null;
        }
        return players.get(0);
    }

    public static Block toBlock(BlockWorldVector pt) {
        return toWorld(pt).getBlockAt(toLocation(pt));
    }

    public static World toWorld(WorldVector pt) {
        return ((BukkitWorld) pt.getWorld()).getWorld();
    }

    /**
     * Bukkit's Location class has serious problems with floating point
     * precision.
     */
    public static boolean equals(Location a, Location b) {
        if (Math.abs(a.getX() - b.getX()) > EQUALS_PRECISION) return false;
        if (Math.abs(a.getY() - b.getY()) > EQUALS_PRECISION) return false;
        if (Math.abs(a.getZ() - b.getZ()) > EQUALS_PRECISION) return false;
        return true;
    }

    public static final double EQUALS_PRECISION = 0.0001;
}
