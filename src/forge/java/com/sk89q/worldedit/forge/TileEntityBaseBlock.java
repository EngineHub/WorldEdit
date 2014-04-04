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

import java.lang.reflect.Constructor;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;

public class TileEntityBaseBlock extends BaseBlock implements TileEntityBlock {
    private TileEntity tile;

    public TileEntity getTile() {
        return this.tile;
    }

    public void setTile(TileEntity tile) {
        this.tile = tile;
    }

    public TileEntityBaseBlock(int type, int data, TileEntity tile) {
        super(type, data);
        this.tile = tile;
    }

    private static NBTTagCompound getNewData(Vector pt, TileEntityBaseBlock block) {
        NBTTagCompound tag = new NBTTagCompound();

        block.tile.writeToNBT(tag);

        tag.setTag("x", new NBTTagInt("x", pt.getBlockX()));
        tag.setTag("y", new NBTTagInt("y", pt.getBlockY()));
        tag.setTag("z", new NBTTagInt("z", pt.getBlockZ()));

        return tag;
    }

    public static void set(World world, Vector pt, TileEntityBaseBlock block) {
        // TODO somehow decide if we want to soft or hard copy based on block type
        set(world, pt, block, true);
    }

    public static void set(World world, Vector pt, TileEntityBaseBlock block, boolean hardcopy) {
        TileEntity newTE = constructTEClass(world, pt, block);
        if (newTE == null) {
            return;
        }
        if (hardcopy) {
            // this causes issues with certain TEs
            NBTTagCompound newTag = getNewData(pt, block);
            newTE.readFromNBT(newTag);
        }
        world.setBlockTileEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newTE);
    }

    private static TileEntity constructTEClass(World world, Vector pt, TileEntityBaseBlock block) {
        Class<? extends TileEntity> clazz = block.tile.getClass();
        Constructor<? extends TileEntity> baseConstructor;
        try {
            baseConstructor = clazz.getConstructor(); // creates "blank" TE
        } catch (Throwable e) {
            return null; // every TE *should* have this constructor, so this isn't necessary
        }
        TileEntity genericTE;
        try {
            // downcast here for return while retaining the type
            genericTE = (TileEntity) baseConstructor.newInstance();
        } catch (Throwable e) {
            return null;
        }
        /*
        genericTE.blockType = Block.blocksList[block.getId()];
        genericTE.blockMetadata = block.getData();
        genericTE.xCoord = pt.getBlockX();
        genericTE.yCoord = pt.getBlockY();
        genericTE.zCoord = pt.getBlockZ();
        genericTE.worldObj = world;
        */ // handled by internal code
        return genericTE;
    }

    public String getNbtId() {
        NBTTagCompound tag = new NBTTagCompound();
        try {
            this.tile.writeToNBT(tag);
        } catch (Exception e) {
            return "";
        }
        return tag.getString("id");
    }
}