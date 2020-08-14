/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.fabric.mixin;

import com.sk89q.worldedit.extension.platform.Watchdog;
import com.sk89q.worldedit.fabric.internal.ExtendedMinecraftServer;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.util.Util;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer extends ReentrantThreadExecutor<ServerTask> implements Watchdog, ExtendedMinecraftServer {

    public MixinMinecraftServer(String name) {
        super(name);
    }

    @Shadow
    private long timeReference;
    @Shadow
    protected LevelStorage.Session session;

    @Override
    public void tick() {
        timeReference = Util.getMeasuringTimeMs();
    }

    @Override
    public Path getStoragePath(World world) {
        return session.getWorldDirectory(world.getRegistryKey()).toPath();
    }

    @Accessor()
    @Override
    public abstract ServerResourceManager getServerResourceManager();
}
