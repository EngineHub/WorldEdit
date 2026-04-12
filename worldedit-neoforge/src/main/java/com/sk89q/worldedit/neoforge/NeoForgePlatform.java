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

package com.sk89q.worldedit.neoforge;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.coremc.internal.CoreMcPlatform;
import com.sk89q.worldedit.util.io.ResourceLoader;
import com.sk89q.worldedit.util.lifecycle.Lifecycled;
import com.sk89q.worldedit.util.lifecycle.SimpleLifecycled;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.enginehub.worldeditcui.protocol.CUIPacket;

class NeoForgePlatform extends CoreMcPlatform {

    private static Lifecycled<MinecraftServer> createMinecraftServerLifecycled() {
        SimpleLifecycled<MinecraftServer> lifecycled = SimpleLifecycled.invalid();
        NeoForge.EVENT_BUS.addListener(
            ServerAboutToStartEvent.class, event -> lifecycled.newValue(event.getServer())
        );
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, _ -> lifecycled.invalidate());
        return lifecycled;
    }

    private final ResourceLoader resourceLoader = new NeoForgeResourceLoader(WorldEdit.getInstance());

    NeoForgePlatform(NeoForgeWorldEdit mod) {
        super(mod, createMinecraftServerLifecycled());
    }

    @Override
    public void sendCUIPacket(ServerPlayer player, CUIPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    @Override
    protected void extraOnBlockStateChange(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        newState.onBlockStateChange(level, pos, oldState);
    }

    @Override
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Override
    public String getPlatformName() {
        return "NeoForge-Official";
    }

    @Override
    public String id() {
        return "enginehub:neoforge";
    }
}
