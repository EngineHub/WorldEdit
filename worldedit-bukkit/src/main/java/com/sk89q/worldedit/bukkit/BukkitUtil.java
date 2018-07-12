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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class BukkitUtil {

    private BukkitUtil() {
    }

    public static com.sk89q.worldedit.world.World getWorld(World w) {
        return new BukkitWorld(w);
    }

    public static BlockVector toVector(Block block) {
        return new BlockVector(block.getX(), block.getY(), block.getZ());
    }

    public static BlockVector toVector(BlockFace face) {
        return new BlockVector(face.getModX(), face.getModY(), face.getModZ());
    }

    public static Vector toVector(org.bukkit.Location loc) {
        return new Vector(loc.getX(), loc.getY(), loc.getZ());
    }

    public static Location toLocation(org.bukkit.Location loc) {
        return new Location(
            getWorld(loc.getWorld()),
            new Vector(loc.getX(), loc.getY(), loc.getZ()),
            loc.getYaw(), loc.getPitch()
        );
    }

    public static Vector toVector(org.bukkit.util.Vector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static org.bukkit.Location toLocation(World world, Vector pt) {
        return new org.bukkit.Location(world, pt.getX(), pt.getY(), pt.getZ());
    }

    public static org.bukkit.Location center(org.bukkit.Location loc) {
        return new org.bukkit.Location(
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
        if (players.isEmpty()) {
            return null;
        }
        return players.get(0);
    }

    /**
     * Bukkit's Location class has serious problems with floating point
     * precision.
     */
    @SuppressWarnings("RedundantIfStatement")
    public static boolean equals(org.bukkit.Location a, org.bukkit.Location b) {
        if (Math.abs(a.getX() - b.getX()) > EQUALS_PRECISION) return false;
        if (Math.abs(a.getY() - b.getY()) > EQUALS_PRECISION) return false;
        if (Math.abs(a.getZ() - b.getZ()) > EQUALS_PRECISION) return false;
        return true;
    }

    public static final double EQUALS_PRECISION = 0.0001;

    public static org.bukkit.Location toLocation(Location location) {
        Vector pt = location.toVector();
        return new org.bukkit.Location(
            toWorld(location.getExtent()),
            pt.getX(), pt.getY(), pt.getZ(),
            location.getYaw(), location.getPitch()
        );
    }

    public static World toWorld(final Extent world) {
        return ((BukkitWorld) world).getWorld();
    }

    public static BlockState toBlock(BlockData blockData) {
        return null; // TODO BLOCKING
    }

    public static BlockData toBlock(BlockStateHolder block) {
        return Bukkit.createBlockData(block.toString()); // TODO BLOCKING
    }

    public static BlockState toBlock(ItemStack itemStack) throws WorldEditException {
        if (itemStack.getType().isBlock()) {
            return toBlock(itemStack.getType().createBlockData());
        } else {
            return BlockTypes.AIR.getDefaultState();
        }
    }

    public static BaseItemStack toBaseItemStack(ItemStack itemStack) {
        return new BaseItemStack(ItemTypes.get(itemStack.getType().getKey().toString()), itemStack.getAmount());
    }

    public static ItemStack toItemStack(BaseItemStack item) {
        BlockData blockData = Bukkit.createBlockData(item.getType().getId());
        return new ItemStack(blockData.getMaterial(), item.getAmount());
    }
}
