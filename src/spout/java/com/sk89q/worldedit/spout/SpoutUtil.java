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

package com.sk89q.worldedit.spout;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import org.spout.api.Engine;
import org.spout.api.Server;
import org.spout.api.entity.Entity;
import org.spout.api.geo.World;
import org.spout.api.geo.cuboid.Block;
import org.spout.api.geo.discrete.Point;
import org.spout.api.material.block.BlockFace;
import org.spout.api.math.GenericMath;
import org.spout.api.math.Vector3;
import org.spout.api.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SpoutUtil {
    private SpoutUtil() {
    }

    private static final Map<World, LocalWorld> wlw = new HashMap<World, LocalWorld>();

    public static LocalWorld getLocalWorld(World w) {
        LocalWorld lw = wlw.get(w);
        if (lw == null) {
            lw = new SpoutWorld(w);
            wlw.put(w, lw);
        }
        return lw;
    }

    public static BlockVector toVector(Block block) {
        return new BlockVector(block.getX(), block.getY(), block.getZ());
    }

    public static BlockVector toVector(BlockFace face) {
        return toBlockVector(face.getOffset());
    }

    public static BlockVector toBlockVector(Vector3 vector) {
        return new BlockVector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static BlockWorldVector toWorldVector(Block block) {
        return new BlockWorldVector(getLocalWorld(block.getWorld()), block.getX(), block.getY(), block.getZ());
    }

    public static Vector toVector(Point loc) {
        return new Vector(loc.getX(), loc.getY(), loc.getZ());
    }

    public static Vector toVector(org.spout.api.math.Vector3 vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Point toPoint(WorldVector pt) {
        return new Point(toWorld(pt), (float)pt.getX(), (float)pt.getY(), (float)pt.getZ());
    }

    public static Point toPoint(World world, Vector pt) {
        return new Point(world, (float)pt.getX(), (float)pt.getY(), (float)pt.getZ());
    }

    public static Point center(Point loc) {
        return new Point(
                loc.getWorld(),
                GenericMath.floor(loc.getX()) + 0.5F,
                GenericMath.floor(loc.getY()) + 0.5F,
                GenericMath.floor(loc.getZ()) + 0.5F
        );
    }

    public static Player matchSinglePlayer(Engine game, String name) {
        return game instanceof Server ? ((Server) game).getPlayer(name, false) : null;
    }

    public static Block toBlock(BlockWorldVector pt) {
        return toWorld(pt).getBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    public static World toWorld(WorldVector pt) {
        return ((SpoutWorld) pt.getWorld()).getWorld();
    }

    public static Location toLocation(Entity ent) {
        return new Location(getLocalWorld(ent.getWorld()), toVector(ent.getScene().getPosition()),
                ent.getScene().getRotation().getYaw(), ent.getScene().getRotation().getPitch());
    }
}
