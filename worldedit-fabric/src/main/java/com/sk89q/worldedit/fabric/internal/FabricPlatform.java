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

package com.sk89q.worldedit.fabric.internal;

import com.sk89q.worldedit.coremc.internal.CoreMcPlatform;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.util.lifecycle.Lifecycled;
import com.sk89q.worldedit.util.lifecycle.SimpleLifecycled;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.enginehub.worldeditcui.protocol.CUIPacket;

class FabricPlatform extends CoreMcPlatform {

    private static Lifecycled<MinecraftServer> createMinecraftServerLifecycled() {
        SimpleLifecycled<MinecraftServer> lifecycledServer = SimpleLifecycled.invalid();
        ServerLifecycleEvents.SERVER_STARTING.register(lifecycledServer::newValue);
        ServerLifecycleEvents.SERVER_STOPPING.register(_ -> lifecycledServer.invalidate());
        return lifecycledServer;
    }

    FabricPlatform(FabricWorldEdit mod) {
        super(mod, createMinecraftServerLifecycled());
    }

    @Override
    public FabricAdapter getAdapter() {
        return FabricAdapter.get();
    }

    @Override
    public void sendCUIPacket(ServerPlayer player, CUIPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    @Override
    public String getPlatformName() {
        return "Fabric-Official";
    }

    @Override
    public String id() {
        return "enginehub:fabric";
    }
}
