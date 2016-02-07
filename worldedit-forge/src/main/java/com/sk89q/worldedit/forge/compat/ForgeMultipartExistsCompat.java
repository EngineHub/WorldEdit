package com.sk89q.worldedit.forge.compat;

import codechicken.multipart.MultipartHelper;
import codechicken.multipart.TileMultipart;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ForgeMultipartExistsCompat implements ForgeMultipartCompat {

    @Override
    public TileEntity overrideTileEntity(World world,
            @Nullable NBTTagCompound tag, TileEntity normal) {
        if (tag == null) {
            return normal;
        }
        TileEntity tile = MultipartHelper.createTileFromNBT(world, tag);
        if (tile == null) {
            return normal;
        }
        return tile;
    }

    @Override
    public void sendDescPacket(World world, TileEntity entity) {
        if (entity instanceof TileMultipart) {
            TileMultipart multi = (TileMultipart) entity;
            MultipartHelper.sendDescPacket(world, multi);
        }
    }

}
