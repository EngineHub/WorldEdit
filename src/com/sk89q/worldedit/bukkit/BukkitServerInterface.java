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

import org.bukkit.*;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseItemStack;

public class BukkitServerInterface extends ServerInterface {
    public Server server;
    
    public BukkitServerInterface(Server server) {
        this.server = server;
    }

    @Override
    public boolean setBlockType(LocalWorld world, Vector pt, int type) {
        ((BukkitWorld)world).getWorld().getBlockAt(
                pt.getBlockX(),
                pt.getBlockY(),
                pt.getBlockZ()).setTypeID(type);
        
        return true;
    }

    @Override
    public int getBlockType(LocalWorld world, Vector pt) {
        return ((BukkitWorld)world).getWorld().getBlockAt(
                pt.getBlockX(),
                pt.getBlockY(),
                pt.getBlockZ()).getTypeID();
    }

    @Override
    public void setBlockData(LocalWorld world, Vector pt, int data) {
        ((BukkitWorld)world).getWorld().getBlockAt(
                pt.getBlockX(),
                pt.getBlockY(),
                pt.getBlockZ()).setData((byte)data);
        
    }

    @Override
    public int getBlockData(LocalWorld world, Vector pt) {
        return ((BukkitWorld)world).getWorld().getBlockAt(
                pt.getBlockX(),
                pt.getBlockY(),
                pt.getBlockZ()).getData();
    }

    @Override
    public void setSignText(LocalWorld world, Vector pt, String[] text) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String[] getSignText(LocalWorld world, Vector pt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BaseItemStack[] getChestContents(LocalWorld world, Vector pt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean setChestContents(LocalWorld world, Vector pt, BaseItemStack[] contents) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean clearChest(LocalWorld world, Vector pt) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isValidMobType(String type) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setMobSpawnerType(LocalWorld world, Vector pt, String mobType) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getMobSpawnerType(LocalWorld world, Vector pt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean generateTree(EditSession editSession, LocalWorld world,
            Vector pt) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void dropItem(LocalWorld world, Vector pt, int type, int count, int times) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void dropItem(LocalWorld world, Vector pt, int type, int count) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void dropItem(LocalWorld world, Vector pt, int type) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void simulateBlockMine(LocalWorld world, Vector pt) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int resolveItem(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int killMobs(LocalWorld world, Vector origin, int radius) {
        // TODO Auto-generated method stub
        return 0;
    }

}
