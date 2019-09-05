package com.sk89q.worldedit.fabric;

import com.sk89q.worldedit.extension.platform.Watchdog;
import com.sk89q.worldedit.fabric.mixin.MixinMinecraftServer;
import net.minecraft.util.SystemUtil;

class FabricWatchdog implements Watchdog {

    private final MixinMinecraftServer server;

    FabricWatchdog(MixinMinecraftServer server) {
        this.server = server;
    }

    @Override
    public void tick() {
        server.timeReference = SystemUtil.getMeasuringTimeMs();
    }
}
