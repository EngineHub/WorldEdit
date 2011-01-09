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

import org.bukkit.World;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseItemStack;

public class BukkitWorld extends LocalWorld {
    private World world;
    
    public BukkitWorld(World world) {
        this.world = world;
    }
    
    public World getWorld() {
        return world;
    }

    @Override
    public boolean setBlockType(Vector pt, int type) {
        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setTypeID(type);
    }

    @Override
    public int getBlockType(Vector pt) {
        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getTypeID();
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
        // TODO Auto-generated method stub
    }

    @Override
    public String[] getSignText(Vector pt) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean generateBigTree(EditSession editSession, Vector pt) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void dropItem(Vector pt, int type, int count, int times) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void dropItem(Vector pt, int type, int count) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void dropItem(Vector pt, int type) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void simulateBlockMine(Vector pt) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int killMobs(Vector origin, int radius) {
        // TODO Auto-generated method stub
        return 0;
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
