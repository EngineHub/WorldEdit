package com.sk89q.worldedit.forge.compat;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class NoForgeMultipartCompat implements ForgeMultipartCompat {

    @Override
    public TileEntity overrideTileEntity(World world, NBTTagCompound tag,
            TileEntity normal) {
        return normal;
    }

    @Override
    public void sendDescPacket(World world, TileEntity entity) {
    }

}
