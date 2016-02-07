package com.sk89q.worldedit.forge.compat;

import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface ForgeMultipartCompat {

    TileEntity overrideTileEntity(World world, @Nullable NBTTagCompound tag,
            TileEntity normal);

    void sendDescPacket(World world, TileEntity entity);

}
