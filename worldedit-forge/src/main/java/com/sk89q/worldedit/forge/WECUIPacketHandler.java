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

package com.sk89q.worldedit.forge;

import com.sk89q.worldedit.LocalSession;

import java.nio.charset.Charset;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class WECUIPacketHandler {
    public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    public static FMLEventChannel WECUI_CHANNEL;
    
    public static void init() {
         WECUI_CHANNEL = NetworkRegistry.INSTANCE.newEventDrivenChannel(ForgeWorldEdit.CUI_PLUGIN_CHANNEL);
         WECUI_CHANNEL.register(new WECUIPacketHandler());
    }

    @SubscribeEvent
    public void onPacketData(ServerCustomPacketEvent event) {
        if (event.packet.channel().equals(ForgeWorldEdit.CUI_PLUGIN_CHANNEL)) {
            EntityPlayerMP player = getPlayerFromEvent(event);
            LocalSession session = ForgeWorldEdit.inst.getSession((EntityPlayerMP) player);

            if (session.hasCUISupport()) {
                return;
            }

            String text = event.packet.payload().toString(UTF_8_CHARSET);
            session.handleCUIInitializationMessage(text);
            session.describeCUI(ForgeWorldEdit.inst.wrap(player));
        }
    }
    
    @SubscribeEvent
    public void callProcessPacket(ClientCustomPacketEvent event) {
        try {
            new S3FPacketCustomPayload(event.packet.channel(), new PacketBuffer(event.packet.payload())).processPacket(event.handler);
        } catch (ThreadQuickExitException suppress) {
        }
    }

    private static EntityPlayerMP getPlayerFromEvent(ServerCustomPacketEvent event) {
        return ((NetHandlerPlayServer) event.handler).playerEntity;
    }
}