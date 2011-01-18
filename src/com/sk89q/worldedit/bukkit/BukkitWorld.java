// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.World;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseItemStack;

public class BukkitWorld extends LocalWorld {
    /**
     * Logger.
     */
    private final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    
    private World world;
    
    public BukkitWorld(World world) {
        this.world = world;
    }
    
    public World getWorld() {
        return world;
    }

    @Override
    public boolean setBlockType(Vector pt, int type) {
        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setTypeId(type);
    }

    @Override
    public int getBlockType(Vector pt) {
        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getTypeId();
    }

    @Override
    public void setBlockData(Vector pt, int data) {
        world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setData((byte)data);
        
    }

    @Override
    public int getBlockData(Vector pt) {
        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getData();
    }

    @Override
    public void setSignText(Vector pt, String[] text) {
        Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) return;
        BlockState state = block.getState();
        if (state == null || !(state instanceof Sign)) return;
        Sign sign = (Sign)state;
        sign.setLine(0, text[0]);
        sign.setLine(1, text[1]);
        sign.setLine(2, text[2]);
        sign.setLine(3, text[3]);
        sign.update();
    }

    @Override
    public String[] getSignText(Vector pt) {
        Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) return new String[] { "", "", "", "" };
        BlockState state = block.getState();
        if (state == null || !(state instanceof Sign)) return new String[] { "", "", "", "" };
        Sign sign = (Sign)state;
        String line0 = sign.getLine(0);
        String line1 = sign.getLine(1);
        String line2 = sign.getLine(2);
        String line3 = sign.getLine(3);
        return new String[] {
                line0 != null ? line0 : "",
                line1 != null ? line1 : "",
                line2 != null ? line2 : "",
                line3 != null ? line3 : "",
            };
    }

    @Override
    public BaseItemStack[] getChestContents(Vector pt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean setChestContents(Vector pt, BaseItemStack[] contents) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean clearChest(Vector pt) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setMobSpawnerType(Vector pt, String mobType) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getMobSpawnerType(Vector pt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean generateTree(EditSession editSession, Vector pt) {
        try {
            return CraftBukkitInterface.generateTree(editSession, pt);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, 
                    "Failed to create tree (do you need to update WorldEdit " +
                    "due to a Minecraft update?)", t);
            return false;
        }
    }

    @Override
    public boolean generateBigTree(EditSession editSession, Vector pt) {
        try {
            return CraftBukkitInterface.generateBigTree(editSession, pt);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, 
                    "Failed to create tree (do you need to update WorldEdit " +
                    "due to a Minecraft update?)", t);
            return false;
        }
    }

    @Override
    public void dropItem(Vector pt, int type, int count) {
        ItemStack item = new ItemStack(type, count);
        world.dropItemNaturally(toLocation(pt), item);
        
    }

    @Override
    public void dropItem(Vector pt, int type) {
        ItemStack item = new ItemStack(type, 1);
        world.dropItemNaturally(toLocation(pt), item);
        
    }

    @Override
    public int killMobs(Vector origin, int radius) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    private Location toLocation(Vector pt) {
        return new Location(world, pt.getX(), pt.getY(), pt.getZ());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BukkitWorld)) {
            return false;
        }
        
        return ((BukkitWorld)other).world.equals(world);
    }

    @Override
    public int hashCode() {
        return world.hashCode();
    }
}
