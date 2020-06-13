package com.sk89q.worldedit.fabric.internal;

import net.minecraft.world.World;

import java.nio.file.Path;

public interface ExtendedMinecraftServer {

    Path getStoragePath(World world);

}
