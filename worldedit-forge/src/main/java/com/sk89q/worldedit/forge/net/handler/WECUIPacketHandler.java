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

package com.sk89q.worldedit.forge.net.handler;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.forge.ForgeWorldEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;

import java.nio.charset.Charset;

public class WECUIPacketHandler {
    public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    private static final String PROTOCOL_VERSION = Integer.toString(1);
    public static EventNetworkChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ForgeWorldEdit.MOD_ID, ForgeWorldEdit.CUI_PLUGIN_CHANNEL))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .eventNetworkChannel();
    
    public static void init() {
        HANDLER.addListener(WECUIPacketHandler::onPacketData);
        HANDLER.addListener(WECUIPacketHandler::callProcessPacket);
    }

    public static void onPacketData(NetworkEvent.ServerCustomPayloadEvent event) {
        EntityPlayerMP player = event.getSource().get().getSender();
        LocalSession session = ForgeWorldEdit.inst.getSession(player);

        if (session.hasCUISupport()) {
            return;
        }

        String text = event.getPayload().toString(UTF_8_CHARSET);
        session.handleCUIInitializationMessage(text);
        session.describeCUI(ForgeWorldEdit.inst.wrap(player));
    }
    
    public static void callProcessPacket(NetworkEvent.ClientCustomPayloadEvent event) {
        try {
            new SPacketCustomPayload(
                    new ResourceLocation(ForgeWorldEdit.MOD_ID, ForgeWorldEdit.CUI_PLUGIN_CHANNEL),
                    event.getPayload()
            ).processPacket(Minecraft.getInstance().player.connection);
        } catch (ThreadQuickExitException suppress) {
        }
    }
}