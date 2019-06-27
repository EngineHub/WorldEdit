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

package com.sk89q.worldedit.fabric.net.handler;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.fabric.FabricPlayer;
import com.sk89q.worldedit.fabric.FabricWorldEdit;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class WECUIPacketHandler {
    private WECUIPacketHandler() {
    }

    public static final Charset UTF_8_CHARSET = StandardCharsets.UTF_8;
    private static final Identifier CUI_IDENTIFIER = new Identifier(FabricWorldEdit.MOD_ID, FabricWorldEdit.CUI_PLUGIN_CHANNEL);

    public static void init() {
        ServerSidePacketRegistry.INSTANCE.register(CUI_IDENTIFIER, (packetContext, packetByteBuf) -> {
            ServerPlayerEntity player = (ServerPlayerEntity) packetContext.getPlayer();
            LocalSession session = FabricWorldEdit.inst.getSession(player);

            if (session.hasCUISupport()) {
                return;
            }

            String text = packetByteBuf.toString(UTF_8_CHARSET);
            final FabricPlayer actor = FabricAdapter.adaptPlayer(player);
            session.handleCUIInitializationMessage(text, actor);
            session.describeCUI(actor);
        });
    }
}