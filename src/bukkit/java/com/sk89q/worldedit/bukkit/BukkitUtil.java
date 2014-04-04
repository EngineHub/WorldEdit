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

import java.util.List;

import com.sk89q.worldedit.NotABlockException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.blocks.SkullBlock;
import org.bukkit.DyeColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bukkit.entity.BukkitEntity;
import com.sk89q.worldedit.bukkit.entity.BukkitExpOrb;
import com.sk89q.worldedit.bukkit.entity.BukkitItem;
import com.sk89q.worldedit.bukkit.entity.BukkitPainting;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

public class BukkitUtil {
    private BukkitUtil() {
    }

    public static LocalWorld getLocalWorld(World w) {
        return new BukkitWorld(w);
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

    public static Vector toVector(org.bukkit.Location loc) {
        return new Vector(loc.getX(), loc.getY(), loc.getZ());
    }

    public static Location toLocation(org.bukkit.Location loc) {
        return new Location(
            getLocalWorld(loc.getWorld()),
            new Vector(loc.getX(), loc.getY(), loc.getZ()),
            loc.getYaw(), loc.getPitch()
        );
    }

    public static Vector toVector(org.bukkit.util.Vector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static org.bukkit.Location toLocation(WorldVector pt) {
        return new org.bukkit.Location(toWorld(pt), pt.getX(), pt.getY(), pt.getZ());
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
    @SuppressWarnings("RedundantIfStatement")
    public static boolean equals(org.bukkit.Location a, org.bukkit.Location b) {
        if (Math.abs(a.getX() - b.getX()) > EQUALS_PRECISION) return false;
        if (Math.abs(a.getY() - b.getY()) > EQUALS_PRECISION) return false;
        if (Math.abs(a.getZ() - b.getZ()) > EQUALS_PRECISION) return false;
        return true;
    }

    public static final double EQUALS_PRECISION = 0.0001;

    public static org.bukkit.Location toLocation(Location location) {
        Vector pt = location.getPosition();
        return new org.bukkit.Location(
            toWorld(location.getWorld()),
            pt.getX(), pt.getY(), pt.getZ(),
            location.getYaw(), location.getPitch()
        );
    }

    public static World toWorld(final LocalWorld world) {
        return ((BukkitWorld) world).getWorld();
    }

    public static BukkitEntity toLocalEntity(Entity e) {
        switch (e.getType()) {
            case EXPERIENCE_ORB:
                return new BukkitExpOrb(toLocation(e.getLocation()), e.getUniqueId(), ((ExperienceOrb)e).getExperience());
            case PAINTING:
                Painting paint = (Painting) e;
                return new BukkitPainting(toLocation(e.getLocation()), paint.getArt(), paint.getFacing(), e.getUniqueId());
            case DROPPED_ITEM:
                return new BukkitItem(toLocation(e.getLocation()), ((Item)e).getItemStack(), e.getUniqueId());
            default:
                return new BukkitEntity(toLocation(e.getLocation()), e.getType(), e.getUniqueId());
        }
    }

    public static BaseBlock toBlock(LocalWorld world, ItemStack itemStack) throws WorldEditException {
        final int typeId = itemStack.getTypeId();

        switch (typeId) {
            case ItemID.INK_SACK:
                final Dye materialData = (Dye) itemStack.getData();
                if (materialData.getColor() == DyeColor.BROWN) {
                    return new BaseBlock(BlockID.COCOA_PLANT, -1);
                }
                break;

            case ItemID.HEAD:
                return new SkullBlock(0, (byte) itemStack.getDurability());

            default:
                final BaseBlock baseBlock = BlockType.getBlockForItem(typeId, itemStack.getDurability());
                if (baseBlock != null) {
                    return baseBlock;
                }
                break;
        }

        if (world.isValidBlockType(typeId)) {
            return new BaseBlock(typeId, -1);
        }

        throw new NotABlockException(typeId);
    }
}
