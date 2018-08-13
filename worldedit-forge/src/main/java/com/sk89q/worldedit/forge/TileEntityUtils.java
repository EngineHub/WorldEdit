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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Constructor;

import javax.annotation.Nullable;

/**
 * Utility methods for setting tile entities in the world.
 */
final class TileEntityUtils {

    private TileEntityUtils() {
    }

    /**
     * Update the given tag compound with position information.
     *
     * @param tag the tag
     * @param position the position
     * @return a tag compound
     */
    private static NBTTagCompound updateForSet(NBTTagCompound tag, Vector position) {
        checkNotNull(tag);
        checkNotNull(position);

        tag.setTag("x", new NBTTagInt(position.getBlockX()));
        tag.setTag("y", new NBTTagInt(position.getBlockY()));
        tag.setTag("z", new NBTTagInt(position.getBlockZ()));

        return tag;
    }

    /**
     * Set a tile entity at the given location.
     *
     * @param world the world
     * @param position the position
     * @param clazz the tile entity class
     * @param tag the tag for the tile entity (may be null to not set NBT data)
     */
    static void setTileEntity(World world, Vector position, Class<? extends TileEntity> clazz, @Nullable NBTTagCompound tag) {
        checkNotNull(world);
        checkNotNull(position);
        checkNotNull(clazz);

        TileEntity tileEntity = constructTileEntity(world, position, clazz);

        if (tileEntity == null) {
            return;
        }

        if (tag != null) {
            // Set X, Y, Z
            updateForSet(tag, position);
            tileEntity.readFromNBT(tag);
        }

        world.setTileEntity(new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ()), tileEntity);
    }

    /**
     * Set a tile entity at the given location using the tile entity ID from
     * the tag.
     *
     * @param world the world
     * @param position the position
     * @param tag the tag for the tile entity (may be null to do nothing)
     */
    static void setTileEntity(World world, Vector position, @Nullable NBTTagCompound tag) {
        if (tag != null) {
            updateForSet(tag, position);
            TileEntity tileEntity = TileEntity.create(world, tag);
            if (tileEntity != null) {
                world.setTileEntity(new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ()), tileEntity);
            }
        }
    }

    /**
     * Construct a tile entity from the given class.
     *
     * @param world the world
     * @param position the position
     * @param clazz the class
     * @return a tile entity (may be null if it failed)
     */
    @Nullable
    static TileEntity constructTileEntity(World world, Vector position, Class<? extends TileEntity> clazz) {
        Constructor<? extends TileEntity> baseConstructor;
        try {
            baseConstructor = clazz.getConstructor(); // creates "blank" TE
        } catch (Throwable e) {
            return null; // every TE *should* have this constructor, so this isn't necessary
        }

        TileEntity genericTE;
        try {
            genericTE = baseConstructor.newInstance();
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

    public static NBTTagCompound copyNbtData(TileEntity tile) {
        NBTTagCompound tag = new NBTTagCompound();
        tile.writeToNBT(tag);
        return tag;
    }
}
